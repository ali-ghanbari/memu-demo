package edu.iastate.memo.commons.misc;

/**
 * ANSI color and text style codes
 *
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class Ansi {
    public enum ColorCode {
        BOLD_FACE   ("\033[1m"),
        UNDERLINED  ("\033[4m"),
        NORMAL      ("\033[0m"),
        BLACK       ("\u001B[30m"),
        RED         ("\u001B[31m"),
        GREEN       ("\u001B[32m"),
        YELLOW      ("\u001B[33m"),
        BLUE        ("\u001B[34m"),
        MAGENTA     ("\u001B[35m"),
        CYAN        ("\u001B[36m"),
        WHITE       ("\u001B[37m");

        ColorCode(String code) {
            this.code = code;
        }

        private final String code;

        @Override
        public String toString() {
            return this.code;
        }
    }

    private Ansi() {

    }

    public static String constructLogMessage(final String logType, final ColorCode color, final String message) {
        return "[" + ColorCode.BOLD_FACE + color.code + logType + ColorCode.NORMAL + "] " + message;
    }

    public static String constructWarningMessage(final String logType, final String message) {
        return "[" + ColorCode.BOLD_FACE + ColorCode.YELLOW + logType + ColorCode.NORMAL + "] " + message;
    }

    public static String constructErrorMessage(final String logType, final String message) {
        return "[" + ColorCode.BOLD_FACE + ColorCode.RED + logType + ColorCode.NORMAL + "] " + message;
    }

    public static String constructInfoMessage(final String logType, final String message) {
        return "[" + ColorCode.BOLD_FACE + ColorCode.BLUE + logType + ColorCode.NORMAL + "] " + message;
    }
}