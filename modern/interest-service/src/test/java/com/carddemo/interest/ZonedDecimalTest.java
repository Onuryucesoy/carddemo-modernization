package com.carddemo.interest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ZonedDecimalTest {

    @ParameterizedTest
    @CsvSource({
            "0000000000{, 0.00",
            "0000012345F, 123.456",
            "0000007333M, -733.34",
            "0000001000}, -100.00",
            "9999999999I, 999999999.99",
    })
    void parsesOverpunchedTrailingSign(String field, String expected) {
        int scale = expected.indexOf('.') < 0 ? 0 : expected.length() - expected.indexOf('.') - 1;
        assertEquals(new BigDecimal(expected), ZonedDecimal.parse(field, scale));
    }

    @Test
    void formatsWithOverpunchedSign() {
        assertEquals("0000000154C", ZonedDecimal.format(new BigDecimal("15.43"), 9, 2));
        assertEquals("0000000104J", ZonedDecimal.format(new BigDecimal("-10.41"), 9, 2));
        assertEquals("0000000000{", ZonedDecimal.format(BigDecimal.ZERO, 9, 2));
        assertEquals("0000000000{", ZonedDecimal.format(new BigDecimal("-0.00"), 9, 2));
    }

    @Test
    void dropsHighOrderDigitsLikeCobolMove() {
        assertEquals("3333249999A", ZonedDecimal.format(new BigDecimal("8333324999.91"), 9, 2));
        assertEquals("02499999999G", ZonedDecimal.format(new BigDecimal("10249999999.97"), 10, 2));
    }
}
