package com.carddemo.batch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

/**
 * Reads a fixed-length record file as {@link FixedRecord}s.
 *
 * <ul>
 *   <li>{@link Encoding#ASCII}: one record per line (LF or CRLF), short lines padded with spaces.</li>
 *   <li>{@link Encoding#EBCDIC}: RECFM=FB binary unload, no line separators; records are
 *       {@code recordLength} bytes and text is decoded with the configured EBCDIC code page.</li>
 * </ul>
 */
public class FixedLengthRecordReader implements ItemStreamReader<FixedRecord> {

    public enum Encoding { ASCII, EBCDIC }

    private final Path file;
    private final int recordLength;
    private final Encoding encoding;
    private final Charset charset;

    private InputStream binary;
    private BufferedReader lines;

    public FixedLengthRecordReader(Path file, int recordLength, Encoding encoding, Charset ebcdicCharset) {
        this.file = file;
        this.recordLength = recordLength;
        this.encoding = encoding;
        this.charset = encoding == Encoding.ASCII ? StandardCharsets.ISO_8859_1 : ebcdicCharset;
    }

    @Override
    public void open(ExecutionContext executionContext) {
        try {
            InputStream in = Files.newInputStream(file);
            if (encoding == Encoding.ASCII) {
                lines = new BufferedReader(new InputStreamReader(in, charset));
            } else {
                binary = in;
            }
        } catch (IOException e) {
            throw new ItemStreamException("Cannot open " + file, e);
        }
    }

    @Override
    public FixedRecord read() {
        try {
            return encoding == Encoding.ASCII ? readLine() : readBlock();
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading " + file, e);
        }
    }

    private FixedRecord readLine() throws IOException {
        String line;
        do {
            line = lines.readLine();
            if (line == null) {
                return null;
            }
            line = line.replace("\r", "");
        } while (line.isEmpty());
        if (line.length() > recordLength) {
            throw new IllegalStateException(file.getFileName() + ": line longer than " + recordLength + " bytes");
        }
        byte[] rec = new byte[recordLength];
        java.util.Arrays.fill(rec, (byte) ' ');
        byte[] data = line.getBytes(charset);
        System.arraycopy(data, 0, rec, 0, data.length);
        return FixedRecord.of(rec, charset);
    }

    private FixedRecord readBlock() throws IOException {
        byte[] rec = binary.readNBytes(recordLength);
        if (rec.length == 0) {
            return null;
        }
        if (rec.length < recordLength) {
            throw new IllegalStateException(file.getFileName() + ": trailing partial record of " + rec.length + " bytes");
        }
        return FixedRecord.of(rec, charset);
    }

    @Override
    public void close() {
        try {
            if (lines != null) {
                lines.close();
            }
            if (binary != null) {
                binary.close();
            }
        } catch (IOException e) {
            throw new ItemStreamException("Cannot close " + file, e);
        } finally {
            lines = null;
            binary = null;
        }
    }
}
