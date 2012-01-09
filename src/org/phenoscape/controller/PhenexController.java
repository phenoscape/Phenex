package org.phenoscape.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import name.pachler.nio.file.ClosedWatchServiceException;
import name.pachler.nio.file.FileSystems;
import name.pachler.nio.file.Path;
import name.pachler.nio.file.Paths;
import name.pachler.nio.file.StandardWatchEventKind;
import name.pachler.nio.file.WatchEvent;
import name.pachler.nio.file.WatchKey;
import name.pachler.nio.file.WatchService;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.bbop.framework.GUIManager;
import org.biojava.bio.seq.io.ParseException;
import org.jdesktop.swingworker.SwingWorker;
import org.nexml.schema_2009.NexmlDocument;
import org.obo.annotation.view.DefaultOntologyCoordinator;
import org.obo.annotation.view.OntologyCoordinator;
import org.obo.annotation.view.SelectionManager;
import org.obo.app.controller.DocumentController;
import org.obo.app.controller.UserCancelledReadException;
import org.obo.app.swing.ListSelectionMaintainer;
import org.obo.app.util.EverythingEqualComparator;
import org.phenoscape.io.CharacterTabReader;
import org.phenoscape.io.NEXUSReader;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.io.NeXMLWriter;
import org.phenoscape.io.TabDelimitedWriter;
import org.phenoscape.io.TaxonTabReader;
import org.phenoscape.io.nexml_1_0.NeXMLReader_1_0;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.NewDataListener;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.phenoscape.model.UndoObserver;
import org.phenoscape.util.DataMerger;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventSelectionModel;

public class PhenexController extends DocumentController {

    private final OntologyController ontologyController;
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
    private final UndoObserver undoObserver;
    private final SelectionManager phenoteSelectionManager;
    private SwingWorker<WatchEvent<?>, Void> fileMonitor;

