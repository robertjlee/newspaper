package org.homelinux.rjlee.news.mockpath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

public class MockFileSystemProvider extends FileSystemProvider {

    private final boolean errorOnCreateDir;

    public MockFileSystemProvider(boolean errorOnCreateDir) {
        this.errorOnCreateDir = errorOnCreateDir;
    }

    @Override
    public InputStream newInputStream(Path path, OpenOption... openOptions) throws IOException {
        MockPath mockPath = (MockPath) path;
        if (!mockPath.isDirectory())
            return mockPath.newInputStream();
        return null;
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... openOptions) throws IOException {
        MockPath mockPath = (MockPath) path;
        if (!mockPath.isDirectory())
            return mockPath.newOutputStream();
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> map) throws IOException {
        return null;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        return null;
    }

    @Override
    public Path getPath(URI uri) {
        return null;
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> set, FileAttribute<?>... fileAttributes) throws IOException {
        return null;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return null;
    }

    @Override
    public void createDirectory(Path path, FileAttribute<?>... fileAttributes) throws IOException {
        if (errorOnCreateDir)
            throw new IOException("Error!");
    }

    @Override
    public void delete(Path path) throws IOException {

    }

    @Override
    public void copy(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    @Override
    public void move(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    @Override
    public boolean isSameFile(Path path, Path path1) throws IOException {
        return false;
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return null;
    }

    @Override
    public void checkAccess(Path path, AccessMode... accessModes) throws IOException {

    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> aClass, LinkOption... linkOptions) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> aClass, LinkOption... linkOptions) throws IOException {
        return (A) new BasicFileAttributes() {

            @Override
            public FileTime lastModifiedTime() {
                return null;
            }

            @Override
            public FileTime lastAccessTime() {
                return null;
            }

            @Override
            public FileTime creationTime() {
                return null;
            }

            @Override
            public boolean isRegularFile() {
                return false;
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public boolean isSymbolicLink() {
                return false;
            }

            @Override
            public boolean isOther() {
                return false;
            }

            @Override
            public long size() {
                return 0;
            }

            @Override
            public Object fileKey() {
                return null;
            }
        };
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String s, LinkOption... linkOptions) throws IOException {
        return null;
    }

    @Override
    public void setAttribute(Path path, String s, Object o, LinkOption... linkOptions) throws IOException {

    }
}
