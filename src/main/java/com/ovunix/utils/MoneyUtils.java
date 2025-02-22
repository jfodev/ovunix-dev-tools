package com.ovunix.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class MoneyUtils {

    private static final Locale LOCALE_FR = new Locale("fr", "FR");

    // ==================== Formatage ====================

    /**
     * Formate un montant numérique en chaîne avec ou sans virgule.
     *
     * @param amount      Montant numérique à formater
     * @param withComma   true pour afficher les centimes, false pour un montant entier
     * @return            Montant formaté en chaîne
     */
    public static String formatToCurrency(double amount, boolean withComma) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(LOCALE_FR);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');

        String pattern = withComma ? "#,##0.00" : "#,##0";
        DecimalFormat formatter = new DecimalFormat(pattern, symbols);
        return formatter.format(amount);
    }

    // ==================== Parsing ====================

    /**
     * Convertit une chaîne formatée en montant numérique.
     *
     * @param formattedAmount  Chaîne à convertir
     * @param withComma        true si la chaîne contient des centimes, false sinon
     * @return                 Montant numérique
     * @throws ParseException  En cas de format incorrect
     */
    public static double parseCurrency(String formattedAmount, boolean withComma) throws ParseException {
        if (formattedAmount == null || formattedAmount.isEmpty()) {
            throw new IllegalArgumentException("Le montant ne peut pas être vide");
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(LOCALE_FR);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');

        String pattern = withComma ? "#,##0.00" : "#,##0";
        DecimalFormat parser = new DecimalFormat(pattern, symbols);
        parser.setParseBigDecimal(true);

        return parser.parse(formattedAmount).doubleValue();
    }


    private static final String[] UNITS = {
            "", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
            "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize",
            "dix-sept", "dix-huit", "dix-neuf"
    };

    private static final String[] TENS = {
            "", "", "vingt", "trente", "quarante", "cinquante", "soixante",
            "soixante-dix", "quatre-vingt", "quatre-vingt-dix"
    };

    /**
     * Convertit un montant numérique en lettres.
     * @param amount Montant à convertir
     * @return Chaîne représentant le montant en lettres
     */
    public static String convertToLetter(double amount) {
        if (amount < 0) {
            return "moins " + convertToLetter(-amount);
        }

        long euros = (long) amount; // Partie entière
        int cents = (int) Math.round((amount - euros) * 100); // Partie décimale

        StringBuilder result = new StringBuilder();

        // Partie euros
        if (euros == 0) {
            result.append("zéro");
        } else {
            result.append(convertNumber(euros));
            result.append(euros > 1 ? " euros" : " euro");
        }

        // Partie centimes
        if (cents > 0) {
            result.append(" et ").append(convertNumber(cents));
            result.append(cents > 1 ? " centimes" : " centime");
        }

        return result.toString();
    }

    // ==================== Méthode privée ====================

    /**
     * Convertit un nombre en lettres (sans unité).
     */
    private static String convertNumber(long number) {
        if (number == 0) return "";
        if (number < 20) return UNITS[(int) number];
        if (number < 100) return convertTens(number);
        if (number < 1000) return convertHundreds(number);
        if (number < 1_000_000) return convertThousands(number);
        return convertMillions(number);
    }

    /**
     * Convertit la dizaine.
     */
    private static String convertTens(long number) {
        int tens = (int) (number / 10);
        int unit = (int) (number % 10);

        if (tens == 7 || tens == 9) {
            return TENS[tens - 1] + "-" + UNITS[10 + unit];
        } else {
            return TENS[tens] + (unit > 0 ? "-" + UNITS[unit] : "");
        }
    }

    /**
     * Convertit la centaine.
     */
    private static String convertHundreds(long number) {
        int hundreds = (int) (number / 100);
        long remainder = number % 100;

        String result = (hundreds == 1) ? "cent" : UNITS[hundreds] + " cent";
        if (remainder > 0) result += " " + convertNumber(remainder);
        return result;
    }

    /**
     * Convertit les milliers.
     */
    private static String convertThousands(long number) {
        long thousands = number / 1000;
        long remainder = number % 1000;

        String result = (thousands == 1) ? "mille" : convertNumber(thousands) + " mille";
        if (remainder > 0) result += " " + convertNumber(remainder);
        return result;
    }

    /**
     * Convertit les millions.
     */
    private static String convertMillions(long number) {
        long millions = number / 1_000_000;
        long remainder = number % 1_000_000;

        String result = (millions == 1) ? "un million" : convertNumber(millions) + " millions";
        if (remainder > 0) result += " " + convertNumber(remainder);
        return result;
    }
}
