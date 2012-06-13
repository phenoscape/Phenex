package org.phenoscape.orb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.jdom.Document;
import org.jdom.filter.ElementFilter;
import org.jdom.input.DOMBuilder;
import org.obo.annotation.view.OntologyCoordinator;
import org.obo.datamodel.OBOClass;
import org.xml.sax.SAXException;

public class ORBController {

	private static final String SERVICE = "http://rest.bioontology.org/bioportal/provisional";
	private static final String APIKEY = "";
	private final OntologyCoordinator ontologyCoordinator;


	public ORBController(OntologyCoordinator ontologyCoordinator) {
		this.ontologyCoordinator = ontologyCoordinator;
	}

	public void runORBTermRequest() {
		final NewTermRequestPanel panel = new NewTermRequestPanel(this.ontologyCoordinator);
		panel.init();
		panel.setSize(400, 250);
		final int result = JOptionPane.showConfirmDialog(null, panel, "Submit new term request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			final ORBTerm requestedTerm = panel.getTerm();
		}
	}

	private OBOClass requestTerm(ORBTerm term) throws ClientProtocolException, IOException, ParserConfigurationException, IllegalStateException, SAXException {
		final HttpPost post = new HttpPost(SERVICE);
		post.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
		post.setEntity(this.createPostEntity(term));
		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpResponse response = new DefaultHttpClient().execute(post);
		final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		final Document xmlDoc = new DOMBuilder().build(docBuilder.parse(response.getEntity().getContent()));
		xmlDoc.getRootElement().getDescendants(new ElementFilter("classBean"));
		/////


		client.getConnectionManager().shutdown();
		return null;
	}

	private HttpEntity createPostEntity(ORBTerm term) throws UnsupportedEncodingException {
		final List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("apikey", APIKEY));
		values.add(new BasicNameValuePair("preferredname", term.getLabel()));
		values.add(new BasicNameValuePair("definition", term.getDefinition()));
		//values.add(new BasicNameValuePair("submittedby", this.getSubmitterID()));
		return new UrlEncodedFormEntity(values);
	}

}
