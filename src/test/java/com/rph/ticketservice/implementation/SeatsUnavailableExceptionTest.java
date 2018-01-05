package com.rph.ticketservice.implementation;

import com.rph.ticketservice.SeatsUnavailableException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class SeatsUnavailableExceptionTest {

    @Test
    public void testAll() {
        try {
            throw new SeatsUnavailableException();
        } catch (SeatsUnavailableException e) {
            assertEquals(null, e.getMessage());
        }
        try {
            throw new SeatsUnavailableException("Bummer!");
        } catch (SeatsUnavailableException e) {
            assertEquals("Bummer!", e.getMessage());
        }
        Exception re = new RuntimeException("This exception was the cause!");
        try {
            throw new SeatsUnavailableException("Bummer!", re);
        } catch (SeatsUnavailableException e) {
            assertEquals("Bummer!", e.getMessage());
            assertEquals(re, e.getCause());
            assertEquals("This exception was the cause!", e.getCause().getMessage());
        }
        try {
            throw new SeatsUnavailableException(re);
        } catch (SeatsUnavailableException e) {
            assertEquals(re, e.getCause());
            assertEquals("This exception was the cause!", e.getCause().getMessage());
        }
    }
}
