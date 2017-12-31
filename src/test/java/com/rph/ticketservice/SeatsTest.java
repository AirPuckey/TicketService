package com.rph.ticketservice;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class SeatsTest {

    @Test
    public void testSettersAndGetters() {
        Seats seats = new Seats(new Venue(10, 20, 4));
        assertTrue(seats.isAvailable(5, 15));
        seats.setAvailability(5, 15, false);
        assertFalse(seats.isAvailable(5, 15));
        Seat seat = seats.getSeat(5, 15);
        assertEquals(5, seat.getRowNum());
        assertEquals(15, seat.getSeatNumInRow());
        assertEquals(20, seats.getNumSeatsPerRow());
        assertEquals(10, seats.getNumRows());
    }

    @Test
    public void testBuildSeatAvailabilityGrid() {
        boolean[][] seatsAvailabilityGrid = Seats.buildSeatAvailabilityGrid(10, 20);
        assertEquals(10, seatsAvailabilityGrid.length);
        assertEquals(20, seatsAvailabilityGrid[0].length);
        assertTrue(seatsAvailabilityGrid[5][15]);
        seatsAvailabilityGrid[5][15] = false;
        assertFalse(seatsAvailabilityGrid[5][15]);
        seatsAvailabilityGrid[5][15] = true;
        assertTrue(seatsAvailabilityGrid[5][15]);
    }

    @Test
    public void testBuildSeatGrid() {
        List<Seat> bestSeats = new Venue(10, 20, 4).getBestSeats();
        Seat[][] seatGrid = Seats.buildSeatGrid(10, 20, bestSeats);
        Seat seat = seatGrid[5][15];
        assertEquals(5, seat.getRowNum());
        assertEquals(15, seat.getSeatNumInRow());

        List<Seat> bestAvailableSeats = new ArrayList<>(bestSeats);   // writeable copy
        seat = bestAvailableSeats.remove(0);
        try {
            Seats.buildSeatGrid(10, 20, bestAvailableSeats);
            fail("Exception expected!");
        } catch (IllegalArgumentException e) {
            // expected exception
        }

        bestAvailableSeats.remove(0);
        bestAvailableSeats.add(0, seat);
        bestAvailableSeats.add(0, seat);
        try {
            Seats.buildSeatGrid(10, 20, bestAvailableSeats);
            fail("Exception expected!");
        } catch (IllegalArgumentException e) {
            // expected exception
        }
    }
}
