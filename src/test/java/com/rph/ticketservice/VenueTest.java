package com.rph.ticketservice;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class VenueTest {

    @Test
    public void testGetBestRowNumbers() {
        int[] nextBestRows = Venue.getBestRowNumbers(10,3,200);
        assertEquals(200, nextBestRows.length);

        assertEquals(3, nextBestRows[0]);
        assertEquals(3, nextBestRows[1]);
        assertEquals(4, nextBestRows[2]);
        assertEquals(2, nextBestRows[3]);
        // ...
        assertEquals(5, nextBestRows[196]);
        assertEquals(1, nextBestRows[197]);
        assertEquals(6, nextBestRows[198]);
        assertEquals(0, nextBestRows[199]);
    }

    @Test
    public void testGetBestSeats() {
        List<Seat> bestSeats = Venue.getBestSeats(10, 20, 4);

        assertEquals(4, bestSeats.get(0).getRowNum());
        assertEquals(9, bestSeats.get(0).getSeatNumInRow());
        assertEquals(4, bestSeats.get(1).getRowNum());
        assertEquals(10, bestSeats.get(1).getSeatNumInRow());
        assertEquals(4, bestSeats.get(2).getRowNum());
        assertEquals(8, bestSeats.get(2).getSeatNumInRow());
        assertEquals(4, bestSeats.get(3).getRowNum());
        assertEquals(11, bestSeats.get(3).getSeatNumInRow());
        // ...
        assertEquals(5, bestSeats.get(10).getRowNum());
        assertEquals(8, bestSeats.get(10).getSeatNumInRow());
        assertEquals(5, bestSeats.get(11).getRowNum());
        assertEquals(11, bestSeats.get(11).getSeatNumInRow());
        // ...
        assertEquals(9, bestSeats.get(198).getRowNum());
        assertEquals(0, bestSeats.get(198).getSeatNumInRow());
        assertEquals(9, bestSeats.get(199).getRowNum());
        assertEquals(19, bestSeats.get(199).getSeatNumInRow());
    }

    @Test
    public void testBuildSeatGrid() {
    }

    public static Venue buildAndValidateVenue(int numRows, int numSeatsPerRow, int bestRowNum) {
        Venue venue = new Venue(numRows, numSeatsPerRow, bestRowNum);
        List<Seat> bestSeats = venue.getBestSeats();
        SeatGrid seatGrid = venue.getSeatGrid();
        assertBestAvailableSeatsListIsValid(numRows, numSeatsPerRow, bestSeats, seatGrid);
        assertEquals(numRows * numSeatsPerRow, bestSeats.size());   // list is full (no missing seats)
        return venue;
    }

    public static void assertBestAvailableSeatsListIsValid(final int numRows, final int numSeatsPerRow,
                                                           List<Seat> bestAvailableSeats, SeatGrid seatGrid) {
        // verify that:
        //  1) each seat's coordinates are in range;
        //  2) each seat is available;
        //  3) there are no duplicate seats (coordinates are unique);
        //  4) list is ordered by increasing bestness number
        boolean[][] seatIsAvailable = buildBooleanGrid(numRows, numSeatsPerRow);
        int highestBestnesSoFar = -1;
        for (Seat seat : bestAvailableSeats) {
            final int rowNum = seat.getRowNum();
            final int seatNumInRow = seat.getSeatNumInRow();
            assertTrue("seat: bad row number: " + rowNum, rowNum >= 0 && rowNum < numRows);
            assertTrue("seat: bad seat number in row: " + seatNumInRow,
                       seatNumInRow >= 0 && seatNumInRow < numSeatsPerRow);
            assertTrue(seatGrid.isAvailable(rowNum, seatNumInRow));
            assertFalse(seatIsAvailable[seat.getRowNum()][seat.getSeatNumInRow()]);
            seatIsAvailable[seat.getRowNum()][seat.getSeatNumInRow()] = true;
            assertTrue(seat.getBestness() > highestBestnesSoFar);
            highestBestnesSoFar = seat.getBestness();
            assertTrue(highestBestnesSoFar < numRows * numSeatsPerRow);
        }
    }

    public static boolean[][] buildBooleanGrid(int numRows, int numCols) {
        boolean[][] bool = new boolean[numRows][];
        for (int rowNum = 0; rowNum < numRows; rowNum++) {
            bool[rowNum] = new boolean[numCols];
        }
        return bool;
    }
}
