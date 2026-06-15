package com.relfor.pcs.payroll.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relfor.pcs.payroll.dto.SalaryComponentDTO;

import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.List;
import jakarta.persistence.AttributeConverter;

@Converter
public class SalaryComponentListConverter implements AttributeConverter<List<SalaryComponentDTO>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<SalaryComponentDTO> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error serializing salary components list", e);
        }
    }

    @Override
    public List<SalaryComponentDTO> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<SalaryComponentDTO>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Error deserializing salary components list", e);
        }
    }
}