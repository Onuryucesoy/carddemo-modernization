package com.carddemo.interest;

final class FixedWidth {
    private FixedWidth() {}

    static String pad(String value, int length) {
        if (value.length() >= length) {
            return value.substring(0, length);
        }
        return value + " ".repeat(length - value.length());
    }

    static String unsigned(long value, int length) {
        String s = Long.toString(value);
        if (s.length() > length) {
            s = s.substring(s.length() - length);
        }
        return "0".repeat(length - s.length()) + s;
    }

    static String slice(String record, int offset, int length) {
        return pad(record.length() > offset ? record.substring(offset) : "", length);
    }
}
