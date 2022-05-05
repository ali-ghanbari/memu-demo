package edu.iastate.memo.commons.misc;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class PropertyUtils {
    private PropertyUtils() { }

    public static int getIntProperty(final String property, final int defaultVal) {
        return Integer.parseInt(System.getProperty(property, String.valueOf(defaultVal)));
    }

    public static long getLongProperty(final String property, final long defaultVal) {
        return Long.parseLong(System.getProperty(property, String.valueOf(defaultVal)));
    }
}
