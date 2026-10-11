package org.apache.tika.sax;

import org.apache.tika.sax.BasicContentHandlerFactory;
import org.apache.tika.sax.BasicContentHandlerFactory.HANDLER_TYPE;
import org.apache.tika.parser.ParseContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
        assertFalse(factory3.equals(factory4));
        assertFalse(factory3.equals(factory5));
        assertFalse(factory4.equals(factory5));
        assertFalse(factory1.equals(null));
        assertFalse(factory1.equals(new Object()));
        // test private fields
        Field typeField = BasicContentHandlerFactory.class.getDeclaredField("type");
        typeField.setAccessible(true);
        assertTrue(factory1.equals(typeField.get(factory1)));
        assertFalse(factory1.equals(typeField.get(factory3)));
        assertFalse(factory1.equals(typeField.get(factory4)));
        assertFalse(factory1.equals(typeField.get(factory5)));
        Field writeLimitField = BasicContentHandlerFactory.class.getDeclaredField("writeLimit");
        writeLimitField.setAccessible(true);
        assertTrue(factory1.equals(writeLimitField.get(factory1)));
        assertFalse(factory1.equals(writeLimitField.get(factory3)));
        assertFalse(factory1.equals(writeLimitField.get(factory4)));
        assertFalse(factory1.equals(writeLimitField.get(factory5)));
        Field throwOnWriteLimitReachedField = BasicContentHandlerFactory.class.getDeclaredField("throwOnWriteLimitReached");
        throwOnWriteLimitReachedField.setAccessible(true);
        assertTrue(factory1.equals(throwOnWriteLimitReachedField.get(factory1)));
        assertFalse(factory1.equals(throwOnWriteLimitReachedField.get(factory4)));
        assertFalse(factory1.equals(throwOnWriteLimitReachedField.get(factory5)));
        Field validateXHTMLField = BasicContentHandlerFactory.class.getDeclaredField("validateXHTML");
        validateXHTMLField.setAccessible(true);
        assertTrue(factory1.equals(validateXHTMLField.get(factory1)));
        assertFalse(factory1.equals(validateXHTMLField.get(factory4)));
        assertFalse(factory1.equals(validateXHTMLField.get(factory5)));
        // test private methods
        Method getFormatHandlerMethod = BasicContentHandlerFactory.class.getDeclaredMethod("getFormatHandler");
        getFormatHandlerMethod.setAccessible(true);
        assertTrue(factory1.equals(getFormatHandlerMethod.invoke(factory1)));
        assertTrue(factory1.equals(getFormatHandlerMethod.invoke(factory2)));
        assertFalse(factory1.equals(getFormatHandlerMethod.invoke(factory3)));
        assertFalse(factory1.equals(getFormatHandlerMethod.invoke(factory4)));
        assertFalse(factory1.equals(getFormatHandlerMethod.invoke(factory5)));
    }
}
