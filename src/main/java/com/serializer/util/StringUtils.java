package com.serializer.util;

/**
 * Utility class for string operations.
 * <p>
 * This class provides helper methods for common string manipulation tasks
 * used during serialization and deserialization.
 * </p>
 * 
 * @author java-serializer
 */
public final class StringUtils {
    
    private StringUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Checks if a string is null or empty.
     *
     * @param str The string to check
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * Escapes special characters in a string for JSON compatibility.
     *
     * @param str The string to escape
     * @return The escaped string
     */
    public static String escapeJson(String str) {
        if (str == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder(str.length() + 16);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int j = 0; j < 4 - hex.length(); j++) {
                            sb.append('0');
                        }
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
    
    /**
     * Unescapes a JSON string, converting escape sequences back to their original characters.
     *
     * @param str The string to unescape
     * @return The unescaped string
     */
    public static String unescapeJson(String str) {
        if (str == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);
                switch (next) {
                    case '"':
                        sb.append('"');
                        i++;
                        break;
                    case '\\':
                        sb.append('\\');
                        i++;
                        break;
                    case 'b':
                        sb.append('\b');
                        i++;
                        break;
                    case 'f':
                        sb.append('\f');
                        i++;
                        break;
                    case 'n':
                        sb.append('\n');
                        i++;
                        break;
                    case 'r':
                        sb.append('\r');
                        i++;
                        break;
                    case 't':
                        sb.append('\t');
                        i++;
                        break;
                    case 'u':
                        if (i + 5 < str.length()) {
                            try {
                                String hex = str.substring(i + 2, i + 6);
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 5;
                            } catch (NumberFormatException e) {
                                sb.append(c);
                            }
                        } else {
                            sb.append(c);
                        }
                        break;
                    default:
                        sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * Converts a camelCase string to snake_case.
     *
     * @param camelCase The camelCase string to convert
     * @return The snake_case string
     */
    public static String camelToSnakeCase(String camelCase) {
        if (camelCase == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    /**
     * Converts a snake_case string to camelCase.
     *
     * @param snakeCase The snake_case string to convert
     * @return The camelCase string
     */
    public static String snakeToCamelCase(String snakeCase) {
        if (snakeCase == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        
        for (int i = 0; i < snakeCase.length(); i++) {
            char c = snakeCase.charAt(i);
            if (c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
}