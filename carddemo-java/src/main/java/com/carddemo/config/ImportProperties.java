package com.carddemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for the sample-data import job.
 *
 * @param onStartup     run the import job when the application starts
 * @param dataDir       directory containing the {@code ASCII/} and {@code EBCDIC/} folders
 * @param source        which copy of the VSAM unloads to load
 * @param ebcdicCharset Java charset used to decode EBCDIC text
 */
@ConfigurationProperties(prefix = "carddemo.import")
public record ImportProperties(boolean onStartup, String dataDir, Source source, String ebcdicCharset) {

    public enum Source { ASCII, EBCDIC }

    public ImportProperties {
        if (dataDir == null) {
            dataDir = "../app/data";
        }
        if (source == null) {
            source = Source.ASCII;
        }
        if (ebcdicCharset == null) {
            ebcdicCharset = "IBM037";
        }
    }
}
