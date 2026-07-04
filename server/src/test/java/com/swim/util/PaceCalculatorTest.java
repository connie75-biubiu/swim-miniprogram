package com.swim.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaceCalculatorTest {

    @Test
    void calcPace_normal() {
        assertEquals(new BigDecimal("120.00"), PaceCalculator.calcPace(200, 240));
    }

    @Test
    void calcPace_zeroDistance() {
        assertNull(PaceCalculator.calcPace(0, 240));
    }
}
