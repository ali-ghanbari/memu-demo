package edu.iastate.memo.commons.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public final class FileUtils {
    private FileUtils() { }

    public static <T> T readObject(final File file, final Class<T> type) {
        try (final InputStream is = new FileInputStream(file);
             final ObjectInputStream ois = new ObjectInputStream(is)) {
            return type.cast(ois.readObject());
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    public static void writeObject(final File file, final Object object) {
        try (final OutputStream os = new FileOutputStream(file);
             final ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(object);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
