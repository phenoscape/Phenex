package org.phenoscape.scigraph;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.util.ProvisionalTermUtil;
import org.xml.sax.SAXException;

public class SciGraphController {

	private final PhenexController controller;
	private static final String SCIGRAPH_ENTITIES_REQUEST_URL = "http://kb.phenoscape.org/scigraph/annotations/entities";


	public SciGraphController(PhenexController controller) {
		this.controller = controller;
	}
	
	public SciGraphResponse runSciGraphRequest(String req) { //boolean isCharacterRequest
		SciGraphResponse returnedRequest = null;
		try {
			returnedRequest = this.sciGraphRequest(req);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Network connection unable to be established.", "Network Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return returnedRequest;
	}
	
	private void fillCharacters() {

	}

	private void fillTaxons() {

	}

	// TODO: pass in list of values and then populate
	private SciGraphResponse sciGraphRequest(String param) throws URISyntaxException, IOException {
		System.out.println("sciGraphRequest()");

		JSONArray responseJSON = sendRequest(param);

		List<String> qualityList = new ArrayList<String>();
		List<String> entityList = new ArrayList<String>();

		for (int i = 0; i < responseJSON.length(); i++) {
			JSONObject jsonObj = (JSONObject) responseJSON.get(i);
			JSONObject tokenObj = jsonObj.getJSONObject("token");

			// TODO: this map length doesn't line up

			String id = tokenObj.getString("id");
			JSONArray terms = tokenObj.getJSONArray("terms");

			String prefix = id.substring(0, id.indexOf(":"));
			if (prefix.equals("UBERON") || prefix.equals("VTO")) { // VTO should
																	// be taxon
																	// only.
																	// should be
																	// seeing
																	// "NCBITaxon"
																	// for taxon
																	// matches
				for (int j = 0; j < terms.length(); j++) { // TODO; Categories
															// and terms have a
															// 1-1 mapping
															// (although
															// categories
															// sometimes is
															// blank?)
					String term = (String) terms.get(j);
					entityList.add(term);
				}
				// TODO: addToEntityList();
			} else if (prefix.equals("PATO") || prefix.equals("BSPO") || prefix.equals("RO")) {
				for (int j = 0; j < terms.length(); j++) { // TODO; Categories
															// and terms have a
															// 1-1 mapping
															// (although
															// categories
															// sometimes is
															// blank?)
					String term = (String) terms.get(j);
					qualityList.add(term);
				}
			} else if (prefix.equals("http")) {
				String taxon = id.substring(id.lastIndexOf("/") + 1, id.length());
				// TODO: addToEntityList();
			} else {
				System.out.println("LILA " + prefix);
			}
		}

		System.out.println(entityList);
		System.out.println(qualityList);
		SciGraphResponse packagedResponse = new SciGraphResponse(entityList, qualityList);

		return packagedResponse;
	}

	private JSONArray sendRequest(String param) throws URISyntaxException, IOException {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("content", param));
		params.add(new BasicNameValuePair("longestOnly", "true"));
		URI uri = new URI(SCIGRAPH_ENTITIES_REQUEST_URL + "?" + URLEncodedUtils.format(params, "utf-8"));
		System.out.println(uri);
		final HttpGet request = new HttpGet(uri);// ProvisionalTermUtil.SERVICE);

		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpResponse response = new DefaultHttpClient().execute(request);
		client.getConnectionManager().shutdown();

		String json = EntityUtils.toString(response.getEntity());
		JSONArray responseJSON = new JSONArray(json);

		return responseJSON;
	}

	@SuppressWarnings("unused")
	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
