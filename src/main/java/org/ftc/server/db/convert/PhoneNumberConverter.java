package org.ftc.server.db.convert;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;


import javax.persistence.AttributeConverter;

import lombok.NonNull;
import lombok.SneakyThrows;

public class PhoneNumberConverter implements AttributeConverter<Phonenumber.PhoneNumber, String> {
    @Override
    public String convertToDatabaseColumn(@NonNull Phonenumber.PhoneNumber phoneNumber) {
        return PhoneNumberUtil.getInstance().format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    @SneakyThrows
    @Override
    public Phonenumber.PhoneNumber convertToEntityAttribute(@NonNull String stringRepresentation) {
        return PhoneNumberUtil.getInstance().parse(stringRepresentation, "de");
    }
}
