package com.ovunix.core.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class Tools {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static String formatPrenom(String prenom) {
        if (prenom == null || prenom.isEmpty()) {
            return prenom;
        }
        return prenom.substring(0, 1).toUpperCase() + prenom.substring(1).toLowerCase();
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return Pattern.matches(EMAIL_REGEX, email);
    }

    public static String removeAccents(String input) {
        if (input == null) {
            return null;
        }
        // Normalisation de la chaîne en Forme NFD (Normalization Form Decomposition)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Suppression des caractères diacritiques
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }
}
