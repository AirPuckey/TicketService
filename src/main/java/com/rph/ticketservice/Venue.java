package com.rph.ticketservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Venue {

    private final int numRows;

    private final int numSeatsPerRow;

    /**
     * List of seats, ordered by decreasing bestness
     */
    private final List<Seat> bestSeats;   // unmodifiable

    /**
     * Seats arranged in two dimensional grid
     */
    private final SeatGrid seatGrid;   // seatGrid[rowNumber][seatNumberInRow]


    public Venue(int numRows, int numSeatsPerRow, int bestRowNum) {
        if (numRows <= 0 || numRows >= 1000) {
            throw new IllegalArgumentException("bad rows: " + numRows);
        }
        if (numSeatsPerRow <= 0 || numSeatsPerRow >= 500) {
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

    public List<Seat> getCopyOfBestSeats(List<Seat> seats) {
        seats.addAll(bestSeats);
        return seats;
    }

    public SeatGrid getSeatGrid() {
        return seatGrid;
    }

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