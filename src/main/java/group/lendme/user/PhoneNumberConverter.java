package group.lendme.user;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PhoneNumberConverter implements AttributeConverter<PhoneNumber, String> {
    @Override public String convertToDatabaseColumn(PhoneNumber p) { return p == null ? null : p.value(); }
    @Override public PhoneNumber convertToEntityAttribute(String s) { return s == null ? null : new PhoneNumber(s); }
}