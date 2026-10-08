package org.apache.tika.config;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Constructor;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TikaExtras_extrasDir_3_1_Test {

    @BeforeEach
    public void setUp() {
        // Set up any necessary mock objects or configurations
    }

    @Test
    public void testExtrasDir_WhenPropertyIsBlank() throws Exception {
        // Arrange
        System.setProperty(TikaExtras.EXTRAS_DIR_PROPERTY, "   ");
        Constructor<TikaExtras> constructor = TikaExtras.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        TikaExtras extras = constructor.newInstance();
        // Act
        Path result = extras.extrasDir();
        // Assert
        assertNull(result);
    }
}
