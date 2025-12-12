package com.ovunix.core.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TemporalFilterDto {
    /**
     * Chemin JPA du champ à filtrer (ex: "creationDate", "audit.createdAt").
     */
    private String field;

    /**
     * Mode choisi par l'utilisateur : DATE_ONLY ou DATE_TIME.
     */
    private TemporalFilterMode mode;

    // Pour mode = DATE_ONLY
    private LocalDate fromDate;
    private LocalDate toDate;

    // Pour mode = DATE_TIME
    private LocalDateTime fromDateTime;
    private LocalDateTime toDateTime;
}
