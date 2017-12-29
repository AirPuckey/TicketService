package com.rph.ticketservice;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.rph.ticketservice.VenueTest.buildAndValidateVenue;
import static org.junit.Assert.*;

public class TicketServiceImplTest {

    private Venue venue;
    private List<Seat> bestSeats;   // unmodifiable
    private SeatGrid seatGrid;
    private List<Seat> bestAvailableSeats;

    private void initialize(int numRows, int numSeatsPerRow) {
        venue = buildAndValidateVenue(numRows, numSeatsPerRow, (numRows - 1) / 2);
        bestSeats = venue.getBestSeats();
        bestAvailableSeats = new ArrayList<>(bestSeats);
        seatGrid = venue.getSeatGrid();   // TODO: make test method
    }

    private void reset() {
        venue = null;
        bestSeats = null;
        bestAvailableSeats = null;
        seatGrid = null;
    }


    @Test
    public void testInsertSeatByBestness() {
        testInsertSeatByBestness(25, 31);
        testInsertSeatByBestness(10, 20);
    }

    private void testInsertSeatByBestness(int numRows, int numSeatsPerRow) {
        initialize(numRows, numSeatsPerRow);
        try {
            List<Seat> unavailableSeats = new ArrayList<>(numRows * numSeatsPerRow);
            for (int i = 0; i < bestAvailableSeats.size(); i++) {
                unavailableSeats.add(bestAvailableSeats.remove(i));   // move every other seat to unavailableSeats
            }
            Collections.shuffle(unavailableSeats, new Random(0));   // deterministic shuffle
            while (!unavailableSeats.isEmpty()) {
                Seat seat = unavailableSeats.remove(unavailableSeats.size() - 1);
                int result = TicketServiceImpl.insertSeatByBestness(seat, bestAvailableSeats, 0);
                assertEquals(bestAvailableSeats.indexOf(seat) + 1, result);
                VenueTest.assertBestAvailableSeatsListIsValid(numRows, numSeatsPerRow, bestAvailableSeats, seatGrid);
            }
            assertEquals(bestSeats, bestAvailableSeats);
        } finally {
            reset();
        }
    }

    @Test
    public void testMakeSeatsAvailable() {
        testMakeSeatsAvailable(25, 31,  15);
        testMakeSeatsAvailable(10, 20,  7);
    }

    private void testMakeSeatsAvailable(int numRows, int numSeatsPerRow, int numSeatsToBeHeld) {
        initialize(numRows, numSeatsPerRow);
        try {
            int numSeatsInVenue = numRows * numSeatsPerRow;
            List<Seat> heldSeats;
            try {
                heldSeats = TicketServiceImpl.getBestAdjacentSeats(numSeatsToBeHeld, bestAvailableSeats, seatGrid);
            } catch (NoSeatsAvailableException e) {
                throw new RuntimeException(e);   // should not heppen
            }
            VenueTest.assertBestAvailableSeatsListIsValid(numRows, numSeatsPerRow, bestAvailableSeats, seatGrid);
            assertEquals(numSeatsInVenue, bestAvailableSeats.size() + heldSeats.size());
            TicketServiceImpl.makeSeatsAvailable(heldSeats, bestAvailableSeats, seatGrid);
            VenueTest.assertBestAvailableSeatsListIsValid(numRows, numSeatsPerRow, bestAvailableSeats, seatGrid);
            assertEquals(numSeatsInVenue, bestAvailableSeats.size());
        } finally {
            reset();
        }
    }

    @Test
    public void testHoldSeat() {
        initialize(10, 20);
        List<Seat> heldSeats = new LinkedList<>();
        try {
            testHoldSeat(4, 9, heldSeats);
            testHoldSeat(4, 10, heldSeats);
            testHoldSeat(3, 3, heldSeats);
            // try to hold a seat that's already held
            try {
                TicketServiceImpl.holdSeat(3, 3, bestAvailableSeats, heldSeats, seatGrid);
                fail("expected exception not thrown!");
            } catch(RuntimeException e) {
                // expected exception
            }
        } finally {
            reset();
        }
    }

