package com.canvais

import spock.lang.Specification

/**
 * Base Spock specification establishing standard behavioral testing conventions.
 */
abstract class SpecificationBase extends Specification {

    /**
     * Helper to verify floating point coordinates with geographic tolerance.
     *
     * @param actual actual coordinate
     * @param expected expected coordinate
     * @param delta allowed tolerance (default 0.0001 degrees)
     * @return true if within tolerance
     */
    boolean isCloseTo(double actual, double expected, double delta = 0.0001) {
        Math.abs(actual - expected) <= delta
    }
}
