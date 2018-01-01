package com.rph.ticketservice.implementation;

/**
 * A {@code Seat} is an immutable object containing the static (unchanging) information
 * about a seat.
 */
public class Seat {

    /** The row number of the seat. */
    private final int rowNum;

    /** The seat number within the row. */
    private final int seatNumInRow;

    /* The bestness value for the seat. A lower number implies a better seat. */
    private final int bestness;   // between zero (inclusive) and the total number of seats in the venue (exclusive).


    /**
     * Constructs a new Seat.
     *
     * @param rowNum the row number
     * @param seatNumInRow the seat number within the row
     * @param bestness the bestness value
     */
    Seat(int rowNum, int seatNumInRow, int bestness) {
        this.rowNum = rowNum;
        this.seatNumInRow = seatNumInRow;
        this.bestness = bestness;
    }

    /**
     * The row number of this seat.
     *
     * @return the row number
     */
    public int getRowNum() {
        return rowNum;
    }

    /**
     * The seat number within the row.
     *
     * @return the seat number within the row
     */
    public int getSeatNumInRow() {
        return seatNumInRow;
    }

    /**
     * The bestness value (a lower number implies a better seat).
     *
     * @return the bestness value
     */
    public int getBestness() {
        return bestness;
    }

    /**
     * See {@code Object.equals()} method, which this method overrides.
     *
     * @param obj the object to compare this one to.
     * @return true if the object is equivalent to this seat, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
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

    /**
     * The hashcode for this seat. Consistent with equals(). See {@code Object.hashcode()} method.
     *
     * @return the hashcode for this seat
     */
    @Override
    public int hashCode() {
        return ((getRowNum() * 31) << 17) + ((getSeatNumInRow() * 31) << 9) + getBestness();
    }

    /**
     * A string representation of this seat. See {@code Object.toString()} method.
     *
     * @return a string representation of this seat.
     */
    @Override
    public String toString() {
        return (getRowNum() + 1) + "x" + (getSeatNumInRow() + 1) + ":" + getBestness();
    }
}
