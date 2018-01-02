package com.rph.ticketservice;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main {

    private String emailAddress = "ronald.hughes@gmail.com";

    private int[] distribution = { 1, 2, 2, 2, 2, 2, 3, 3, 4, 4, 4, 4, 4, 5, 5, 6, 6, 6, 0};

    private Random randomNumberGenerator = new Random(0);

    private Factory factory = new Factory();

    private PrintStream out = System.out;

    public static void main(String[] args) throws FileNotFoundException {
        new Main().runMain(args);
    }

    private void runMain(String[] args) throws FileNotFoundException {
        List<String> argsList = Collections.unmodifiableList(Arrays.asList(args));
        int outputFilenameIndex = argsList.indexOf("-o");
        if (outputFilenameIndex >= 0) {
            out = new PrintStream(argsList.get(outputFilenameIndex + 1));
        }
        try {
            if (argsList.contains("-v")) {
                runVenue();
            }
            if (argsList.contains("-t")) {
                runTicketService();
            }
        } finally {
            out.flush();
        }
    }

    private void runVenue() {
        final int numRows = 10, numSeatsPerRow = 20, bestRow = 4;
        Venue venue = factory.createVenue(numRows, numSeatsPerRow, bestRow);
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

    private void runTicketService() {
        final int numRows = 10, numSeatsPerRow = 20, bestRow = 3;
        Venue venue = factory.createVenue(numRows, numSeatsPerRow, bestRow);
        TicketService ticketService = factory.createTicketService(venue, 200);

        try {
            out.println(getVenueString(venue));
            List<SeatHold> pendingHolds = new ArrayList<>();
            int maximumNumberOfAdjacentSeatsAvailable = numSeatsPerRow;
            while (ticketService.numSeatsAvailable() > 0) {
                int numSeats = getNumSeatsInParty(maximumNumberOfAdjacentSeatsAvailable);
                SeatHold seatHold = ticketService.findAndHoldSeats(numSeats, emailAddress);
                if (seatHold == null) {
                    if (numSeats <= maximumNumberOfAdjacentSeatsAvailable) {
                        maximumNumberOfAdjacentSeatsAvailable = numSeats - 1;
                    }
                    continue;
                }
                nap(30);
                if (randomNumberGenerator.nextInt(100) < 90) {
                    String reservationId = ticketService.reserveSeats(seatHold.getSeatHoldId(), emailAddress);
                } else {
                    pendingHolds.add(seatHold);
                }
                out.println(getSeatHoldString(seatHold));

                List<SeatHold> newlyExpiredHolds = new ArrayList<>();
                for (SeatHold pendingHold : pendingHolds) {
                    if (pendingHold.isExpired()) {
                        newlyExpiredHolds.add(pendingHold);
                        out.println(getSeatHoldString(pendingHold));
                    }
                }
                for (SeatHold expiredHold : newlyExpiredHolds) {
                    pendingHolds.remove(expiredHold);
                }
            }
        } finally {
            out.println();
            out.println();
        }
    }

    private int getNumSeatsInParty(int maximum) {
        int numSeats = distribution[randomNumberGenerator.nextInt(distribution.length)];
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
