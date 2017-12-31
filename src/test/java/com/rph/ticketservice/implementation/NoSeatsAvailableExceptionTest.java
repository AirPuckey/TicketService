package com.rph.ticketservice.implementation;

import com.rph.ticketservice.implementation.NoSeatsAvailableException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class NoSeatsAvailableExceptionTest {

    @Test
    public void testAll() {
        try {
            throw new NoSeatsAvailableException();
        } catch (NoSeatsAvailableException e) {
            assertEquals(null, e.getMessage());
        }
        try {
            throw new NoSeatsAvailableException("Bummer!");
        } catch (NoSeatsAvailableException e) {
            assertEquals("Bummer!", e.getMessage());
        }
        Exception re = new RuntimeException("This exception was the cause!");
        try {
            throw new NoSeatsAvailableException("Bummer!", re);
        } catch (NoSeatsAvailableException e) {
            assertEquals("Bummer!", e.getMessage());
            assertEquals(re, e.getCause());
            assertEquals("This exception was the cause!", e.getCause().getMessage());
        }
        try {
            throw new NoSeatsAvailableException(re);
        } catch (NoSeatsAvailableException e) {
            assertEquals(re, e.getCause());
            assertEquals("This exception was the cause!", e.getCause().getMessage());
        }
    }
}
