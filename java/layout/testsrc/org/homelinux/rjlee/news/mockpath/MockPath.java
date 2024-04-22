package org.homelinux.rjlee.news.mockpath;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Iterator;

public class MockPath implements Path {

    private String name;
    private String content;
    private boolean isDirectory;
    private boolean errorOnOpenOutput;
    private boolean errorOnCloseOutput;
    private boolean errorOnCreateDir;
    private boolean missingFile;
    private final boolean errorOnOpenInput;

    MockPath(String name, String content, boolean isDirectory, boolean errorOnOpenOutput, boolean errorOnCloseOutput, boolean errorOnCreateDir, boolean missingFile, boolean errorOnOpenInput) {
        this.name = name;
        this.content = content;
        this.isDirectory = isDirectory;
        this.errorOnOpenOutput = errorOnOpenOutput;
        this.errorOnCloseOutput = errorOnCloseOutput;
        this.errorOnCreateDir = errorOnCreateDir;
        this.missingFile = missingFile;
        this.errorOnOpenInput = errorOnOpenInput;
    }

    public static MockPath createMockDirectoryWithSettingsFile(String content) {
        return new MockPath("", content, true, false, false, false, false, false);
    }

    public static MockPath createMockDirectoryWithMissingSettingsFile(String content) {
        return new MockPath("", content, true, false, false, false, true, false);
    }

    public static MockPath createMockDirectoryWithSettingsFileIOError() {
        return new MockPath("", "", true, false, false, false, false, true);
    }

    public static MockPath createMockPathWithName(String name) {
        return new MockPath(name, "", false, false, false, false, false, false);
    }

    public static MockPath createMockPathWithName(String name, boolean isDirectory) {
        return new MockPath(name, "", isDirectory, false, false, false, false, false);
    }

    public static MockPath createMockPathWithNameAndContent(String name, String content) {
        return new MockPath(name, content, false, false, false, false, false, false);
    }

    public static Path createMockPathForErrorOnOpenOutput(boolean isDirectory) {
        return new MockPath("out", "", isDirectory, true, false, false, false, false);
    }

    public static Path createMockPathForErrorOnCloseOutput(boolean isDirectory) {
        return new MockPath("out", "", isDirectory, false, true, false, false, false);
    }

    public static Path createMockPathForErrorOnCreateDir(String name) {
        return new MockPath(name, "", true, false, false, true, false, false);
    }

    public static MockPath createMockPathForErrorOnOpenInput(String name) {
        return new MockPath(name, "", false, false, false, false, false, true);
    }


    @Override
    public Path resolve(String s) {
        return s.equals("settings.properties") ? new MockPath(name + "/" + s, this.content, false, false, false, false, missingFile, errorOnOpenInput) : this;
    }

    @Override
    public FileSystem getFileSystem() {
        return new MockFileSystem(errorOnCreateDir);
    }

    InputStream newInputStream() throws IOException {
        if (missingFile) {
            throw new NoSuchFileException("No file");
        } else if (errorOnOpenInput) {
            throw new IOException("Error!");
        }
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
    public OutputStream newOutputStream() throws IOException {
        if (errorOnOpenOutput)
            throw new IOException("Error!");
        if (errorOnCloseOutput) {
            return new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    throw new IOException("Error!");
                }
            };
        }
        return new ByteArrayOutputStream();
    }

    @Override
    public boolean isAbsolute() {
        return false;
    }

    @Override
    public Path getRoot() {
        return null;
    }

    @Override
    public Path getFileName() {
        return this;
    }

    @Override
    public Path getParent() {
        if (isDirectory) return null; // needed for tmp folders??
        return new MockPath("/","", true, errorOnOpenOutput, errorOnCloseOutput, errorOnCreateDir, missingFile, errorOnOpenInput);
    }

    @Override
    public int getNameCount() {
        return 0;
    }

    @Override
    public Path getName(int i) {
        return null;
    }

    @Override
    public Path subpath(int i, int i1) {
        return null;
    }

    @Override
    public boolean startsWith(Path path) {
        return false;
    }

    @Override
    public boolean startsWith(String s) {
        return false;
    }

    @Override
    public boolean endsWith(Path path) {
        return false;
    }

    @Override
    public boolean endsWith(String s) {
        return false;
    }

    @Override
    public Path normalize() {
        return null;
    }

    @Override
    public Path resolve(Path path) {
        if (isDirectory)
            return new MockPath(name, content, false, errorOnOpenOutput, errorOnCloseOutput, errorOnCreateDir, false, false);
        return null;
    }


    @Override
    public Path resolveSibling(Path path) {
        return null;
    }

    @Override
    public Path resolveSibling(String s) {
        return null;
    }

    @Override
    public Path relativize(Path path) {
        return path;
    }

    @Override
    public URI toUri() {
        return null;
    }

    @Override
    public Path toAbsolutePath() {
        return this;
    }

    @Override
    public Path toRealPath(LinkOption... linkOptions) throws IOException {
        return null;
    }

    @Override
    public File toFile() {
        return null;
    }

    @Override
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers) throws IOException {
        return null;
    }

    @Override
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>... kinds) throws IOException {
        return null;
    }

    @Override
    public Iterator<Path> iterator() {
        return null;
    }

    @Override
    public int compareTo(Path path) {
        return 0;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String toString() {
        return name;
    }

}
