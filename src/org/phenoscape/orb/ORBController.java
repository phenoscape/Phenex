package org.phenoscape.orb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.html.HTMLParamElement;
import org.xml.sax.SAXException;

public class ORBController {

	private final PhenexController controller;

	public ORBController(PhenexController controller) {
		this.controller = controller;
	}

	public void runORBTermRequest() {
		if (ProvisionalTermUtil.getAPIKey() == null || ProvisionalTermUtil.getUserID() == null) {
			JOptionPane.showMessageDialog(null, "Please first enter your ORB connection preferences using the ORB Connection Settings panel.", "ORB not configured", JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			final ProvisionalTermRequestPanel panel = new ProvisionalTermRequestPanel(this.controller);
			panel.init();
			panel.setSize(400, 100);
			final int result = JOptionPane.showConfirmDialog(null, panel, "Submit new term request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				final ORBTerm requestedTerm = panel.getTerm();
				try {
					this.requestTerm(requestedTerm);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private OBOClass requestTerm(ORBTerm term) throws ClientProtocolException, IOException, ParserConfigurationException, IllegalStateException, SAXException {
		final OBOSession session = this.controller.getOntologyController().getOBOSession();
		final HttpPost post = new HttpPost(ProvisionalTermUtil.SERVICE);
		post.setHeader(new BasicHeader("Content-Type", "application/json"));
		post.setHeader(new BasicHeader("Authorization", "apikey token=" + ProvisionalTermUtil.getAPIKey()));
		post.setEntity(this.createPostEntity(term));
		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpResponse response = new DefaultHttpClient().execute(post);
		client.getConnectionManager().shutdown();
		final JSONObject json = new JSONObject(IOUtils.toString(response.getEntity().getContent(), "utf-8"));
		final OBOClass newTerm = ProvisionalTermUtil.createClassForProvisionalTerm(json, session);
		session.addObject(newTerm);
		this.controller.getOntologyController().invalidateAllTermSets();
		this.controller.getOntologyCoordinator().getSelectionManager().selectTerm(this, newTerm, false);
		return newTerm;
	}
	
	//TODO: move this to a new class SciGraph Controller

	public SciGraphResponse runSciGraphRequest(String req){
		SciGraphResponse returnedRequest = null;
		try {
			returnedRequest = this.sciGraphRequest(req);
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnedRequest;
	}
	
	
	//TODO: pass in list of values and then populate	
	private SciGraphResponse sciGraphRequest(String param) throws ClientProtocolException, IOException, URISyntaxException{
		final String url = "http://kb.phenoscape.org/scigraph/annotations/entities"; //json return object instead of HTML
		System.out.println("sciGraphRequest()");
		
//		final OBOSession session = this.controller.getOntologyController().getOBOSession();
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair( "content", param ) );
		URI uri = new URI(url + "?" + URLEncodedUtils.format( params, "utf-8" ));
		System.out.println(uri);
		final HttpGet request = new HttpGet(uri);//ProvisionalTermUtil.SERVICE);

		System.out.println("requestLine");
		System.out.println(request.getRequestLine());
		
		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpResponse response = new DefaultHttpClient().execute(request);
		
//		System.out.println(response.getEntity().getContent());
//		System.out.println(response.getParams());
		String json = EntityUtils.toString(response.getEntity());

		System.out.println("testtest");
		
//		Map<String, ArrayList<String>> categoryMap = new HashMap<String, ArrayList<String>>();
//		Map<String, ArrayList<String>> termMap = new HashMap<String, ArrayList<String>>();
		
		//1-1 mapping TODO: make into a map of term to category
//		List<String> categoryList = new ArrayList<String>();
//		List<String> termMap = new ArrayList<String>();
		
//		Map<String, String> termToID = new HashMap<String, String>();
//		Map<String, ArrayList<String>> IDToTerm = new HashMap<String, ArrayList<String>>();
		
		List<String> qualityList = new ArrayList<String>();
		List<String> entityList = new ArrayList<String>();

		JSONArray responseJSON = new JSONArray(json);
		for(int i = 0; i < responseJSON.length(); i++){ 
			JSONObject jsonObj = (JSONObject) responseJSON.get(i);
			JSONObject tokenObj = jsonObj.getJSONObject("token");

			//TODO: this map length doesn't line up 
			
			String id = tokenObj.getString("id");
			JSONArray terms = tokenObj.getJSONArray("terms");
			
			String prefix = id.substring(0,id.indexOf(":"));
			if (prefix.equals("UBERON") || prefix.equals("http") || prefix.equals("VTO")){
				for (int j = 0; j < terms.length(); j++){ //TODO; Categories and terms have a 1-1 mapping (although categories sometimes is blank?)
					String term = (String) terms.get(j);
					entityList.add(term);
				}
			}
			else if(prefix.equals("PATO") || prefix.equals("BSPO") || prefix.equals("RO")){
				for (int j = 0; j < terms.length(); j++){ //TODO; Categories and terms have a 1-1 mapping (although categories sometimes is blank?)
					String term = (String) terms.get(j);
					qualityList.add(term);
				}
			}
			else{
				System.out.println("LILA " + prefix);
			}
		}
		
		System.out.println(entityList);
		System.out.println(qualityList);
		SciGraphResponse packagedResponse = new SciGraphResponse(entityList, qualityList);
		
		//Package lists
		//TODO: perhaps make an object or a better data structure
//		Map<List<String>, List<String>> packagedLists = new HashMap<List<String>, List<String>>();
//		packagedLists.put(entityList,  qualityList);
		
		return packagedResponse;
	}

	private HttpEntity createPostEntity(ORBTerm term) throws UnsupportedEncodingException {
		final JSONObject json = new JSONObject();
		json.put("label", term.getLabel());
		json.put("definition", new JSONArray(Collections.singleton(term.getDefinition())));
		if (term.getParent() != null) {
			final String parentURI = ProvisionalTermUtil.toURI(term.getParent().getID());
			json.put("subclassOf", parentURI);
		}
		if (!term.getSynonyms().isEmpty()) {
			//FIXME this is not doing anything
			StringUtils.join(term.getSynonyms(), ",");
		}
		json.put("creator", ProvisionalTermUtil.getUserID());
		return new StringEntity(json.toString(), "utf-8");
	}

	@SuppressWarnings("unused")
	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