    private void testHoldSeat(int rowNum, int seatNumInRow, List<Seat> heldSeats) {
        Seat seat = seatGrid.getSeat(rowNum, seatNumInRow);
        int numAvailableSeats = bestAvailableSeats.size();
        int numHeldSeats = heldSeats.size();
        assertFalse(heldSeats.contains(seat));
        assertTrue(bestAvailableSeats.contains(seat));
        TicketServiceImpl.holdSeat(rowNum, seatNumInRow, bestAvailableSeats, heldSeats, seatGrid);
        assertFalse(bestAvailableSeats.contains(seat));
        assertEquals(seat, heldSeats.get(heldSeats.size() - 1));
        assertEquals(numAvailableSeats - 1, bestAvailableSeats.size());
        assertEquals(numHeldSeats + 1, heldSeats.size());
    }

    @Test
    public void testHoldSeats() {
        initialize(35, 40);
        try {
            List<Seat> heldSeats1, heldSeats2, heldSeats3;
            testHoldSeats(7, 10, 19, heldSeats1 = new LinkedList<>(), +1);
            testHoldSeats(3, 12, 2, heldSeats2 = new LinkedList<>(), -1);
            heldSeats3 = new LinkedList<>();
            testHoldSeats(4, 34, 10, heldSeats3, +1);
            testHoldSeats(3, 34, 9, heldSeats3, -1);
        } finally {
            reset();
        }
    }

    private void testHoldSeats(int numSeats, int rowNum, int seatNumInRow, List<Seat> heldSeats, int direction) {
        TicketServiceImpl.holdSeats(numSeats, rowNum, seatNumInRow, bestAvailableSeats, heldSeats, seatGrid, direction);
        assertAdjacentAndHeld(heldSeats);
    }

    @Test
    public void testExtractAdjacentSeats() {
        initialize(6, 6);
        try {
            List<List<Seat>> holds = new ArrayList<>();   // repository for heldSeats lists (so we don't lose any seats)
            holds.add(testExtractAdjacentSeats(3, seatGrid.getSeat(2,2)));
            holds.add(testExtractAdjacentSeats(6,seatGrid.getSeat(3,2)));
            holds.add(testExtractAdjacentSeats(3,seatGrid.getSeat(0,0)));
            holds.add(testExtractAdjacentSeats(3, seatGrid.getSeat(0,3)));
            holds.add(testExtractAdjacentSeats(4, seatGrid.getSeat(4,2)));
            try {
                holds.add(testExtractAdjacentSeats(7, seatGrid.getSeat(5,3)));
            } catch (RuntimeException e) {
                // expected exception
            }
        } finally {
            reset();
        }
    }

    private List<Seat> testExtractAdjacentSeats(int numSeatsNeeded, Seat initialSeat) {
        List<Seat> heldSeats = TicketServiceImpl.extractAdjacentSeats(numSeatsNeeded, initialSeat,
                                                                      bestAvailableSeats, seatGrid);
        assertTrue(heldSeats.contains(initialSeat));
        assertAdjacentAndHeld(heldSeats);
        assertDisjoint(bestAvailableSeats, heldSeats);
        return heldSeats;
    }

