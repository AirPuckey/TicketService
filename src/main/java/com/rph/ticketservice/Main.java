package com.rph.ticketservice;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This class exercises this implementation of the ticket service.
 * It is an integration test, and a source of data for the viewer.
 */
public class Main {

    /** Default percent of SeatHolds to NOT be reserved (they will expire). */
    private static final int DEFAULT_EXPIRE_PERCENT = 25;

    /** Default expiration timeout (milliseconds) (small value for testing). */
    private static final int EXPIRE_MILLIES = 375;

    /** Default delay time for each SeatHold that happens, to allow expirations to occur before termination. */
    private static final int LOOP_MILLIES = 50;

    /** Some random customer email address. Insignificant. */
    private static final String EMAIL_ADDRESS = "ronald.hughes@gmail.com";

    /** Distribution of number of seats per SeatHold request. Zero implies anything up to row size. */
    private static final int[] DISTRIBUTION = { 1, 1, 2, 2, 2, 2, 2, 3, 3, 4, 4, 4, 4, 4, 5, 5, 6, 6, 6, 0};

    /** The random number generator, used for SeatHold size requests (number of customers in a party). */
    private Random randomNumberGenerator;

    /** The Factory used to create Venues and TicketServices. */
    private Factory factory = new Factory();

    /** Where to print data. */
    private PrintStream out = System.out;

    /** The actual percent of SeatHolds to not be reserved (each will expire). */
    private int expirePercent = DEFAULT_EXPIRE_PERCENT;

    /** Venue parameters. */
    private int numRows = 10, numSeatsPerRow = 20, bestRowNum = 4;

    /**
     * Entry point for the viewer.
     *
     * @param args program args
     * @throws FileNotFoundException if an output file is specified and it cannot be created/truncated.
     * This may occur if the file is not writeable, or if a specified directory doesn't exist.
     */
    public static void main(String[] args) throws FileNotFoundException {
        new Main().runMain(args);
    }

    /**
     * This is the entry point within the instance of this class.
     * Initializes the runtime. Parses the args (does not check them).
     * Invokes the appropriate exercise (venue or ticketService).
     *
     * @param args program args
     * @throws FileNotFoundException if an output file is specified and it cannot be created/truncated.
     * This may occur if the file is not writeable, or if a specified directory doesn't exist.
     */
    private void runMain(String[] args) throws FileNotFoundException {
        List<String> argsList = Collections.unmodifiableList(Arrays.asList(args));

        /*
         * TODO: make the argument parsing much more robust.
         */

        if ((argsList.size() == 0)
                || argsList.contains("-u") || argsList.contains("-usage") || argsList.contains("-Usage")) {
            System.err.println();
            System.err.println("usage: java -cp ticketService.jar RunMe [ -ticketService | -bestSeats ] [ options ]");
            System.err.println("  where options include:");
            System.err.println("    -usage (you'll also get this message if no args are specified)");
            System.err.println("    -outputFile fileName (default: standard output)");
            System.err.println("    -expirePercent 1to100 (default: 25)");
            System.err.println("    -rows numberOfRows (default: 10)");
            System.err.println("    -seatsPerRow numberOfSeatsPerRow (default: 20)");
            System.err.println("    -bestRow rowWithTheBestSeat (default: 4)");
            System.err.println("    -randomSeed seedValue (default: a number between 0 and 99)");
            System.err.println();
            return;
        }

        int randomSeed = 0;
        int randomSeedFlag = argsList.indexOf("-randomSeed");
        if (randomSeedFlag >= 0) {
            randomSeed = Integer.parseInt(argsList.get(randomSeedFlag + 1));
        } else {
            Random random = new Random();   // scratch random number generator, to get a low-value seed
            randomSeed = random.nextInt(100);
        }
        randomNumberGenerator = new Random(randomSeed);
        System.err.println("Random seed: " + randomSeed);   // print the random seed, for use in subsequent runs

        int outputFilenameIndexFlag = argsList.indexOf("-outputFile");
        if (outputFilenameIndexFlag >= 0) {
            out = new PrintStream(argsList.get(outputFilenameIndexFlag + 1));
        }

        int expirePercentFlag = argsList.indexOf("-expirePercent");
        if (expirePercentFlag >= 0) {
            expirePercent = Integer.parseInt(argsList.get(expirePercentFlag + 1));
        }

        int numRowsFlag = argsList.indexOf("-rows");
        if (numRowsFlag >= 0) {
            numRows = Integer.parseInt(argsList.get(numRowsFlag + 1));
        }

        int numSeatsPerRowFlag = argsList.indexOf("-seatsPerRow");
        if (numSeatsPerRowFlag >= 0) {
            numSeatsPerRow = Integer.parseInt(argsList.get(numSeatsPerRowFlag + 1));
        }

        int bestRowNumFlag = argsList.indexOf("-bestRow");
        if (bestRowNumFlag >= 0) {
            bestRowNum = Integer.parseInt(argsList.get(bestRowNumFlag + 1));
        }

        try {
            if (argsList.contains("-bestSeats")) {
                runVenue();
                return;
            }
            if (argsList.contains("-ticketService")) {
                runTicketService();
                return;
            }
        } finally {
            out.flush();
        }
    }

