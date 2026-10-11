/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.mime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.jupiter.api.*;
import org.mockito.*;

class Patterns_matches_5_2_Test {

    @Mock
    private MediaTypeRegistry registry;

    @Mock
    private MimeType mimeType1;

    @Mock
    private MimeType mimeType2;

    @Mock
    private MimeType mimeType3;

    private Patterns patterns;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        Map<String, MimeType> names = new HashMap<>();
        names.put("name1", mimeType1);
        names.put("name2", mimeType2);
        Map<String, MimeType> extensions = new HashMap<>();
        extensions.put("ext1", mimeType1);
        extensions.put("ext2", mimeType2);
        SortedMap<String, MimeType> globs = new TreeMap<>();
        globs.put("glob1", mimeType1);
        globs.put("glob2", mimeType2);
        patterns = new Patterns(registry);
        Field namesField = Patterns.class.getDeclaredField("names");
        namesField.setAccessible(true);
        namesField.set(patterns, names);
        Field extensionsField = Patterns.class.getDeclaredField("extensions");
        extensionsField.setAccessible(true);
        extensionsField.set(patterns, extensions);
        Field globsField = Patterns.class.getDeclaredField("globs");
        globsField.setAccessible(true);
        globsField.set(patterns, globs);
        Field minExtensionLengthField = Patterns.class.getDeclaredField("minExtensionLength");
        minExtensionLengthField.setAccessible(true);
        minExtensionLengthField.set(patterns, 3);
        Field maxExtensionLengthField = Patterns.class.getDeclaredField("maxExtensionLength");
        maxExtensionLengthField.setAccessible(true);
        maxExtensionLengthField.set(patterns, 5);
    }

    @Test
    void testMatchesNameMatch() {
        when(mimeType1.getName()).thenReturn("name1");
        assertEquals(mimeType1, patterns.matches("name1"));
    }

    @Test
    void testMatchesExtensionMatch() {
        when(mimeType1.getName()).thenReturn("name1");
        assertEquals(mimeType1, patterns.matches("name1.ext1"));
    }

    @Test
    void testMatchesGlobMatch() {
        when(mimeType1.getName()).thenReturn("name1");
        assertEquals(mimeType1, patterns.matches("name1.ext1"));
    }

    @Test
    void testMatchesNoMatch() {
        assertNull(patterns.matches("name3"));
    }
}
