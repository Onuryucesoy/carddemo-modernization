package com.carddemo.interest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Characterization tests: the Java service must reproduce, record for record, the outputs that the unmodified
 * legacy CBACT04C produced under GnuCOBOL (modern/golden-master/run-cobol.sh).
 */
class GoldenMasterTest {

    private static final Path GOLDEN = Path.of("..", "golden-master");
    private static final int TIMESTAMPS_START = 278;
    private static final int TIMESTAMPS_END = 330;

    @ParameterizedTest(name = "scenario {0}")
    @ValueSource(strings = {"seed", "month-end"})
    void matchesLegacyCobolOutput(String scenario) throws IOException {
        Path fixtures = GOLDEN.resolve("fixtures").resolve(scenario);
        Path expected = GOLDEN.resolve("expected").resolve(scenario);

        InterestCalculator.Result result = InterestBatch.run(
                new InterestBatch.Files4(fixtures.resolve("tcatbal.txt"), fixtures.resolve("cardxref.txt"),
                        fixtures.resolve("acctdata.txt"), fixtures.resolve("discgrp.txt")),
                "2022071800",
                Clock.fixed(Instant.parse("2022-07-18T00:00:00Z"), ZoneOffset.UTC),
                InterestCalculator.Mode.LEGACY_PARITY);

        assertEquals(
                normalize(Files.readAllLines(expected.resolve("acctdata.txt")), false),
                normalize(result.accounts().stream().map(Account::toRecord).toList(), false),
                "account master after REWRITE");
        assertEquals(
                normalize(Files.readAllLines(expected.resolve("transact.txt")), true),
                normalize(result.transactions().stream().map(InterestTransaction::toRecord).toList(), true),
                "generated interest transactions");
    }

    /** Line-sequential output drops trailing spaces; timestamps are wall-clock and excluded from comparison. */
    private static List<String> normalize(List<String> records, boolean maskTimestamps) {
        return records.stream()
                .map(r -> {
                    String s = FixedWidth.pad(r, maskTimestamps ? 350 : 300);
                    if (maskTimestamps) {
                        s = s.substring(0, TIMESTAMPS_START) + "#".repeat(TIMESTAMPS_END - TIMESTAMPS_START)
                                + s.substring(TIMESTAMPS_END);
                    }
                    return s.stripTrailing();
                })
                .toList();
    }
}
