package com.ovunix.core.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {

    public static String formatToYearMonthYear(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La date ne peut pas être null");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMyy");
        return date.format(formatter);
    }

    public static LocalDate parseFromYearMonthYear(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            throw new IllegalArgumentException("La date doit être au format 'yyyyMMyy' et contenir exactement 8 caractères");
        }
        try {

            int yearFull = Integer.parseInt(dateStr.substring(0, 4)); // yyyy
            int month = Integer.parseInt(dateStr.substring(4, 6));     // MM
            int yearShort = Integer.parseInt(dateStr.substring(6, 8)); // yy

            // Convertir l'année courte en une année complète
            int yearFinal = (yearShort >= 50) ? 1900 + yearShort : 2000 + yearShort;

            // Créer l'objet LocalDate
            return LocalDate.of(yearFinal, month, 1); // On fixe le jour au 1er du mois
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("Format de date invalide : " + dateStr, e);
        }
    }


    public static int calculateAge(LocalDate birthDate,LocalDate base) {
        if (birthDate == null) {
            throw new IllegalArgumentException("La date de naissance ne peut pas être null");
        }
        // Calculer la différence entre la date de naissance et aujourd'hui
        return Period.between(birthDate, base).getYears();
    }

    // ==================== LocalDate ====================

    /**
     * Ajoute un nombre de jours à une date.
     */
    public static LocalDate addDays(LocalDate date, int days) {
        return date.plusDays(days);
    }

    /**
     * Soustrait un nombre de jours à une date.
     */
    public static LocalDate subtractDays(LocalDate date, int days) {
        return date.minusDays(days);
    }

    /**
     * Ajoute une période à une date.
     */
    public static LocalDate addPeriod(LocalDate date, int years, int months, int days) {
        return date.plus(Period.of(years, months, days));
    }

    /**
     * Soustrait une période à une date.
     */
    public static LocalDate subtractPeriod(LocalDate date, int years, int months, int days) {
        return date.minus(Period.of(years, months, days));
    }

    /**
     * Calcule l'âge d'une personne à partir de sa date de naissance.
     */
    public static int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // ==================== LocalTime ====================

    /**
     * Ajoute des heures à une heure donnée.
     */
    public static LocalTime addHours(LocalTime time, int hours) {
        return time.plusHours(hours);
    }

    /**
     * Soustrait des heures à une heure donnée.
     */
    public static LocalTime subtractHours(LocalTime time, int hours) {
        return time.minusHours(hours);
    }

    /**
     * Ajoute des minutes à une heure donnée.
     */
    public static LocalTime addMinutes(LocalTime time, int minutes) {
        return time.plusMinutes(minutes);
    }

    /**
     * Soustrait des minutes à une heure donnée.
     */
    public static LocalTime subtractMinutes(LocalTime time, int minutes) {
        return time.minusMinutes(minutes);
    }

    /**
     * Ajoute une durée à une heure donnée.
     */
    public static LocalTime addDuration(LocalTime time, long hours, long minutes, long seconds) {
        return time.plus(Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds));
    }

    /**
     * Soustrait une durée à une heure donnée.
     */
    public static LocalTime subtractDuration(LocalTime time, long hours, long minutes, long seconds) {
        return time.minus(Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds));
    }

    // ==================== LocalDateTime ====================

    /**
     * Ajoute des jours et des heures à une date et heure.
     */
    public static LocalDateTime addDaysAndHours(LocalDateTime dateTime, int days, int hours) {
        return dateTime.plusDays(days).plusHours(hours);
    }

    /**
     * Soustrait des jours et des heures à une date et heure.
     */
    public static LocalDateTime subtractDaysAndHours(LocalDateTime dateTime, int days, int hours) {
        return dateTime.minusDays(days).minusHours(hours);
    }

    /**
     * Ajoute une durée à une date et heure.
     */
    public static LocalDateTime addDuration(LocalDateTime dateTime, long hours, long minutes, long seconds) {
        return dateTime.plus(Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds));
    }

    /**
     * Soustrait une durée à une date et heure.
     */
    public static LocalDateTime subtractDuration(LocalDateTime dateTime, long hours, long minutes, long seconds) {
        return dateTime.minus(Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds));
    }

}
