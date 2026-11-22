package group.lendme.user;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {
    @Override public String convertToDatabaseColumn(Email e) { return e == null ? null : e.value(); }
    @Override public Email convertToEntityAttribute(String s) { return s == null ? null : new Email(s); }
}