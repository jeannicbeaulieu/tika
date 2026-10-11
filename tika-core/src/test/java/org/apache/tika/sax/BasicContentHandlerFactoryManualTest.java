package org.apache.tika.sax;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.xml.sax.ContentHandler;

public class BasicContentHandlerFactoryManualTest {

    @Test
    public void testCreateHandlerWithWriteLimitZero() {
        BasicContentHandlerFactory factory = new BasicContentHandlerFactory(
                BasicContentHandlerFactory.HANDLER_TYPE.TEXT, 0);
        ContentHandler handler = factory.createHandler();

        assertTrue(handler instanceof WriteOutContentHandler);
    }

    @Test
    public void testCreateHandlerWithWriteLimitNegativeIsUnlimited() {
        BasicContentHandlerFactory factory = new BasicContentHandlerFactory(
                BasicContentHandlerFactory.HANDLER_TYPE.TEXT, -1);
        ContentHandler handler = factory.createHandler();

        assertFalse(handler instanceof WriteOutContentHandler);
    }
}
