package org.phenoscape.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
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
import org.jdesktop.swingworker.SwingWorker;
import org.nexml.schema_2009.NexmlDocument;
import org.obo.annotation.view.DefaultOntologyCoordinator;
import org.obo.annotation.view.OntologyCoordinator;
import org.obo.annotation.view.SelectionManager;
import org.obo.app.controller.DocumentController;
import org.obo.app.controller.UserCancelledReadException;
import org.obo.app.model.ObservableEventList;
import org.obo.app.swing.BlockingProgressDialog;
import org.obo.app.swing.ListSelectionMaintainer;
import org.obo.app.util.EverythingEqualComparator;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.impl.OBOClassImpl;
import org.phenoscape.io.BioCreativeTabFormat;
import org.phenoscape.io.CharacterTabReader;
import org.phenoscape.io.CharaparserEvaluationTabFormat;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.io.NeXMLWriter;
import org.phenoscape.io.PhenotypeProposalsLoader;
import org.phenoscape.io.TabDelimitedWriter;
import org.phenoscape.io.TaxonTabReader;
import org.phenoscape.io.nexml_1_0.NeXMLReader_1_0;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.MatrixCell;
import org.phenoscape.model.MatrixCellSelectionListener;
import org.phenoscape.model.NewDataListener;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.phenoscape.model.Tree;
import org.phenoscape.model.UndoObserver;
import org.phenoscape.orb.ORBController;
import org.phenoscape.scigraph.SciGraphController;
import org.phenoscape.scigraph.SciGraphResponse;
import org.phenoscape.util.DataMerger;
import org.phenoscape.util.TreeBuilder;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventSelectionModel;

public class PhenexController extends DocumentController {

	private final OntologyController ontologyController;
	private final ORBController orbController;
	private final SciGraphController sciGraphController;
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
	private final List<MatrixCellSelectionListener> matrixCellSelectionListeners = new ArrayList<MatrixCellSelectionListener>();
	private final UndoObserver undoObserver;
	private final SelectionManager phenoteSelectionManager;
	private SwingWorker<WatchEvent<?>, Void> fileMonitor;
	private MatrixCell selectedCell;

