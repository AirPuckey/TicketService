package com.rph.ticketservice;

import java.util.List;

public class SeatGrid {

    private final SeatState[][] seatStates;


    public SeatGrid(int numRows, int numSeatsPerRow, List<Seat> bestSeats) {
        seatStates = buildSeatArray(numRows, numSeatsPerRow, bestSeats);
    }

    static SeatState[][] buildSeatArray(int numRows, int numSeatsPerRow, List<Seat> bestSeats) {
        if (numRows * numSeatsPerRow != bestSeats.size()) {
            throw new IllegalArgumentException("bad bestSeats size: " + bestSeats.size());
        }
        SeatState[][] seats = new SeatState[numRows][];
        for (int rowNum = 0; rowNum < numRows; rowNum++) {
            seats[rowNum] = new SeatState[numSeatsPerRow];
        }
        for (Seat seat: bestSeats) {
            int rowNum = seat.getRowNum();
            int seatNumInRow = seat.getSeatNumInRow();
            if (seats[rowNum][seatNumInRow] != null) {
                throw new IllegalArgumentException("bestSeats contains duplicate seats!");
            }
            seats[rowNum][seatNumInRow] = (SeatState) seat;
        }
        return seats;
    }

    public int getNumRows() {
        return seatStates.length;
    }

    public int getNumSeatsPerRow() {
        return seatStates[0].length;
    }

    public SeatState getSeatState(int rowNum, int seatNumInRow) {
        return seatStates[rowNum][seatNumInRow];
    }

    public Seat getSeat(int rowNum, int seatNumInRow) {
        return getSeatState(rowNum, seatNumInRow);
    }

    public boolean isAvailable(int rowNum, int seatNumInRow) {
        return seatStates[rowNum][seatNumInRow].isAvailable();
    }

    public void setAvailable(int rowNum, int seatNumInRow) {
        seatStates[rowNum][seatNumInRow].setAvailable();
    }

    public boolean isHeld(int rowNum, int seatNumInRow) {
        return seatStates[rowNum][seatNumInRow].isHeld();
    }

    public void setHeld(int rowNum, int seatNumInRow) {
        seatStates[rowNum][seatNumInRow].setHeld();
    }
}
