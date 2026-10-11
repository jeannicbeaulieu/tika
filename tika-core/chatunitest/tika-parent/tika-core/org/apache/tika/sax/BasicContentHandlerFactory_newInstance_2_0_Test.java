package org.apache.tika.sax;

import org.apache.tika.sax.BasicContentHandlerFactory.HANDLER_TYPE;
import org.apache.tika.config.OutputLimits;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BasicContentHandlerFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.tika.annotation.TikaComponent;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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
