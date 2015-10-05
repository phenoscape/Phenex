package org.phenoscape.orb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.util.ProvisionalTermUtil;
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
