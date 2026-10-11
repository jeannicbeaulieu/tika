package org.apache.tika.sax;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.tika.sax.BasicContentHandlerFactory;
import org.apache.tika.parser.ParseContext;
import java.util.HashMap;
import java.util.Map;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.tika.annotation.TikaComponent;
import org.apache.tika.config.OutputLimits;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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
