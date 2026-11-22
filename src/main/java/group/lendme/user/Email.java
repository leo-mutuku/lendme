package group.lendme.user;
import java.util.Locale;
import java.util.regex.Pattern;

/** An email address, always stored lower-case. */
public record Email(String value) {

    private static final Pattern SIMPLE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || !SIMPLE.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() { return value; }
}
