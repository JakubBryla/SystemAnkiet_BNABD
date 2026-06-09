package com.systemankiet.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SubmitResponseRequest {

    // Klucz: id pytania, wartosc: tresc odpowiedzi
    private Map<Long, String> answers;
}
