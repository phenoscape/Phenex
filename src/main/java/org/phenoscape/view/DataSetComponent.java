package org.phenoscape.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.xswingx.PromptSupport;
import org.obo.app.swing.TabActionTextField;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.DataSet;

public class DataSetComponent extends PhenoscapeGUIComponent {

	private JTextField curatorsField;
	private JTextField publicationField;
	private JTextField publicationLabelField;
	private JTextField publicationURIField;
	private JTextField publicationCitationField;
	private JTextArea pubNotesField;

	public DataSetComponent(String id, PhenexController controller) {
		super(id, controller);
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();
	}

	private void initializeInterface() {
		this.setLayout(new GridBagLayout());
		this.curatorsField = new TabActionTextField();
		PromptSupport.setPrompt("None", this.curatorsField);
		this.curatorsField.setBackground(new JTextField().getBackground());
		this.curatorsField.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getController().getDataSet().setCurators(StringUtils.stripToNull(curatorsField.getText()));
			}
		});
		this.publicationField = new TabActionTextField();
		PromptSupport.setPrompt("None", this.publicationField);
		this.publicationField.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getController().getDataSet().setPublication(StringUtils.stripToNull(publicationField.getText()));
			}
		});
		this.publicationLabelField = new TabActionTextField();
		PromptSupport.setPrompt("None", this.publicationLabelField);
		this.publicationLabelField.setBackground(new JTextField().getBackground());
		this.publicationLabelField.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getController().getDataSet().setPublicationLabel(StringUtils.stripToNull(publicationLabelField.getText()));
			}
		});
		this.publicationURIField = new TabActionTextField();
		PromptSupport.setPrompt("None", this.publicationURIField);
		this.publicationURIField.setBackground(new JTextField().getBackground());
		this.publicationURIField.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getController().getDataSet().setPublicationURI(StringUtils.stripToNull(publicationURIField.getText()));
			}
		});
		this.publicationCitationField = new TabActionTextField();
		PromptSupport.setPrompt("None", this.publicationCitationField);
		this.publicationCitationField.setBackground(new JTextField().getBackground());
		this.publicationCitationField.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getController().getDataSet().setPublicationCitation(StringUtils.stripToNull(publicationCitationField.getText()));
			}
		});
		this.pubNotesField = new JTextArea();
		this.pubNotesField.setLineWrap(true);
		this.pubNotesField.setWrapStyleWord(true);
		this.pubNotesField.getDocument().addDocumentListener(new PubNotesListener());

		final GridBagConstraints curatorsLabelConstraints = new GridBagConstraints();
		curatorsLabelConstraints.anchor = GridBagConstraints.EAST;
		this.add(new JLabel("Curators:"), curatorsLabelConstraints);
		final GridBagConstraints curatorsFieldConstraints = new GridBagConstraints();
		curatorsFieldConstraints.gridx = 1;
		curatorsFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		curatorsFieldConstraints.weightx = 1.0;
		this.add(this.curatorsField, curatorsFieldConstraints);
		
		final GridBagConstraints pubLabelLabelConstraints = new GridBagConstraints();
		pubLabelLabelConstraints.anchor = GridBagConstraints.EAST;
		pubLabelLabelConstraints.gridy = 1;
		this.add(new JLabel("Publication Label:"), pubLabelLabelConstraints);
		final GridBagConstraints pubLabelFieldConstraints = new GridBagConstraints();
		pubLabelFieldConstraints.gridx = 1;
		pubLabelFieldConstraints.gridy = 1;
		pubLabelFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		pubLabelFieldConstraints.weightx = 1.0;
		this.add(this.publicationLabelField, pubLabelFieldConstraints);
		
		final GridBagConstraints pubDOILabelConstraints = new GridBagConstraints();
		pubDOILabelConstraints.anchor = GridBagConstraints.EAST;
		pubDOILabelConstraints.gridy = 2;
		this.add(new JLabel("Publication DOI/URI:"), pubDOILabelConstraints);
		final GridBagConstraints pubDOIFieldConstraints = new GridBagConstraints();
		pubDOIFieldConstraints.gridx = 1;
		pubDOIFieldConstraints.gridy = 2;
		pubDOIFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		pubDOIFieldConstraints.weightx = 1.0;
		this.add(this.publicationURIField, pubDOIFieldConstraints);
		
		final GridBagConstraints fullCitationLabelConstraints = new GridBagConstraints();
		fullCitationLabelConstraints.anchor = GridBagConstraints.EAST;
		fullCitationLabelConstraints.gridy = 3;
		this.add(new JLabel("Publication Citation:"), fullCitationLabelConstraints);
		final GridBagConstraints fullCitationFieldConstraints = new GridBagConstraints();
		fullCitationFieldConstraints.gridx = 1;
		fullCitationFieldConstraints.gridy = 3;
		fullCitationFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		fullCitationFieldConstraints.weightx = 1.0;
		this.add(this.publicationCitationField, fullCitationFieldConstraints);

		final GridBagConstraints pubNotesLabelConstraints = new GridBagConstraints();
		pubNotesLabelConstraints.gridy = 4;
		pubNotesLabelConstraints.gridwidth = 2;
		pubNotesLabelConstraints.anchor = GridBagConstraints.WEST;
		this.add(new JLabel("Publication Notes:"), pubNotesLabelConstraints);
		final GridBagConstraints pubNotesFieldConstraints = new GridBagConstraints();
		pubNotesFieldConstraints.gridy = 5;
		pubNotesFieldConstraints.gridwidth = 2;
		pubNotesFieldConstraints.fill = GridBagConstraints.BOTH;
		pubNotesFieldConstraints.weightx = 1.0;
		pubNotesFieldConstraints.weighty = 1.0;
		this.add(new JScrollPane(this.pubNotesField), pubNotesFieldConstraints);

		this.updateInterface();
		this.getController().getDataSet().addPropertyChangeListener(DataSet.CURATORS, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				curatorsField.setText(getController().getDataSet().getCurators());
			}
		});
		this.getController().getDataSet().addPropertyChangeListener(DataSet.PUBLICATION, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				publicationField.setText(getController().getDataSet().getPublication());
			}
		});
		this.getController().getDataSet().addPropertyChangeListener(DataSet.PUBLICATION_LABEL, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				publicationLabelField.setText(getController().getDataSet().getPublicationLabel());
			}
		});
		this.getController().getDataSet().addPropertyChangeListener(DataSet.PUBLICATION_URI, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				publicationURIField.setText(getController().getDataSet().getPublicationURI());
			}
		});
		this.getController().getDataSet().addPropertyChangeListener(DataSet.PUBLICATION_CITATION, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				publicationCitationField.setText(getController().getDataSet().getPublicationCitation());
			}
		});
		this.getController().getDataSet().addPropertyChangeListener(DataSet.PUBLICATION_NOTES, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				final String newNotes = getController().getDataSet().getPublicationNotes();
				final String currentNotes = pubNotesField.getText();
				if (!StringUtils.equals(newNotes, currentNotes)) {
					pubNotesField.setText(newNotes);
				}
			}
		});
	}

	private void updateInterface() {
		this.curatorsField.setText(this.getController().getDataSet().getCurators());
		this.publicationField.setText(this.getController().getDataSet().getPublication());
		this.publicationLabelField.setText(this.getController().getDataSet().getPublicationLabel());
		this.publicationURIField.setText(this.getController().getDataSet().getPublicationURI());
		this.publicationCitationField.setText(this.getController().getDataSet().getPublicationCitation());
		this.pubNotesField.setText(this.getController().getDataSet().getPublicationNotes());
	}

	private class PubNotesListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
			this.documentChanged();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			this.documentChanged();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			this.documentChanged();
		}

		private void documentChanged() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					getController().getDataSet().setPublicationNotes(pubNotesField.getText());                
				}
			});
		}

	}

}