    public PhenexController(OntologyController ontologyController) {
        super();
        this.phenoteSelectionManager = new org.obo.annotation.view.SelectionManager();
        this.ontologyController = ontologyController;
        this.sortedCharacters = new SortedList<Character>(this.dataSet.getCharacters(), new EverythingEqualComparator<Character>());
        this.charactersSelectionModel = new EventSelectionModel<Character>(this.sortedCharacters);
        new ListSelectionMaintainer<Character>(this.sortedCharacters, this.charactersSelectionModel);
        this.charactersSelectionModel.setSelectionMode(EventSelectionModel.SINGLE_SELECTION);
        this.sortedTaxa = new SortedList<Taxon>(this.dataSet.getTaxa(), new EverythingEqualComparator<Taxon>());
        this.taxaSelectionModel = new EventSelectionModel<Taxon>(this.sortedTaxa);
        new ListSelectionMaintainer<Taxon>(this.sortedTaxa, this.taxaSelectionModel);
        this.taxaSelectionModel.setSelectionMode(EventSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.currentSpecimens = new SortedList<Specimen>(new CollectionList<Taxon, Specimen>(this.taxaSelectionModel.getSelected(),
                new CollectionList.Model<Taxon, Specimen>(){
            @Override
            public List<Specimen> getChildren(Taxon parent) {
                return parent.getSpecimens();
            }
        } 
        ), new EverythingEqualComparator<Specimen>());
        this.currentSpecimensSelectionModel = new EventSelectionModel<Specimen>(this.currentSpecimens);
        new ListSelectionMaintainer<Specimen>(this.currentSpecimens, this.currentSpecimensSelectionModel);
        this.currentStates = new SortedList<State>(new CollectionList<Character, State>(this.charactersSelectionModel.getSelected(),
                new CollectionList.Model<Character, State>() {
            @Override
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
            @Override
            public List<Phenotype> getChildren(State parent) {
                return parent.getPhenotypes();
            }
        }
        ), new EverythingEqualComparator<Phenotype>());
        this.currentPhenotypesSelectionModel = new EventSelectionModel<Phenotype>(this.currentPhenotypes);
        new ListSelectionMaintainer<Phenotype>(this.currentPhenotypes, this.currentPhenotypesSelectionModel);
        this.currentPhenotypesSelectionModel.setSelectionMode(EventSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.undoObserver = new UndoObserver(this.getUndoController());
        this.undoObserver.setDataSet(this.dataSet);
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

    @Override
    public void readData(File aFile) throws IOException {
        try {
            this.readNeXML(aFile);
        } catch (XmlException e) {
            log().info("File not valid NeXML 2009, trying previous schema", e);
            this.readNeXML_1_0(aFile);
            //TODO should warn user that file format will be upgraded upon save
        }
    }

    private void readNeXML(File aFile) throws XmlException, IOException {
        final NeXMLReader reader = new NeXMLReader(aFile, this.getOntologyController().getOBOSession());
        if (reader.didCreateDanglers()) {
            final boolean result = this.runDanglerAlert(aFile, reader.getDanglersList());
            if (!result) {
                throw new UserCancelledReadException();
            }
        }
        if (reader.didMigrateSecondaryIDs()) {
            final boolean result = this.runSecondaryIDAlert(aFile, reader.getMigratedSecondaryIDsList());
            if (!result) {
                throw new UserCancelledReadException();
            }
        }
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
    }

    private void readNeXML_1_0(File aFile) throws IOException {
        try {
            final NeXMLReader_1_0 reader = new NeXMLReader_1_0(aFile, this.getOntologyController().getOBOSession());
            if (reader.didCreateDanglers()) {
                final boolean result = this.runDanglerAlert(aFile, reader.getDanglersList());
                if (!result) {
                    throw new UserCancelledReadException();
                }
            }
            if (reader.didMigrateSecondaryIDs()) {
                final boolean result = this.runSecondaryIDAlert(aFile, reader.getMigratedSecondaryIDsList());
                if (!result) {
                    throw new UserCancelledReadException();
                }
            }
            this.xmlDoc = NexmlDocument.Factory.newInstance();
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
        } catch (XmlException e) {
            final IOException ioe = new IOException(e.getLocalizedMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }

    @Override
    public void writeData(File aFile) throws IOException {
        final NeXMLWriter writer = new NeXMLWriter(this.charactersBlockID, this.xmlDoc);
        writer.setDataSet(this.dataSet);
        writer.setGenerator(this.getAppName() + " " + this.getAppVersion());
        writer.write(aFile);
    }

    public void openMergeTaxa() {
        final JFileChooser fileChooser = this.createFileChooser();
        final int result = fileChooser.showOpenDialog(GUIManager.getManager().getFrame());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            this.mergeTaxa(file);
        }
    }

    public void openMergeCharacters() {
        final JFileChooser fileChooser = this.createFileChooser();
        final int result = fileChooser.showOpenDialog(GUIManager.getManager().getFrame());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            this.mergeCharacters(file);
        }
    }

    public void openMergeNEXUS() {
        final JFileChooser fileChooser = this.createFileChooser();
        final int result = fileChooser.showOpenDialog(GUIManager.getManager().getFrame());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            this.mergeNEXUS(file);
        }
    }

    public void openMergeNeXML() {
        final JFileChooser fileChooser = this.createFileChooser();
        final int result = fileChooser.showOpenDialog(GUIManager.getManager().getFrame());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            this.mergeNeXML(file);
        }
    }

    public void openMergeModifiedFile() {
        log().debug("Data has changed - ask the user if we should merge.");
        JOptionPane.showConfirmDialog(this.getWindow(), "Your data changed! Do you want to merge???");
        this.monitorFileForChanges(this.getCurrentFile());
    }

    public void exportToExcel() {
        final JFileChooser fileChooser = this.createFileChooser();
        final int result = fileChooser.showSaveDialog(GUIManager.getManager().getFrame());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            this.writeForExcel(file);
        }
    }

    @Override
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

    @Override
    public String getDefaultFileExtension() {
        return "xml";
    }

    private SelectionManager getPhenoteSelectionManager() {
        return this.phenoteSelectionManager;
    }

