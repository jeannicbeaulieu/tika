package org.apache.tika.config;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.util.Arrays;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TikaExtras_appendJarsToClasspath_2_1_Test {

    private TikaExtras tikaExtras;

    private Path mockDir;

    @BeforeEach
    public void setUp() throws Exception {
        tikaExtras = Mockito.mock(TikaExtras.class);
        mockDir = mock(Path.class);
        System.setProperty(TikaExtras.EXTRAS_DIR_PROPERTY, mockDir.toString());
    }

    @Test
    public void testAppendJarsToClasspath_NoJars() throws Exception {
        when(mockDir.toAbsolutePath()).thenReturn(Paths.get("nonexistent").toAbsolutePath());
        when(Files.isDirectory(mockDir)).thenReturn(false);
        String result = (String) TikaExtras.class.getDeclaredMethod("appendJarsToClasspath", String.class).invoke(tikaExtras, "existing:classpath");
        assertEquals("existing:classpath", result);
    }

    @Test
    public void testAppendJarsToClasspath_NoExtraJars() throws Exception {
        when(mockDir.toAbsolutePath()).thenReturn(Paths.get("nonexistent").toAbsolutePath());
        when(Files.isDirectory(mockDir)).thenReturn(true);
        String result = (String) TikaExtras.class.getDeclaredMethod("appendJarsToClasspath", String.class).invoke(tikaExtras, "existing:classpath");
        assertEquals("existing:classpath", result);
    }

    @Test
    public void testAppendJarsToClasspath_ExtraJars() throws Exception {
        Path mockJar1 = mock(Path.class);
        Path mockJar2 = mock(Path.class);
        List<Path> mockJars = new ArrayList<>(Arrays.asList(mockJar1, mockJar2));
        when(mockDir.toAbsolutePath()).thenReturn(Paths.get("existing").toAbsolutePath());
        when(Files.isDirectory(mockDir)).thenReturn(true);
        when(Files.newDirectoryStream(mockDir, "*.jar")).thenReturn(mock(DirectoryStream.class));
        when(mockJars.get(0).getFileName()).thenReturn(Paths.get("jar1.jar").getFileName());
        when(mockJars.get(1).getFileName()).thenReturn(Paths.get("jar2.jar").getFileName());
        String result = (String) TikaExtras.class.getDeclaredMethod("appendJarsToClasspath", String.class).invoke(tikaExtras, "existing:classpath");
        assertEquals("existing:classpath" + File.pathSeparator + "existing/jar1.jar" + File.pathSeparator + "existing/jar2.jar", result);
    }

    @Test
    public void testAppendJarsToClasspath_ExtraJarsWithException() throws Exception {
        Path mockJar1 = mock(Path.class);
        List<Path> mockJars = new ArrayList<>(Arrays.asList(mockJar1));
        when(mockDir.toAbsolutePath()).thenReturn(Paths.get("existing").toAbsolutePath());
        when(Files.isDirectory(mockDir)).thenReturn(true);
        when(Files.newDirectoryStream(mockDir, "*.jar")).thenReturn(mock(DirectoryStream.class));
        when(mockJars.get(0).getFileName()).thenReturn(Paths.get("jar1.jar").getFileName());
        doThrow(new Exception("Error")).when(mockJars.get(0)).toAbsolutePath();
        String result = (String) TikaExtras.class.getDeclaredMethod("appendJarsToClasspath", String.class).invoke(tikaExtras, "existing:classpath");
        assertEquals("existing:classpath", result);
    }
}
