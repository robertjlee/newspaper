package org.homelinux.rjlee.news.latex;

import org.homelinux.rjlee.news.logging.Logger;
import org.homelinux.rjlee.news.settings.Settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;

public class FileCache {
    private static final FileCache instance = new FileCache();
    private Path cacheFile;
    private final Map<CacheKey, Instant> dateCache = new HashMap<>();
    private final Map<CacheKey, Double> valueCache = new HashMap<>();

    public static FileCache getInstance() {
        return instance;
    }

    public void init(Settings settings) {
        Path out = settings.getOut();
        cacheFile = out.resolve(settings.getLengthsCache());
        if (Files.exists(cacheFile)) {
            load();
        }
    }

    private void load() {
        try (BufferedReader in = Files.newBufferedReader(cacheFile, StandardCharsets.UTF_8)) {
            String line;
            for (int i = 0; (line = in.readLine()) != null; i++) {
                if (line.trim().isEmpty()) continue; // skip blank lines (makes eof handling easier)
                String[] parts = line.split(";;", 3);
                if (parts.length < 3) {
                    throw new IOException("File format error on line " + i + "Expected 2 semicolons");
                }
                CacheKey key = new CacheKey(parts[0]);
                if (Files.exists(key.path)) {
                    Instant date = Instant.parse(parts[1]);
                    // evict anything that's out of date (we assume the output doesn't change during a compile)
                    if (!Files.getLastModifiedTime(key.path).toInstant().isAfter(date)) {
                        double value = Double.parseDouble(parts[2]);
                        valueCache.put(key, value);
                        dateCache.put(key, date);
                    }
                }
            }
        } catch (IOException e) {
            PrintWriter quietLogger = Logger.getInstance().quiet();
            quietLogger.println("Failed to read cache: " + e.getMessage());
            e.printStackTrace(quietLogger);
        }

    }

    public void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(cacheFile, StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(bw)) {
            for (Map.Entry<CacheKey, Double> next : valueCache.entrySet()) {

                CacheKey key = next.getKey();
                Instant lastModified = Objects.requireNonNull(dateCache.get(key));
                pw.printf("%s;;%s;;%s%n", key, lastModified, next.getValue());
            }
        } catch (IOException e) {
            PrintWriter quietLogger = Logger.getInstance().quiet();
            quietLogger.println("WARNING: Failed to write cache: " + e.getMessage());
            e.printStackTrace(quietLogger);
        }
    }

    public double calculate(Path path, List<Double> fragments, DoubleSupplier cacheFunction) {
        CacheKey key= new CacheKey(path, fragments);
        if (valueCache.containsKey(key)) {
            return valueCache.get(key);
        }
        double rtn = cacheFunction.getAsDouble();
        try {
            dateCache.put(key, Objects.requireNonNull(Files.getLastModifiedTime(key.path).toInstant()));
            valueCache.put(key, rtn);
        } catch (IOException e) {
            Logger.getInstance().quiet().println("WARNING: Can't access modified time for " + key + " " + e.getMessage());
        }
        return rtn;
    }


    private class CacheKey {
        private final Path path;
        private final List<Double> fragments;

        public CacheKey(Path path, List<Double> fragments) {
            this.path = path;
            this.fragments = new ArrayList<>(fragments);
        }

        public CacheKey(String bits) {
            String[] parts = bits.split(";");
            path = cacheFile.getFileSystem().getPath(parts[0]);
            fragments = Arrays.stream(parts).skip(1).map(Double::parseDouble).collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return path.toString() + fragments.stream().map(f -> ";" + f).collect(Collectors.joining());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(path, cacheKey.path) && Objects.equals(fragments, cacheKey.fragments);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, fragments);
        }
    }
}
