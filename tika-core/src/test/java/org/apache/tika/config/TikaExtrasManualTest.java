package org.apache.tika.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TikaExtrasManualTest {

    private String previousProperty;
    private ClassLoader previousContextLoader;
    private ClassLoader installed;
    private URLClassLoader custom;

    @BeforeEach
    public void saveState() {
        previousProperty = System.getProperty(TikaExtras.EXTRAS_DIR_PROPERTY);
        previousContextLoader = Thread.currentThread().getContextClassLoader();
    }

    @AfterEach
    public void restoreState() throws Exception {
        if (previousProperty == null) {
            System.clearProperty(TikaExtras.EXTRAS_DIR_PROPERTY);
        } else {
            System.setProperty(TikaExtras.EXTRAS_DIR_PROPERTY, previousProperty);
        }
        Thread.currentThread().setContextClassLoader(previousContextLoader);
        ServiceLoader.setContextClassLoader(null);
        if (installed instanceof URLClassLoader) {
            ((URLClassLoader) installed).close();
        }
        if (custom != null) {
            custom.close();
        }
    }

    @Test
    public void extraJarsAreSortedByFileName(@TempDir Path tmp) throws Exception {
        for (String name : new String[] {"B.jar", "a.jar", "C.jar", "d.jar"}) {
            Files.createFile(tmp.resolve(name));
        }
        System.setProperty(TikaExtras.EXTRAS_DIR_PROPERTY, tmp.toString());

        List<String> names = TikaExtras.extraJars().stream()
                .map(jar -> jar.getFileName().toString())
                .collect(Collectors.toList());

        assertEquals(List.of("B.jar", "C.jar", "a.jar", "d.jar"), names);
    }

    @Test
    public void installUsesThreadContextClassLoaderAsParent(@TempDir Path tmp) throws Exception {
        Files.createFile(tmp.resolve("extra.jar"));
        System.setProperty(TikaExtras.EXTRAS_DIR_PROPERTY, tmp.toString());
        custom = new URLClassLoader(new java.net.URL[0]);
        Thread.currentThread().setContextClassLoader(custom);

        installed = TikaExtras.install();

        assertSame(custom, installed.getParent());
    }

    @Test
    public void installWithNullContextClassLoaderFallsBackToTikaExtrasLoader(@TempDir Path tmp)
            throws Exception {
        Files.createFile(tmp.resolve("extra.jar"));
        System.setProperty(TikaExtras.EXTRAS_DIR_PROPERTY, tmp.toString());
        Thread.currentThread().setContextClassLoader(null);

        installed = TikaExtras.install();

        assertSame(TikaExtras.class.getClassLoader(), installed.getParent());
    }

        @Test
    public void installRegistersLoaderWithServiceLoader(@TempDir Path tmp) throws Exception {
        Files.createFile(tmp.resolve("extra.jar"));
        System.setProperty(TikaExtras.EXTRAS_DIR_PROPERTY, tmp.toString());

        installed = TikaExtras.install();

        assertSame(installed, ServiceLoader.getContextClassLoader());
    }
}