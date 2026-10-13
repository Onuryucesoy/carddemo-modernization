package com.carddemo.interest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs the batch on a directory of ASCII datasets and writes the same files that
 * golden-master/run-cobol.sh records for the COBOL program.
 *
 * <pre>usage: InterestBatchCli &lt;input-dir&gt; &lt;output-dir&gt; &lt;parm&gt; [yyyy-MM-ddTHH:mm:ss]</pre>
 */
public final class InterestBatchCli {
    private InterestBatchCli() {}

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("usage: InterestBatchCli <input-dir> <output-dir> <parm> [timestamp]");
            System.exit(2);
        }
        LocalDateTime at = args.length > 3 ? LocalDateTime.parse(args[3]) : LocalDateTime.now();
        BatchResult r = run(Path.of(args[0]), Path.of(args[1]), args[2], at);
        r.sysout().forEach(System.out::println);
        System.exit(r.abended() ? 16 : 0);
    }

    public static BatchResult run(Path inputDir, Path outDir, String parmDate, LocalDateTime runTimestamp) throws IOException {
        BatchResult r = new InterestBatch(parmDate, runTimestamp).run(BatchInput.read(inputDir));
        Files.createDirectories(outDir);
        write(outDir.resolve("sysout.txt"), r.sysout());
        write(outDir.resolve("transact.txt"), r.transactions().stream().map(InterestTransaction::record).toList());
        write(outDir.resolve("acctdata.txt"), r.accounts().stream().map(Account::record).toList());
        write(outDir.resolve("result.txt"), List.of(r.result()));
        return r;
    }

    private static void write(Path file, List<String> lines) throws IOException {
        StringBuilder sb = new StringBuilder();
        lines.forEach(l -> sb.append(l).append('\n'));
        Files.writeString(file, sb, StandardCharsets.US_ASCII);
    }
}
