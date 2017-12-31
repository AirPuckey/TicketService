package com.rph.ticketservice;

import java.util.List;

public class Seats {

    private final Seat seatGrid[][];

    private boolean[][] seatAvailabilityGrid;


    public Seats(Venue venue) {
        this(venue.getNumberOfRows(), venue.getNumberOfSeatsPerRow(), venue.getBestSeats());
    }

    public Seats(int numRows, int numSeatsPerRow, List<Seat> bestSeats) {
        seatGrid = buildSeatGrid(numRows, numSeatsPerRow, bestSeats);
        seatAvailabilityGrid = buildSeatAvailabilityGrid(numRows, numSeatsPerRow);
    }

    static Seat[][] buildSeatGrid(int numRows, int numSeatsPerRow, List<Seat> bestSeats) {
        if (numRows * numSeatsPerRow != bestSeats.size()) {
            throw new IllegalArgumentException("bad bestSeats size: " + bestSeats.size());
        }
        Seat[][] seatGrid = new Seat[numRows][];
        for (int rowNum = 0; rowNum < numRows; rowNum++) {
            seatGrid[rowNum] = new Seat[numSeatsPerRow];
        }
        for (Seat seat: bestSeats) {
            int rowNum = seat.getRowNum();
            int seatNumInRow = seat.getSeatNumInRow();
            if (seatGrid[rowNum][seatNumInRow] != null) {
                throw new IllegalArgumentException("bestSeats contains duplicate seats!");
            }
            seatGrid[rowNum][seatNumInRow] = seat;
        }
        return seatGrid;
    }

    static boolean[][] buildSeatAvailabilityGrid(int numRows, int numSeatsPerRow) {
        boolean[][] seatAvailableGrid = new boolean[numRows][];
        for (int rowNum = 0; rowNum < numRows; rowNum++) {
            seatAvailableGrid[rowNum] = new boolean[numSeatsPerRow];
            for (int seatNumInRow = 0; seatNumInRow < numSeatsPerRow; seatNumInRow++) {
                seatAvailableGrid[rowNum][seatNumInRow] = true;
            }
        }
        return seatAvailableGrid;
    }

    public int getNumRows() {
        return seatGrid.length;
    }

    public int getNumSeatsPerRow() {
        return seatGrid[0].length;
    }

    public Seat getSeat(int rowNum, int seatNumInRow) {
        return seatGrid[rowNum][seatNumInRow];
    }

    public boolean isAvailable(int rowNum, int seatNumInRow) {
        return seatAvailabilityGrid[rowNum][seatNumInRow];
    }

    public void setAvailability(int rowNum, int seatNumInRow, boolean available) {
        seatAvailabilityGrid[rowNum][seatNumInRow] = available;
    }
}
