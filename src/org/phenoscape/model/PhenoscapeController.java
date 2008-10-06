package org.phenoscape.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.bbop.framework.GUIManager;
import org.biojava.bio.seq.io.ParseException;
import org.nexml.x10.NexmlDocument;
import org.phenoscape.app.DocumentController;
import org.phenoscape.io.CharacterTabReader;
import org.phenoscape.io.NEXUSReader;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.io.NeXMLWriter;
import org.phenoscape.io.TaxonTabReader;
import org.phenoscape.swing.ListSelectionMaintainer;
import org.phenoscape.util.DataMerger;

import phenote.gui.selection.SelectionManager;
import phenote.util.EverythingEqualComparator;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventSelectionModel;

public class PhenoscapeController extends DocumentController {
  
  private final OntologyController ontologyController = new OntologyController();
  private final DataSet dataSet = new DataSet();
  private final SortedList<Character> sortedCharacters;
  private final EventSelectionModel<Character> charactersSelectionModel;
  private final SortedList<Taxon> sortedTaxa;
  private final EventSelectionModel<Taxon> taxaSelectionModel;
  private final SortedList<Specimen> currentSpecimens;
  private final EventSelectionModel<Specimen> currentSpecimensSelectionModel;
  private final SortedList<State> currentStates;
  private final EventSelectionModel<State> currentStatesSelectionModel;
  private final SortedList<Phenotype> currentPhenotypes;
  private final EventSelectionModel<Phenotype> currentPhenotypesSelectionModel;
  private String charactersBlockID = UUID.randomUUID().toString();
  private NexmlDocument xmlDoc = NexmlDocument.Factory.newInstance();
  private String appName;
  private final List<NewDataListener> newDataListeners = new ArrayList<NewDataListener>();
  
