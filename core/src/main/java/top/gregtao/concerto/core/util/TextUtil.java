package top.gregtao.concerto.core.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

public class TextUtil {
    public static boolean isDigit(String str) {
        for (char ch : str.toCharArray()) {
            if (!Character.isDigit(ch)) return false;
        }
        return true;
    }

    public static String getCurrentTime() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static String toBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String fromBase64(String str) {
        return new String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8);
    }

    public static String trimSurrounding(String s, String r1, String r2) {
        s = s.trim();
        if (s.startsWith(r1) && s.endsWith(r2)) {
            if (s.length() <= r1.length() + r2.length()) return "";
            return s.substring(r1.length(), s.length() - r2.length());
        }
        return s;
    }

    private static final Pattern SENSITIVE_PARAM = Pattern.compile("(?i)(token|userid)=([^&]+)");

    /**
     * 过滤 URL 中的敏感参数
     */
    public static String maskUrl(String url) {
        if (url == null) return null;
        return SENSITIVE_PARAM.matcher(url).replaceAll("$1=***");
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.isEmpty();
    }
}
