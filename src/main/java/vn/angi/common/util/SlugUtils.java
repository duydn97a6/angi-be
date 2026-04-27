package vn.angi.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SlugUtils {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern EDGES_DASHES = Pattern.compile("(^-|-$)");

    private SlugUtils() {}

    public static String slugify(String input) {
        if (input == null) return null;
        String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D');
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        slug = EDGES_DASHES.matcher(slug).replaceAll("");
        return slug.toLowerCase(Locale.ROOT);
    }
}
