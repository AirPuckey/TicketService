package com.rph.ticketservice.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A {@code Venue} is an immutable class encapsulating the static state of the venue.
 * It contains an unmodifiable list of all the seats ordered by decreasing bestness
 * (earlier seats in the list are better than those later in the list, and have a lower
 * bestness value). Those same seats are also contained in a rectangular grid, allowing
 * them to be obtained by seat coordinate.
 */
class Venue {

    private static final int MAXIMUM_NUMBER_OF_ROWS = 1000;   // for sanity check

    private static final int MAXIMUM_NUMBER_OF_SEATS_PER_ROW = 500;   // for sanity check

    /** Number of rows of seats at this venue. */
    private final int numRows;

    /** Number of seats in each row at this venue. Each row contains the same number of seats. */
    private final int numSeatsPerRow;

    /** All the seats, ordered by decreasing bestness (lower index: better seat). */
    private final List<Seat> bestSeats;   // unmodifiable

    /** The seats in a rectangular grid. */
    private final Seat[][] seatGrid;


    /**
     * Constructs a new immutable venue.
     *
     * @param numRows number of rows in the venue
     * @param numSeatsPerRow number of seats in each row
     * @param bestRowNum the best row number of the venue
     */
    Venue(final int numRows, final int numSeatsPerRow, final int bestRowNum) {
        if (numRows <= 0 || numRows >= MAXIMUM_NUMBER_OF_ROWS) {
            throw new IllegalArgumentException("bad rows: " + numRows);
        }
        if (numSeatsPerRow <= 0 || numSeatsPerRow >= MAXIMUM_NUMBER_OF_SEATS_PER_ROW) {
            throw new IllegalArgumentException("bad seatsPerRow: " + numSeatsPerRow);
        }
        if (bestRowNum < 0 || bestRowNum >= numRows) {
            throw new IllegalArgumentException("bad bestRow: " + bestRowNum);
        }
        this.bestSeats = Collections.unmodifiableList(buildBestSeatsList(numRows, numSeatsPerRow, bestRowNum));
        this.seatGrid = buildSeatGrid(numRows, numSeatsPerRow, bestSeats);
        this.numRows = numRows;
        this.numSeatsPerRow = numSeatsPerRow;
    }

    /**
     * Number of seats in the venue.
     *
     * @return number of seats in the venue
     */
    int getNumberOfSeats() {
        return bestSeats.size();
    }

    /**
     * Number of rows in the venue.
     *
     * @return number of rows in the venue
     */
    int getNumRows() {
        return numRows;
    }

    /**
     * Number of seats in each row.
     *
     * @return number of seats in each row
     */
    int getNumSeatsPerRow() {
        return numSeatsPerRow;
    }

    /**
     * The list of seats, ordered by bestness. This list is unmodifiable.
     *
     * @return unmodifiable list of seats, ordered by decreasing bestness
     */
    List<Seat> getBestSeats() {
        return bestSeats;
    }

    /**
     * Returns the seat corresponding to the specified row number and seat number in row.
     * Note that each seat returned is immutable.
     *
     * @param rowNum the row containing the seat
     * @param seatNumInRow the seat number in the row
     * @return the seat at the specified coordinates
     */
    Seat getSeat(int rowNum, int seatNumInRow) {
        return seatGrid[rowNum][seatNumInRow];
    }

    /**
     * Builds and returns a list containing all the seats in the venue, ordered by decreasing bestness
     * (increasing numeric values -- smaller bestness numbers are better seats). The first seat in the list
     * (the best seat) has a bestness value of zero, the next seat has a bestness value of 1, etc.
     * <p>
     * The list starts with the seat in the center of the best row, expanding outward in a diamond shape.
     * <p>
     * @param numRows number of rows in the venue
     * @param numSeatsPerRow number of seats in each row
     * @param bestRowNum the best row number of the venue
     * @return the list of seats ordered by bestness
     */
    @VisibleForTesting
    static List<Seat> buildBestSeatsList(int numRows, int numSeatsPerRow, int bestRowNum) {
        final int numSeats = numRows * numSeatsPerRow;
        int numSeatsPerAdd = Math.max((numSeatsPerRow / numRows) * 2, 1);
        final int bestSeatNumInRow = (numSeatsPerRow - 1) / 2;
        List<Seat> bestSeats = new ArrayList<>(numSeats);
        List<List<Integer>> bestSeatNumbersPerRow = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            // Seat numbers in each row ordered by bestness: start in the middle,
            // and alternate to the right and left, gradually moving out to the
            // right and left edges of the venue.
            bestSeatNumbersPerRow.add(
                    Stream.iterate(0, n -> (n <= 0) ? (-n + 1) : (-n))   // 0, +1, -1, +2, -2, +3, -3, ...
                    .map(n -> bestSeatNumInRow + n)   // bSNIR, bSNIR+1, bSNIR-1, ...
                    .limit(numSeatsPerRow)            // bSNIR, bSNIR+1, bSNIR-1, ... 0, numSeatsPerRow-1
                    .collect(Collectors.toCollection(LinkedList::new)));
        }
        int bestness = 0;
        for (int row : buildBestRowSeries(numRows, bestRowNum, numSeats)) {
            for (int i = 0; i < numSeatsPerAdd; i++) {
                List<Integer> bestSeatNumbersInThisRow = bestSeatNumbersPerRow.get(row);
                if (!bestSeatNumbersInThisRow.isEmpty()) {
                    Seat seat = new Seat(row, bestSeatNumbersInThisRow.get(0), bestness++);
                    bestSeats.add(seat);
                    bestSeatNumbersInThisRow.remove(0);
                }
            }
        }
        return bestSeats;
    }

    /**
     * Builds and returns an array of row numbers in the order that best seats should be allocated in.
     *
     * Consider the following series (disregard line breaks):
     *
     * 0,
     * 0, +1, -1,
     * 0, +1, -1, +2, -2,
     * 0, +1, -1, +2, -2, +3, -3,
     * ...
     *
     * Add bestRowNum to each element in the series, and throw out any resulting row number
     * less than zero or greater than or equal to numRows. Limit the length of the final series to len.
     *
     * @param numRows number of rows in the venue
     * @param bestRowNum the best row number in the venue
     * @param len the number of elements (row numbers) in the series
     * @return the row number sequence as described above
     */
    @VisibleForTesting
    static int[] buildBestRowSeries(final int numRows, final int bestRowNum, final int len) {
        int[] rowNumSeries = new int[len];
        try {
            int x = 0;
            for (int i = 0; true; i++) {
                rowNumSeries[x++] = bestRowNum;
                for (int j = 1; j <= i; j++) {
                    int rowNum = bestRowNum + j;
                    if ((rowNum >= 0) && (rowNum < numRows)) {
                        rowNumSeries[x++] = rowNum;
                    }
                    rowNum = bestRowNum - j;
                    if ((rowNum >= 0) && (rowNum < numRows)) {
                        rowNumSeries[x++] = rowNum;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // The rowNumSeries array is full. Return it.
            return rowNumSeries;
        }
    }

    @VisibleForTesting
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
}
