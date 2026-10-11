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
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;

import org.apache.tika.config.OutputLimits;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BasicContentHandlerFactory.HANDLER_TYPE;

class BasicContentHandlerFactory_newInstance_2_0_Test {

    @Test
    void testNewInstance() throws Exception {
        ParseContext parseContext = mock(ParseContext.class);
        OutputLimits outputLimits = mock(OutputLimits.class);
        when(parseContext.get(OutputLimits.class)).thenReturn(outputLimits);
        when(outputLimits.getWriteLimit()).thenReturn(1000);
        BasicContentHandlerFactory factory = BasicContentHandlerFactory.newInstance(HANDLER_TYPE.MARKDOWN, parseContext);
        assertEquals(HANDLER_TYPE.MARKDOWN, factory.getType());
        assertEquals(1000, factory.getWriteLimit());
        assertEquals(parseContext, getParseContext(factory));
    }

    @Test
    void testNewInstanceDefaults() throws Exception {
        ParseContext parseContext = mock(ParseContext.class);
        OutputLimits outputLimits = mock(OutputLimits.class);
        when(parseContext.get(OutputLimits.class)).thenReturn(outputLimits);
        when(outputLimits.getWriteLimit()).thenReturn(-1);
        BasicContentHandlerFactory factory = BasicContentHandlerFactory.newInstance(HANDLER_TYPE.MARKDOWN, parseContext);
        assertEquals(HANDLER_TYPE.MARKDOWN, factory.getType());
        assertEquals(-1, factory.getWriteLimit());
        assertEquals(parseContext, getParseContext(factory));
    }

    private ParseContext getParseContext(BasicContentHandlerFactory factory) throws Exception {
        java.lang.reflect.Field field = BasicContentHandlerFactory.class.getDeclaredField("parseContext");
        field.setAccessible(true);
        return (ParseContext) field.get(factory);
    }
}
