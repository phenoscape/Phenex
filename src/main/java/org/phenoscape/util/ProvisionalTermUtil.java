package org.phenoscape.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.DanglingClassImpl;
import org.obo.datamodel.impl.OBOClassImpl;
import org.obo.datamodel.impl.OBORestrictionImpl;

public class ProvisionalTermUtil {

	public static final String SERVICE = "http://data.bioontology.org/provisional_classes";
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

	public static List<OBOClass> getProvisionalTerms(OBOSession session) throws IOException {
		if (ProvisionalTermUtil.getAPIKey() == null || ProvisionalTermUtil.getUserID() == null) {
			//JOptionPane.showMessageDialog(null, "You need to set your prefs", "Error", JOptionPane.ERROR_MESSAGE);
			return Collections.emptyList();
		} else {
			final List<NameValuePair> values = new ArrayList<NameValuePair>();
			values.add(new BasicNameValuePair("apikey", getAPIKey()));
			//values.add(new BasicNameValuePair("submittedby", getUserID()));
			values.add(new BasicNameValuePair("pagesize", "5000"));
			final String paramString = URLEncodedUtils.format(values, "utf-8");
			final HttpGet get = new HttpGet(SERVICE + "?" + paramString);
			final DefaultHttpClient client = new DefaultHttpClient();
			final HttpResponse response = new DefaultHttpClient().execute(get);
			client.getConnectionManager().shutdown();
			final JSONObject json = new JSONObject(IOUtils.toString(response.getEntity().getContent(), "utf-8"));
			final JSONArray termResults = json.getJSONArray("collection");
			final List<OBOClass> terms = new ArrayList<OBOClass>();
			for (int i = 0; i < termResults.length(); i++) {
				final JSONObject provisionalTerm = termResults.getJSONObject(i);
				// this check should be removed once Bioportal implements server-side filtering by creator
				final String creator = provisionalTerm.getString("creator");
				if (creator.equals(getUserID())) {
					terms.add(createClassForProvisionalTerm(provisionalTerm, session));
				}
			}
			return terms;

		}
	}

	public static OBOClass createClassForProvisionalTerm(JSONObject item, OBOSession session) {
		final String termID = item.getString("@id");
		final String label = item.getString("label");
		final String definition;
		final JSONArray definitions = item.getJSONArray("definition");
		if (definitions.length() > 0) {
			definition = definitions.getString(0);
		} else {
			definition = null;
		}
		final String parentURI;
		if (item.isNull("subclassOf")) {
			parentURI = null;
		} else {
			parentURI = item.getString("subclassOf");
		}
		final OBOClass newTerm = new OBOClassImpl(termID);
		newTerm.setName(label);
		newTerm.setDefinition(definition);
		final String permanentID;
		if (item.isNull("permanentId")) {
			permanentID = null;
		} else {
			permanentID = item.getString("permanentId");
		}
		if (permanentID != null) {
			newTerm.setObsolete(true);
			final String replacedByID = toOBOID(permanentID);
			final OBOClass replacedBy = findClassOrCreateDangler(replacedByID,
					session);
			newTerm.addReplacedBy(replacedBy);
		}
		if ((!newTerm.isObsolete()) && (parentURI != null)) {
			final String parentOBOID = toOBOID(parentURI);
			final OBOClass parent = findClassOrCreateDangler(parentOBOID, session);
			newTerm.addParent(new OBORestrictionImpl(newTerm, parent, (OBOProperty) (session.getObject("OBO_REL:is_a"))));
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
			return id.substring(0, underscore) + ":"
			+ id.substring(underscore + 1, id.length());
		} else {
			return uri;
		}

	}

	private static OBOClass findClassOrCreateDangler(String oboID,
			OBOSession session) {
		final OBOClass term;
		if (session.getObject(oboID) != null) {
			term = (OBOClass) (session.getObject(oboID));
		} else {
			term = new DanglingClassImpl(oboID);
			session.addObject(term);
		}
		return term;
	}

	private static Preferences getPrefsRoot() {
		return Preferences.userNodeForPackage(ProvisionalTermUtil.class).node("orb");
	}

	private static Logger log() {
		return Logger.getLogger(ProvisionalTermUtil.class);
	}

}
