package org.phenoscape.scigraph;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.phenoscape.controller.PhenexController;

public class SciGraphController {

	private final PhenexController controller;
	private static final String SCIGRAPH_ENTITIES_REQUEST_URL = "http://kb.phenoscape.org/scigraph/annotations/entities";

	public SciGraphController(PhenexController controller) {
		this.controller = controller;
	}

	public SciGraphResponse runSciGraphCharacterRequest(String req) { // boolean
		// TODO: entity map
		JSONArray responseJSON = sendRequest(req);

		List<String> qualityList = new ArrayList<String>();
		List<String> entityList = new ArrayList<String>();
		Set<String> seenID = new HashSet<String>();
		
		System.out.println("runnnnnn " + req);
		System.out.println(responseJSON);
		
		for (int i = 0; i < responseJSON.length(); i++) {
			System.out.println("lila");
			JSONObject jsonObj = (JSONObject) responseJSON.get(i);
			JSONObject tokenObj = jsonObj.getJSONObject("token");

			String id = tokenObj.getString("id");
			System.out.println(id);
			if (seenID.contains(id))
				continue;
			JSONArray terms = tokenObj.getJSONArray("terms");

			/*
			 * Check prefixes to determine if value is an entity or quality
			 * Ignore taxon matches with VTO and NCBITaxon prefixes
			 */
			String prefix = id.substring(0, id.indexOf(":"));
			seenID.add(id);
			if (prefix.equals("VTO")
					|| (prefix.equals("http") && id.substring(0, id.indexOf(":")).equals("NCBITaxon"))) {
				// do nothing
			} else if (prefix.equals("PATO") || prefix.equals("BSPO") || prefix.equals("RO")) {
				for (int j = 0; j < terms.length(); j++) {
					String term = (String) terms.get(j);
					qualityList.add(term);
				}
			} else { // for everything else (mainly UBERON)
				for (int j = 0; j < terms.length(); j++) {
					String term = (String) terms.get(j);
					System.out.println("ADD UBERON!");
					System.out.println(term);
					entityList.add(term);
					System.out.println(entityList);
				}
			}
		}
		SciGraphResponse packagedResponse = new SciGraphResponse(entityList, qualityList);
		return packagedResponse;
	}

	public List<String> runSciGraphTaxonRequest(String req) { // boolean
		JSONArray responseJSON = sendRequest(req);

		List<String> taxonList = new ArrayList<String>();
		Set<String> seenID = new HashSet<String>();

		for (int i = 0; i < responseJSON.length(); i++) {
			JSONObject jsonObj = (JSONObject) responseJSON.get(i);
			JSONObject tokenObj = jsonObj.getJSONObject("token");

			String id = tokenObj.getString("id");
			if (seenID.contains(id))
				continue;
			JSONArray terms = tokenObj.getJSONArray("terms");

			// Only use taxon prefixes VTO and NCBITaxon
			String prefix = id.substring(0, id.indexOf(":"));
			seenID.add(id);
			// Always prefer VTO over NCBITaxon. Otherwise take first result
			// (for simplicity)
			if (prefix.equals("VTO")
					|| (prefix.equals("http") && id.substring(0, id.indexOf(":")).equals("NCBITaxon"))) {
				for (int j = 0; j < terms.length(); j++) {
					String term = (String) terms.get(j);
					taxonList.add(term);
				}
			}
		}
		return taxonList;
	}

	private JSONArray sendRequest(String param) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("content", param));
		params.add(new BasicNameValuePair("longestOnly", "true"));
		URI uri = null;
		try {
			uri = new URI(SCIGRAPH_ENTITIES_REQUEST_URL + "?" + URLEncodedUtils.format(params, "utf-8"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		final HttpGet request = new HttpGet(uri);// ProvisionalTermUtil.SERVICE);
		
		System.out.println("REQUESTTT: " + request.getRequestLine());
		
		final DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		try {
			response = new DefaultHttpClient().execute(request);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Network connection unable to be established.", "Network Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return null;
		}
		client.getConnectionManager().shutdown();
		String json = null;
		try {
			json = EntityUtils.toString(response.getEntity());
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("REPONSEEE: " + request.getRequestLine());
		// TODO: find out why abdominal _____ has no response
		System.out.println(json);

		JSONArray responseJSON = new JSONArray(json);
		System.out.println(responseJSON.toString());

		return responseJSON;
	}

	@SuppressWarnings("unused")
	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
