package random.call.global.encrypt;

import jakarta.persistence.AttributeConverter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CryptoConverter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            return attribute != null ? AES256Util.encrypt(attribute) : null;
        } catch (Exception e) {
            throw new IllegalStateException("암호화 실패", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            return dbData != null ? AES256Util.decrypt(dbData) : null;
        } catch (Exception e) {
            throw new IllegalStateException("복호화 실패", e);
        }
    }
}
