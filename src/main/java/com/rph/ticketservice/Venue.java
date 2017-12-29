package com.rph.ticketservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A {@code Venue} is an immutable class encapsulating the static (unchanging) state of the venue.
 * It consists of a two dimensional grid of seats. There is also an unmodifiable list containing
 * those same seats in a list ordered by decreasing bestness (earlier seats in the list are better
 * than those later in the list).
 */
public class Venue {

    public static final int MAXIMUM_ROWS = 1000;

    public static final int MAXIMUM_SEATS_PER_ROW = 500;

    /** Number of rows of seats at this venue. */
    private final int numRows;

    /** Number of seats in each row at this venue. Each row contains the same number of seats. */
    private final int numSeatsPerRow;

    /** All the seats, ordered by decreasing bestness (lower index: better seat). */
    private final List<Seat> bestSeats;   // unmodifiable

    /** Two dimensional grid containing all the seats (rowNum x seatNumInRow). */
    private final SeatGrid seatGrid;


    public Venue(final int numRows, final int numSeatsPerRow, final int bestRowNum) {
        if (numRows <= 0 || numRows >= MAXIMUM_ROWS) {
            throw new IllegalArgumentException("bad rows: " + numRows);
        }
        if (numSeatsPerRow <= 0 || numSeatsPerRow >= MAXIMUM_SEATS_PER_ROW) {
            throw new IllegalArgumentException("bad seatsPerRow: " + numSeatsPerRow);
        }
        if (bestRowNum < 0 || bestRowNum >= numRows) {
            throw new IllegalArgumentException("bad bestRow: " + bestRowNum);
        }

        List<Seat> bestSeats = Collections.unmodifiableList(getBestSeats(numRows, numSeatsPerRow, bestRowNum));
        SeatGrid seatGrid = new SeatGrid(numRows, numSeatsPerRow, bestSeats);
        this.numRows = numRows;
        this.numSeatsPerRow = numSeatsPerRow;
        this.bestSeats = bestSeats;
        this.seatGrid = seatGrid;
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

    public SeatGrid getSeatGrid() {
        return seatGrid;
    }

    @VisibleForTesting
    static List<Seat> getBestSeats(int numRows, int numSeatsInRow, int bestRowNum) {
        final int numSeats = numRows * numSeatsInRow;
        int numSeatsPerAdd = (numSeatsInRow / numRows) * 2;
        if (numSeatsPerAdd <= 0) {
            numSeatsPerAdd = 1;
        }
        final int bestSeatNumInRow = (numSeatsInRow - 1) / 2;
        List<Seat> bestSeats = new ArrayList<>(numSeats);
        List<List<Integer>> bestSeatNumbersPerRow = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            bestSeatNumbersPerRow.add(
                    Stream.iterate(0, n -> (n <= 0) ? (-n + 1) : (-n))   // 0, 1, -1, 2, -2, ...
                    .map(n -> bestSeatNumInRow + n)   // bestSeatNumInRow, bestSeatNumInRow+1, bestSeatNumInRow-1, ...
                    .limit(numSeatsInRow)             // bestSeatNumInRow, ... 0, numSeatsInRow-1
                    .collect(Collectors.toCollection(LinkedList::new)));
        }
        int bestness = 0;
        for (int row : getBestRowNumbers(numRows, bestRowNum, numSeats)) {
            for (int i = 0; i < numSeatsPerAdd; i++) {
                List<Integer> nextBestSeatNumbersInRow = bestSeatNumbersPerRow.get(row);
                if (!nextBestSeatNumbersInRow.isEmpty()) {
                    Seat seat = new SeatState(row, nextBestSeatNumbersInRow.get(0), bestness++);
                    bestSeats.add(seat);
                    nextBestSeatNumbersInRow.remove(0);
                }
            }
        }
        return bestSeats;
    }

    @VisibleForTesting
    static int[] getBestRowNumbers(final int numRows, final int bestRowNum, final int len) {
        int[] rowNumSequence = new int[len];
        try {
            int x = 0;
            for (int i = 0; true; i++) {
                rowNumSequence[x++] = bestRowNum;
                for (int j = 1; j <= i; j++) {
                    int rowNum = bestRowNum + j;
                    if ((rowNum >= 0) && (rowNum < numRows)) {
                        rowNumSequence[x++] = rowNum;
                    }
                    rowNum = bestRowNum - j;
                    if ((rowNum >= 0) && (rowNum < numRows)) {
                        rowNumSequence[x++] = rowNum;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // The rowNumSequence array is full. Return it.
            return rowNumSequence;
        }
    }
}
