package org.apache.tika.mime;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class Patterns_add_1_0_Test {

    private Patterns patterns;

    private MediaTypeRegistry registry;

    @BeforeEach
    public void setUp() {
        registry = MediaTypeRegistry.getDefaultRegistry();
        patterns = new Patterns(registry);
    }

    @Test
    public void testAddName() throws MimeTypeException, NoSuchFieldException, IllegalAccessException {
        MimeType type1 = new MimeType(MediaType.parse("text/plain"));
        patterns.add("text", false, type1);
        Field namesField = Patterns.class.getDeclaredField("names");
        namesField.setAccessible(true);
        Map<String, MimeType> names = (Map<String, MimeType>) namesField.get(patterns);
        assertEquals(type1, names.get("text"));
        assertNull(names.get("text/plain"));
        MimeType type2 = new MimeType(MediaType.parse("text/html"));
        patterns.add("text", false, type2);
        assertEquals(type2, names.get("text"));
    }

    @Test
    public void testAddExtension() throws MimeTypeException, NoSuchFieldException, IllegalAccessException {
        MimeType type1 = new MimeType(MediaType.parse("text/plain"));
        patterns.add("txt", false, type1);
        Field extensionsField = Patterns.class.getDeclaredField("extensions");
        extensionsField.setAccessible(true);
        Map<String, MimeType> extensions = (Map<String, MimeType>) extensionsField.get(patterns);
        assertEquals(type1, extensions.get("txt"));
        assertNull(extensions.get("txt"));
        MimeType type2 = new MimeType(MediaType.parse("text/html"));
        patterns.add("html", false, type2);
        assertEquals(type2, extensions.get("html"));
    }

    @Test
    public void testAddGlob() throws MimeTypeException, NoSuchFieldException, IllegalAccessException {
        MimeType type1 = new MimeType(MediaType.parse("text/plain"));
        patterns.add("*.txt", false, type1);
        Field globsField = Patterns.class.getDeclaredField("globs");
        globsField.setAccessible(true);
        SortedMap<String, MimeType> globs = (SortedMap<String, MimeType>) globsField.get(patterns);
        assertEquals(type1, globs.get("*.txt"));
        assertNull(globs.get("*.txt"));
        MimeType type2 = new MimeType(MediaType.parse("text/html"));
        patterns.add("*.html", false, type2);
        assertEquals(type2, globs.get("*.html"));
    }
}
