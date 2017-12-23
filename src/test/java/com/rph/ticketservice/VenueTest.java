package com.rph.ticketservice;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

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
        for (int i : nextBestRows) {
            System.out.println(i);
        }
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

        for (Seat seat : bestSeats) {
            System.out.println(seat.getRowNum() + " x " + seat.getSeatNumInRow());
        }
    }

    @Test
    public void testBuildSeatGrid() {
    }
}
