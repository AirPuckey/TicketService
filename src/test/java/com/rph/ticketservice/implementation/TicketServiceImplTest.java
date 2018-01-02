package com.rph.ticketservice.implementation;

import com.rph.ticketservice.SeatHold;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.rph.ticketservice.implementation.VenueTest.buildAndValidateVenue;
import static org.junit.Assert.*;

public class TicketServiceImplTest {

    private VenueImpl venue;
    private List<SeatImpl> bestSeats;   // unmodifiable
    private SeatGrid seatGrid;
    private List<SeatImpl> bestAvailableSeats;

    private void initialize(int numRows, int numSeatsPerRow) {
        venue = buildAndValidateVenue(numRows, numSeatsPerRow, (numRows - 1) / 2);
        bestSeats = venue.getBestSeats();
        bestAvailableSeats = new ArrayList<>(bestSeats);
        seatGrid = new SeatGrid(venue);
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
            List<SeatImpl> unavailableSeats = new ArrayList<>(numRows * numSeatsPerRow);
            for (int i = 0; i < bestAvailableSeats.size(); i++) {
                unavailableSeats.add(bestAvailableSeats.remove(i));   // move every other seat to unavailableSeats
            }
            Collections.shuffle(unavailableSeats, new Random(0));   // deterministic shuffle
            while (!unavailableSeats.isEmpty()) {
                SeatImpl seat = unavailableSeats.remove(unavailableSeats.size() - 1);
                int result = TicketServiceImpl.insertSeatByBestness(seat, bestAvailableSeats, 0);
                assertEquals(bestAvailableSeats.indexOf(seat) + 1, result);
                VenueTest.assertBestAvailableSeatsListIsValid(numRows, numSeatsPerRow, bestAvailableSeats);
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
            List<SeatImpl> heldSeats;
            try {
                heldSeats = TicketServiceImpl.holdBestAdjacentSeats(numSeatsToBeHeld, bestAvailableSeats, seatGrid);
            } catch (NoSeatsAvailableException e) {
                throw new RuntimeException(e);   // should not heppen
            }
            VenueTest.assertBestAvailableSeatsListIsValid(numRows, numSeatsPerRow, bestAvailableSeats);
            assertEquals(numSeatsInVenue, bestAvailableSeats.size() + heldSeats.size());
            TicketServiceImpl.makeSeatsAvailable(heldSeats, bestAvailableSeats, seatGrid);
            VenueTest.assertBestAvailableSeatsListIsValid(numRows, numSeatsPerRow, bestAvailableSeats);
            assertEquals(numSeatsInVenue, bestAvailableSeats.size());
        } finally {
            reset();
        }
    }

    @Test
    public void testHoldSeats() {
        initialize(10, 20);
        try {
            List<SeatImpl> seatsToBeHeld = new LinkedList<>();
            seatsToBeHeld.add(seatGrid.getSeat(5, 9));
            seatsToBeHeld.add(seatGrid.getSeat(5, 10));
            seatsToBeHeld.add(seatGrid.getSeat(5, 11));
            testHoldSeats(seatsToBeHeld, bestAvailableSeats, seatGrid);
        } finally {
            reset();
        }
    }

    private void testHoldSeats(List<SeatImpl> seatsToBeHeld, List<SeatImpl> availableSeats, SeatGrid seatGrid) {
        for (SeatImpl seat : seatsToBeHeld) {
            assertTrue(seatGrid.isAvailable(seat.getRowNum(), seat.getSeatNumInRow()));
        }
        TicketServiceImpl.holdSeats(seatsToBeHeld, availableSeats, seatGrid);
        assertDisjoint(seatsToBeHeld, availableSeats);
        for (SeatImpl seat : seatsToBeHeld) {
            assertFalse(seatGrid.isAvailable(seat.getRowNum(), seat.getSeatNumInRow()));
        }
    }

    @Test
    public void testCollectAdjacentSeats() {
        initialize(6, 6);
        try {
            List<List<SeatImpl>> holds = new ArrayList<>();   // repository for heldSeats lists (so we don't lose any seats)
            holds.add(testCollectAdjacentSeats(3, seatGrid.getSeat(2,2)));
            holds.add(testCollectAdjacentSeats(6, seatGrid.getSeat(3,2)));
            holds.add(testCollectAdjacentSeats(3, seatGrid.getSeat(0,0)));
            holds.add(testCollectAdjacentSeats(3, seatGrid.getSeat(0,3)));
            holds.add(testCollectAdjacentSeats(4, seatGrid.getSeat(4,2)));
            assertEquals(5, holds.size());
            try {
                holds.add(testCollectAdjacentSeats(7, seatGrid.getSeat(5,3)));
            } catch (RuntimeException e) {
                // expected exception
            }
        } finally {
            reset();
        }
    }

