package com.kelompoksepuluh.famspay.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class FormatUtils {
    private static final Locale ID = new Locale("id", "ID");

    private FormatUtils() {}

    public static String rupiah(long amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(ID);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount).replace("Rp", "Rp").replace(",00", "");
    }

    public static long parseRupiah(String value) {
        if (value == null) return 0;
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String toApiDate(String displayDate) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy", ID);
            parser.setLenient(false);
            Date date = parser.parse(displayDate);
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
        } catch (ParseException e) {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        }
    }

    public static boolean isValidDisplayDate(String displayDate) {
        return parseDisplayDate(displayDate) != null;
    }

    public static boolean isFutureDisplayDate(String displayDate) {
        Date date = parseDisplayDate(displayDate);
        if (date == null) return false;

        Calendar selected = Calendar.getInstance();
        selected.setTime(date);
        truncateTime(selected);

        Calendar today = Calendar.getInstance();
        truncateTime(today);

        return selected.after(today);
    }

    public static String apiDateToDisplay(String apiDate) {
        if (apiDate == null || apiDate.length() < 10) return "";
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            parser.setLenient(false);
            Date date = parser.parse(apiDate.substring(0, 10));
            return new SimpleDateFormat("dd/MM/yyyy", ID).format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    public static String displayDate(String apiDate) {
        if (apiDate == null) return "-";
        try {
            String normalized = apiDate.length() >= 10 ? apiDate.substring(0, 10) : apiDate;
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            parser.setLenient(false);
            Date date = parser.parse(normalized);
            return new SimpleDateFormat("d MMMM yyyy", ID).format(date);
        } catch (ParseException e) {
            return apiDate;
        }
    }

    public static String currentPeriodMonth() {
        return new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
    }

    private static Date parseDisplayDate(String displayDate) {
        if (displayDate == null) return null;
        try {
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy", ID);
            parser.setLenient(false);
            return parser.parse(displayDate);
        } catch (ParseException e) {
            return null;
        }
    }

    private static void truncateTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