    /**
     * Creates a Venue and prints each seat contained therein, in bestness order.
     * (This looks interesting in the Viewer.)
     */
    private void runVenue() {
        Venue venue = factory.createVenue(numRows, numSeatsPerRow, bestRowNum);
        try {
            out.println(getVenueString(venue));
            for (Seat seat : venue.bestSeats()) {
                out.println(getSeatString(seat));
            }
        } finally {
            out.println();
            out.println();
        }
    }

    /**
     * Creates a Venue and a TicketService, which it exercises. Seats are held.
     * Some SeatHolds are reserved; some are not. Prints each SeatHold, which
     * may or may not be reserved. Moniters unreserved SeatHolds to see if they
     * become expired; if so, the expired SeatHold is printed. Runs until the
     * venue is full.
     */
    private void runTicketService() {
        Venue venue = factory.createVenue(numRows, numSeatsPerRow, bestRowNum);
        TicketService ticketService = factory.createTicketService(venue, EXPIRE_MILLIES);

        try {
            out.println(getVenueString(venue));
            List<SeatHold> pendingHolds = new ArrayList<>();
            while (true) {
                while ((ticketService.numSeatsAvailable() > 0) || (pendingHolds.size() > 0)) {
                    int numSeats = getNumSeatsInParty(numSeatsPerRow);
                    SeatHold seatHold = ticketService.findAndHoldSeats(numSeats, EMAIL_ADDRESS);
                    if (seatHold == null) {
                        continue;
                    }
                    nap(LOOP_MILLIES);
                    if (randomNumberGenerator.nextInt(100) < expirePercent) {
                        pendingHolds.add(seatHold);
                    } else {
                        String reservationId = ticketService.reserveSeats(seatHold.getSeatHoldId(), EMAIL_ADDRESS);
                        if (reservationId == null) {
                            pendingHolds.add(seatHold);
                        } else {
                            SeatHold newlyReservedHold = null;
                            for (SeatHold pendingHold : pendingHolds) {
                                if (sameSeats(pendingHold, seatHold)) {
                                    newlyReservedHold = pendingHold;
                                    break;
                                }
                            }
                            if (newlyReservedHold != null) {
                                pendingHolds.remove(newlyReservedHold);
                            }
                        }
                    }
                    out.println(getSeatHoldString(seatHold));
                    checkPending(pendingHolds);
                }
                nap(EXPIRE_MILLIES + 50);
                if ((checkPending(pendingHolds) == 0) && (ticketService.numSeatsAvailable() == 0)) {
                    break;
                }
            }
        } finally {
            out.println();
            out.println();
        }
    }