	public PhenexController(OntologyController ontologyController) {
		super();
		this.phenoteSelectionManager = new org.obo.annotation.view.SelectionManager();
		this.ontologyController = ontologyController;
		this.orbController = new ORBController(this);
		this.sciGraphController = new SciGraphController(this);
		this.sortedCharacters = new SortedList<Character>(this.dataSet.getCharacters(),
				new EverythingEqualComparator<Character>());
		this.charactersSelectionModel = new EventSelectionModel<Character>(this.sortedCharacters);
		new ListSelectionMaintainer<Character>(this.sortedCharacters, this.charactersSelectionModel);
		this.charactersSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.sortedTaxa = new SortedList<Taxon>(this.dataSet.getTaxa(), new EverythingEqualComparator<Taxon>());
		this.taxaSelectionModel = new EventSelectionModel<Taxon>(this.sortedTaxa);
		new ListSelectionMaintainer<Taxon>(this.sortedTaxa, this.taxaSelectionModel);
		this.taxaSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.currentSpecimens = new SortedList<Specimen>(new CollectionList<Taxon, Specimen>(
				this.taxaSelectionModel.getSelected(), new CollectionList.Model<Taxon, Specimen>() {
					@Override
					public List<Specimen> getChildren(Taxon parent) {
						return parent.getSpecimens();
					}
				}), new EverythingEqualComparator<Specimen>());
		this.currentSpecimensSelectionModel = new EventSelectionModel<Specimen>(this.currentSpecimens);
		new ListSelectionMaintainer<Specimen>(this.currentSpecimens, this.currentSpecimensSelectionModel);
		this.currentStates = new SortedList<State>(new CollectionList<Character, State>(
				this.charactersSelectionModel.getSelected(), new CollectionList.Model<Character, State>() {
					@Override
					public List<State> getChildren(Character parent) {
						return parent.getStates();
					}
				}), new EverythingEqualComparator<State>());
		this.currentStatesSelectionModel = new EventSelectionModel<State>(this.currentStates);
		new ListSelectionMaintainer<State>(this.currentStates, this.currentStatesSelectionModel);
		this.currentStatesSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.currentPhenotypes = new SortedList<Phenotype>(new CollectionList<State, Phenotype>(
				this.currentStatesSelectionModel.getSelected(), new CollectionList.Model<State, Phenotype>() {
					@Override
					public List<Phenotype> getChildren(State parent) {
						return parent.getPhenotypes();
					}
				}), new EverythingEqualComparator<Phenotype>());
		this.currentPhenotypesSelectionModel = new EventSelectionModel<Phenotype>(this.currentPhenotypes);
		new ListSelectionMaintainer<Phenotype>(this.currentPhenotypes, this.currentPhenotypesSelectionModel);
		this.currentPhenotypesSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
			// TODO should warn user that file format will be upgraded upon save
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
		if (reader.didReplaceObsoleteTerms()) {
			final boolean result = this.runReplacedIDsAlert(aFile, reader.getReplacedIDsList());
			if (!result) {
				throw new UserCancelledReadException();
			}
		}
		this.xmlDoc = reader.getXMLDoc();
		this.charactersBlockID = reader.getCharactersBlockID();
		this.dataSet.getCharacters().clear(); // TODO this is not well
												// encapsulated
		this.dataSet.getCharacters().addAll(reader.getDataSet().getCharacters());
		this.getDataSet().getTaxa().clear(); // TODO this is not well
												// encapsulated
		this.getDataSet().getTaxa().addAll(reader.getDataSet().getTaxa());
		this.getDataSet().getTrees().clear(); // TODO this is not well
												// encapsulated
		this.getDataSet().getTrees().addAll(reader.getDataSet().getTrees());
		this.getDataSet().setCurators(reader.getDataSet().getCurators());
		this.getDataSet().setPublication(reader.getDataSet().getPublication());
		this.getDataSet().setPublicationLabel(reader.getDataSet().getPublicationLabel());
		this.getDataSet().setPublicationURI(reader.getDataSet().getPublicationURI());
		this.getDataSet().setPublicationCitation(reader.getDataSet().getPublicationCitation());
		this.getDataSet().setPublicationNotes(reader.getDataSet().getPublicationNotes());
		this.getDataSet().setMatrixData(reader.getDataSet().getMatrixData());
		this.getDataSet().getAssociationSupport().clear();
		this.getDataSet().getAssociationSupport().putAll(reader.getDataSet().getAssociationSupport());
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
			this.dataSet.getCharacters().clear(); // TODO this is not well
													// encapsulated
			this.dataSet.getCharacters().addAll(reader.getDataSet().getCharacters());
			this.getDataSet().getTaxa().clear(); // TODO this is not well
													// encapsulated
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
		this.monitorFileForChanges(null);
		writer.write(aFile);
		this.monitorFileForChanges(this.getCurrentFile());
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
		if (this.getUndoController().hasUnsavedChanges()) {
			final int result = JOptionPane.showOptionDialog(this.getWindow(),
					String.format(
							"The file for the document at %s has been modified by another application. Do you want to discard your unsaved changes and replace with the newly edited version, or instead save your copy to another file?",
							this.getCurrentFile()),
					"Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					new String[] { "Replace with new data", "Save as a copy of this version" },
					"Replace with new data");
			if (result == JOptionPane.OK_OPTION) {
				this.loadModifiedFile();
			} else {
				this.saveAs(); // TODO if cancel this need to go back to
								// previous dialog
			}
		} else {
			final JDialog dialog = new JDialog(this.getWindow(), false);
			dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			dialog.setResizable(false);
			dialog.setLayout(new GridBagLayout());
			// surrounding with html tags makes the JLabel wrap its text
			final JLabel label = new JLabel("<HTML>"
					+ String.format("The file for the document at %s has been modified by another application.",
							this.getCurrentFile())
					+ "</HTML>");
			final GridBagConstraints labelConstraints = new GridBagConstraints();
			labelConstraints.insets = new Insets(11, 11, 11, 11);
			labelConstraints.fill = GridBagConstraints.BOTH;
			labelConstraints.weightx = 1.0;
			labelConstraints.weighty = 1.0;
			dialog.add(label, labelConstraints);
			final GridBagConstraints progressConstraints = new GridBagConstraints();
			progressConstraints.gridy = 1;
			progressConstraints.fill = GridBagConstraints.HORIZONTAL;
			progressConstraints.insets = new Insets(11, 11, 11, 11);
			final JProgressBar progress = new JProgressBar();
			progress.setIndeterminate(true);
			dialog.add(progress, progressConstraints);
			dialog.setTitle("Reloading modified file...");
			dialog.setSize(400, 150);
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
			this.loadModifiedFile();
			dialog.setVisible(false);
		}
		this.monitorFileForChanges(this.getCurrentFile());
	}

	private void loadModifiedFile() {
		try {
			this.getUndoController().beginIgnoringEdits();
			this.readData(this.getCurrentFile());
			this.getUndoController().discardAllEdits();
			this.getUndoController().markChangesSaved();
		} catch (IOException e) {
			log().error("Failed to read file", e);
		} finally {
			this.getUndoController().endIgnoringEdits();
		}
	}

	public void exportToExcel() {
		final JFileChooser fileChooser = this.createFileChooser();
		final int result = fileChooser.showSaveDialog(GUIManager.getManager().getFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			this.writeForExcel(file);
		}
	}

