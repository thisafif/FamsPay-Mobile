package com.kelompoksepuluh.famspay.utils;

public final class TransactionCategoryUtils {
    public static final String OTHER_CATEGORY = "Lainnya";

    public static final String[] DEFAULT_CATEGORIES = {
            "Makanan",
            "Transportasi",
            "Edukasi",
            "Tagihan",
            "Belanja",
            "Kesehatan",
            "Hiburan",
            "Tabungan",
            OTHER_CATEGORY
    };

    private TransactionCategoryUtils() {}

    public static boolean isDefaultCategory(String category) {
        if (category == null) {
            return false;
        }
        for (String defaultCategory : DEFAULT_CATEGORIES) {
            if (defaultCategory.equalsIgnoreCase(category.trim())) {
                return true;
            }
        }
        return false;
    }

    public static String normalizeDefaultCategory(String category) {
        if (category == null) {
            return OTHER_CATEGORY;
        }
        for (String defaultCategory : DEFAULT_CATEGORIES) {
            if (defaultCategory.equalsIgnoreCase(category.trim())) {
                return defaultCategory;
            }
        }
        return OTHER_CATEGORY;
    }
}
