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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PatternsManualTest {

    private Patterns patterns;
    private MimeTypes types;

    @BeforeEach
    public void setUp() throws MimeTypeException {
        types = new MimeTypes();
        patterns = new Patterns(MediaTypeRegistry.getDefaultRegistry());
    }

    @Test
    public void testAddGlobStoredAndFoundByMatches() throws MimeTypeException {
        MimeType typeRegex = types.forName("text/plain");
        MimeType typeGlob  = types.forName("text/html");

        patterns.add(".*\\.log", true, typeRegex);
        assertEquals(typeRegex, patterns.matches("app.log"));
        assertNull(patterns.matches("app.txt"));

        patterns.add("*.t?t", false, typeGlob);
        assertEquals(typeGlob, patterns.matches("file.txt"));
        assertNull(patterns.matches("file.csv"));
    }

    @Test
    public void testExtensionRoutingAndMatches() throws MimeTypeException {
        MimeType type = types.forName("text/plain");

        patterns.add("*.txt", false, type);
        assertEquals(type, patterns.matches("document.txt"));
        assertEquals(type, patterns.matches("README.txt"));
        assertNull(patterns.matches("document.csv"));

        assertTrue(type.getExtensions().contains(".txt"));
    }

    @Test
    public void testAddConditionsDoNotThrowAndPreserveEntry() throws MimeTypeException {
        MimeType typePlain = types.forName("text/plain");
        MimeType typeHtml  = types.forName("text/html");

        patterns.add("Makefile",  false, typePlain);
        patterns.add("*.txt",     false, typePlain);
        patterns.add("*.t?t",     false, typePlain);

        patterns.add("Makefile",  false, typeHtml);
        patterns.add("*.txt",     false, typeHtml);
        patterns.add("*.t?t",     false, typeHtml);

        assertEquals(typePlain, patterns.matches("Makefile"));
        assertEquals(typePlain, patterns.matches("file.txt"));

        assertEquals(typePlain, patterns.matches("file.tat"));
    }

    @Test
    public void testAddSameObjectTwiceDoesNotThrow() throws MimeTypeException {
        MimeType type = types.forName("text/plain");

        patterns.add("Makefile", false, type);
        patterns.add("Makefile", false, type);
        assertEquals(type, patterns.matches("Makefile"));

        patterns.add("*.log", false, type);
        patterns.add("*.log", false, type);
        assertEquals(type, patterns.matches("app.log"));

        patterns.add("*.t?t", false, type);
        patterns.add("*.t?t", false, type);
        assertEquals(type, patterns.matches("file.tat"));
    }

    @Test
    public void testCompileProducesCorrectRegex() throws MimeTypeException {
        MimeType type = types.forName("text/plain");

        patterns.add("*.t?t", false, type);

        assertEquals(type, patterns.matches("file.txt"));
        assertEquals(type, patterns.matches("archive.tAt"));

        assertNull(patterns.matches("file.tt"));
        assertNull(patterns.matches("file.txxt"));

        assertNull(patterns.matches("filextxt"));
    }
}
