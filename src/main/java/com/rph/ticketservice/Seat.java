package com.rph.ticketservice;

/**
 * This class encapsulates a seat.
 */
public interface Seat {

    /**
     * The row number of this seat.
     *
     * @return the seat's row number
     */
    int getRowNum();

    /**
     * The seat number in the row.
     *
     * @return the seat's seat number in the row
     */
    int getSeatNumInRow();

    /**
     * The bestness value for this seat. Lower values are better seats.
     *
     * @return the seat's bestness number
     */
    int getBestness();
}
