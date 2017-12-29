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
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Seat)) {
            return false;
        }
        Seat that = (Seat) obj;
        return this.getRowNum() == that.getRowNum()
                && this.getSeatNumInRow() == that.getSeatNumInRow()
                && this.getBestness() == that.getBestness();
    }

    @Override
    public int hashCode() {
        return (rowNum << 20) + (seatNumInRow << 10) + bestness;
    }

    @Override
    public String toString() {
        return rowNum + " x " + seatNumInRow;
    }
}
