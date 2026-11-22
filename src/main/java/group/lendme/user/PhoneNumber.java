package group.lendme.user;

import java.util.regex.Pattern;

/** A phone number in E.164 format, e.g. +254712345678. It cannot exist in an invalid state. */
public record PhoneNumber(String value) {

    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,11}$"); // max 13 chars, matches the column

    public PhoneNumber {
        if (value == null || !E164.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Phone number must be in E.164 format, e.g. +254712345678");
        }
        value = value.trim();
    }

    @Override
    public String toString() { return value; }
}