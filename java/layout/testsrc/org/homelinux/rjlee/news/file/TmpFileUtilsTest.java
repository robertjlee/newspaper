package org.homelinux.rjlee.news.file;

import org.homelinux.rjlee.news.logging.CapturingLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

class TmpFileUtilsTest {

    @Test
    void recursiveDeleteOnExit() {

        CapturingLogger logger = new CapturingLogger();
        Path cwd = new File("").toPath();
        TmpFileUtils.walkFilePath(cwd, logger, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                throw new IOException("error!");
            }
       });
        logger.close();
        Assertions.assertEquals("", logger.quietCollected());
    }
}