package com.rph.ticketservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A {@code Venue} is an immutable class encapsulating the static (unchanging) state of the venue.
 * It contains an unmodifiable list of all the seats ordered by decreasing bestness
 * (earlier seats in the list are better than those later in the list, and have a lower bestness value).
 */
public class Venue {

    private static final int MAXIMUM_NUMBER_OF_ROWS = 1000;

    private static final int MAXIMUM_NUMBER_OF_SEATS_PER_ROW = 500;

    /** Number of rows of seats at this venue. */
    private final int numRows;

    /** Number of seats in each row at this venue. Each row contains the same number of seats. */
    private final int numSeatsPerRow;

    /** All the seats, ordered by decreasing bestness (lower index: better seat). */
    private final List<Seat> bestSeats;   // unmodifiable


    /**
     * Constructs a new immutable venue.
     *
     * @param numRows number of rows in the venue
     * @param numSeatsPerRow number of seats in each row
     * @param bestRowNum the best row number of the venue
     */
    public Venue(final int numRows, final int numSeatsPerRow, final int bestRowNum) {
        if (numRows <= 0 || numRows >= MAXIMUM_NUMBER_OF_ROWS) {
            throw new IllegalArgumentException("bad rows: " + numRows);
        }
        if (numSeatsPerRow <= 0 || numSeatsPerRow >= MAXIMUM_NUMBER_OF_SEATS_PER_ROW) {
            throw new IllegalArgumentException("bad seatsPerRow: " + numSeatsPerRow);
        }
        if (bestRowNum < 0 || bestRowNum >= numRows) {
            throw new IllegalArgumentException("bad bestRow: " + bestRowNum);
        }

        List<Seat> bestSeats = Collections.unmodifiableList(buildBestSeatsList(numRows, numSeatsPerRow, bestRowNum));
        this.numRows = numRows;
        this.numSeatsPerRow = numSeatsPerRow;
        this.bestSeats = bestSeats;
    }

    public int getNumberOfSeats() {
        return bestSeats.size();
    }

    public int getNumberOfRows() {
        return numRows;
    }

    public int getNumberOfSeatsPerRow() {
        return numSeatsPerRow;
    }

    public List<Seat> getBestSeats() {
        return bestSeats;
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
            // Seat numbers in row ordered by bestness: start in the middle, and alternate to the right and left,
            // gradually moving out to the right and left edges of the venue.
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
     * Then add bestRowNum to each element in the series,
     * and throw out any resulting row number less than zero or greater than or equal to numRows.
     * Limit the length of the final series to len.
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
}