  public PhenoscapeController() {
    super();
    this.sortedCharacters = new SortedList<Character>(this.dataSet.getCharacters(), new EverythingEqualComparator<Character>());
    this.charactersSelectionModel = new EventSelectionModel<Character>(this.sortedCharacters);
    new ListSelectionMaintainer<Character>(this.sortedCharacters, this.charactersSelectionModel);
    this.charactersSelectionModel.setSelectionMode(EventSelectionModel.SINGLE_SELECTION);
    this.sortedTaxa = new SortedList<Taxon>(this.dataSet.getTaxa(), new EverythingEqualComparator<Taxon>());
    this.taxaSelectionModel = new EventSelectionModel<Taxon>(this.sortedTaxa);
    new ListSelectionMaintainer<Taxon>(this.sortedTaxa, this.taxaSelectionModel);
    this.taxaSelectionModel.setSelectionMode(EventSelectionModel.SINGLE_SELECTION);
    this.currentSpecimens = new SortedList<Specimen>(new CollectionList<Taxon, Specimen>(this.taxaSelectionModel.getSelected(),
        new CollectionList.Model<Taxon, Specimen>(){
      public List<Specimen> getChildren(Taxon parent) {
        return parent.getSpecimens();
      }
    } 
    ), new EverythingEqualComparator<Specimen>());
    this.currentSpecimensSelectionModel = new EventSelectionModel<Specimen>(this.currentSpecimens);
    new ListSelectionMaintainer<Specimen>(this.currentSpecimens, this.currentSpecimensSelectionModel);
    this.currentStates = new SortedList<State>(new CollectionList<Character, State>(this.charactersSelectionModel.getSelected(),
        new CollectionList.Model<Character, State>() {
      public List<State> getChildren(Character parent) {
        return parent.getStates();
      }
    }
    ), new EverythingEqualComparator<State>());
    this.currentStatesSelectionModel = new EventSelectionModel<State>(this.currentStates);
    new ListSelectionMaintainer<State>(this.currentStates, this.currentStatesSelectionModel);
    this.currentStatesSelectionModel.setSelectionMode(EventSelectionModel.SINGLE_SELECTION);
    this.currentPhenotypes = new SortedList<Phenotype>(new CollectionList<State, Phenotype>(this.currentStatesSelectionModel.getSelected(),
        new CollectionList.Model<State, Phenotype>() {
      public List<Phenotype> getChildren(State parent) {
        return parent.getPhenotypes();
      }
    }
    ), new EverythingEqualComparator<Phenotype>());
    this.currentPhenotypesSelectionModel = new EventSelectionModel<Phenotype>(this.currentPhenotypes);
    new ListSelectionMaintainer<Phenotype>(this.currentPhenotypes, this.currentPhenotypesSelectionModel);
    this.currentPhenotypesSelectionModel.setSelectionMode(EventSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  }
  
  public OntologyController getOntologyController() {
    return this.ontologyController;
  }
  
  public DataSet getDataSet() {
    return this.dataSet;
  }
  
  public SortedList<Character> getSortedCharacters() {
    return this.sortedCharacters;
  }

  public EventSelectionModel<Character> getCharactersSelectionModel() {
    return this.charactersSelectionModel;
  }
  
  public SortedList<State> getStatesForCurrentCharacterSelection() {
    return this.currentStates;
  }
  
  public EventSelectionModel<State> getCurrentStatesSelectionModel() {
    return this.currentStatesSelectionModel;
  }
  
  public SortedList<Phenotype> getPhenotypesForCurrentStateSelection() {
    return this.currentPhenotypes;
  }
  
  public EventSelectionModel<Phenotype> getCurrentPhenotypesSelectionModel() {
    return this.currentPhenotypesSelectionModel;
  }
  
  public SortedList<Taxon> getSortedTaxa() {
    return this.sortedTaxa;
  }
  
  public EventSelectionModel<Taxon> getTaxaSelectionModel() {
    return this.taxaSelectionModel;
  }
  
  public SortedList<Specimen> getSpecimensForCurrentTaxonSelection() {
    return this.currentSpecimens;
  }
  
  public EventSelectionModel<Specimen> getCurrentSpecimensSelectionModel() {
    return this.currentSpecimensSelectionModel;
  }

  //@Override
  public boolean readData(File aFile) {
    log().debug("Read file: " + aFile);
    return this.readNeXML(aFile);
  }
  
  private boolean readNeXML(File aFile) {
    try {
      final NeXMLReader reader = new NeXMLReader(aFile, this.getOntologyController().getOBOSession());
      this.xmlDoc = reader.getXMLDoc();
      this.charactersBlockID = reader.getCharactersBlockID();
      this.dataSet.getCharacters().clear(); //TODO this is not well encapsulated
      this.dataSet.getCharacters().addAll(reader.getDataSet().getCharacters());
      this.getDataSet().getTaxa().clear(); //TODO this is not well encapsulated
      this.getDataSet().getTaxa().addAll(reader.getDataSet().getTaxa());
      this.getDataSet().setCurators(reader.getDataSet().getCurators());
      this.getDataSet().setPublication(reader.getDataSet().getPublication());
      this.getDataSet().setPublicationNotes(reader.getDataSet().getPublicationNotes());
      this.getDataSet().setMatrixData(reader.getDataSet().getMatrixData());
      
      this.fireDataChanged();
      return true;
    } catch (XmlException e) {
      log().error("Unable to parse XML", e);
    } catch (IOException e) {
      log().error("Unable to read file", e);
    }
    return false;
  }
  
  @Override
  public boolean writeData(File aFile) {
    final NeXMLWriter writer = new NeXMLWriter(this.charactersBlockID, this.xmlDoc);
    writer.setDataSet(this.dataSet);
    writer.setGenerator(this.getAppName() + " " + this.getAppVersion());
    try {
      writer.write(aFile);
      return true;
    } catch (IOException e) {
      log().error("Unable to write NeXML file", e);
      return false;
    }
  }
  
  public void openMergeTaxa() {
    final JFileChooser fileChooser = new JFileChooser();
    final int result = fileChooser.showOpenDialog(GUIManager.getManager().getFrame());
    if (result == JFileChooser.APPROVE_OPTION) {
      final File file = fileChooser.getSelectedFile();
      this.mergeTaxa(file);
    }
  }
  
  public void openMergeCharacters() {
    final JFileChooser fileChooser = new JFileChooser();
    final int result = fileChooser.showOpenDialog(GUIManager.getManager().getFrame());
    if (result == JFileChooser.APPROVE_OPTION) {
      final File file = fileChooser.getSelectedFile();
      this.mergeCharacters(file);
    }
  }
  
  public void openMergeNEXUS() {
    final JFileChooser fileChooser = new JFileChooser();
    final int result = fileChooser.showOpenDialog(GUIManager.getManager().getFrame());
    if (result == JFileChooser.APPROVE_OPTION) {
      final File file = fileChooser.getSelectedFile();
      this.mergeNEXUS(file);
    }
  }
  
  public JFrame getWindow() {
    return GUIManager.getManager().getFrame();
  }
    
  @Override
  public String getAppName() {
    return this.appName;
  }
  
  public void setAppName(String name) {
    this.appName = name;
  }
  
  public String getAppVersion() {
    return System.getProperty("phenex.version");
  }

  public SelectionManager getPhenoteSelectionManager() {
    return SelectionManager.inst();
  }
  
  public void addNewDataListener(NewDataListener listener) {
    this.newDataListeners.add(listener);
  }
  
  public void removeNewDataListener(NewDataListener listener) {
    this.newDataListeners.remove(listener);
  }
  
  private void fireDataChanged() {
    for (NewDataListener listener : this.newDataListeners) {
      listener.reloadData();
    }
  }
  
  private void mergeTaxa(File aFile) {
    try {
      final TaxonTabReader reader = new TaxonTabReader(aFile, this.getOntologyController().getOBOSession(), this.getOntologyController().getCollectionTermSet());
      DataMerger.mergeTaxa(reader, this.getDataSet());
    } catch (IOException e) {
      log().error("Error reading taxon list file", e);
    }
    this.fireDataChanged();
  }
  
  private void mergeCharacters(File aFile) {
    try {
      final CharacterTabReader reader = new CharacterTabReader(aFile, this.getOntologyController().getOBOSession());
      DataMerger.mergeCharacters(reader, this.getDataSet());
    } catch (IOException e) {
      log().error("Error reading character list file", e);
    }
    this.fireDataChanged();
  }
  
  private void mergeNEXUS(File aFile) {
    try {
      final NEXUSReader reader = new NEXUSReader(aFile);
      DataMerger.mergeDataSets(reader.getDataSet(), this.getDataSet());
    } catch (ParseException e) {
      log().error("Error parsing NEXUS file", e);
    } catch (IOException e) {
      log().error("Error reading NEXUS file", e);
    }
    this.fireDataChanged();
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
 
}
