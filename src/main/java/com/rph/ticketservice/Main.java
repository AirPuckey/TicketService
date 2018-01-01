package com.rph.ticketservice;

import com.rph.ticketservice.implementation.Reservation;
import com.rph.ticketservice.implementation.SeatHold;
import com.rph.ticketservice.implementation.TicketServiceImpl;
import com.rph.ticketservice.implementation.Venue;

import java.util.Random;

public class Main {

    private String emailAddress = "ronald.hughes@gmail.com";

    private int[] distribution = { 1, 2, 2, 2, 2, 2, 3, 3, 4, 4, 4, 4, 4, 5, 5, 6, 6, 6, 0};

    private Random randomNumberGenerator = new Random(0);

    public static void main(String[] args) {
        new Main().runMain(args);
    }

    void runMain(String[] args) {
        final int numRows = 10, numSeatsPerRow = 20, bestRow = 3;
        Venue venue = new Venue(numRows, numSeatsPerRow, bestRow);
        TicketService ticketService = new TicketServiceImpl(venue, 100);

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
//            System.out.println(seatHold.toString());
            if (randomNumberGenerator.nextInt(100) < 10) {
                nap(200);
            }
            String reservationId = ticketService.reserveSeats(seatHold.getSeatHoldId(), emailAddress);
            System.out.println(seatHold.toString());
        }
    }

    private int getNumSeatsInParty(int maximum) {
        int numSeats = distribution[randomNumberGenerator.nextInt(distribution.length)];
        if (numSeats <= 0 || numSeats > maximum) {
            numSeats = randomNumberGenerator.nextInt(maximum) + 1;
        }
        return numSeats;
    }

    private void nap(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
