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

	public Map<String, String> runSciGraphRequest(){
		Map<String, String> returnedRequest = null;
		try {
			returnedRequest = this.sciGraphRequest();
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnedRequest;
	}
	
	//TODO: pass in list of values and then populate
	
	private Map<String, String> sciGraphRequest() throws ClientProtocolException, IOException, URISyntaxException{
		final String url = "http://kb.phenoscape.org/scigraph/annotations/entities"; //json return object instead of HTML
		System.out.println("sciGraphRequest()");
		
		final OBOSession session = this.controller.getOntologyController().getOBOSession();
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair( "content", "Lateral ethmoid" ) );
		URI uri = new URI(url + "?" + URLEncodedUtils.format( params, "utf-8" ));
		System.out.println(uri);
		final HttpGet request = new HttpGet(uri);//ProvisionalTermUtil.SERVICE);
		
//		String url = "http://example.com";
//		URI uri = new URI( url + "?" + URLEncodedUtils.format( params, "utf-8" );
//		
//		request.setParams(new BasicNameValuePair("content", "Lateral+ethmoid"));
//		System.out.println(request);
//		System.out.println(request.getURI());
//		System.out.println();
		System.out.println(request.getRequestLine());
		
		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpResponse response = new DefaultHttpClient().execute(request);
		
		
		
//		System.out.println(response.getEntity().getContent());
//		System.out.println(response.getParams());
		String json = EntityUtils.toString(response.getEntity());

		System.out.println("testtest");
		
		Map<String, ArrayList<String>> categoryMap = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> termMap = new HashMap<String, ArrayList<String>>();
		
		//1-1 mapping TODO: make into a map of term to category
//		List<String> categoryList = new ArrayList<String>();
//		List<String> termMap = new ArrayList<String>();
		
		Map<String, String> termToCategory = new HashMap<String, String>();

		JSONArray responseJSON = new JSONArray(json);
		for(int i = 0; i < responseJSON.length(); i++){ 
			JSONObject jsonObj = (JSONObject) responseJSON.get(i);
			JSONObject tokenObj = jsonObj.getJSONObject("token");

			JSONArray categories = tokenObj.getJSONArray("categories");
			JSONArray terms = tokenObj.getJSONArray("terms");
			
			System.out.println(categories.length());
			System.out.println(terms.length());

			
			for (int j = 0; j < terms.length(); j++){ //TODO; Categories and terms have a 1-1 mapping (although categories sometimes is blank?)
				String cat = null;
				if (categories.length() > 0)
					 cat = (String) categories.get(j);
				String term = (String) terms.get(j);

//				System.out.println(cat);
//				System.out.println(term);
				termToCategory.put(term, cat);
				//termToCategory.put
			}
//			for (int j = 0; i < terms.length(); j++){
//				System.out.println(term);
//			}
		}
		System.out.println("testtest");
		System.out.println(termToCategory);
		
//		Iterator itr = responseJSON.keys();
//
//		System.out.println("key");
//		while (itr.hasNext()){
//			System.out.println("==");
//		    String key = (String) itr.next();
//		    System.out.println(key);
//		    System.out.println(responseJSON.get(key));
//		}

//		JSONArray rsp = new JSONArray(responseJSON);
//		System.out.println(rsp);
//		JSONArray jsonArray = responseJSON.getJSONArray("");
//		for(int i = 0; i < jsonArray.length(); i++){
//			jsonArray.get(i);
//			
//		}
//		System.out.println("arrll");
//		System.out.println(jsonArray);
//		responseJSON.get
//		responseJSON.

//		System.out.println(responseJSON);
//		System.out.println(json);

		//http://kb.phenoscape.org/scigraph/annotations/entities
		//?content=Lateral+ethmoid&minLength=4
		
		
		//TODO: lateral side in categories does not show up, probably because categories is empty

		return termToCategory;
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
