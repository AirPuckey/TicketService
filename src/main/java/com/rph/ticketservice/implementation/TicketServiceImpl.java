package com.rph.ticketservice.implementation;

import com.rph.ticketservice.SeatHold;
import com.rph.ticketservice.TicketService;
import com.rph.ticketservice.Venue;

import java.util.ArrayList;
import java.util.Collection;
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

    /** Number of seconds until an unreserved SeatHold expires. */
    private static final long EXPIRE_SECONDS = 5 * 60;   // five minutes

    /** Global synchronization lock. */
    private final Object synchroLock = new Object();

    /** Dynamic list of available seats, ordered by decreasing bestness (increasing bestness number). */
    private final List<SeatImpl> bestAvailableSeats;

    /** Encapsulates a rectangular grid of per-seat info. */
    private SeatGrid seatGrid;

    /** Maps a SeatHold ID to a SeatHold instance. */
    private final Map<Integer, SeatHoldImpl> seatHolds = new HashMap<>();

    /** Maps a reservation ID to a Reservation instance. */
    private final Map<String, Reservation> reservations = new HashMap<>();

    /** Expiration timer. */
    private final Timer expirationTimer = new Timer(true);   // isDaemon: true

    /** Number of milliseconds until an unreserved SeatHold is expired. */
    private final long expireMillies;

    /** Next SeatHold ID. */
    private int nextSeatHoldId = 0;


    /**
     * Constructs a new TicketServiceImpl.
     *
     * @param venue the venue to be served by this TicketService
     */
    public TicketServiceImpl(Venue venue) {
        this(venue, EXPIRE_SECONDS * 1000);
    }

    /**
     * Constructs a new TicketServiceImpl.
     *
     * @param venue the venue to be served by this TicketService
     * @param expireMillies seatHold expiration duration
     */
    public TicketServiceImpl(Venue venue, long expireMillies) {
        VenueImpl venueImpl = (VenueImpl) venue;
        this.bestAvailableSeats = new ArrayList<>(venueImpl.getBestSeats());
        this.seatGrid = new SeatGrid(venueImpl);
        this.expireMillies = expireMillies;
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
            return getCustomerSeatHold(findAndHoldSeatsInternal(numSeats, customerEmail));
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
            SeatHoldImpl seatHold = getSeatHold(seatHoldId);
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
            return reservationId;
        }
    }

    /**
     * Finds and returns the reservation associated with the specified reservation ID.
     *
     * @param reservationId the reservation ID
     * @return the corresponding reservation
     */
    public Reservation getReservation(String reservationId) {
        synchronized (synchroLock) {
            return reservations.get(reservationId);
        }
    }

    @VisibleForTesting
    static SeatHoldImpl getCustomerSeatHold(SeatHoldImpl seatHold) {
        return seatHold;
    }

    @VisibleForTesting
    SeatHoldImpl findAndHoldSeatsInternal(int numSeats, String customerEmail) {
        try {
            List<SeatImpl> heldSeats = holdBestAdjacentSeats(numSeats, bestAvailableSeats, seatGrid);
            SeatHoldImpl seatHold = new SeatHoldImpl(nextSeatHoldId++, customerEmail, heldSeats);
            seatHolds.put(seatHold.getSeatHoldId(), seatHold);
            setExpirationTimeout(seatHold, expireMillies);
            return seatHold;
        } catch (NoSeatsAvailableException e) {
            return null;
        }
    }

    /**
     * Finds and returns the seatHold associated with the specified seat hold ID.
     *
     * @param seatHoldId the seat hold ID
     * @return the corresponding seatHold
     */
    @VisibleForTesting
    SeatHoldImpl getSeatHold(int seatHoldId) {
        return seatHolds.get(seatHoldId);
    }

    /**
     * Tries to expire the specified seatHold. If the seatHold was not previously
     * expired or reserved, the seatHold is marked expired and the seats are
     * made available.
     *
     * @param seatHold the seatHold to be expired
     */
    void expire(SeatHoldImpl seatHold) {
        synchronized (synchroLock) {
            if (seatHold.isHeld()) {
                seatHold.expire();
                makeSeatsAvailable(seatHold.getHeldSeats(), bestAvailableSeats, seatGrid);
            }
        }
    }

    /**
     * Sets an expiration timeout for the specified seatHold. When the timer expires,
     * if the seats have not been reserved (committed) they will be returned to
     * the list of available seats.
     *
     * @param seatHold the held seats
     * @param timeoutMilliseconds number of milliseconds until expiration
     */
    @VisibleForTesting
    void setExpirationTimeout(final SeatHoldImpl seatHold, long timeoutMilliseconds) {
        expirationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                expire(seatHold);
            }
        }, timeoutMilliseconds);
    }

    /**
     * Finds the best adjacent available seats, and holds them.
     *
     * @param numSeats number of seats needed
     * @param bestAvailableSeats the destination list of seats, ordered by bestness
     * @param seatGrid the grid of all seats
     * @return list of the best adjacent available seats, which are now held
     * @throws NoSeatsAvailableException if there are insufficient adjacent available seats
     */
    @VisibleForTesting
    static List<SeatImpl> holdBestAdjacentSeats(int numSeats, List<SeatImpl> bestAvailableSeats, SeatGrid seatGrid)
            throws NoSeatsAvailableException {
        /*
         * This method populates and evaluates 10 lists of sufficient available seats,
         * working off of the bestAvailableSeats list. The best list found (best
         * average bestness of the collected adjacent seats) is held and returned.
         */
        int numCandidatesRemaining = 10;
        Collection<List<SeatImpl>> candidates = new ArrayList<>(numCandidatesRemaining);
        for (SeatImpl nextBestAvailableSeat : bestAvailableSeats) {
            if (getNumberOfAdjacentAvailableSeats(nextBestAvailableSeat, seatGrid) >= numSeats) {
                // found a seat with sufficient adjacent available seats
                candidates.add(collectAdjacentSeats(numSeats, nextBestAvailableSeat, seatGrid));
                if (--numCandidatesRemaining == 0) {
                    break;
                }
            }
        }
        if (candidates.size() > 0) {
            List<SeatImpl> winner = getBestCandidate(candidates);
            holdSeats(winner, bestAvailableSeats, seatGrid);
            return winner;
        }
        throw new NoSeatsAvailableException();   // almost sold out -- insufficient adjacent available seats
    }

    /**
     * Given a collection of seat lists, this method determines the best one.
     *
     * @param candidates collection of seat lists
     * @return the best list of seats
     */
    @VisibleForTesting
    static List<SeatImpl> getBestCandidate(Collection<List<SeatImpl>> candidates) {
        List<SeatImpl> bestSoFar = null;
        double bestAverageBestnessSoFar = Double.MAX_VALUE;
        for (List<SeatImpl> candidate : candidates) {
            double candidateBestness = getAverageBestness(candidate);
            if (candidateBestness < bestAverageBestnessSoFar) {   // smaller bestness values implies better seats
                bestSoFar = candidate;
                bestAverageBestnessSoFar = candidateBestness;
            }
        }
        return bestSoFar;
    }

    /**
     * Calculates the average bestness for the specified seats.
     *
     * @param seatList the seats
     * @return the average bestness
     */
    @VisibleForTesting
    static double getAverageBestness(List<SeatImpl> seatList) {
        if (seatList.size() <= 0) {
            throw new IllegalArgumentException("Empty seatlist!");
        }
        double total = 0;
        for (SeatImpl seat : seatList) {
            total += seat.getBestness();
        }
        return total / seatList.size();
    }

    /**
     * Determines the number of seats adjacent to the specified seat that are available.
     *
     * @param seat the seat
     * @param seatGrid the grid of all seats
     * @return the number of adjacent available seats
     */
    @VisibleForTesting
    static int getNumberOfAdjacentAvailableSeats(SeatImpl seat, SeatGrid seatGrid) {
        final int rowNum = seat.getRowNum();
        final int initialSeatNumInRow = seat.getSeatNumInRow();
        final int numSeatsInRow = seatGrid.getNumSeatsPerRow();
        int numAdjacentAvailableSeats = 0;
        for (int seatNumInRow = initialSeatNumInRow;
             seatNumInRow < numSeatsInRow && seatGrid.isAvailable(rowNum, seatNumInRow); seatNumInRow++) {
             numAdjacentAvailableSeats += 1;
        }
        for (int seatNumInRow = initialSeatNumInRow - 1;
             seatNumInRow >= 0 && seatGrid.isAvailable(rowNum, seatNumInRow); seatNumInRow--) {
            numAdjacentAvailableSeats += 1;
        }
        return numAdjacentAvailableSeats;
    }

    /**
     * Finds the required number of seats that are available and adjacent.
     *
     * @param numSeatsNeeded the number of seats to be held
     * @param initialSeat the initial seat in the row
     * @param seatGrid the grid of all seats
     * @return a list containing the newly held seats
     *
     * @see VenueImpl for the way in which seat bestness is initialized.
     */
    @VisibleForTesting
    static List<SeatImpl> collectAdjacentSeats(int numSeatsNeeded, SeatImpl initialSeat, SeatGrid seatGrid) {
        /*
         * Here is the algorithm: From the initial seat, collect alternate seats on the right
         * and left sides, spreading outward as long as seats are available. If an unavailable
         * seat is encountered, continue down the other side until the required number of seats
         * have been collected.
         *
         * This algorithm makes some reasonable assumptions about bestness ordering, but
         * no assumptions about seat availability. In particular, it assumes that, for any
         * given row, central seats are always better that seats off to the side. Also,
         * one side is not better than the other. Under normal usage, the initial seat is
         * always better than its neighbors.
         */
        final int rowNum = initialSeat.getRowNum();
        final int initialSeatNumInRow = initialSeat.getSeatNumInRow();
        final int numSeatsInRow = seatGrid.getNumSeatsPerRow();
        List<SeatImpl> adjacentSeats = new LinkedList<>();
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
                if (seatGrid.isAvailable(rowNum, seatNumInRow)) {
                    SeatImpl seat = seatGrid.getSeat(rowNum, seatNumInRow);
                    adjacentSeats.add(seat);

                    if (--numSeatsRemaining <= 0) {
                        return adjacentSeats;
                    }
                } else {
                    // The seat is not available. The rest of the available seats must be on the other side.
                    encounteredUnavailableSeat = true;
                }
                continue;
            }
            // We encountered an unavailable seat on the other side of the venue.
            int direction = seatNumInRow < initialSeatNumInRow ? -1 : +1;   // left or right
            while (numSeatsRemaining > 0) {
                SeatImpl seat = seatGrid.getSeat(rowNum, seatNumInRow);
                adjacentSeats.add(seat);
                seatNumInRow += direction;
                numSeatsRemaining -= 1;
            }
            return adjacentSeats;
        }
        // Should not happen, since the caller already made sure that there are enough adjacent available seats.
        throw new RuntimeException("Not enough seats!");
    }

    /**
     * Makes the specified seats held. The seats are removed from the {@code bestAvailableSeats} list.
     * The state of each seat is set to held.
     *
     * @param seatsToBeHeld the seats to be held
     * @param bestAvailableSeats the ordered list of available seats (source)
     * @param seatGrid the grid of all seats
     */
    @VisibleForTesting
    static void holdSeats(List<SeatImpl> seatsToBeHeld, List<SeatImpl> bestAvailableSeats, SeatGrid seatGrid) {
        for (SeatImpl seat : seatsToBeHeld) {
            seatGrid.setAvailability(seat.getRowNum(), seat.getSeatNumInRow(), false);
            bestAvailableSeats.remove(seat);
        }
    }

    /**
     * Makes the held seats available by moving each seat from {@code heldSeats} to {@code bestAvailableSeats}.
     * The {@code bestAvailableSeats} list is ordered by bestness, and each added seat is inserted in such a way
     * as to maintain this ordering. The state of each moved seat is set to available.
     *
     * @param heldSeats the source list of seats to be made available
     * @param bestAvailableSeats the destination list of seats, ordered by bestness
     * @param seatGrid the grid of all seats
     */
    static void makeSeatsAvailable(List<SeatImpl> heldSeats, List<SeatImpl> bestAvailableSeats, SeatGrid seatGrid) {
        heldSeats.sort(Comparator.comparingInt(SeatImpl::getBestness));
        int availIndex = 0;
        for (SeatImpl seat : new ArrayList<>(heldSeats)) {
            availIndex = insertSeatByBestness(seat, bestAvailableSeats, availIndex);
            seatGrid.setAvailability(seat.getRowNum(), seat.getSeatNumInRow(), true);
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
    static int insertSeatByBestness(SeatImpl seatToInsert, List<SeatImpl> bestAvailableSeats, int startingIndex) {
        int numSeats = bestAvailableSeats.size();
        int insertSeatBestness = seatToInsert.getBestness();
        for (int index = startingIndex; index < numSeats; index++) {
            SeatImpl seat = bestAvailableSeats.get(index);
            if (insertSeatBestness < seat.getBestness()) {
                bestAvailableSeats.add(index, seatToInsert);   // insert it
                return index + 1;   // return the subsequent index
            }
        }
        bestAvailableSeats.add(seatToInsert);   // append it at the end of the list
        return bestAvailableSeats.size();   // the index just after the appended seat is the list size
    }
}
