package com.ovunix.core.dto;

public enum TemporalFilterMode {
    /**
     * Filtre par jour : on considère du début de journée (00:00)
     * à la fin de journée (23:59:59.999999999) si le champ en base est LocalDateTime.
     */
    DATE_ONLY,

    /**
     * Filtre par timestamp exact : l'utilisateur fournit des LocalDateTime.
     */
    DATE_TIME
}
