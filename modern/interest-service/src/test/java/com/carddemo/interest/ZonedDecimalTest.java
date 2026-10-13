package com.carddemo.interest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ZonedDecimalTest {

    @Test
    void parsesOverpunchedSigns() {
        assertEquals(new BigDecimal("194.00"), ZonedDecimal.parse("00000001940{", 2));
        assertEquals(new BigDecimal("-260.41"), ZonedDecimal.parse("00000002604J", 2));
        assertEquals(new BigDecimal("-250.00"), ZonedDecimal.parse("00000002500}", 2));
        assertEquals(new BigDecimal("15.00"), ZonedDecimal.parse("00150{", 2));
    }

    @Test
    void formatsLikeCobolMove() {
        assertEquals("00000019594D", ZonedDecimal.format(new BigDecimal("1959.44"), 10, 2));
        assertEquals("0000000104J", ZonedDecimal.format(new BigDecimal("-10.41"), 9, 2));
        assertEquals("00000000000{", ZonedDecimal.format(BigDecimal.ZERO, 10, 2));
    }

    @Test
    void roundTrips() {
        for (String v : new String[] {"0.01", "-0.01", "99999999.99", "-1234.56"}) {
            assertEquals(new BigDecimal(v), ZonedDecimal.parse(ZonedDecimal.format(new BigDecimal(v), 10, 2), 2));
        }
    }
}
