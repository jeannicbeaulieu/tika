package org.apache.tika.sax;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.tika.sax.BasicContentHandlerFactory;
import org.apache.tika.sax.BasicContentHandlerFactory.HANDLER_TYPE;
import org.apache.tika.parser.ParseContext;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.tika.annotation.TikaComponent;
import org.apache.tika.config.OutputLimits;

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