	public void exportForBioCreative() {
		final JFileChooser fileChooser = this.createFileChooser();
		final int result = fileChooser.showSaveDialog(GUIManager.getManager().getFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			this.writeForBioCreative(file);
		}
	}

	public void exportForCharaparserEvaluation() {
		final JFileChooser fileChooser = this.createFileChooser();
		final int result = fileChooser.showSaveDialog(GUIManager.getManager().getFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			this.writeForCharaparserEvaluation(file);
		}
	}

	public void openImportPhenotypeProposals() {
		final JFileChooser fileChooser = this.createFileChooser();
		final int result = fileChooser.showOpenDialog(GUIManager.getManager().getFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			final File file = fileChooser.getSelectedFile();
			this.importPhenotypeProposals(file);
		}
	}

	private void importPhenotypeProposals(File file) {
		final PhenotypeProposalsLoader loader = new PhenotypeProposalsLoader(this.getDataSet(),
				this.getOntologyController().getOBOSession());
		try {
			loader.loadProposals(file);
		} catch (IOException e) {
			log().error("Failed to load phenotype proposals file: " + file, e);
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
		return new DefaultOntologyCoordinator(this.getOntologyController().getOBOSession(),
				this.getPhenoteSelectionManager());
	}

	public void addNewDataListener(NewDataListener listener) {
		this.newDataListeners.add(listener);
	}

	public void removeNewDataListener(NewDataListener listener) {
		this.newDataListeners.remove(listener);
	}

	public void addMatrixCellSelectionListener(MatrixCellSelectionListener listener) {
		this.matrixCellSelectionListeners.add(listener);
	}

	public void removeMatrixCellSelectionListener(MatrixCellSelectionListener listener) {
		this.matrixCellSelectionListeners.remove(listener);
	}

	public void runORBTermRequest() {
		this.orbController.runORBTermRequest();
		// final NewTermRequestPanel panel = new
		// NewTermRequestPanel(this.getOntologyCoordinator());
		// panel.init();
		// panel.setSize(400, 250);
		// final int result = JOptionPane.showConfirmDialog(this.getWindow(),
		// panel, "Submit new term request", JOptionPane.OK_CANCEL_OPTION,
		// JOptionPane.PLAIN_MESSAGE);

	}

	private void runSciGraphRequest(ObservableEventList<Character> characterList,
			ObservableEventList<Taxon> taxonList) {
		// auto-fill characters
		if (characterList.size() > 0) {
			for (int i = 0; i < characterList.size(); i++) {
				String character = characterList.get(i).toString();
				ObservableEventList<State> states = characterList.get(i).getStates();
				for (int j = 0; j < states.size(); j++) {
					String request = character + " " + states.get(j).getLabel();
					SciGraphResponse response = this.sciGraphController.runSciGraphCharacterRequest(request);
					updateCharacterEntityWithResponse(response, i, j);
				}
			}
		}

		// auto-fill taxons
		if (taxonList.size() > 0) {
			for (int i = 0; i < taxonList.size(); i++) {
				String request = taxonList.get(i).getPublicationName().toString();
				List<OBOClass> list = this.sciGraphController.runSciGraphTaxonRequest(request);
				if (list.size() > 0) {
					OBOClass term = list.get(0); // select first entry if one
													// exists
					taxonList.get(i).setValidName(term);
				}
			}
		}
	}

	private void updateCharacterEntityWithResponse(SciGraphResponse response, int characterIndex, int stateIndex) {
		Map<String, String> qMap = response.getQualityList();
		Map<String, String> eMap = response.getEntityList();
		if (qMap.isEmpty()) {
			qMap.put("", "");
		}
		if (eMap.isEmpty()) {
			eMap.put("", "");
		}
		for (String e : eMap.keySet()) {
			for (String q : qMap.keySet()) {
				Phenotype phenotype = new Phenotype();
				OBOClass entity = new OBOClassImpl(eMap.get(e), e);
				phenotype.setEntity(entity);

				OBOClass quality = new OBOClassImpl(qMap.get(q), q);
				phenotype.setQuality(quality);

				this.dataSet.getCharacters().get(characterIndex).getStates().get(stateIndex).getPhenotypes()
						.add(phenotype);
			}
		}
	}

	public void generateTree() {
		final Map<LinkedObject, LinkedObject> topology = TreeBuilder.buildTree(this.dataSet,
				this.getOntologyController().getOBOSession());
		final Tree tree = new Tree();
		tree.setTopology(topology);
		this.dataSet.addTree(tree);
	}

	public void fillEntities() {
		SciGraphLoader fillEntitiesLoader = new SciGraphLoader(this.dataSet.getCharacters(), this.dataSet.getTaxa());
		final BlockingProgressDialog<Integer, Void> dialog = new BlockingProgressDialog<Integer, Void>(
				fillEntitiesLoader,
				"Auto-filling currently in progress. It may take some time to recognize and fill all entities.");
		dialog.setTitle("Auto-fill Entities");
		dialog.setSize(400, 150);
		dialog.setLocationRelativeTo(null);
		dialog.run();
	}

	public void setSelectedMatrixCell(MatrixCell cell) {
		this.selectedCell = cell;
		for (MatrixCellSelectionListener listener : this.matrixCellSelectionListeners) {
			listener.matrixCellWasSelected(this.selectedCell, this);
		}
	}

	public MatrixCell getSelectedMatrixCell() {
		return this.selectedCell;
	}

	private void fireDataChanged() {
		for (NewDataListener listener : this.newDataListeners) {
			listener.reloadData();
		}
	}

	private void mergeTaxa(File aFile) {
		try {
			final TaxonTabReader reader = new TaxonTabReader(aFile, this.getOntologyController().getOBOSession(),
					this.getOntologyController().getCollectionTermSet());
			DataMerger.mergeTaxa(reader.getDataSet(), this.getDataSet());
		} catch (IOException e) {
			log().error("Error reading taxon list file", e);
		}
		this.fireDataChanged();
	}

	private void mergeCharacters(File aFile) {
		try {
			final CharacterTabReader reader = new CharacterTabReader(aFile,
					this.getOntologyController().getOBOSession());
			DataMerger.mergeCharacters(reader.getDataSet(), this.getDataSet());
		} catch (IOException e) {
			log().error("Error reading character list file", e);
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

	private void writeForBioCreative(File aFile) {
		final BioCreativeTabFormat writer = new BioCreativeTabFormat(this.getDataSet());
		try {
			writer.write(aFile);
		} catch (IOException e) {
			log().error("Error writing to tab-delimited file", e);
		}
	}

	private void writeForCharaparserEvaluation(File aFile) {
		final CharaparserEvaluationTabFormat writer = new CharaparserEvaluationTabFormat(this.getDataSet());
		try {
			writer.write(aFile);
		} catch (IOException e) {
			log().error("Error writing to tab-delimited file", e);
		}
	}

	private boolean runDanglerAlert(File file, Collection<String> danglerIDs) {
		final String[] options = { "Continue Opening", "Cancel" };
		final String message = "The file \"" + file.getName()
				+ "\" contains references to ontology term IDs which could not be found. You can safely edit other values in the file, but fields referring to \"dangling\" terms should not be edited. Proceed with caution.";
		final int result = JOptionPane.showOptionDialog(null, message, "Missing Terms", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		return result == JOptionPane.YES_OPTION;
	}

	private boolean runReplacedIDsAlert(File file, Collection<String> replacedIDs) {
		final String[] options = { "Continue Opening", "Cancel" };
		final String message = "The file \"" + file.getName()
				+ "\" contains references to obsolete term IDs which have been automatically updated using the replaced_by tag.";
		final int result = JOptionPane.showOptionDialog(null, message, "Replaced Obsolete Terms",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		return result == JOptionPane.YES_OPTION;
	}

	private boolean runSecondaryIDAlert(File file, Collection<String> secondaryIDs) {
		final String[] options = { "Continue Opening", "Cancel" };
		final String message = "The file \"" + file.getName()
				+ "\" contains references to some ontology terms by secondary IDs. When you save this file, these term references will be migrated to each term's primary ID.";
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
		final int result = JOptionPane.showOptionDialog(null, panel, "Secondary Identifiers", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		return result == JOptionPane.YES_OPTION;
	}

	private void monitorFileForChanges(final File aFile) {
		if (this.fileMonitor != null) {
			this.fileMonitor.cancel(true);
		}
		if (aFile == null) {
			return;
		}
		final Path watchedPath = Paths.get(aFile.getParentFile().getAbsolutePath());
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
						// VERY IMPORTANT! call reset() AFTER pollEvents() to
						// allow the
						// key to be reported again by the watch service
						signalledKey.reset();
						for (WatchEvent<?> event : events) {
							log().debug("Event: " + event);
							if (event.kind().equals(StandardWatchEventKind.ENTRY_MODIFY)) {
								final Path modifiedPath = (Path) (event.context());
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

			/*
			 * (non-Javadoc) This method runs on the Event Dispatch Thread
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

	private class SciGraphLoader extends SwingWorker<Integer, Void> {

		private ObservableEventList<Character> characterList;
		private ObservableEventList<Taxon> taxonList;

		public SciGraphLoader(ObservableEventList<Character> charList, ObservableEventList<Taxon> taxonList) {
			this.characterList = charList;
			this.taxonList = taxonList;
		}

		protected Integer doInBackground() {
			runSciGraphRequest(characterList, taxonList);
			return 1;
		}
	}

}
