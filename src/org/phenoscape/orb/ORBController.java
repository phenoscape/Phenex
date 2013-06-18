package org.phenoscape.orb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.util.ProvisionalTermUtil;
import org.xml.sax.SAXException;

public class ORBController {

	private static final String SERVICE = "http://rest.bioontology.org/bioportal/provisional";
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
		final HttpPost post = new HttpPost(SERVICE);
		post.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
		post.setEntity(this.createPostEntity(term));
		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpResponse response = new DefaultHttpClient().execute(post);
		client.getConnectionManager().shutdown();
		final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		final Document xmlDoc = new DOMBuilder().build(docBuilder.parse(response.getEntity().getContent()));
		final Element newTermElement = xmlDoc.getRootElement().getChild("data").getChild("classBean");
		final OBOClass newTerm = ProvisionalTermUtil.createClassForProvisionalTerm(newTermElement, session);
		session.addObject(newTerm);
		this.controller.getOntologyController().invalidateAllTermSets();
		this.controller.getOntologyCoordinator().getSelectionManager().selectTerm(this, newTerm, false);
		return newTerm;
	}

	private HttpEntity createPostEntity(ORBTerm term) throws UnsupportedEncodingException {
		final List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("apikey", ProvisionalTermUtil.getAPIKey()));
		values.add(new BasicNameValuePair("preferredname", term.getLabel()));
		values.add(new BasicNameValuePair("definition", term.getDefinition()));
		if (term.getParent() != null) {
			final String parentURI = ProvisionalTermUtil.toURI(term.getParent().getID());
			values.add(new BasicNameValuePair("superclass", parentURI));
		}
		if (!term.getSynonyms().isEmpty()) {
			StringUtils.join(term.getSynonyms(), ",");
		}
		values.add(new BasicNameValuePair("submittedby", ProvisionalTermUtil.getUserID()));
		return new UrlEncodedFormEntity(values);
	}

	@SuppressWarnings("unused")
	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
