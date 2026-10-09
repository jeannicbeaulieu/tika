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
package org.apache.tika.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Constructor;
import java.nio.file.Path;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

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
