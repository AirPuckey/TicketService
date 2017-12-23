package com.rph.ticketservice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TicketServiceImpl implements TicketService {

    private static final int EXPIRE_SECONDS = 5 * 60;   // five minutes

    private Object synchroLock = new Object();

    private final List<Seat> bestAvailableSeats;

    private final SeatGrid seatGrid;

    private final List<SeatHold> seatHolds = new ArrayList<>();

    private final Timer timer = new Timer(true);


    public TicketServiceImpl(Venue venue) {
        this.bestAvailableSeats = venue.getCopyOfBestSeats(new ArrayList<>(venue.getNumberOfSeats()));
        this.seatGrid = venue.getSeatGrid();
    }

    /**
     * The number of seats in the venue that are neither held nor reserved
     *
     * @return the number of tickets available in the venue
     */
    @Override
    public int numSeatsAvailable() {
        synchronized (synchroLock) {
            return bestAvailableSeats.size();
        }
    }

    /**
     * Find and hold the best available seats for a customer
     *
     * @param numSeats      the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related
     * information
     */
    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        synchronized (synchroLock) {
            try {
                List<Seat> heldSeats = getBestAdjacentSeats(numSeats, bestAvailableSeats, seatGrid);
                SeatHold seatHold = new SeatHold(seatHolds.size(), customerEmail, heldSeats);
                seatHolds.add(seatHold);
                setExpirationTimeout(seatHold);
                return seatHold;
            } catch (NoSeatsAvailableException e) {
                return null;
            }
        }
    }

    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId    the seat hold identifier
     * @param customerEmail the email address of the customer to which the
     *                      seat hold is assigned
     * @return a reservation confirmation code
     */
    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        synchronized (synchroLock) {
            SeatHold seatHold = seatHolds.get(seatHoldId);
            seatHold.reserveSeats();
            return "" + seatHold.getSeatHoldId();
        }
    }

    public void expire(SeatHold seatHold) {
        synchronized (synchroLock) {
            if (seatHold.expire()) {
                makeSeatsAvailable(seatHold.getHeldSeats(), bestAvailableSeats, seatGrid);
            }
        }
    }


    static List<Seat> getBestAdjacentSeats(int numSeats, List<Seat> bestAvailableSeats, SeatGrid seatGrid)
            throws NoSeatsAvailableException {
        for (Seat bestAvailableSeat : bestAvailableSeats) {
            if (getNumberOfAdjacentAvailableSeats(bestAvailableSeat, seatGrid) <= numSeats) {
                List<Seat> heldSeats = new ArrayList<>(numSeats);
                try {
                    extractBestAdjacentSeats(numSeats, bestAvailableSeat, bestAvailableSeats, heldSeats, seatGrid);
                } catch (Exception e) {
                    makeSeatsAvailable(heldSeats, bestAvailableSeats, seatGrid);
                    throw e;
                }
                return heldSeats;
            }
        }
        throw new NoSeatsAvailableException();   // sold out
    }

    static int getNumberOfAdjacentAvailableSeats(Seat seat, SeatGrid seatGrid) {
        int rowNum = seat.getRowNum();
        int seatNumInRow = seat.getSeatNumInRow();
        int numSeatsInRow = seatGrid.getNumSeatsPerRow();
        int numAdjacentAvailableSeats = 0;
        for (int i = seatNumInRow; i < numSeatsInRow; i++) {
            if (seatGrid.getSeatState(rowNum, i).isAvailable()) {
                numAdjacentAvailableSeats += 1;
            } else {
                break;
            }
        }
        for (int i = seatNumInRow - 1; i >= 0; i--) {
            if (seatGrid.getSeatState(rowNum, i).isAvailable()) {
                numAdjacentAvailableSeats += 1;
            } else {
                break;
            }
        }
        return numAdjacentAvailableSeats;
    }

    static void extractBestAdjacentSeats(int numSeats, Seat bestSeat, List<Seat> bestAvailableSeats,
                                         List<Seat> heldSeats, SeatGrid seatGrid) {
        final int rowNum = bestSeat.getRowNum();
        final int bestAvailableSeatNumInRow = bestSeat.getSeatNumInRow();
        int numSeatsInRow = seatGrid.getNumSeatsPerRow();
        boolean doneIncreasing = false;
        boolean doneDecreasing = false;
        for (int seatNumInRow :
                Stream.iterate(0, n -> (n <= 0) ? (-n + 1) : (-n))   // 0, 1, -1, 2, -2, ...
                .map(n -> bestAvailableSeatNumInRow + n)   // bASNIR, bASNIR+1, bASNIR-1, bASNIR+2, bASNIR-2, ...
                .filter(n -> (n >= 0) && (n < numSeatsInRow))
                .collect(Collectors.toList())) {
            if (doneIncreasing && (seatNumInRow > bestAvailableSeatNumInRow)) {
                continue;
            }
            if (doneDecreasing && (seatNumInRow < bestAvailableSeatNumInRow)) {
                continue;
            }
            SeatState seatState = seatGrid.getSeatState(rowNum, seatNumInRow);
            if (seatState.getRowNum() != rowNum) {
                throw new RuntimeException("Unexpected seat rowNum!");
            }
            if (seatState.getSeatNumInRow() != seatNumInRow) {
                throw new RuntimeException("Unexpected seatNumInRow!");
            }
            if (!seatState.isAvailable()) {
                if (seatNumInRow > bestAvailableSeatNumInRow) {
                    doneIncreasing = true;
                } else if (seatNumInRow < bestAvailableSeatNumInRow) {
                    doneDecreasing = true;
                }
                if (doneIncreasing && doneDecreasing) {
                    break;
                }
            }
            seatState.setHeld();
            heldSeats.add(seatState);
            if (!bestAvailableSeats.remove(seatState)) {
                throw new RuntimeException("Seat not found on bestAvailableSeats list!");
            }
            if (heldSeats.size() >= numSeats) {
                return;
            }
        }
        throw new RuntimeException("Not enough seats!");
    }

    static void makeSeatsAvailable(List<Seat> heldSeats, List<Seat> bestAvailableSeats, SeatGrid seatGrid) {
        heldSeats.sort(Comparator.comparingInt(Seat::getBestness));   // to be sure
        int availIndex = 0;
        for (Seat seat : heldSeats) {
            availIndex = insertSeatByBestness(seat, bestAvailableSeats, availIndex);
            ((SeatState) seat).setAvailable();
        }
        heldSeats.clear();
    }

    static int insertSeatByBestness(Seat seatToInsert, List<Seat> seats, int startingIndex) {
        int numSeats = seats.size();
        int bestness = seatToInsert.getBestness();
        for (int index = startingIndex; index < numSeats; index++) {
            Seat seat = seats.get(index);
            if (seat.getBestness() > bestness) {
                seats.add(index - 1, seatToInsert);
                return index;
            }
        }
        seats.add(seatToInsert);
        return seats.size() - 1;
    }

    void setExpirationTimeout(final SeatHold seatHold) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                expire(seatHold);
            }
        }, EXPIRE_SECONDS * 1000);
    }
}
