package com.kelompoksepuluh.famspay.utils;

import com.kelompoksepuluh.famspay.data.model.Transaction;

public final class TransactionTextUtils {
    private static final String NOTE_SEPARATOR = "\n---\n";

    private TransactionTextUtils() {}

    public static String packNote(String title, String note) {
        String safeTitle = title == null ? "" : title.trim();
        String safeNote = note == null ? "" : note.trim();
        return safeTitle + NOTE_SEPARATOR + safeNote;
    }

    public static String displayTitle(Transaction transaction) {
        if (transaction == null) return "Transaksi";
        if (transaction.title != null && !transaction.title.trim().isEmpty()) {
            return transaction.title.trim();
        }
        String title = titleFromStoredNote(transaction.note);
        if (!title.isEmpty()) {
            return title;
        }
        return transaction.categoryName == null || transaction.categoryName.trim().isEmpty()
                ? "Transaksi"
                : transaction.categoryName;
    }

    public static String displayNote(Transaction transaction) {
        if (transaction == null) return "-";
        String note = noteFromStoredNote(transaction.note);
        return note.isEmpty() ? "-" : note;
    }

    public static String titleFromStoredNote(String storedNote) {
        String rawNote = storedNote == null ? "" : storedNote;
        if (rawNote.trim().isEmpty()) return "";
        int separatorIndex = rawNote.indexOf(NOTE_SEPARATOR);
        if (separatorIndex >= 0) {
            return rawNote.substring(0, separatorIndex).trim();
        }
        return rawNote.trim();
    }

    public static String noteFromStoredNote(String storedNote) {
        String note = storedNote == null ? "" : storedNote;
        int separatorIndex = note.indexOf(NOTE_SEPARATOR);
        if (separatorIndex < 0) {
            return note.trim();
        }
        return note.substring(separatorIndex + NOTE_SEPARATOR.length()).trim();
    }
}
