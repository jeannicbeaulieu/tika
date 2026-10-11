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

import java.lang.reflect.Method;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BasicContentHandlerFactory.HANDLER_TYPE;

@ExtendWith(MockitoExtension.class)
public class BasicContentHandlerFactory_parseHandlerType_3_0_Test {

    @Mock
    private ParseContext parseContext;

    @Test
    public void testParseHandlerType() throws Exception {
        // Given
        BasicContentHandlerFactory factory = new BasicContentHandlerFactory();
        Method method = BasicContentHandlerFactory.class.getDeclaredMethod("parseHandlerType", String.class, HANDLER_TYPE.class);
        method.setAccessible(true);
        // When
        String handlerTypeName = "md";
        HANDLER_TYPE defaultType = HANDLER_TYPE.MARKDOWN;
        HANDLER_TYPE result = (HANDLER_TYPE) method.invoke(factory, handlerTypeName, defaultType);
        // Then
        assertEquals(HANDLER_TYPE.MARKDOWN, result);
    }

    @Test
    public void testParseHandlerTypeInvalidType() throws Exception {
        // Given
        BasicContentHandlerFactory factory = new BasicContentHandlerFactory();
        Method method = BasicContentHandlerFactory.class.getDeclaredMethod("parseHandlerType", String.class, HANDLER_TYPE.class);
        method.setAccessible(true);
        // When
        String handlerTypeName = "invalid";
        HANDLER_TYPE defaultType = HANDLER_TYPE.MARKDOWN;
        HANDLER_TYPE result = (HANDLER_TYPE) method.invoke(factory, handlerTypeName, defaultType);
        // Then
        assertEquals(defaultType, result);
    }

    @Test
    public void testParseHandlerTypeNull() throws Exception {
        // Given
        BasicContentHandlerFactory factory = new BasicContentHandlerFactory();
        Method method = BasicContentHandlerFactory.class.getDeclaredMethod("parseHandlerType", String.class, HANDLER_TYPE.class);
        method.setAccessible(true);
        // When
        String handlerTypeName = null;
        HANDLER_TYPE defaultType = HANDLER_TYPE.MARKDOWN;
        HANDLER_TYPE result = (HANDLER_TYPE) method.invoke(factory, handlerTypeName, defaultType);
        // Then
        assertEquals(defaultType, result);
    }

    @Test
    public void testParseHandlerTypeEmpty() throws Exception {
        // Given
        BasicContentHandlerFactory factory = new BasicContentHandlerFactory();
        Method method = BasicContentHandlerFactory.class.getDeclaredMethod("parseHandlerType", String.class, HANDLER_TYPE.class);
        method.setAccessible(true);
        // When
        String handlerTypeName = "";
        HANDLER_TYPE defaultType = HANDLER_TYPE.MARKDOWN;
        HANDLER_TYPE result = (HANDLER_TYPE) method.invoke(factory, handlerTypeName, defaultType);
        // Then
        assertEquals(defaultType, result);
    }

    @Test
    public void testParseHandlerTypeLowerCase() throws Exception {
        // Given
        BasicContentHandlerFactory factory = new BasicContentHandlerFactory();
        Method method = BasicContentHandlerFactory.class.getDeclaredMethod("parseHandlerType", String.class, HANDLER_TYPE.class);
    }
}
