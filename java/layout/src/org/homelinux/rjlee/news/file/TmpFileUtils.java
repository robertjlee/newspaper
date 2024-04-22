package org.homelinux.rjlee.news.file;

import org.homelinux.rjlee.news.logging.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Utilities for handling temporary files
 */
public class TmpFileUtils {
    public static void recursiveDeleteOnExit(Path path) {
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                                             @SuppressWarnings("unused") BasicFileAttributes attrs) {
                file.toFile().deleteOnExit();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                                                     @SuppressWarnings("unused") BasicFileAttributes attrs) {
                dir.toFile().deleteOnExit();
                return FileVisitResult.CONTINUE;
            }
        };
        walkFilePath(path, Logger.getInstance(), visitor);
    }


     static void walkFilePath(Path path, Logger logger, FileVisitor<Path> visitor) {
        try {
            Files.walkFileTree(path, visitor);
        } catch (IOException e) {
            PrintWriter fullLogger = logger.quiet();
            fullLogger.println("Error tidying up temp directory " + path);
            e.printStackTrace(fullLogger);
        }
    }
}
