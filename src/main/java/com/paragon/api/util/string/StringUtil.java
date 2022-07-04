package com.paragon.api.util.string;

public class StringUtil {

    public static String getFormattedText(Enum<?> enumIn) {
        String text = enumIn.name();
        StringBuilder formatted = new StringBuilder();

        boolean isFirst = true;
        for (char c : text.toCharArray()) {
            if (c == '_') {
                isFirst = true;
                continue;
            }

            if (isFirst) {
                formatted.append(String.valueOf(c).toUpperCase());
                isFirst = false;
            } else {
                formatted.append(String.valueOf(c).toLowerCase());
            }
        }

        return formatted.toString();
    }

    public static String wrap(String s, int length) {
        StringBuilder result = new StringBuilder();
        int lastDelimPos = 0;

        for (String token : s.split(" ", -1)) {
            if (result.length() - lastDelimPos + token.length() > length) {
                result.append("\n").append(token);
                lastDelimPos = result.length() + 1;
            }

            else {
                result.append((result.length() == 0) ? "" : " ").append(token);
            }
        }

        return result.toString();
    }

}