    public OntologyCoordinator getOntologyCoordinator() {
        return new DefaultOntologyCoordinator(this.getOntologyController().getOBOSession(), this.getPhenoteSelectionManager());
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
            DataMerger.mergeTaxa(reader.getDataSet(), this.getDataSet());
        } catch (IOException e) {
            log().error("Error reading taxon list file", e);
        }
        this.fireDataChanged();
    }

    private void mergeCharacters(File aFile) {
        try {
            final CharacterTabReader reader = new CharacterTabReader(aFile, this.getOntologyController().getOBOSession());
            DataMerger.mergeCharacters(reader.getDataSet(), this.getDataSet());
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

    private void mergeNeXML(File aFile) {
        try {
            final NeXMLReader_1_0 reader = new NeXMLReader_1_0(aFile, this.getOntologyController().getOBOSession());
            DataMerger.mergeDataSets(reader.getDataSet(), this.getDataSet());
        } catch (XmlException e) {
            log().error("Error parsing NeXML file", e);
        } catch (IOException e) {
            log().error("Error reading NeXML file", e);
        }
    }

    private void writeForExcel(File aFile) {
        final TabDelimitedWriter writer = new TabDelimitedWriter();
        writer.setDataSet(this.getDataSet());
        try {
            writer.write(aFile);
        } catch (IOException e) {
            log().error("Error writing to tab-delimited file", e);
        }
    }

    private boolean runDanglerAlert(File file, Collection<String> danglerIDs) {
        final String[] options = {"Continue Opening", "Cancel"};
        final String message = "The file \"" + file.getName() + "\" contains references to ontology term IDs which could not be found. You can safely edit other values in the file, but fields referring to \"dangling\" terms should not be edited. Proceed with caution.";
        final int result = JOptionPane.showOptionDialog(null, message, "Missing Terms", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        return result == JOptionPane.YES_OPTION;
    }

    private boolean runSecondaryIDAlert(File file, Collection<String> secondaryIDs) {
        final String[] options = {"Continue Opening", "Cancel"};
        final String message = "The file \"" + file.getName() + "\" contains references to some ontology terms by secondary IDs. When you save this file, these term references will be migrated to each term's primary ID.";
        final JPanel panel = new JPanel(new BorderLayout());
        final List<String> ids = new ArrayList<String>(secondaryIDs);
        final TableModel model = new AbstractTableModel() {

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public int getRowCount() {
                return ids.size();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return ids.get(rowIndex);
            }

            @Override
            public String getColumnName(int column) {
                return "Referenced ID";
            }

        };
        panel.setPreferredSize(new Dimension(400, 200));
        panel.add(new JLabel("<HTML>" + message + "</HTML"), BorderLayout.NORTH);
        panel.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        final int result = JOptionPane.showOptionDialog(null, panel, "Secondary Identifiers", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        return result == JOptionPane.YES_OPTION;
    }

    private void monitorFileForChanges(final File aFile) {
        final Path watchedPath = Paths.get(aFile.getParentFile().getAbsolutePath());
        if (this.fileMonitor != null) {
            this.fileMonitor.cancel(true);
        }
        this.fileMonitor = new SwingWorker<WatchEvent<?>, Void>() {
            @Override
            protected WatchEvent<?> doInBackground() {
                try {
                    final WatchService watchService = FileSystems.getDefault().newWatchService();
                    watchedPath.register(watchService, StandardWatchEventKind.ENTRY_MODIFY);
                    while (!this.isCancelled()) {
                        // take() will block until a file has been modified
                        final WatchKey signalledKey = watchService.take();
                        log().debug("Take returned - got an event.");
                        // get list of events from key
                        final List<WatchEvent<?>> events = signalledKey.pollEvents();
                        // VERY IMPORTANT! call reset() AFTER pollEvents() to allow the
                        // key to be reported again by the watch service
                        signalledKey.reset();
                        for (WatchEvent<?> event : events) {
                            log().debug("Event: " + event);
                            if (event.kind().equals(StandardWatchEventKind.ENTRY_MODIFY)) {
                                final Path modifiedPath = (Path)(event.context());
                                log().debug("Modified path: " + event.context());
                                final File modifiedFile = new File(modifiedPath.toString());
                                log().debug("Modified file: " + modifiedFile);
                                if (modifiedPath.toString().equals(aFile.getName())) {
                                    return event;
                                }
                            }
                        }
                    }
                } catch (ClosedWatchServiceException e) {
                    log().error("Can't monitor file, watch service closed.", e);
                } catch (IOException e) {
                    log().error("Can't monitor file.", e);
                } catch (InterruptedException e) {
                    log().error("Watch service interrupted.");
                } 
                return null;
            }
            /* (non-Javadoc)
             * This method runs on the Event Dispatch Thread
             */
            @Override
            protected void done() {
                super.done();
                if (!this.isCancelled()) {
                    openMergeModifiedFile();                    
                }
            }

        };
        this.fileMonitor.execute();
    }

    @Override
    public void setCurrentFile(final File aFile) {
        super.setCurrentFile(aFile);
        this.monitorFileForChanges(aFile);
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
