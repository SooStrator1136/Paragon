package com.paragon.api.util.string;

import java.util.Locale;

public final class StringUtil {

    public static String getFormattedText(final Enum<?> enumIn) {
        final String text = enumIn.name();
        final StringBuilder formatted = new StringBuilder(text.length());

        boolean isFirst = true;
        for (final char c : text.toCharArray()) {
            if (c == '_') {
                isFirst = true;
                continue;
            }

            if (isFirst) {
                formatted.append(String.valueOf(c).toUpperCase(Locale.ROOT));
                isFirst = false;
            } else {
                formatted.append(String.valueOf(c).toLowerCase(Locale.ROOT));
            }
        }

        return formatted.toString();
    }

    public static String wrap(final String str, final int length) {
        final StringBuilder result = new StringBuilder(str.length());
        int lastDelimPos = 0;

        for (final String token : str.split(" ", -1)) {
            if (result.length() - lastDelimPos + token.length() > length) {
                result.append(System.lineSeparator()).append(token);
                lastDelimPos = result.length() + 1;
            } else {
                result.append((result.length() == 0) ? "" : " ").append(token);
            }
        }

        return result.toString();
    }

}
