package com.rph.ticketservice;

public class Seat {

    private int rowNum;

    private int seatNumInRow;

    private int bestness;   // low numbers are better seats

    public Seat(int rowNum, int seatNumInRow, int bestness) {
        this.rowNum = rowNum;
        this.seatNumInRow = seatNumInRow;
        this.bestness = bestness;
    }

    public int getRowNum() {
        return rowNum;
    }

    public int getSeatNumInRow() {
        return seatNumInRow;
    }

    public int getBestness() {
        return bestness;
    }

    @Override
    public String toString() {
        return rowNum + " x " + seatNumInRow;
    }
}