    @Test
    public void testGetNumberOfAdjacentAvailableSeats() {
        initialize(10, 20);
        try {
            testExtractAdjacentSeats(4, seatGrid.getSeat(6, 9));
            testExtractAdjacentSeats(4, seatGrid.getSeat(8, 9));
            testExtractAdjacentSeats(4, seatGrid.getSeat(8, 0));
            assertEquals(8,
                    TicketServiceImpl.getNumberOfAdjacentAvailableSeats(seatGrid.getSeat(6, 4), seatGrid));
            assertEquals(8,
                    TicketServiceImpl.getNumberOfAdjacentAvailableSeats(seatGrid.getSeat(6, 3), seatGrid));
            assertEquals(8,
                    TicketServiceImpl.getNumberOfAdjacentAvailableSeats(seatGrid.getSeat(6, 1), seatGrid));
            assertEquals(8,
                    TicketServiceImpl.getNumberOfAdjacentAvailableSeats(seatGrid.getSeat(6, 6), seatGrid));
            assertEquals(8,
                    TicketServiceImpl.getNumberOfAdjacentAvailableSeats(seatGrid.getSeat(6, 0), seatGrid));
            assertEquals(8,
                    TicketServiceImpl.getNumberOfAdjacentAvailableSeats(seatGrid.getSeat(6, 7), seatGrid));
            assertEquals(4,
                    TicketServiceImpl.getNumberOfAdjacentAvailableSeats(seatGrid.getSeat(8, 6), seatGrid));
        } finally {
            reset();
        }

    }

    @Test
    public void testGetBestAdjacentSeats() {
        initialize(10, 20);
        final int numSeats = 4;
        final int numSeatsHeldOperations = (10 * 20) / 4;   // exactly
        try {
            int numExceptions = 0;
            List<List<Seat>> holds = new ArrayList<>();   // heldSeats lists (so we don't lose any seats)
            for (int i = 0; i < numSeatsHeldOperations + 1; i++) {
                try {
                    List<Seat> heldSeats = testGetBestAdjacentSeats(numSeats);
                    assertAdjacentAndHeld(heldSeats);
                    assertDisjoint(heldSeats, bestAvailableSeats);
                    holds.add(heldSeats);
                } catch (NoSeatsAvailableException e) {
                    numExceptions += 1;
                    assertEquals(numSeatsHeldOperations, holds.size());
                    assertEquals(0, bestAvailableSeats.size());
                }
            }
            assertEquals(1, numExceptions);
        } finally {
            reset();
        }
    }

    private List<Seat> testGetBestAdjacentSeats(int numSeats) throws NoSeatsAvailableException {
        List<Seat> heldSeats = TicketServiceImpl.getBestAdjacentSeats(numSeats, bestAvailableSeats, seatGrid);
        assertAdjacentAndHeld(heldSeats);
        assertDisjoint(heldSeats, bestAvailableSeats);
        return heldSeats;
    }

    @Test
    public void testExpiration() {
        initialize(10, 20);
        try {
            TicketServiceImpl tsi = new TicketServiceImpl(new Venue(10, 20, 7));

            List<Seat> heldSeats;
            try {
                heldSeats = testGetBestAdjacentSeats(7);
            } catch (NoSeatsAvailableException e) {
                throw new RuntimeException(e);
            }
            SeatHold seatHold = new SeatHold(17, "ronald.hughes@gmail.com", heldSeats);
            assertFalse(seatHold.isExpired());
            assertEquals(7, seatHold.getHeldSeats().size());
            tsi.setExpirationTimeout(seatHold, 1 * 1000);
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertTrue(seatHold.isExpired());
            assertTrue(seatHold.getHeldSeats().isEmpty());
        } finally {
            reset();
        }
    }

    private void assertAdjacentAndHeld(List<Seat> heldSeats) {
        List<Seat> seats = new LinkedList<>(heldSeats);   // don't change the passed-in heldSeats list; make a copy
        seats.sort(Comparator.comparingInt(Seat::getSeatNumInRow));
        int nextSeatNumInRow = seats.get(0).getSeatNumInRow();
        for (Seat seat : seats) {
            assertTrue(seatGrid.isHeld(seat.getRowNum(), seat.getSeatNumInRow()));
            assertEquals(nextSeatNumInRow, seat.getSeatNumInRow());
            nextSeatNumInRow += 1;
        }
    }

    private void assertDisjoint(List<Seat> seats1, List<Seat> seats2) {
        for (Seat seat1 : seats1) {
            for (Seat seat2 : seats2) {
                assertNotEquals(seat1, seat2);
            }
        }
    }
}
