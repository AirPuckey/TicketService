package com.rph.ticketservice.implementation;

import com.rph.ticketservice.Seat;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class VenueTest {

    @Test
    public void testBuildSeatGrid() {
        List<SeatImpl> bestSeats = new VenueImpl(10, 20, 5).getBestSeats();
        SeatImpl[][] seatGrid = VenueImpl.buildSeatGrid(10, 20, bestSeats);
        SeatImpl seat = seatGrid[5][15];
        assertEquals(5, seat.getRowNum());
        assertEquals(15, seat.getSeatNumInRow());

        List<SeatImpl> bestAvailableSeats = new ArrayList<>(bestSeats);   // writeable copy
        seat = bestAvailableSeats.remove(0);
        try {
            VenueImpl.buildSeatGrid(10, 20, bestAvailableSeats);
            fail("Exception expected!");
        } catch (IllegalArgumentException e) {
            // expected exception
        }

        bestAvailableSeats.remove(0);
        bestAvailableSeats.add(0, seat);
        bestAvailableSeats.add(0, seat);
        try {
            VenueImpl.buildSeatGrid(10, 20, bestAvailableSeats);
            fail("Exception expected!");
        } catch (IllegalArgumentException e) {
            // expected exception
        }
    }

    @Test
    public void testBuildBestRowSeries() {
        int[] nextBestRows = VenueImpl.buildBestRowSeries(10,3,200);
        assertEquals(200, nextBestRows.length);

        assertEquals(3, nextBestRows[0]);
        assertEquals(4, nextBestRows[1]);
        assertEquals(2, nextBestRows[2]);
        assertEquals(3, nextBestRows[3]);
        assertEquals(4, nextBestRows[4]);
        assertEquals(2, nextBestRows[5]);
        assertEquals(5, nextBestRows[6]);
        assertEquals(1, nextBestRows[7]);
        assertEquals(3, nextBestRows[8]);
        // ...
        assertEquals(1, nextBestRows[196]);
        assertEquals(6, nextBestRows[197]);
        assertEquals(0, nextBestRows[198]);
        assertEquals(7, nextBestRows[199]);
    }

    @Test
    public void testBuildBestSeatsList() {
        List<SeatImpl> bestSeats = VenueImpl.buildBestSeatsImplList(10, 20, 4);

        assertEquals(4, bestSeats.get(0).getRowNum());
        assertEquals(9, bestSeats.get(0).getSeatNumInRow());
        assertEquals(4, bestSeats.get(1).getRowNum());
        assertEquals(10, bestSeats.get(1).getSeatNumInRow());
        assertEquals(4, bestSeats.get(2).getRowNum());
        assertEquals(8, bestSeats.get(2).getSeatNumInRow());
        assertEquals(4, bestSeats.get(3).getRowNum());
        assertEquals(11, bestSeats.get(3).getSeatNumInRow());
        // ...
        assertEquals(3, bestSeats.get(10).getRowNum());
        assertEquals(8, bestSeats.get(10).getSeatNumInRow());
        assertEquals(3, bestSeats.get(11).getRowNum());
        assertEquals(11, bestSeats.get(11).getSeatNumInRow());
        // ...
        assertEquals(9, bestSeats.get(198).getRowNum());
        assertEquals(0, bestSeats.get(198).getSeatNumInRow());
        assertEquals(9, bestSeats.get(199).getRowNum());
        assertEquals(19, bestSeats.get(199).getSeatNumInRow());
    }

    @Test
    public void testConstructor() {
        buildAndValidateVenue(10, 20, 4);
        try {
            buildAndValidateVenue(-1, 20, 4);
            fail("Exception expected!");
        } catch (IllegalArgumentException e) {
            // expected exception
        }
        try {
            buildAndValidateVenue(10, -1, 4);
            fail("Exception expected!");
        } catch (IllegalArgumentException e) {
            // expected exception
        }
        try {
            buildAndValidateVenue(10, 20, -1);
            fail("Exception expected!");
        } catch (IllegalArgumentException e) {
            // expected exception
        }
    }

    @Test
    public void testSettersAndGetters() {
        VenueImpl venue = new VenueImpl(10, 20, 5);
        assertNotNull(venue.bestSeats());
        assertEquals(10, venue.getNumRows());
        assertEquals(20, venue.getNumSeatsPerRow());
        assertEquals(10 * 20, venue.getNumberOfSeats());
        assertBestAvailableSeatListIsValid(10, 20, venue.getBestSeats());
        SeatImpl seat = venue.getSeat(5, 7);
        assertEquals(5, seat.getRowNum());
        assertEquals(7, seat.getSeatNumInRow());
    }

    static VenueImpl buildAndValidateVenue(int numRows, int numSeatsPerRow, int bestRowNum) {
        VenueImpl venue = new VenueImpl(numRows, numSeatsPerRow, bestRowNum + 1);
        List<SeatImpl> bestSeats = venue.getBestSeats();
        assertBestAvailableSeatListIsValid(numRows, numSeatsPerRow, bestSeats);
        assertEquals(numRows * numSeatsPerRow, bestSeats.size());   // list is full (no missing seats)
        return venue;
    }

    /**
     * Verifies that:
     *  1) each seat's coordinates are in range;
     *  2) there are no duplicate seats (coordinate pairs are unique);
     *  3) list is ordered by increasing bestness numeric value (decreasing bestness).
     *
     * @param numRows number of rows (to check the seat's row rnge)
     * @param numSeatsPerRow number of seats per row (to check the seat's seat in row range
     * @param bestAvailableSeats the ordered list to be verifies
     */
    public static void assertBestAvailableSeatListIsValid(final int numRows, final int numSeatsPerRow,
                                                          List<SeatImpl> bestAvailableSeats) {
        boolean[][] seatIsContainedInList = new boolean[numRows][];
        for (int rowNum = 0; rowNum < numRows; rowNum++) {
            seatIsContainedInList[rowNum] = new boolean[numSeatsPerRow];
        }
        int highestBestnesValueSoFar = -1;
        for (SeatImpl seat : bestAvailableSeats) {
            final int rowNum = seat.getRowNum();
            final int seatNumInRow = seat.getSeatNumInRow();
            assertTrue("seat: bad row number: " + rowNum, rowNum >= 0 && rowNum < numRows);
            assertTrue("seat: bad seat number in row: " + seatNumInRow,
                       seatNumInRow >= 0 && seatNumInRow < numSeatsPerRow);
            assertFalse(seatIsContainedInList[seat.getRowNum()][seat.getSeatNumInRow()]);
            seatIsContainedInList[seat.getRowNum()][seat.getSeatNumInRow()] = true;
            assertTrue(seat.getBestness() > highestBestnesValueSoFar);
            highestBestnesValueSoFar = seat.getBestness();
            assertTrue(highestBestnesValueSoFar < numRows * numSeatsPerRow);
        }
    }
}
