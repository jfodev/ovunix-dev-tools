package com.ovunix.core.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

public class FilteUtils {

    // Magic numbers pour différents types de fichiers
    private static final byte[] PDF_MAGIC_NUMBER = {0x25, 0x50, 0x44, 0x46}; // %PDF
    private static final byte[] PNG_MAGIC_NUMBER = {(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG
    private static final byte[] JPEG_MAGIC_NUMBER = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // JPEG
    private static final byte[] ZIP_MAGIC_NUMBER = {0x50, 0x4B, 0x03, 0x04}; // ZIP
    private static final byte[] GIF_MAGIC_NUMBER = {0x47, 0x49, 0x46}; // GIF
    private static final byte[] MP3_MAGIC_NUMBER = {0x49, 0x44, 0x33}; // MP3
    private static final byte[] DOCX_MAGIC_NUMBER = {0x50, 0x4B, 0x03, 0x04}; // DOCX (ZIP format)

    // Enum pour les unités de taille
    public enum SizeUnit {
        BYTES, KB, MB
    }

    // Expressions régulières pour détecter du SQL
    private static final Pattern[] SQL_PATTERNS = {
            Pattern.compile("select\\s.*\\sfrom", Pattern.CASE_INSENSITIVE),
            Pattern.compile("insert\\sinto", Pattern.CASE_INSENSITIVE),
            Pattern.compile("update\\s.*\\sset", Pattern.CASE_INSENSITIVE),
            Pattern.compile("delete\\sfrom", Pattern.CASE_INSENSITIVE),
            Pattern.compile("drop\\s.*table", Pattern.CASE_INSENSITIVE),
            Pattern.compile("union\\sselect", Pattern.CASE_INSENSITIVE),
            Pattern.compile("--"),  // Commentaire SQL
            Pattern.compile(";.*\\b")  // Point-virgule suivi d'une commande
    };

    // Expressions régulières pour détecter du JavaScript
    private static final Pattern[] JS_PATTERNS = {
            Pattern.compile("<script.*?>.*?</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("alert\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("document\\.", Pattern.CASE_INSENSITIVE),
            Pattern.compile("window\\.", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onerror\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onclick\\s*=", Pattern.CASE_INSENSITIVE)
    };

    /**
     * Convertit un tableau de bytes en une chaîne encodée en Base64.
     *
     * @param byteArray Le tableau de bytes à encoder.
     * @return La chaîne encodée en Base64.
     */
    public static String encodeToBase64(byte[] byteArray) {
        return Base64.getEncoder().encodeToString(byteArray);
    }

    /**
     * Décoder une chaîne encodée en Base64 en texte.
     *
     * @param base64String La chaîne Base64 à décoder.
     * @return Le contenu décodé en UTF-8.
     */
    public static String decodeBase64(String base64String) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Vérifie si une chaîne contient du SQL ou du JavaScript suspect.
     *
     * @param content La chaîne de texte à vérifier.
     * @return true si une injection SQL ou JS est détectée, sinon false.
     */
    public static boolean containsSqlOrJs(String content) {
        if (content == null || content.isEmpty()) return false;

        for (Pattern pattern : SQL_PATTERNS) {
            if (pattern.matcher(content).find()) {
                return true;
            }
        }

        for (Pattern pattern : JS_PATTERNS) {
            if (pattern.matcher(content).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Méthode principale pour analyser un tableau de bytes encodé en Base64.
     *
     * @param byteArray Tableau de bytes représentant le fichier.
     * @return true si du SQL ou JS suspect est détecté, sinon false.
     */
    public static boolean checkBase64FileContent(byte[] byteArray) {
        String base64 = encodeToBase64(byteArray); // Étape 1 : Encodage en Base64
        String content = decodeBase64(base64);     // Étape 2 : Décodage en texte
        return containsSqlOrJs(content);           // Étape 3 : Recherche d'injections
    }


    /**
     * Retourne la taille du tableau de bytes dans l'unité spécifiée.
     *
     * @param byteArray Tableau de bytes.
     * @param unit      Unité de taille (BYTES, KB, MB).
     * @return Taille dans l'unité spécifiée sous forme d'Optional, ou vide si le tableau est null.
     */
    public static Optional<Double> getByteArraySize(byte[] byteArray, SizeUnit unit) {
        if (byteArray == null) {
            System.err.println(" Le tableau de bytes est null.");
            return Optional.empty();
        }

        long sizeInBytes = byteArray.length; // Taille en octets
        double result;

        // Conversion en fonction de l'unité choisie
        result = switch (unit) {
            case BYTES -> sizeInBytes;                    // Octets
            case KB -> sizeInBytes / 1024.0;              // Ko (1 Ko = 1024 octets)
            case MB -> sizeInBytes / (1024.0 * 1024.0);   // Mo (1 Mo = 1024 * 1024 octets)
        };

        return Optional.of(result);
    }

    /**
     * Retourne le type du fichier en fonction de ses bytes.
     *
     * @param byteArray Tableau de bytes représentant le fichier.
     * @return Type du fichier sous forme de String.
     */
    public static String detectFileType(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 4) {
            return "Fichier non reconnu ou trop petit";
        }

        if (startsWith(byteArray, PDF_MAGIC_NUMBER)) return "PDF";
        if (startsWith(byteArray, PNG_MAGIC_NUMBER)) return "PNG";
        if (startsWith(byteArray, JPEG_MAGIC_NUMBER)) return "JPEG";
        if (startsWith(byteArray, ZIP_MAGIC_NUMBER)) return "ZIP/DOCX";
        if (startsWith(byteArray, GIF_MAGIC_NUMBER)) return "GIF";
        if (startsWith(byteArray, MP3_MAGIC_NUMBER)) return "MP3";

        return "Type inconnu";
    }

    /**
     * Vérifie si le tableau commence par un certain motif de bytes.
     */
    private static boolean startsWith(byte[] byteArray, byte[] prefix) {
        if (byteArray.length < prefix.length) return false;
        return Arrays.equals(Arrays.copyOfRange(byteArray, 0, prefix.length), prefix);
    }

}
