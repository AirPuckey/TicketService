package com.rph.ticketservice.implementation;


/**
 * This class contains seat information (static and dynamic).
 */
public class SeatGrid {

    /** The venue containing the seats. */
    private final Venue venue;

    /** A rectangular grid containing the availability of each seat. */
    private boolean[][] seatIsAvailable;


    /**
     * Constructs a new {@code Seats} instance.
     *
     * @param venue the associated venue
     */
    SeatGrid(Venue venue) {
        this.venue = venue;
        this.seatIsAvailable = buildSeatAvailabilityGrid(venue.getNumRows(), venue.getNumSeatsPerRow());
    }

    /**
     * Builds a rectangular array of booleans. Each element indicates whether the seat
     * at the corresponding coordinates is available (true) or not (false). Initially
     * all values are true (all seats are available).
     *
     * @param numRows number of rows in the grid
     * @param numSeatsPerRow number of columns in the grid
     * @return the two dimensional boolean array
     */
    @VisibleForTesting
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

    /**
     * Number of rows.
     *
     * @return number of rows
     */
    public int getNumRows() {
        return venue.getNumRows();
    }

    /**
     * Number of seats per row.
     *
     * @return number of seats per row
     */
    public int getNumSeatsPerRow() {
        return venue.getNumSeatsPerRow();
    }

    /**
     * Returns the seat at the specified coordinates.
     *
     * @param rowNum the row number
     * @param seatNumInRow the seat number in the row
     * @return the seat
     */
    public Seat getSeat(int rowNum, int seatNumInRow) {
        return venue.getSeat(rowNum, seatNumInRow);
    }

    /**
     * Returns true if the specified seat is available, else false.
     *
     * @param rowNum the row number
     * @param seatNumInRow the seat number in the row
     * @return the seat availability
     */
    public boolean isAvailable(int rowNum, int seatNumInRow) {
        return seatIsAvailable[rowNum][seatNumInRow];
    }

    /**
     * Sets the availability of the specified seat.
     *
     * @param rowNum the row number
     * @param seatNumInRow the seat number in the row
     * @param available the seat availability (true implies available)
     */
    void setAvailability(int rowNum, int seatNumInRow, boolean available) {
        seatIsAvailable[rowNum][seatNumInRow] = available;
    }
}
