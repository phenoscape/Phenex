package org.phenoscape.io;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.nexml.schema_2009.AbstractBlock;
import org.nexml.schema_2009.AbstractStates;
import org.nexml.schema_2009.Annotated;
import org.nexml.schema_2009.LiteralMeta;
import org.nexml.schema_2009.Meta;
import org.nexml.schema_2009.NexmlDocument;
import org.nexml.schema_2009.ResourceMeta;
import org.nexml.schema_2009.StandardCells;
import org.nexml.schema_2009.StandardFormat;
import org.nexml.schema_2009.Taxa;
import org.obo.datamodel.OBOObject;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NeXMLUtil {

    public static final String PHENOSCAPE_NAMESPACE = "http://vocab.phenoscape.org/";
    public static final String PHENOSCAPE_PREFIX = "ps";
    public static final QName MATRIX_NAME_PREDICATE = new QName(PHENOSCAPE_NAMESPACE, "hasMatrixName", PHENOSCAPE_PREFIX);
    public static final QName FIGURE_PREDICATE = new QName(PHENOSCAPE_NAMESPACE, "inFigure", PHENOSCAPE_PREFIX);
    public static final QName PHENOTYPE_PREDICATE = new QName(PHENOSCAPE_NAMESPACE, "describesPhenotype", PHENOSCAPE_PREFIX);
    
    public static final String PHENOXML_NAMESPACE = "http://www.bioontologies.org/obd/schema/pheno";
    public static final String PHENOXML_PREFIX = "phen";

    public static final String DUBLIN_CORE_NAMESPACE = "http://purl.org/dc/terms/";
    public static final String DUBLIN_CORE_PREFIX = "dc";    
    public static final QName CURATORS_PREDICATE = new QName(DUBLIN_CORE_NAMESPACE, "creator", DUBLIN_CORE_PREFIX);
    public static final QName PUBLICATION_PREDICATE = new QName(DUBLIN_CORE_NAMESPACE, "references", DUBLIN_CORE_PREFIX);
    public static final QName PUBLICATION_NOTES_PREDICATE = new QName(DUBLIN_CORE_NAMESPACE, "description", DUBLIN_CORE_PREFIX);

    public static final String DARWIN_CORE_NAMESPACE = "http://rs.tdwg.org/dwc/terms/";
    public static final String DARWIN_CORE_PREFIX = "dwc";
    public static final QName VALID_NAME_PREDICATE = new QName(DARWIN_CORE_NAMESPACE, "taxonID", DARWIN_CORE_PREFIX);
    public static final QName SPECIMEN_PREDICATE = new QName(DARWIN_CORE_NAMESPACE, "individualID", DARWIN_CORE_PREFIX);
    public static final QName COLLECTION_PREDICATE = new QName(DARWIN_CORE_NAMESPACE, "collectionID", DARWIN_CORE_PREFIX);
    public static final QName ACCESSION_PREDICATE = new QName(DARWIN_CORE_NAMESPACE, "catalogNumber", DARWIN_CORE_PREFIX);

    public static final String RDFS_NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String RDFS_PREFIX = "rdfs";
    public static final QName COMMENT_PREDICATE = new QName(RDFS_NAMESPACE, "comment", RDFS_PREFIX);
    
    public static final String OBO_NAMESPACE = "http://purl.obolibrary.org/obo/";

    public static Taxa findOrCreateTaxa(NexmlDocument doc, String id) {
        for (Taxa taxaBlock : doc.getNexml().getOtusArray()) {
            if (taxaBlock.getId().equals(id)) return taxaBlock;
        }
        // no taxa block was found, so create one for that id
        final Taxa newTaxa = doc.getNexml().insertNewOtus(0);
        newTaxa.setId(id);
        return newTaxa;
    }

    public static AbstractBlock findOrCreateCharactersBlock(NexmlDocument doc, String id) {
        for (AbstractBlock block : doc.getNexml().getCharactersArray()) {
            if (block.getId().equals(id)) return block;
        }
        // no characters block was found, so create one for that id
        final AbstractBlock newBlock = StandardCells.Factory.newInstance();
        newBlock.addNewFormat();
        newBlock.setId(id);
        final AbstractBlock[] currentBlocksArray = doc.getNexml().getCharactersArray();
        final List<AbstractBlock> currentBlocks = new ArrayList<AbstractBlock>(Arrays.asList(currentBlocksArray));
        currentBlocks.add(newBlock);
        doc.getNexml().setCharactersArray(currentBlocks.toArray(currentBlocksArray));
        // need to find it again, as a copy is made and we'll be making more edits
        return findOrCreateCharactersBlock(doc, id);
    }

    public static AbstractStates findOrCreateStates(StandardFormat format, String id) {
        for (AbstractStates abstractStates : format.getStatesArray()) {
            if (abstractStates.getId().equals(id)) return abstractStates;
        }
        // no states block was found, so create one for that id
        final AbstractStates newStates = format.addNewStates();
        newStates.setId(id);
        return newStates;
    }

    public static Element getFirstChildWithTagName(Element parent, String tagName) {
        final NodeList elements = parent.getElementsByTagName(tagName);
        return (elements.getLength() > 0) ? (Element)(elements.item(0)) : null;
    }

    public static Element getFirstChildWithTagNameNS(Element parent, String namespaceURI, String localName) {
        final NodeList elements = parent.getElementsByTagNameNS(namespaceURI, localName);
        return (elements.getLength() > 0) ? (Element)(elements.item(0)) : null;
    }
    
    public static Element getLastChildWithTagNameNS(Element parent, String namespaceURI, String localName) {
        final NodeList elements = parent.getElementsByTagNameNS(namespaceURI, localName);
        return (elements.getLength() > 0) ? (Element)(elements.item(elements.getLength() - 1)) : null;
    }

    public static List<Meta> findMetadata(Annotatable node, QName predicate) {
        final List<Meta> metas = new ArrayList<Meta>();
        for (Meta meta : node.getMetaArray()) {
            if (meta instanceof ResourceMeta) {
                if (((ResourceMeta)meta).getRel().equals(predicate)) {
                    metas.add(meta);
                }
            } else if (meta instanceof LiteralMeta) {
                if (((LiteralMeta)meta).getProperty().equals(predicate)) {
                    metas.add(meta);
                }
            }
        }
        return metas;
    }

    public static Meta findFirstMetadata(Annotatable node, QName predicate) {
        final List<Meta> metas = findMetadata(node, predicate);
        if (metas.isEmpty()) {
            return null;
        } else {
            return metas.get(0);
        }
    }
    
    public static void addMetadata(Annotatable node, QName predicate, String value) {
        setAbout(node);
        final Meta tempMeta = node.addNewMeta();
        final LiteralMeta literalMeta = (LiteralMeta)(tempMeta.changeType(LiteralMeta.type));
        literalMeta.setProperty(predicate);
        setTextContent((Element)(literalMeta.getDomNode()), value);
    }
  
    public static void setMetadata(Annotatable node, QName predicate, String value) {
        setAbout(node);
        if (value == null) {
            unsetMetadata(node, predicate);
        }
        final Meta meta = findFirstMetadata(node, predicate);
        final LiteralMeta literalMeta;
        if (meta != null) {
            if  (meta instanceof LiteralMeta) {
                literalMeta = (LiteralMeta)meta;
            } else {
                removeMeta(node, meta);
                final Meta tempMeta = node.addNewMeta();
                literalMeta = (LiteralMeta)(tempMeta.changeType(LiteralMeta.type));
            }
        } else {
            final Meta tempMeta = node.addNewMeta();
            literalMeta = (LiteralMeta)(tempMeta.changeType(LiteralMeta.type));
        }
        literalMeta.setProperty(predicate);
        if (literalMeta.isSetContent()) {
            literalMeta.unsetContent();    
        }
        //literalMeta.setDatatype(prefixedName(XmlString.type.getName())); //TODO this is just a guess for how to do this properly - don't really need to do it
        setTextContent((Element)(literalMeta.getDomNode()), value);
    }
    
    public static void addMetadata(Annotatable node, QName predicate, Node value) {
        setAbout(node);
        final Meta tempMeta = node.addNewMeta();
        final LiteralMeta literalMeta = (LiteralMeta)(tempMeta.changeType(LiteralMeta.type));
        literalMeta.setProperty(predicate);
        final Node importedNode = literalMeta.getDomNode().getOwnerDocument().importNode(value, true);
        literalMeta.getDomNode().appendChild(importedNode);
    }

    public static void setMetadata(Annotatable node, QName predicate, Node value) {
        setAbout(node);
        if (value == null) {
            unsetMetadata(node, predicate);
        }
        final Meta meta = findFirstMetadata(node, predicate);
        final LiteralMeta literalMeta;
        if (meta != null) {
            if  (meta instanceof LiteralMeta) {
                literalMeta = (LiteralMeta)meta;
            } else {
                removeMeta(node, meta);
                final Meta tempMeta = node.addNewMeta();
                literalMeta = (LiteralMeta)(tempMeta.changeType(LiteralMeta.type));
            }
        } else {
            final Meta tempMeta = node.addNewMeta();
            literalMeta = (LiteralMeta)(tempMeta.changeType(LiteralMeta.type));
        }
        literalMeta.setProperty(predicate);
        if (literalMeta.isSetContent()) {
            literalMeta.unsetContent();    
        }
        final Node importedNode = literalMeta.getDomNode().getOwnerDocument().importNode(value, true);
        clearChildren((Element)(literalMeta.getDomNode()));
        literalMeta.getDomNode().appendChild(importedNode);
    }
    
    public static void addMetadata(Annotatable node, QName predicate, URI value) {
        setAbout(node);
        final Meta tempMeta = node.addNewMeta();
        final ResourceMeta resourceMeta = (ResourceMeta)(tempMeta.changeType(ResourceMeta.type));
        resourceMeta.setRel(predicate);
        resourceMeta.setHref(value.toString());
    }

    public static void setMetadata(Annotatable node, QName predicate, URI value) {
        setAbout(node);
        if (value == null) {
            unsetMetadata(node, predicate);
        }
        final Meta meta = findFirstMetadata(node, predicate);
        final ResourceMeta resourceMeta;
        if (meta != null) {
            if (meta instanceof ResourceMeta) {
                resourceMeta = (ResourceMeta)meta;
            } else {
                removeMeta(node, meta);
                final Meta tempMeta = node.addNewMeta();
                resourceMeta = (ResourceMeta)(tempMeta.changeType(ResourceMeta.type));
            }
        } else {
            final Meta tempMeta = node.addNewMeta();
            resourceMeta = (ResourceMeta)(tempMeta.changeType(ResourceMeta.type));
        }
        resourceMeta.setRel(predicate);
        resourceMeta.setMetaArray(null);
        resourceMeta.setHref(value.toString());
    }
    
    @SuppressWarnings("unchecked")
    public static void addMetadata(Annotatable node, QName predicate, Map<QName, Object> blankNodeValue) {
        setAbout(node);
        final Meta tempMeta = node.addNewMeta();
        final ResourceMeta resourceMeta = (ResourceMeta)(tempMeta.changeType(ResourceMeta.type));
        resourceMeta.setRel(predicate);
        resourceMeta.setMetaArray(new Meta[] {});
        final Annotatable annotatableMeta = new Annotatable(resourceMeta);
        for (Entry<QName, Object> item : blankNodeValue.entrySet()) {
            if (item.getValue() instanceof Map<?, ?>) {
                setMetadata(annotatableMeta, item.getKey(), (Map<QName, Object>)(item.getValue()));
            } else if (item.getValue() instanceof Node) {
                setMetadata(annotatableMeta, item.getKey(), (Node)(item.getValue()));
            } else if (item.getValue() instanceof URI) {
                setMetadata(annotatableMeta, item.getKey(), (URI)(item.getValue()));
            } else {
                setMetadata(annotatableMeta, item.getKey(), item.getValue().toString());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void setMetadata(Annotatable node, QName predicate, Map<QName, Object> blankNodeValue) {
        setAbout(node);
        if (blankNodeValue == null) {
            unsetMetadata(node, predicate);
        }
        final Meta meta = findFirstMetadata(node, predicate);
        final ResourceMeta resourceMeta;
        if (meta != null) {
            if (meta instanceof ResourceMeta) {
                resourceMeta = (ResourceMeta)meta;
            } else {
                removeMeta(node, meta);
                final Meta tempMeta = node.addNewMeta();
                resourceMeta = (ResourceMeta)(tempMeta.changeType(ResourceMeta.type));
            }
        } else {
            final Meta tempMeta = node.addNewMeta();
            resourceMeta = (ResourceMeta)(tempMeta.changeType(ResourceMeta.type));
        }
        resourceMeta.setRel(predicate);
        if (resourceMeta.isSetHref()) {
            resourceMeta.unsetHref();    
        }
        resourceMeta.setMetaArray(new Meta[] {});
        final Annotatable annotatableMeta = new Annotatable(resourceMeta);
        for (Entry<QName, Object> item : blankNodeValue.entrySet()) {
            if (item.getValue() instanceof Map<?, ?>) {
                setMetadata(annotatableMeta, item.getKey(), (Map<QName, Object>)(item.getValue()));
            } else if (item.getValue() instanceof Node) {
                setMetadata(annotatableMeta, item.getKey(), (Node)(item.getValue()));
            } else if (item.getValue() instanceof URI) {
                setMetadata(annotatableMeta, item.getKey(), (URI)(item.getValue()));
            } else {
                setMetadata(annotatableMeta, item.getKey(), item.getValue().toString());
            }
        }
    }
    
    private static void setAbout(Annotatable node) {
        if (node.getNode() instanceof Annotated) {
            final Annotated annotated = (Annotated)(node.getNode());
            if (annotated.isSetId()) {
                annotated.setAbout("#" + annotated.getId());
            }
        }
       
    }
    
    public static void unsetMetadata(Annotatable node, QName predicate) {
        final List<Meta> metas = findMetadata(node, predicate);
        for (Meta meta : metas) {
            removeMeta(node, meta);
        }
    }

    public static void removeMeta(Annotatable node, Meta meta) {
        final List<Meta> metas = new ArrayList<Meta>(Arrays.asList(node.getMetaArray()));
        node.removeMeta(metas.indexOf(meta));
    }

    /**
     * Objects in list could be String, Map<QName, List<Object>>, or NodeList
     */
    public static List<Object> getMetadataValues(Annotated node, QName predicate) {
        final List<Object> values = new ArrayList<Object>();
        final Annotatable annotable = new Annotatable(node);
        final List<Meta> metas = findMetadata(annotable, predicate);
        for (Meta meta : metas) {
            if (meta instanceof ResourceMeta) {
                final ResourceMeta resource = (ResourceMeta)meta;
                if (resource.getRel().equals(predicate)) {
                    if (resource.isSetHref()) {
                        values.add(resource.getHref());
                    }
                    if (resource.getMetaArray().length > 0) {
                        values.add(translate(Arrays.asList(resource.getMetaArray())));
                    }
                }
            } else if (meta instanceof LiteralMeta) {
                final LiteralMeta literal = (LiteralMeta)meta;
                if (literal.getProperty().equals(predicate)) {
                    if (literal.isSetContent()) {
                        values.add(literal.getContent());
                    }
                    final Element element = (Element)(literal.getDomNode()); 
                    if (element.hasChildNodes()) {
                        values.add(new LiteralContents(element));
                    }
                }
            }
        }
        return values;
    }

    /**
     * Value could be String, Map<QName, List<Object>>, or NodeList
     */
    public static Object getFirstMetadataValue(Annotated node, QName predicate) {
        final List<Object> values = getMetadataValues(node, predicate);
        if (values.isEmpty()) {
            return null;
        } else {
            return values.get(0);
        }
    }
    
    /**
     * Objects in list could be String, Map<QName, List<Object>>, or LiteralContents
     */
    public static Map<QName, List<Object>> translate(List<Meta> metas) {
        final Map<QName, List<Object>> map = new HashMap<QName, List<Object>>();
        for (Meta meta : metas) {
            final QName predicate = getPredicate(meta);
            if (!map.containsKey(predicate)) {
                map.put(predicate, new ArrayList<Object>());
            }
            if (meta instanceof ResourceMeta) {
                final ResourceMeta resource = (ResourceMeta)meta;
                if (resource.isSetHref()) {
                    map.get(predicate).add(resource.getHref());
                }
                if (resource.getMetaArray().length > 0) {
                    map.get(predicate).add(translate(Arrays.asList(resource.getMetaArray())));
                }
            } else if (meta instanceof LiteralMeta) {
                final LiteralMeta literal = (LiteralMeta)meta;
                if (literal.isSetContent()) {
                    map.get(predicate).add(literal.getContent());
                }
                final Element element = (Element)(literal.getDomNode()); 
                if (element.hasChildNodes()) {
                    map.get(predicate).add(new LiteralContents(element));
                }
            }
        }
        return map;
    }
    
    public static QName getPredicate(Meta meta) {
        if (meta instanceof ResourceMeta) {
            final ResourceMeta resource = (ResourceMeta)meta;
            return resource.getRel();
        } else if (meta instanceof LiteralMeta) {
            final LiteralMeta literal = (LiteralMeta)meta;
            return literal.getProperty();
        }
        return null;
    }

    /**
     * This method is useful when DOM Level 3 "getTextContent" is not implemented
     */
    public static String getTextContent(Node node) {
        // 
        if (node.getNodeType() == Node.TEXT_NODE) { return ((CharacterData)node).getData(); }
        final StringBuffer pieces = new StringBuffer();
        final NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                pieces.append(((CharacterData)child).getData());
            } else {
                pieces.append(getTextContent(child));
            }
        }
        return pieces.toString();
    }

    /**
     * This method is useful when DOM Level 3 "setTextContent" is not implemented
     */
    public static void setTextContent(Element node, String text) {
        final NodeList children = node.getChildNodes();
        for (int i = (children.getLength() - 1); i > -1; i--) {
            node.removeChild(children.item(i));
        }
        node.appendChild(node.getOwnerDocument().createTextNode(text));
    }

    public static void clearChildren(Element node) {
        final NodeList children = node.getChildNodes();
        for (int i = (children.getLength() - 1); i > -1; i--) {
            final Node child = children.item(i);
            node.removeChild(child);
        }
    }
    
    public static URI oboURI(OBOObject term) {
        final String idFragment = term.getID().replaceFirst(":", "_");
        try {
            return new URI(OBO_NAMESPACE + idFragment);
        } catch (URISyntaxException e) {
            log().error("Term ID resulted in invalid URI", e);
        }
        return null;
    }
    
    public static String oboID(URI uri) throws OBOURISyntaxException {
        final String uriString = uri.toString();
        if (uriString.startsWith(OBO_NAMESPACE)) {
            final String idFragment = uriString.replaceFirst(Pattern.quote(OBO_NAMESPACE), "");
            return idFragment.replaceFirst("_", ":");
        } else {
            throw new OBOURISyntaxException("URI does not begin with OBO namespace");
        }
    }
    
    public static <T> T first(List<T> list) {
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }
    
    public static class OBOURISyntaxException extends Exception {

        public OBOURISyntaxException() {
            super();
        }

        public OBOURISyntaxException(String message, Throwable cause) {
            super(message, cause);
        }

        public OBOURISyntaxException(String message) {
            super(message);
        }

        public OBOURISyntaxException(Throwable cause) {
            super(cause);
        }
        
    }

    public static class Annotatable {

        private final Object node;

        public Annotatable(Annotated node) {
            this.node = node;
        }

        public Annotatable(ResourceMeta node) {
            this.node = node;
        }

        public Object getNode() {
            return this.node;
        }

        public Meta addNewMeta() {
            if (this.node instanceof Annotated) {
                return ((Annotated)this.node).addNewMeta();
            } else {
                return ((ResourceMeta)this.node).addNewMeta();
            }
        }

        public Meta[] getMetaArray() {
            if (this.node instanceof Annotated) {
                return ((Annotated)this.node).getMetaArray();
            } else {
                return ((ResourceMeta)this.node).getMetaArray();
            }
        }

        public Meta getMetaArray(int index) {
            if (this.node instanceof Annotated) {
                return ((Annotated)this.node).getMetaArray(index);
            } else {
                return ((ResourceMeta)this.node).getMetaArray(index);
            }
        }

        public Meta insertNewMeta(int index) {
            if (this.node instanceof Annotated) {
                return ((Annotated)this.node).insertNewMeta(index);
            } else {
                return ((ResourceMeta)this.node).insertNewMeta(index);
            }
        }

        public void removeMeta(int index) {
            if (this.node instanceof Annotated) {
                ((Annotated)this.node).removeMeta(index);
            } else {
                ((ResourceMeta)this.node).removeMeta(index);
            }
        }

        public void setMetaArray(int index, Meta meta) {
            if (this.node instanceof Annotated) {
                ((Annotated)this.node).setMetaArray(index, meta);
            } else {
                ((ResourceMeta)this.node).setMetaArray(index, meta);
            }
        }

        public void setMetaArray(Meta[] metas) {
            if (this.node instanceof Annotated) {
                ((Annotated)this.node).setMetaArray(metas);
            } else {
                ((ResourceMeta)this.node).setMetaArray(metas);
            }
        }

        public int sizeOfMetaArray() {
            if (this.node instanceof Annotated) {
                return ((Annotated)this.node).sizeOfMetaArray();
            } else {
                return ((ResourceMeta)this.node).sizeOfMetaArray();
            }
        }

    }
    
    /**
     * TODO: add other datatypes as needed
     */
    public static class LiteralContents {
        
        private final Element element;
        
        public LiteralContents(Element node) {
            this.element = node;
        }
        
        public String asString() {
            return getTextContent(element);
        }
        
        public Element getElement() {
            return this.element;
        }
        
        public NodeList asXMLNodes() {
            return this.element.getChildNodes();
        }
        
        @Override
        public String toString() {
            return this.asString();
        }
        
    }

    private static Logger log() {
        return Logger.getLogger(NeXMLUtil.class);
    }

}
