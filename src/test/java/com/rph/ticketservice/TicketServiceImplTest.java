package com.rph.ticketservice;

import org.junit.Test;

import java.util.stream.Stream;

public class TicketServiceImplTest {

    @Test
    public void testTest() {
        Stream.iterate(0, n -> (n <= 0) ? (-n + 1) : (-n))
                .map(n -> n + 9)
                .limit(20)
                .forEach(System.out::println);
    }
}
