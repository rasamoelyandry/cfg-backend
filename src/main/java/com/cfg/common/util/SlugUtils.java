package com.cfg.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtils {

    private static final Pattern NON_ASCII = Pattern.compile("[^\\p{ASCII}]");
    private static final Pattern NON_ALPHANUM = Pattern.compile("[^a-z0-9]+");

    private SlugUtils() {}

    public static String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String ascii = NON_ASCII.matcher(normalized).replaceAll("");
        String slug = NON_ALPHANUM.matcher(ascii.toLowerCase(Locale.ROOT)).replaceAll("-");
        return slug.replaceAll("^-|-$", "");
    }
}
