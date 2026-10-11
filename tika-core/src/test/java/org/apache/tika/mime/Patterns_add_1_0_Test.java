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
import java.util.Map;
import java.util.SortedMap;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

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
        // addName does not overwrite when the two types are unrelated (neither is a specialization of the other)
        assertEquals(type1, names.get("text"));
    }

    @Test
    public void testAddExtension() throws MimeTypeException, NoSuchFieldException, IllegalAccessException {
        MimeType type1 = new MimeType(MediaType.parse("text/plain"));
        // "*.txt" routes to addExtension; the stored key is ".txt" (everything after the leading *)
        patterns.add("*.txt", false, type1);
        Field extensionsField = Patterns.class.getDeclaredField("extensions");
        extensionsField.setAccessible(true);
        Map<String, MimeType> extensions = (Map<String, MimeType>) extensionsField.get(patterns);
        assertEquals(type1, extensions.get(".txt"));
        assertNull(extensions.get(".html"));
        MimeType type2 = new MimeType(MediaType.parse("text/html"));
        patterns.add("*.html", false, type2);
        assertEquals(type2, extensions.get(".html"));
    }

    @Test
    public void testAddGlob() throws MimeTypeException, NoSuchFieldException, IllegalAccessException {
        MimeType type1 = new MimeType(MediaType.parse("text/plain"));
        // isJavaRegex=true bypasses routing logic and stores the pattern as-is in globs
        patterns.add("*.txt", true, type1);
        Field globsField = Patterns.class.getDeclaredField("globs");
        globsField.setAccessible(true);
        SortedMap<String, MimeType> globs = (SortedMap<String, MimeType>) globsField.get(patterns);
        assertEquals(type1, globs.get("*.txt"));
        assertNull(globs.get("*.html"));
        MimeType type2 = new MimeType(MediaType.parse("text/html"));
        patterns.add("*.html", true, type2);
        assertEquals(type2, globs.get("*.html"));
    }

}
