package org.phenoscape.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.nexml.x10.AbstractBlock;
import org.nexml.x10.AbstractStates;
import org.nexml.x10.Annotated;
import org.nexml.x10.Dict;
import org.nexml.x10.NexmlDocument;
import org.nexml.x10.StandardCells;
import org.nexml.x10.StandardFormat;
import org.nexml.x10.Taxa;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NeXMLUtil {
  
  public static String COMMENT_KEY = "phenex_comment";
  
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
  
  
  
  public static Dict findOrCreateMetadataDict(NexmlDocument doc) {
    final Document dom = doc.getNexml().getDomNode().getOwnerDocument();
    final Element any = dom.createElement("any");
    any.appendChild(dom.createElement("curators"));
    any.appendChild(dom.createElement("publication"));
    any.appendChild(dom.createElement("publicationNotes"));
    final Dict newDict = findOrCreateDict(doc.getNexml(), "phenex-metadata", any);
    return newDict;
  }
  
  public static Dict findOrCreateDict(Annotated node, String key, Element defaultValue) {
    for (Dict dict : node.getDictArray()) {
      final String[] keys = dict.getKeyArray();
      if ((keys.length > 0) && (keys[0].equals(key))) {
        return dict;
      }
    }
    // no such dict was found, so create
    final Dict newDict = node.addNewDict();
    newDict.setKeyArray(new String[] {key});
    newDict.getDomNode().appendChild(defaultValue);
    return newDict;
  }
  
  public static void removeDict(Annotated node, Dict dict) {
    final List<Dict> dicts = new ArrayList<Dict>(Arrays.asList(node.getDictArray()));
    dicts.remove(dict);
    node.setDictArray(dicts.toArray(new Dict[] {}));
  }
  
  public static Element getDictValueNode(Dict dict) {
    final NodeList children = dict.getDomNode().getChildNodes();
    int elementsFound = 0;
    for (int i = 0; i < children.getLength(); i++) {
      final Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        elementsFound++;
        if (elementsFound == 2) {
          return (Element)child;
        }
      }
    }
    return null;
  }
  
  public static String getTextContent(Node node) {
    // this method is useful when DOM Level 3 "getTextContent" is not implemented
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
  
  public static void setTextContent(Element node, String text) {
    // this method is useful when DOM Level 3 "setTextContent" is not implemented
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
  
  @SuppressWarnings("unused")
  private static Logger log() {
    return Logger.getLogger(NeXMLUtil.class);
  }
  
}
