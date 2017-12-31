package com.rph.ticketservice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * An implementation of {@code TicketService}.
 */
public class TicketServiceImpl implements TicketService {

    private static final long EXPIRE_SECONDS = 5 * 60;   // five minutes

    private final Object synchroLock = new Object();

    private final List<Seat> bestAvailableSeats;

    private Seats seats;

    private final Map<Integer, SeatHold> seatHolds = new HashMap<>();

    private final Map<String, Reservation> reservations = new HashMap<>();

    private final Timer timer = new Timer(true);   // isDaemon: true


    public TicketServiceImpl(Venue venue) {
        this.bestAvailableSeats = new ArrayList<>(venue.getBestSeats());
        this.seats = new Seats(venue);
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
                List<Seat> heldSeats = getBestAdjacentSeats(numSeats, bestAvailableSeats, seats);
                SeatHold seatHold = new SeatHold(seatHolds.size(), customerEmail, heldSeats);
                seatHolds.put(seatHold.getSeatHoldId(), seatHold);
                setExpirationTimeout(seatHold, EXPIRE_SECONDS * 1000L);
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
            SeatHold seatHold = getSeatHold(seatHoldId);
            String reservationId = Integer.toString(seatHold.getSeatHoldId());
            if (seatHold.isReserved()) {
                return reservationId;
            }
            if (seatHold.isExpired()) {
                return null;
            }
            seatHold.reserve();
            Reservation reservation = new Reservation(seatHold, reservationId);
            reservations.put(reservationId, reservation);
            seatHolds.remove(seatHoldId);
            return reservationId;
        }
    }

    public Reservation getReservation(String reservationId) {
        synchronized (synchroLock) {
            return reservations.get(reservationId);
        }
    }

    @VisibleForTesting
    SeatHold getSeatHold(int seatHoldId) {
        return seatHolds.get(seatHoldId);
    }

    @VisibleForTesting
    void expire(SeatHold seatHold) {
        synchronized (synchroLock) {
            if (seatHold.expire()) {
                makeSeatsAvailable(seatHold.getHeldSeats(), bestAvailableSeats, seats);
            }
        }
    }

    @VisibleForTesting
    void setExpirationTimeout(final SeatHold seatHold, long timeoutMilliseconds) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                expire(seatHold);
            }
        }, timeoutMilliseconds);
    }


    @VisibleForTesting
    static List<Seat> getBestAdjacentSeats(int numSeats, List<Seat> bestAvailableSeats, Seats seats)
            throws NoSeatsAvailableException {
        for (Seat nextBestAvailableSeat : bestAvailableSeats) {
            if (getNumberOfAdjacentAvailableSeats(nextBestAvailableSeat, seats) >= numSeats) {
                return extractAdjacentSeats(numSeats, nextBestAvailableSeat, bestAvailableSeats, seats);
            }
        }
        throw new NoSeatsAvailableException();   // sold out
    }

    @VisibleForTesting
    static int getNumberOfAdjacentAvailableSeats(Seat seat, Seats seats) {
        final int rowNum = seat.getRowNum();
        final int initialSeatNumInRow = seat.getSeatNumInRow();
        final int numSeatsInRow = seats.getNumSeatsPerRow();
        int numAdjacentAvailableSeats = 0;
        for (int seatNumInRow = initialSeatNumInRow;
             seatNumInRow < numSeatsInRow && seats.isAvailable(rowNum, seatNumInRow); seatNumInRow++) {
             numAdjacentAvailableSeats += 1;
        }
        for (int seatNumInRow = initialSeatNumInRow - 1;
             seatNumInRow >= 0 && seats.isAvailable(rowNum, seatNumInRow); seatNumInRow--) {
            numAdjacentAvailableSeats += 1;
        }
        return numAdjacentAvailableSeats;
    }

    /**
     * Finds the required number of seats that are available and adjacent.
     * Makes them unavailable (held) and returns them in a list.
     * <p>
     * This algorithm actually finds the single best seat that has a sufficient number
     * of available adjacent seats, and adds it and the adjacent seats to the set of seats
     * to be held. Note that this algorithm is probably less than perfect. For example,
     * consider a venue with almost half the seats in row N reserved, all on the right side,
     * and none of the seats in row N+1 reserved. A situation like this can occur because
     * seats can be provisionally reserved (held), then made available again after a time.
     * So the best available seat in the venue might be in the center of row N, but the
     * available adjacent seats all stretch out to one side. The lucky patron who gets the
     * center seat of row N might be very happy, but his friends who got stuck with the
     * letfmost seats in the row might wish that they had reserved the more balanced set
     * of seats in the middle of row N+1 instead. So an alternative algorithm might consider
     * the average bestness of all the seats to be reserved, rather than working off of
     * the single best available seat.
     * <p>
     * An even more complex algorithm might allocate nearby seats in multiple rows for
     * larger groups.
     * <p>
     * But assuming that there are not a lot of overlapping provisional reservations that
     * get undone, the algorithm implemented herein would undoubtedly be quite sufficient
     * for most venues.
     * <p>
     * Here is the algorithm.
     * For each available seat, in order of bestness, see if there are enough available
     * adjacent seats. If so, allocate the next seat on the right, then the next left seat,
     * then the right again, then the left, etc. If an unavailable seat is encountered,
     * continue down the other side.
     * <p>
     * This algorithm makes some reasonable assumptions about bestness ordering, but no
     * assumptions about seat availability. In particular, it assumes that, for any
     * given row, central seats are always better that seats off to the side. Also,
     * one side is not better than the other.
     *
     * @param numSeatsNeeded the number of seats to be held
     * @param initialSeat the initial seat in the row
     * @param bestAvailableSeats the list of seats from which the newly held seats are to be removed
     * @param seats the grid of all seats
     * @return a list containing the newly held seats
     *
     * @see com.rph.ticketservice.Venue for the way in which seat bestness is initialized.
     */
    @VisibleForTesting
    static List<Seat> extractAdjacentSeats(int numSeatsNeeded, Seat initialSeat, List<Seat> bestAvailableSeats,
                                           Seats seats) {
        final int rowNum = initialSeat.getRowNum();
        final int initialSeatNumInRow = initialSeat.getSeatNumInRow();
        final int numSeatsInRow = seats.getNumSeatsPerRow();
        List<Seat> heldSeats = new LinkedList<>();
        int numSeatsRemaining = numSeatsNeeded;
        boolean encounteredUnavailableSeat = false;
        for (int seatNumInRow :   // check seats on alternating sides, expanding outward from the initial seat
                Stream.iterate(0, n -> (n <= 0) ? (-n + 1) : (-n))   // 0, 1, -1, 2, -2, ...
                .map(n -> initialSeatNumInRow + n)   // iSNIR, iSNIR+1, iSNIR-1, iSNIR+2, iSNIR-2, ...
                .filter(n -> (n >= 0) && (n < numSeatsInRow))   // don't go past the end of the row
                .limit(numSeatsInRow)
                .collect(Collectors.toList())) {
            if (!encounteredUnavailableSeat) {
                // Check the next adjacent seat on the other side of the venue.
                if (seats.isAvailable(rowNum, seatNumInRow)) {
                    holdSeat(rowNum, seatNumInRow, bestAvailableSeats, heldSeats, seats);
                    if (--numSeatsRemaining <= 0) {
                        return heldSeats;
                    }
                } else {
                    // The seat is not available. The rest of the available seats must be on the other side.
                    encounteredUnavailableSeat = true;
                }
                continue;
            }
            // We encountered an unavailable seat on the other side of the venue.
            int direction = seatNumInRow < initialSeatNumInRow ? -1 : +1;   // left or right
            holdSeats(numSeatsRemaining, rowNum, seatNumInRow, bestAvailableSeats, heldSeats, seats, direction);
            return heldSeats;
        }
        // Should not happen, since the caller already made sure that there are enough adjacent available seats.
        throw new RuntimeException("Not enough seats!");
    }

    /**
     * Makes the specified adjacent seats held. The seats are removed from the {@code bestAvailableSeats} list
     * and appended to the {@code heldSeats} list. The state of each seat is set to held.
     *
     * @param numSeats the number of seats to be held
     * @param rowNum the row number of the seats
     * @param seatNumInRow the number in the row of one of the seats to be held (leftmost or rightmost)
     * @param bestAvailableSeats the ordered list of available seats (source)
     * @param heldSeats the list of held seats to which the specified seats are appended (destination)
     * @param seats the grid of all seats
     * @param direction the direction of the adjacent seats to be held (-1 for left, +1 for right)
     */
    @VisibleForTesting
    static void holdSeats(int numSeats, int rowNum, int seatNumInRow, List<Seat> bestAvailableSeats,
                          List<Seat> heldSeats, Seats seats, int direction) {
        for (int numSeatsRemaining = 0; numSeatsRemaining < numSeats; numSeatsRemaining++) {
            holdSeat(rowNum, seatNumInRow, bestAvailableSeats, heldSeats, seats);
            seatNumInRow = seatNumInRow + direction;   // direction is -1 or +1
        }
    }

    /**
     * Makes the specified seat held. The seat is removed from the {@code bestAvailableSeats} list
     * and appended to the {@code heldSeats} list. The state of the seat is set to held.
     *
     * @param rowNum the seat's row number
     * @param seatNumInRow the seat's number in its row
     * @param bestAvailableSeats the ordered list of available seats
     * @param heldSeats the list of held seats
     * @param seats the grid of all seats
     */
    @VisibleForTesting
    static void holdSeat(int rowNum, int seatNumInRow, List<Seat> bestAvailableSeats, List<Seat> heldSeats,
                         Seats seats) {
        Seat seat = seats.getSeat(rowNum, seatNumInRow);
        if (!seats.isAvailable(rowNum, seatNumInRow)
                || heldSeats.contains(seat)
                || !bestAvailableSeats.contains(seat)) {
            throw new RuntimeException("Unexpected state error!");
        }
        seats.setAvailability(rowNum, seatNumInRow, false);
        heldSeats.add(seat);
        bestAvailableSeats.remove(seat);
    }

    /**
     * Makes the held seats available by moving each seat from {@code heldSeats} to {@code bestAvailableSeats}.
     * The {@code bestAvailableSeats} list is ordered by bestness, and each added seat is inserted in such a way
     * as to maintain this ordering. The state of each moved seat is set to available.
     *
     * @param heldSeats the source list of seats to be made available
     * @param bestAvailableSeats the destination list of seats, ordered by bestness
     * @param seats the grid of all seats
     */
    @VisibleForTesting
    static void makeSeatsAvailable(List<Seat> heldSeats, List<Seat> bestAvailableSeats, Seats seats) {
        heldSeats.sort(Comparator.comparingInt(Seat::getBestness));
        int availIndex = 0;
        for (Seat seat : new ArrayList<>(heldSeats)) {
            availIndex = insertSeatByBestness(seat, bestAvailableSeats, availIndex);
            heldSeats.remove(0);
            seats.setAvailability(seat.getRowNum(), seat.getSeatNumInRow(), true);
        }
    }

    /**
     * Inserts the seat into the specified ordered list of seats at the appropriate bestness point.
     *
     * @param seatToInsert the seat to be inserted
     * @param bestAvailableSeats the list of seats, ordered by bestness
     * @param startingIndex where to start
     * @return the list index of the seat just after the newly inserted seat
     */
    @VisibleForTesting
    static int insertSeatByBestness(Seat seatToInsert, List<Seat> bestAvailableSeats, int startingIndex) {
        int numSeats = bestAvailableSeats.size();
        int insertSeatBestness = seatToInsert.getBestness();
        for (int index = startingIndex; index < numSeats; index++) {
            Seat seat = bestAvailableSeats.get(index);
            if (insertSeatBestness < seat.getBestness()) {
                bestAvailableSeats.add(index, seatToInsert);   // insert it
                return index + 1;   // return the subsequent index
            }
        }
        bestAvailableSeats.add(seatToInsert);   // append it at the end of the list
        return bestAvailableSeats.size();   // the index just after the appended seat is the list size
    }
}
