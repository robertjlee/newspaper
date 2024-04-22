package org.homelinux.rjlee.news.mockpath;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MockFileSystem extends FileSystem {
    private final boolean errorOnCreateDir;

    public MockFileSystem(boolean errorOnCreateDir) {
        this.errorOnCreateDir = errorOnCreateDir;
    }

    @Override
    public FileSystemProvider provider() {
        return new MockFileSystemProvider(errorOnCreateDir);
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return null;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return null;
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return null;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return null;
    }

    @Override
    public Path getPath(String s, String... strings) {
        String filename = Stream.concat(Stream.of(s), Stream.of(strings)).collect(Collectors.joining("/", "", "/"));
        return new MockPath(filename, null, true, false, false, errorOnCreateDir, false, false);
    }

    @Override
    public PathMatcher getPathMatcher(String s) {
        return null;
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return null;
    }
}
