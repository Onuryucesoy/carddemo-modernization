package com.carddemo.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FixedRecordTest {

    private static FixedRecord ascii(String s) {
        return FixedRecord.of(s.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1);
    }

    @Test
    void zonedOverpunchSigns() {
        assertThat(ascii("00000001940{").zoned(12, 2)).isEqualByComparingTo("194.00");
        assertThat(ascii("0000005047G").zoned(11, 2)).isEqualByComparingTo("504.77");
        assertThat(ascii("0000005047P").zoned(11, 2)).isEqualByComparingTo("-504.77");
        assertThat(ascii("0000000000}").zoned(11, 2)).isEqualByComparingTo("0");
        assertThat(ascii("00150{").zoned(6, 2)).isEqualByComparingTo("15.00");
        assertThat(ascii("123").zoned(3, 0)).isEqualByComparingTo("123");
        assertThat(ascii("   ").zoned(3, 0)).isNull();
    }

    @Test
    void zonedRejectsGarbageWithoutLeakingContent() {
        assertThatThrownBy(() -> ascii("4111X11111111111").zoned(16, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid zoned decimal at offset 0 (length 16)")
                .hasMessageNotContaining("4111");
    }

    @Test
    void ebcdicZonedDecodesThroughCodePage() {
        Charset cp037 = Charset.forName("IBM037");
        byte[] b = {(byte) 0xF0, (byte) 0xF1, (byte) 0xF2, (byte) 0xD5};
        assertThat(FixedRecord.of(b, cp037).zoned(4, 2)).isEqualByComparingTo("-1.25");
        byte[] text = {(byte) 0xC1, (byte) 0xC4, (byte) 0xD4, 0x40, 0x00};
        assertThat(FixedRecord.of(text, cp037).text(5)).isEqualTo("ADM");
    }

    @Test
    void packedComp3() {
        byte[] pos = {0x00, 0x00, 0x00, 0x00, 0x19, 0x40, 0x0C};
        assertThat(FixedRecord.of(pos, StandardCharsets.ISO_8859_1).packed(12, 2)).isEqualByComparingTo("194.00");
        byte[] neg = {0x00, 0x00, 0x50, 0x47, 0x7D};
        assertThat(FixedRecord.of(neg, StandardCharsets.ISO_8859_1).packed(9, 2)).isEqualByComparingTo("-504.77");
        byte[] unsigned = {0x27, 0x4F};
        assertThat(FixedRecord.of(unsigned, StandardCharsets.ISO_8859_1).packed(3, 0)).isEqualByComparingTo("274");
        byte[] bad = {0x2A, 0x4C};
        assertThatThrownBy(() -> FixedRecord.of(bad, StandardCharsets.ISO_8859_1).packed(3, 0))
                .hasMessageContaining("packed decimal digit");
    }

    @Test
    void binaryCompSizesFollowDigitCount() {
        byte[] b = {0x00, 0x2F, 0x00, 0x00, 0x01, (byte) 0xFD, 0, 0, 0, 0, 0, 0, 0, 0x32};
        FixedRecord r = FixedRecord.of(b, StandardCharsets.ISO_8859_1);
        assertThat(r.binary(3)).isEqualTo(47);
        assertThat(r.binary(9)).isEqualTo(509);
        assertThat(r.binary(11)).isEqualTo(50);
        assertThat(r.position()).isEqualTo(14);
    }

    @Test
    void datesTimestampsAndFiller() {
        FixedRecord r = ascii("2014-11-20          2022-06-10 19:27:53.000000XX");
        assertThat(r.date()).isEqualTo(LocalDate.of(2014, 11, 20));
        assertThat(r.date()).isNull();
        assertThat(r.timestamp()).isEqualTo(LocalDateTime.of(2022, 6, 10, 19, 27, 53));
        r.skip(2);
        r.assertFullyConsumed("test");
    }

    @Test
    void detectsLayoutLengthMismatch() {
        FixedRecord r = ascii("ABCD");
        r.text(3);
        assertThatThrownBy(() -> r.assertFullyConsumed("X")).hasMessage("X consumed 3 bytes but the record is 4 bytes");
        assertThatThrownBy(() -> r.text(2)).hasMessageContaining("Record too short");
    }

    @Test
    void zonedHandlesFullPrecision() {
        assertThat(ascii("99999999999I").zoned(12, 2)).isEqualTo(new BigDecimal("9999999999.99"));
    }
}
