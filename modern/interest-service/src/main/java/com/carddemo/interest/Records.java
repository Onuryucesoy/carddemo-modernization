package com.carddemo.interest;

final class Records {
    private Records() {}

    /** Fixed-length record as COBOL sees it: CR dropped, short lines space padded. */
    static String fixed(String line, int length) {
        String s = line.replace("\r", "");
        if (s.length() > length) {
            throw new IllegalArgumentException("record longer than " + length + ": [" + s + "]");
        }
        return String.format("%-" + length + "s", s);
    }
}
