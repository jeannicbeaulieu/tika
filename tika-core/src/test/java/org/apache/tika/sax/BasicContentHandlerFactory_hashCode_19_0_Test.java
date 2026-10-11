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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.apache.tika.parser.ParseContext;

@ExtendWith(MockitoExtension.class)
public class BasicContentHandlerFactory_hashCode_19_0_Test {

    @InjectMocks
    private BasicContentHandlerFactory basicContentHandlerFactory;

    @Mock
    private ParseContext parseContext;

    @Test
    public void testHashCode() throws Exception {
        basicContentHandlerFactory.setType(BasicContentHandlerFactory.HANDLER_TYPE.MARKDOWN);
        basicContentHandlerFactory.setWriteLimit(10);
        basicContentHandlerFactory.setThrowOnWriteLimitReached(true);
        basicContentHandlerFactory.setValidateXHTML(false);
        int hashCode = basicContentHandlerFactory.hashCode();
        assertEquals(hashCode, basicContentHandlerFactory.hashCode());
    }

    @Test
    public void testConstructor() {
        BasicContentHandlerFactory basicContentHandlerFactory1 = new BasicContentHandlerFactory();
        BasicContentHandlerFactory basicContentHandlerFactory2 = new BasicContentHandlerFactory(BasicContentHandlerFactory.HANDLER_TYPE.MARKDOWN, 10);
        BasicContentHandlerFactory basicContentHandlerFactory3 = new BasicContentHandlerFactory(BasicContentHandlerFactory.HANDLER_TYPE.MARKDOWN, 10, true, parseContext);
        assertNotNull(basicContentHandlerFactory1);
        assertNotNull(basicContentHandlerFactory2);
        assertNotNull(basicContentHandlerFactory3);
    }

    @Test
    public void testSetters() throws Exception {
        basicContentHandlerFactory.setType(BasicContentHandlerFactory.HANDLER_TYPE.MARKDOWN);
        basicContentHandlerFactory.setWriteLimit(10);
        basicContentHandlerFactory.setThrowOnWriteLimitReached(true);
        basicContentHandlerFactory.setValidateXHTML(false);
        assertEquals(BasicContentHandlerFactory.HANDLER_TYPE.MARKDOWN, basicContentHandlerFactory.getType());
        assertEquals(10, basicContentHandlerFactory.getWriteLimit());
        assertEquals(true, basicContentHandlerFactory.isThrowOnWriteLimitReached());
        assertEquals(false, basicContentHandlerFactory.isValidateXHTML());
    }

    @Test
    public void testGetters() throws Exception {
        basicContentHandlerFactory.setType(BasicContentHandlerFactory.HANDLER_TYPE.MARKDOWN);
        basicContentHandlerFactory.setWriteLimit(10);
        basicContentHandlerFactory.setThrowOnWriteLimitReached(true);
        basicContentHandlerFactory.setValidateXHTML(false);
        assertEquals(BasicContentHandlerFactory.HANDLER_TYPE.MARKDOWN, basicContentHandlerFactory.getType());
        assertEquals(10, basicContentHandlerFactory.getWriteLimit());
        assertEquals(true, basicContentHandlerFactory.isThrowOnWriteLimitReached());
        assertEquals(false, basicContentHandlerFactory.isValidateXHTML());
    }
}
