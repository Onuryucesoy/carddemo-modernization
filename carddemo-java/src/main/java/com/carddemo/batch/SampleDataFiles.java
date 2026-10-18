package com.carddemo.batch;

import com.carddemo.batch.CopybookLayouts.Layout;
import com.carddemo.batch.FixedLengthRecordReader.Encoding;
import com.carddemo.config.ImportProperties;
import com.carddemo.config.ImportProperties.Source;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Resolves sample files for the configured source and opens readers on them. */
@Component
public class SampleDataFiles {

    private final Path dataDir;
    private final Source source;
    private final Charset ebcdicCharset;

    @Autowired
    public SampleDataFiles(ImportProperties props) {
        this(Path.of(props.dataDir()), props.source(), Charset.forName(props.ebcdicCharset()));
    }

    public SampleDataFiles(Path dataDir, Source source, Charset ebcdicCharset) {
        this.dataDir = dataDir;
        this.source = source;
        this.ebcdicCharset = ebcdicCharset;
    }

    public Source source() {
        return source;
    }

    /** Reader for the configured source; datasets without an ASCII copy fall back to EBCDIC. */
    public FixedLengthRecordReader reader(SampleDataset dataset, int recordLength) {
        boolean ascii = source == Source.ASCII && dataset.hasAscii();
        return reader(dataset, recordLength, ascii ? Encoding.ASCII : Encoding.EBCDIC);
    }

    public FixedLengthRecordReader reader(SampleDataset dataset, int recordLength, Encoding encoding) {
        Path file = encoding == Encoding.ASCII
                ? dataDir.resolve("ASCII").resolve(dataset.asciiFile())
                : dataDir.resolve("EBCDIC").resolve(dataset.ebcdicFile());
        return new FixedLengthRecordReader(file, recordLength, encoding, ebcdicCharset);
    }

    /** Reads a whole dataset into memory (used by reconciliation tests and tooling). */
    public <T> List<T> readAll(SampleDataset dataset, Layout<T> layout, Encoding encoding) {
        return readAll(reader(dataset, layout.recordLength(), encoding), layout::map);
    }

    public static <T> List<T> readAll(FixedLengthRecordReader reader, Function<FixedRecord, T> mapper) {
        List<T> out = new ArrayList<>();
        reader.open(new ExecutionContext());
        try {
            FixedRecord r;
            while ((r = reader.read()) != null) {
                out.add(mapper.apply(r));
            }
        } finally {
            reader.close();
        }
        return out;
    }
}
