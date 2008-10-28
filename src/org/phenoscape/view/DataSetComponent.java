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

import org.apache.log4j.Logger;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.NewDataListener;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.swing.PlaceholderText;
import org.phenoscape.swing.TabActionTextField;

public class DataSetComponent extends PhenoscapeGUIComponent {
  
  private JTextField curatorsField;
  private JTextField publicationField;
  private JTextArea pubNotesField;

  public DataSetComponent(String id, PhenoscapeController controller) {
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
    new PlaceholderText(this.curatorsField, "None");
    this.curatorsField.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        getController().getDataSet().setCurators(curatorsField.getText());
      }
    });
    this.publicationField = new TabActionTextField();
    new PlaceholderText(this.publicationField, "None");
    this.publicationField.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        getController().getDataSet().setPublication(publicationField.getText());
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
    
    final GridBagConstraints pubLabelConstraints = new GridBagConstraints();
    pubLabelConstraints.anchor = GridBagConstraints.EAST;
    pubLabelConstraints.gridy = 1;
    this.add(new JLabel("Publication:"), pubLabelConstraints);
    final GridBagConstraints pubFieldConstraints = new GridBagConstraints();
    pubFieldConstraints.gridx = 1;
    pubFieldConstraints.gridy = 1;
    pubFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
    pubFieldConstraints.weightx = 1.0;
    this.add(this.publicationField, pubFieldConstraints);
    
    final GridBagConstraints pubNotesLabelConstraints = new GridBagConstraints();
    pubNotesLabelConstraints.gridy = 2;
    pubNotesLabelConstraints.gridwidth = 2;
    pubNotesLabelConstraints.anchor = GridBagConstraints.WEST;
    this.add(new JLabel("Publication Notes:"), pubNotesLabelConstraints);
    final GridBagConstraints pubNotesFieldConstraints = new GridBagConstraints();
    pubNotesFieldConstraints.gridy = 3;
    pubNotesFieldConstraints.gridwidth = 2;
    pubNotesFieldConstraints.fill = GridBagConstraints.BOTH;
    pubNotesFieldConstraints.weightx = 1.0;
    pubNotesFieldConstraints.weighty = 1.0;
    this.add(new JScrollPane(this.pubNotesField), pubNotesFieldConstraints);
    
    //TODO can probably remove this
    this.getController().addNewDataListener(new DataListener());
    this.updateInterface();
    this.getController().getDataSet().addPropertyChangeListener(DataSet.CURATORS, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            curatorsField.setText(getController().getDataSet().getCurators());
        }
    });
    this.getController().getDataSet().addPropertyChangeListener(DataSet.PUBLICATION, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            publicationField.setText(getController().getDataSet().getPublication());
        }
    });
    this.getController().getDataSet().addPropertyChangeListener(DataSet.PUBLICATION_NOTES, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            pubNotesField.setText(getController().getDataSet().getPublicationNotes());
        }
    });
  }
  
  private void updateInterface() {
    this.curatorsField.setText(this.getController().getDataSet().getCurators());
    this.publicationField.setText(this.getController().getDataSet().getPublication());
    this.pubNotesField.setText(this.getController().getDataSet().getPublicationNotes());
  }
  
  private class PubNotesListener implements DocumentListener {

    public void changedUpdate(DocumentEvent e) {
      this.documentChanged();
    }

    public void insertUpdate(DocumentEvent e) {
      this.documentChanged();
    }

    public void removeUpdate(DocumentEvent e) {
      this.documentChanged();
    }
    
    private void documentChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getController().getDataSet().setPublicationNotes(pubNotesField.getText());                
            }
        });
    }
    
  }
  
  //TODO get rid of this and just use observer
  private class DataListener implements NewDataListener {

    public void reloadData() {
      updateInterface();      
    }
    
  }
  
  @SuppressWarnings("unused")
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
