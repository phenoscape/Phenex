package org.phenoscape.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.nexml.schema_2009.Annotated;
import org.nexml.schema_2009.Taxon;
import org.phenoscape.io.NeXMLUtil.Annotatable;
import org.phenoscape.io.NeXMLUtil.LiteralContents;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NeXMLUtilTest {
    
    @Test
    public void setAndGetHrefMetadata() throws URISyntaxException {
        final Annotated annotated = Taxon.Factory.newInstance();
        final Annotatable annotatable = new Annotatable(annotated);
        final QName predicate = new QName("http://example.org/", "testpredicate", "ex");
        final URI value1 = new URI("http://phenex.org/value1");
        NeXMLUtil.setMetadata(annotatable, predicate, value1);
        final Object retrievedValue1 = NeXMLUtil.getFirstMetadataValue(annotated, predicate);
        final URI retrievedURI1 = new URI((String)retrievedValue1);
        Assert.assertNotNull("Value should be a URI", retrievedURI1);
        Assert.assertEquals("Retrieved URI should be same as given", value1, retrievedURI1);
        //try changing value after it's been set
        final URI value2 = new URI("http://phenex.org/value2");
        Assert.assertFalse("Second value should not equal first", value2.equals(value1));
        NeXMLUtil.setMetadata(annotatable, predicate, value2);
        final Object retrievedValue2 = NeXMLUtil.getFirstMetadataValue(annotated, predicate);
        final URI retrievedURI2 = new URI((String)retrievedValue2);
        Assert.assertNotNull("Value should be a URI", retrievedURI2);
        Assert.assertEquals("Retrieved URI should be same as given", value2, retrievedURI2);
    }
    
    @Test
    public void setAndGetXMLLiteralMetadata() throws SAXException, IOException, ParserConfigurationException {
        final Annotated annotated = Taxon.Factory.newInstance();
        final Annotatable annotatable = new Annotatable(annotated);
        final QName predicate = new QName("http://example.org/", "testpredicate", "ex");
        final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Reader reader1 = new StringReader("<outer><inner1></inner1></outer>");
        final Document doc1 = docBuilder.parse(new InputSource(reader1));
        final Node value1 = doc1.getDocumentElement();
        NeXMLUtil.setMetadata(annotatable, predicate, value1);
        final LiteralContents retrievedNode1 = (LiteralContents)(NeXMLUtil.getFirstMetadataValue(annotated, predicate));
        Assert.assertNotNull("Value should be an XML node", retrievedNode1.getElement());
        Assert.assertEquals("Retrieved XML should be same as given", value1.getChildNodes().item(0).getNodeName(), retrievedNode1.getElement().getChildNodes().item(0).getChildNodes().item(0).getNodeName());
        //try changing value after it's been set
        final Reader reader2 = new StringReader("<outer><inner2></inner2></outer>");
        final Document doc2 = docBuilder.parse(new InputSource(reader2));
        final Node value2 = doc2.getDocumentElement();
        NeXMLUtil.setMetadata(annotatable, predicate, value2);
        final LiteralContents retrievedNode2 = (LiteralContents)(NeXMLUtil.getFirstMetadataValue(annotated, predicate));
        Assert.assertNotNull("Value should be an XML node", retrievedNode2.getElement());
        Assert.assertEquals("Retrieved XML should be same as given", value2.getChildNodes().item(0).getNodeName(), retrievedNode2.getElement().getChildNodes().item(0).getChildNodes().item(0).getNodeName());
    }

}
