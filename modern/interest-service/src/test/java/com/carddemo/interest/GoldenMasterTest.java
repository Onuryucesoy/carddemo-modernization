package com.carddemo.interest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Replays every scenario recorded from the real COBOL program
 * (modern/golden-master/run-cobol.sh) through the Java port and requires
 * byte-for-byte identical output.
 */
class GoldenMasterTest {

    static final Path GOLDEN = Path.of(System.getProperty("golden.dir", "../golden-master"));
    /** Same PARM and clock that run-cobol.sh gives the COBOL run. */
    static final String PARM = "2022071800";
    static final LocalDateTime RUN_AT = LocalDateTime.of(2022, 7, 18, 23, 30, 0);
    static final List<String> OUTPUTS = List.of("sysout.txt", "transact.txt", "acctdata.txt", "result.txt");

    static Stream<String> scenarios() throws IOException {
        try (Stream<Path> dirs = Files.list(GOLDEN.resolve("expected"))) {
            return dirs.filter(Files::isDirectory).map(p -> p.getFileName().toString()).sorted().toList().stream();
        }
    }

    @Test
    void everyRecordedScenarioIsReplayed() throws IOException {
        assertEquals(
                List.of("abend-missing-account", "abend-missing-default", "abend-missing-xref",
                        "month-end", "overflow", "seed"),
                scenarios().toList());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void javaMatchesRecordedCobolOutput(String scenario, @TempDir Path out) throws IOException {
        InterestBatchCli.run(GOLDEN.resolve("fixtures").resolve(scenario), out, PARM, RUN_AT);
        for (String file : OUTPUTS) {
            Path expected = GOLDEN.resolve("expected").resolve(scenario).resolve(file);
            assertTrue(Files.exists(expected), "missing recording " + expected);
            assertSameLines(scenario + "/" + file, Files.readAllLines(expected), Files.readAllLines(out.resolve(file)));
        }
    }

    private static void assertSameLines(String what, List<String> expected, List<String> actual) {
        for (int i = 0; i < Math.min(expected.size(), actual.size()); i++) {
            if (!expected.get(i).equals(actual.get(i))) {
                fail(what + " line " + (i + 1) + " differs\n  COBOL: [" + expected.get(i) + "]\n  Java : [" + actual.get(i) + "]");
            }
        }
        assertEquals(expected.size(), actual.size(), what + " line count");
    }
}