    /**
     * Returns true if the two SeatHolds contain the same seats.
     *
     * @param hold1 the first SeatHold
     * @param hold2 the second SeatHold
     * @return true if the SeatHolds contain the same seats.
     */
    private boolean sameSeats(SeatHold hold1, SeatHold hold2) {
        if (hold1 == hold2) {
            return true;
        }
        if ((hold1 == null) || (hold2 == null)) {
            return false;
        }
        if (hold1.numSeatsHeld() != hold2.numSeatsHeld()) {
            return false;
        }
        for (int i = 0; i < hold1.numSeatsHeld(); i++) {
            boolean foundSameSeat = false;
            for (int j = 0; j < hold2.numSeatsHeld(); j++) {
                Seat seat1 = hold1.getSeat(i);
                Seat seat2 = hold2.getSeat(j);
                if ((seat1.getRowNum() == seat2.getRowNum()) && (seat1.getSeatNumInRow() == seat2.getSeatNumInRow())) {
                    foundSameSeat = true;
                    break;
                }
            }
            if (!foundSameSeat) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks the specified list of pending SeatHolds (held but not reserved)
     * to see if any of them have become expired. The implementation of
     * SeatHold is such that the checking of the expired state is thread safe.
     * Any expired SeatHolds are printed and removed from the pending list.
     *
     * @param pendingHolds the list of pending SeatHolds
     * @return the number of expired SeatHolds found
     */
    private int checkPending(List<SeatHold> pendingHolds) {
        List<SeatHold> newlyExpiredHolds = new ArrayList<>();
        for (SeatHold pendingHold : pendingHolds) {
            if (pendingHold.isExpired()) {
                newlyExpiredHolds.add(pendingHold);
                out.println(getSeatHoldString(pendingHold));   // expired
            }
        }
        int n = newlyExpiredHolds.size();
        for (SeatHold expiredHold : newlyExpiredHolds) {
            pendingHolds.remove(expiredHold);
        }
        return n;
    }

    /**
     * Generates a random number of seats for the next attempt to hold some seats.
     *
     * @param maximum the maximum number of seats
     * @return the number of seats to be attempted
     */
    private int getNumSeatsInParty(int maximum) {
        maximum = Math.max(maximum, 1);
        int numSeats = DISTRIBUTION[randomNumberGenerator.nextInt(DISTRIBUTION.length)];
        if (numSeats <= 0 || numSeats > maximum) {
            numSeats = randomNumberGenerator.nextInt(maximum) + 1;
        }
        return numSeats;
    }

    /**
     * Builds and returns a string representation of the specified venue.
     *
     * "Venue 10x20"
     *
     * @param venue the venue
     * @return venue as a string
     */
    private static String getVenueString(Venue venue) {
        return "Venue " + venue.getNumRows() + "x" + venue.getNumSeatsPerRow();
    }

    /**
     * Builds and returns a string representation of the specified seat.
     *
     * "Seat 5x15 99"
     *
     * @param seat the seat
     * @return seat as a string
     */
    private static String getSeatString(Seat seat) {
        return "Seat " + seat.getRowNum() + "x" + seat.getSeatNumInRow() + " " + seat.getBestness();
    }

    private static String getSeatHoldString(SeatHold seatHold) {
        String status = seatHold.isReserved() ? "Reserved" : seatHold.isExpired() ? "Expired" : "Held";
        StringBuilder buf = new StringBuilder(30 + (10 * seatHold.numSeatsHeld()));
        buf.append("SeatHold ");
        buf.append(status);
        for (int i = 0; i < seatHold.numSeatsHeld(); i++) {
            Seat seat = seatHold.getSeat(i);
            buf.append(" ");
            buf.append(seat.getRowNum());
            buf.append("x");
            buf.append(seat.getSeatNumInRow());
        }
        return buf.toString();
    }

    private void nap(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
