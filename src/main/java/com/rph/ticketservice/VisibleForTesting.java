package com.rph.ticketservice;

/**
 * Annotation to indicate that a method which would otherwise be private
 * has default (package) visibility, making it accessible to unit tests
 * in the same package.
 */
public @interface VisibleForTesting { }
