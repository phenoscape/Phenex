package org.phenoscape.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.DanglingClassImpl;
import org.obo.datamodel.impl.OBOClassImpl;
import org.obo.datamodel.impl.OBORestrictionImpl;
import org.xml.sax.SAXException;

public class ProvisionalTermUtil {

	private static final String SERVICE = "http://rest.bioontology.org/bioportal/provisional";
	private static final String APIKEYKEY = "apikey";
	private static final String USERIDKEY = "userid";

	public static String getAPIKey() {
		return getPrefsRoot().get(APIKEYKEY, null);
	}

	public static void setAPIKey(String key) {
		if (StringUtils.isBlank(key)) {
			getPrefsRoot().remove(APIKEYKEY);
		} else {
			getPrefsRoot().put(APIKEYKEY, key);
		}
	}

	public static String getUserID() {
		return getPrefsRoot().get(USERIDKEY, null);
	}

	public static void setUserID(String id) {
		if (StringUtils.isBlank(id)) {
			getPrefsRoot().remove(USERIDKEY);
		} else {
			getPrefsRoot().put(USERIDKEY, id);
		}

	}

	public static List<OBOClass> getProvisionalTerms(OBOSession session) throws IllegalStateException, SAXException, IOException, ParserConfigurationException {
		if (ProvisionalTermUtil.getAPIKey() == null || ProvisionalTermUtil.getUserID() == null) {
			//JOptionPane.showMessageDialog(null, "You need to set your prefs", "Error", JOptionPane.ERROR_MESSAGE);
			return Collections.emptyList();
		} else {
			final List<NameValuePair> values = new ArrayList<NameValuePair>();
			values.add(new BasicNameValuePair("apikey", getAPIKey()));
			values.add(new BasicNameValuePair("submittedby", getUserID()));
			values.add(new BasicNameValuePair("pagesize", "9999"));
			final String paramString = URLEncodedUtils.format(values, "utf-8");
			final HttpGet get = new HttpGet(SERVICE + "?" + paramString);
			final DefaultHttpClient client = new DefaultHttpClient();
			final HttpResponse response = new DefaultHttpClient().execute(get);
			client.getConnectionManager().shutdown();
			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			final Document xmlDoc = new DOMBuilder().build(docBuilder.parse(response.getEntity().getContent()));
			if (xmlDoc.getRootElement().getChild("data") != null) {
				@SuppressWarnings("unchecked")
				final List<Element> termElements = xmlDoc.getRootElement().getChild("data").getChild("page").getChild("contents").getChild("classBeanResultList").getChildren("classBean");
				final List<OBOClass> terms = new ArrayList<OBOClass>();
				for (Element element : termElements) {
					terms.add(createClassForProvisionalTerm(element, session));
				}
				return terms;
			} else {
				return Collections.emptyList();
			}

		}
	}

	public static OBOClass createClassForProvisionalTerm(Element element, OBOSession session) {
		final String termID = element.getChild("id").getValue();
		final String label = element.getChild("label").getValue();
		final String definition = element.getChild("definitions").getValue();
		final String parentURI = findParentURI(element);
		final OBOClass newTerm = new OBOClassImpl(termID);
		newTerm.setName(label);
		newTerm.setDefinition(definition);
		final String permanentID = findPermanentID(element);
		if (permanentID != null) {
			newTerm.setObsolete(true);
			final String replacedByID = toOBOID(permanentID);
			final OBOClass replacedBy = findClassOrCreateDangler(replacedByID, session);
			newTerm.addReplacedBy(replacedBy);
		}
		if ((!newTerm.isObsolete()) && (parentURI != null)) {
			final String parentOBOID = toOBOID(parentURI);
			final OBOClass parent = findClassOrCreateDangler(parentOBOID, session);
			newTerm.addParent(new OBORestrictionImpl(newTerm, parent, (OBOProperty)(session.getObject("OBO_REL:is_a"))));
		}		
		newTerm.setNamespace(new Namespace("bioportal_provisional"));
		return newTerm;
	}

	public static String toURI(String oboID) {
		return "http://purl.obolibrary.org/obo/" + oboID.replaceAll(":", "_");
	}

	public static String toOBOID(String uri) {
		if (uri.contains("http://purl.obolibrary.org/obo/")) {
			final String id = uri.split("http://purl.obolibrary.org/obo/")[1];
			final int underscore = id.lastIndexOf("_");
			return id.substring(0, underscore) + ":" + id.substring(underscore + 1, id.length());
		} else {
			return uri;
		}

	}

	private static OBOClass findClassOrCreateDangler(String oboID, OBOSession session) {
		final OBOClass term;
		if (session.getObject(oboID) != null) {
			term = (OBOClass)(session.getObject(oboID));
		} else {
			term = new DanglingClassImpl(oboID);
			session.addObject(term);
		}
		return term;
	}

	private static String findPermanentID(Element element) {
		for (Object item : element.getChild("relations").getChildren("entry")) {
			final Element entry = (Element)item;
			final String entryKey = entry.getChild("string").getValue();
			if (entryKey.equals("provisionalPermanentId")) {
				if (entry.getChild("null") != null) {
					return null;
				} else {
					return ((Element)(entry.getChildren("string").get(1))).getValue();
				}
			}
		}
		return null;
	}

	private static String findParentURI(Element term) {
		for (Object item : term.getChild("relations").getChildren("entry")) {
			final Element entry = (Element)item;
			final String entryKey = entry.getChild("string").getValue();
			if (entryKey.equals("provisionalSubclassOf")) {
				final Element uriElement = entry.getChild("org.openrdf.model.URI");
				if (uriElement != null) {
					return uriElement.getChild("uriString").getValue();
				} else {
					return null;
				}
			}
		}
		return null;
	}

	private static Preferences getPrefsRoot() {
		return Preferences.userNodeForPackage(ProvisionalTermUtil.class).node("orb");
	}

}
