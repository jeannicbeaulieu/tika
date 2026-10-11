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
package org.apache.tika.sax;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.*;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BasicContentHandlerFactory.HANDLER_TYPE;

public class BasicContentHandlerFactory_equals_18_1_Test {

    @Test
    public void testEquals() throws Exception {
        BasicContentHandlerFactory factory1 = new BasicContentHandlerFactory();
        BasicContentHandlerFactory factory2 = new BasicContentHandlerFactory();
        BasicContentHandlerFactory factory3 = new BasicContentHandlerFactory(HANDLER_TYPE.MARKDOWN, 100);
        BasicContentHandlerFactory factory4 = new BasicContentHandlerFactory(HANDLER_TYPE.MARKDOWN, 100, true, new ParseContext());
        BasicContentHandlerFactory factory5 = new BasicContentHandlerFactory(HANDLER_TYPE.HTML, 200, false, new ParseContext());
        assertTrue(factory1.equals(factory1));
        assertTrue(factory1.equals(factory2));
        assertFalse(factory1.equals(factory3));
        assertFalse(factory1.equals(factory4));
        assertFalse(factory1.equals(factory5));
        // factory3 and factory4 share the same 4 fields compared by equals()
        // (type=MARKDOWN, writeLimit=100, throwOnWriteLimitReached=true, validateXHTML=false)
        // equals() does not compare parseContext, so they are equal
        assertTrue(factory3.equals(factory4));
        assertFalse(factory3.equals(factory5));
        assertFalse(factory4.equals(factory5));
        assertFalse(factory1.equals(null));
        assertFalse(factory1.equals(new Object()));
        // test private fields: equal factories (factory1 == factory2) must share the same field values
        Field typeField = BasicContentHandlerFactory.class.getDeclaredField("type");
        typeField.setAccessible(true);
        assertEquals(typeField.get(factory1), typeField.get(factory2));
        assertFalse(factory1.equals(typeField.get(factory3)));
        assertFalse(factory1.equals(typeField.get(factory4)));
        assertFalse(factory1.equals(typeField.get(factory5)));
        Field writeLimitField = BasicContentHandlerFactory.class.getDeclaredField("writeLimit");
        writeLimitField.setAccessible(true);
        assertEquals(writeLimitField.get(factory1), writeLimitField.get(factory2));
        assertFalse(factory1.equals(writeLimitField.get(factory3)));
        assertFalse(factory1.equals(writeLimitField.get(factory4)));
        assertFalse(factory1.equals(writeLimitField.get(factory5)));
        Field throwOnWriteLimitReachedField = BasicContentHandlerFactory.class.getDeclaredField("throwOnWriteLimitReached");
        throwOnWriteLimitReachedField.setAccessible(true);
        assertEquals(throwOnWriteLimitReachedField.get(factory1), throwOnWriteLimitReachedField.get(factory2));
        assertFalse(factory1.equals(throwOnWriteLimitReachedField.get(factory4)));
        assertFalse(factory1.equals(throwOnWriteLimitReachedField.get(factory5)));
        Field validateXHTMLField = BasicContentHandlerFactory.class.getDeclaredField("validateXHTML");
        validateXHTMLField.setAccessible(true);
        assertEquals(validateXHTMLField.get(factory1), validateXHTMLField.get(factory2));
        assertFalse(factory1.equals(validateXHTMLField.get(factory4)));
        assertFalse(factory1.equals(validateXHTMLField.get(factory5)));
        // test private methods: equal factories must produce the same type of format handler
        Method getFormatHandlerMethod = BasicContentHandlerFactory.class.getDeclaredMethod("getFormatHandler");
        getFormatHandlerMethod.setAccessible(true);
        assertEquals(getFormatHandlerMethod.invoke(factory1).getClass(), getFormatHandlerMethod.invoke(factory2).getClass());
        assertFalse(factory1.equals(getFormatHandlerMethod.invoke(factory3)));
        assertFalse(factory1.equals(getFormatHandlerMethod.invoke(factory4)));
        assertFalse(factory1.equals(getFormatHandlerMethod.invoke(factory5)));
    }
}
