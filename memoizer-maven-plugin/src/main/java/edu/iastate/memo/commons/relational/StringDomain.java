package edu.iastate.memo.commons.relational;

import edu.utdallas.relational.Domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *@author Ali Ghanbari (alig@iastate.edu)
 */
public class StringDomain extends Domain<String> {

    public StringDomain(final String name) {
        setName(name);
    }

    public void load(final String dirName) {
        final String[] fields;
        final File domFile = new File(dirName, getName() + ".dom");
        try (final Reader reader = new FileReader(domFile);
             final BufferedReader br = new BufferedReader(reader)) {
            fields = br.readLine().split("\\s");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (!fields[0].equals(getName())) {
            throw new IllegalArgumentException();
        }
        final int size = Integer.parseInt(fields[1]);
        final File mapFile = new File(dirName, fields[2]);
        try (final Reader reader = new FileReader(mapFile);
             final BufferedReader br = new BufferedReader(reader)) {
            for (int i = 0; i < size; i++) {
                String line = br.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("Unexpected end of file");
                }
                line = line.trim();
                add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(String dirName, boolean saveDomMap) {
        try {
            super.save(dirName, saveDomMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Collection<Integer> getIndices() {
        return this.hmap.values();
    }

    public List<Integer> getIndices(final Collection<String> keys) {
        final List<Integer> list = new ArrayList<>(keys.size());
        for (final String key : keys) {
            list.add(indexOf(key));
        }
        return list;
    }

    public List<String> getKeys(final Collection<Integer> indices) {
        final List<String> list = new ArrayList<>(indices.size());
        for (final int index : indices) {
            list.add(get(index));
        }
        return list;
    }
}
