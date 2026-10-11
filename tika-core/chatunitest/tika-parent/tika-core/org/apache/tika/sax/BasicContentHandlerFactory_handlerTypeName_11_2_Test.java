package org.apache.tika.sax;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BasicContentHandlerFactory;
import org.apache.tika.sax.BasicContentHandlerFactory.HANDLER_TYPE;
import java.util.HashMap;
import java.util.Map;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
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
import org.apache.tika.config.OutputLimits;

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
