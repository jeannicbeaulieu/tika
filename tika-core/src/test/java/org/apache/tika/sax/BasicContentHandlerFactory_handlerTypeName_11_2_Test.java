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

import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BasicContentHandlerFactory.HANDLER_TYPE;

public class BasicContentHandlerFactory_handlerTypeName_11_2_Test {

    @Test
    public void testHandlerTypeName() throws Exception {
        // Create a new instance of BasicContentHandlerFactory
        BasicContentHandlerFactory factory = new BasicContentHandlerFactory();
        // Set the type to MARKDOWN
        factory.setType(HANDLER_TYPE.MARKDOWN);
        // Get the handler type name and verify that it is "MARKDOWN"
        assertEquals("MARKDOWN", factory.handlerTypeName());
        // Set the type to HTML
        factory.setType(HANDLER_TYPE.HTML);
        // Get the handler type name and verify that it is "HTML"
        assertEquals("HTML", factory.handlerTypeName());
    }

    @Test
    public void testHandlerTypeNameWithParseContext() throws Exception {
        // Create a new instance of BasicContentHandlerFactory
        BasicContentHandlerFactory factory = new BasicContentHandlerFactory();
        // Set the type to MARKDOWN
        factory.setType(HANDLER_TYPE.MARKDOWN);
        // Set a ParseContext
        ParseContext context = new ParseContext();
        factory.setParseContext(context);
        // Get the handler type name and verify that it is "MARKDOWN"
        assertEquals("MARKDOWN", factory.handlerTypeName());
    }
}