    private List<SeatImpl> testCollectAdjacentSeats(int numSeatsNeeded, SeatImpl initialSeat) {
        List<SeatImpl> collectedSeats = TicketServiceImpl.collectAdjacentSeats(numSeatsNeeded, initialSeat, seatGrid);
        assertTrue(collectedSeats.contains(initialSeat));
        assertAdjacent(collectedSeats);
        return collectedSeats;
    }

    @Test
    public void testGetNumberOfAdjacentAvailableSeats() {
        initialize(10, 20);
        try {
            List<SeatImpl> collectedSeats = TicketServiceImpl.collectAdjacentSeats(4,
                    seatGrid.getSeat(6, 9), seatGrid);
            TicketServiceImpl.holdSeats(collectedSeats, bestAvailableSeats, seatGrid);
            collectedSeats = TicketServiceImpl.collectAdjacentSeats(4,
                    seatGrid.getSeat(8, 9), seatGrid);
            TicketServiceImpl.holdSeats(collectedSeats, bestAvailableSeats, seatGrid);
            collectedSeats = TicketServiceImpl.collectAdjacentSeats(4,
                    seatGrid.getSeat(8, 0), seatGrid);
            TicketServiceImpl.holdSeats(collectedSeats, bestAvailableSeats, seatGrid);
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
    public void testGetAverageBestness() {
        List<SeatImpl> seatList = new LinkedList<>();
        seatList.add(new SeatImpl(5, 9, 0));
        seatList.add(new SeatImpl(5, 10, 1));
        seatList.add(new SeatImpl(5, 11, 5));
        seatList.add(new SeatImpl(5, 11, 10));
        assertEquals(4.0, TicketServiceImpl.getAverageBestness(seatList), 0.001);

        try {
            TicketServiceImpl.getAverageBestness(new LinkedList<>());
            fail("Exception expected!");
        } catch (IllegalArgumentException e) {
            // expected exception
        }
    }

    @Test
    public void testGetBestCandidate() {
        List<List<SeatImpl>> candidates = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            List<SeatImpl> candidate = new ArrayList<>(4);
            for (int j = 0; j < 4; j++) {
                candidate.add(new SeatImpl(j, j + 1, j * j));
            }
            candidates.add(candidate);
        }
        assertEquals(candidates.get(3), TicketServiceImpl.getBestCandidate(candidates));
    }

    @Test
    public void testHoldBestAdjacentSeats() {
        initialize(10, 20);
        final int numSeats = 4;
        final int numSeatsHeldOperations = (10 * 20) / 4;   // exactly
        try {
            int numExceptions = 0;
            List<List<SeatImpl>> holds = new ArrayList<>();   // heldSeats lists (so we don't lose any seats)
            for (int i = 0; i < numSeatsHeldOperations + 1; i++) {
                try {
                    List<SeatImpl> heldSeats = testHoldBestAdjacentSeats(numSeats);
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

    private List<SeatImpl> testHoldBestAdjacentSeats(int numSeats) throws NoSeatsAvailableException {
        List<SeatImpl> heldSeats = TicketServiceImpl.holdBestAdjacentSeats(numSeats, bestAvailableSeats, seatGrid);
        assertAdjacent(heldSeats);
        assertDisjoint(heldSeats, bestAvailableSeats);
        return heldSeats;
    }

    @Test
    public void testExpiration() {
        initialize(10, 20);
        try {
            TicketServiceImpl tsi = new TicketServiceImpl(venue);
            List<SeatImpl> heldSeats;
            try {
                heldSeats = testHoldBestAdjacentSeats(7);
            } catch (NoSeatsAvailableException e) {
                throw new RuntimeException(e);
            }
            SeatHoldImpl seatHold = new SeatHoldImpl(17, "ronald.hughes@gmail.com", heldSeats);
            assertFalse(seatHold.isExpired());
            assertEquals(7, seatHold.getHeldSeats().size());
            tsi.setExpirationTimeout(seatHold, 100);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertTrue(seatHold.isExpired());
        } finally {
            reset();
        }
    }

    @Test
    public void testGetSeatHold() {
        initialize(10, 20);
        try {
            final String customerEmail = "ronald.hughes@gmail.com";
            TicketServiceImpl tsi = new TicketServiceImpl(venue);
            SeatHoldImpl seatHold1 = tsi.findAndHoldSeatsInternal(7, customerEmail);
            SeatHoldImpl seatHold2 = tsi.getSeatHold(seatHold1.getSeatHoldId());
            assertEquals(seatHold1.getSeatHoldId(), seatHold2.getSeatHoldId());
        } finally {
            reset();
        }
    }

    @Test
    public void testGetCustomerSeatHold() {
        initialize(10, 20);
        try {
            TicketServiceImpl tsi = new TicketServiceImpl(venue);
            SeatHoldImpl seatHold = tsi.findAndHoldSeatsInternal(7, "ronald.hughes@gmail.com");
            SeatHoldImpl customerSeatHold = TicketServiceImpl.getCustomerSeatHold(seatHold);
            assertEquals(seatHold, customerSeatHold);
        } catch (Exception e) {
            reset();
        }
    }

    @Test
    public void testGetReservation() {
        initialize(10, 20);
        try {
            final String customerEmail = "ronald.hughes@gmail.com";
            TicketServiceImpl tsi = new TicketServiceImpl(venue);
            SeatHoldImpl seatHold = tsi.findAndHoldSeatsInternal(7, customerEmail);
            String reservationId;
            reservationId = tsi.reserveSeats(seatHold.getSeatHoldId(), customerEmail);
            Reservation reservation = tsi.getReservation(reservationId);
            assertEquals(reservationId, reservation.getReservationId());
        } finally {
            reset();
        }
    }

    @Test
    public void testReserveSeats() {
        initialize(10, 20);
        try {
            final String customerEmail = "ronald.hughes@gmail.com";
            TicketServiceImpl tsi = new TicketServiceImpl(venue);
            SeatHoldImpl seatHold = tsi.findAndHoldSeatsInternal(7, customerEmail);
            String reservationId = tsi.reserveSeats(seatHold.getSeatHoldId(), customerEmail);
            Reservation reservation = tsi.getReservation(reservationId);
            assertEquals(reservationId, reservation.getReservationId());
            reservationId = tsi.reserveSeats(seatHold.getSeatHoldId(), customerEmail);

            seatHold = tsi.findAndHoldSeatsInternal(7, customerEmail);
            tsi.expire(seatHold);
            reservationId = tsi.reserveSeats(seatHold.getSeatHoldId(), customerEmail);
            assertNull(reservationId);
        } finally {
            reset();
        }
    }

    @Test
    public void TestFindAndHoldSeatsInternal() {
        initialize(10, 20);
        try {
            final String customerEmail = "ronald.hughes@gmail.com";
            TicketServiceImpl tsi = new TicketServiceImpl(venue);
            SeatHoldImpl seatHold1 = tsi.findAndHoldSeatsInternal(20, customerEmail);
            SeatHoldImpl seatHold2 = tsi.getSeatHold(seatHold1.getSeatHoldId());
            assertEquals(seatHold1.getSeatHoldId(), seatHold2.getSeatHoldId());

            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNull(tsi.findAndHoldSeatsInternal(20, customerEmail));   // no more seats available
        } finally {
            reset();
        }
    }

    @Test
    public void testFindAndHoldSeats() {
        initialize(10, 20);
        try {
            final String customerEmail = "ronald.hughes@gmail.com";
            TicketServiceImpl tsi = new TicketServiceImpl(venue);
            SeatHoldImpl seatHold1 = tsi.findAndHoldSeatsInternal(20, customerEmail);
            SeatHoldImpl seatHold2 = tsi.getSeatHold(seatHold1.getSeatHoldId());
            assertEquals(seatHold1.getSeatHoldId(), seatHold2.getSeatHoldId());

            SeatHold seatHold = tsi.findAndHoldSeats(20, customerEmail);
            assertNotNull(seatHold);

            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNotNull(tsi.findAndHoldSeatsInternal(20, customerEmail));
            assertNull(tsi.findAndHoldSeatsInternal(20, customerEmail));   // no more seats available
        } finally {
            reset();
        }
    }

    @Test
    public void testNumSeatsAvailable() {
        initialize(10, 20);
        try {
            final String customerEmail = "ronald.hughes@gmail.com";
            TicketServiceImpl tsi = new TicketServiceImpl(venue);
            tsi.findAndHoldSeatsInternal(7, customerEmail);
            int numSeatsAvailable = tsi.numSeatsAvailable();
            assertEquals(193, numSeatsAvailable);
        } finally {
            reset();
        }
    }

    @Test
    public void testConstructor() {
        VenueImpl venue = new VenueImpl(10, 20, 8);
        TicketServiceImpl tsi = new TicketServiceImpl(venue);
        assertNotNull(tsi);
    }

    private void assertAdjacent(List<SeatImpl> heldSeats) {
        List<SeatImpl> seats = new LinkedList<>(heldSeats);   // don't change the passed-in heldSeats list; make a copy
        seats.sort(Comparator.comparingInt(SeatImpl::getSeatNumInRow));
        int nextSeatNumInRow = seats.get(0).getSeatNumInRow();
        for (SeatImpl seat : seats) {
            assertEquals(nextSeatNumInRow, seat.getSeatNumInRow());
            nextSeatNumInRow += 1;
        }
    }

    private void assertDisjoint(List<SeatImpl> seats1, List<SeatImpl> seats2) {
        for (SeatImpl seat1 : seats1) {
            for (SeatImpl seat2 : seats2) {
                assertNotEquals(seat1, seat2);
            }
        }
    }
}
