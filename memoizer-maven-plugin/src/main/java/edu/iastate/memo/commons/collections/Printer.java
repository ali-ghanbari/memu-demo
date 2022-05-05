package edu.iastate.memo.commons.collections;

import org.pitest.functional.F;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class Printer {
    private Printer() { }

    public static <T> String join(final Iterable<T> elements,
                                  final F<T, String> mapper,
                                  final String delimiter) {
        final StringBuilder sb = new StringBuilder();
        for (final T element : elements) {
            sb.append(delimiter).append(mapper.apply(element));
        }
        return sb.substring(delimiter.length());
    }

    public static <T> String join(final T[] elements,
                                  final F<T, String> mapper,
                                  final String delimiter) {
        final StringBuilder sb = new StringBuilder();
        final int iMax = elements.length - 1;
        if (iMax >= 0) {
            for (int i = 0; ; i++) {
                sb.append(mapper.apply(elements[i]));
                if (i == iMax) {
                    return sb.toString();
                }
                sb.append(delimiter);
            }
        }
        return "";
    }
}
