package org.phenoscape.orb;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.obo.annotation.view.TermAutocompleteFieldFactory;
import org.obo.annotation.view.TermRenderer;
import org.obo.app.swing.AutocompleteField;
import org.obo.app.swing.BugWorkaroundTable;
import org.obo.app.swing.TabActionTextField;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.orb.ORBTerm.Synonym;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

public class ProvisionalTermRequestPanel extends AbstractGUIComponent {

	private JTextField preferredLabelField;
	private AutocompleteField<OBOObject> parentBox;
	private JTextArea definitionField;
	private JTextField contactInfoField;
	private EventList<Synonym> synonyms = new BasicEventList<Synonym>();
	private EventSelectionModel<Synonym> selectionModel = new EventSelectionModel<Synonym>(synonyms);
	{
		selectionModel.setSelectionMode(EventSelectionModel.SINGLE_SELECTION);
	}
	private SynonymsTableFormat tableFormat;
	private JTable synonymsTable;
	private JButton addSynonymButton;
	private JButton deleteSynonymButton;
	private final PhenexController controller;

	public ProvisionalTermRequestPanel(String id, PhenexController controller) {
		super(id);
		this.controller = controller;
	}

	public ProvisionalTermRequestPanel(PhenexController controller) {
		this("", controller);
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();    
	}

	public ORBTerm getTerm() {
		final ORBTerm term = new ORBTerm();
		term.setLabel(StringUtils.stripToNull(this.preferredLabelField.getText()));
		term.setParent((OBOClass)(this.parentBox.getValue()));
		term.setDefinition(StringUtils.stripToNull(this.definitionField.getText() + "\n\nRequested by: " + this.contactInfoField.getText()));
		for (Synonym synonym : this.synonyms) {
			final String text = StringUtils.stripToNull(synonym.getLabel());
			if (text != null) {
				term.addSynonym(synonym);
			}
		}
		return term;
	}

	public void addSynonym() {
		final Synonym newSynonym = new Synonym();
		this.synonyms.add(newSynonym);
		final int index = this.synonyms.indexOf(newSynonym);
		this.selectionModel.setSelectionInterval(index, index);
	}

	public void deleteSelectedSynonym() {
		this.selectionModel.getSelected().clear();
	}

	private void initializeInterface() {
		this.setLayout(new GridBagLayout());
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.EAST;
		this.add(new JLabel("Preferred name:"), constraints);
		constraints.gridx += 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1.0;
		this.preferredLabelField = new TabActionTextField();
		this.preferredLabelField.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO enable/disable OK button?
			}
		});
		this.add(preferredLabelField, constraints);
		constraints.gridx = 0;
		constraints.gridy += 1;
		constraints.weightx = 0;
		constraints.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Parent:"), constraints);
		constraints.gridx += 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1.0;
		this.parentBox = TermAutocompleteFieldFactory.createAutocompleteBox(this.controller.getOntologyController().getAllTermsSet(), this.controller.getOntologyCoordinator());
		this.add(this.parentBox, constraints);
		this.definitionField = new JTextArea();
	    this.definitionField.setLineWrap(true);
	    this.definitionField.setWrapStyleWord(true);
	    this.definitionField.setRows(3);
	    constraints.gridx = 0;
	    constraints.gridy += 1;
		constraints.weightx = 0;
	    constraints.fill = GridBagConstraints.NONE;
	    this.add(new JLabel("Definition:"), constraints);
	    constraints.gridx += 1;
	    constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1.0;
		this.add(new JScrollPane(this.definitionField), constraints);
		this.tableFormat = new SynonymsTableFormat();
		final EventTableModel<Synonym> model = new EventTableModel<Synonym>(this.synonyms, this.tableFormat);
		this.synonymsTable = new BugWorkaroundTable(model);
		this.synonymsTable.setSelectionModel(this.selectionModel);
		this.synonymsTable.setDefaultRenderer(OBOObject.class, new TermRenderer("None"));
		this.synonymsTable.putClientProperty("Quaqua.Table.style", "striped");
		this.synonymsTable.setPreferredScrollableViewportSize(new Dimension(300, 100));
		constraints.gridy += 1;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 1.0;
		this.add(new JScrollPane(synonymsTable), constraints);
		constraints.gridy += 1;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1.0;
		this.add(this.createToolBar(), constraints);
		constraints.gridy += 1;
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Contact email:"), constraints);
	    constraints.gridx += 1;
	    constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1.0;
		this.contactInfoField = new TabActionTextField();
		this.add(this.contactInfoField, constraints);
	}

	private JToolBar createToolBar() {
		final JToolBar toolBar = new JToolBar();
		this.addSynonymButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-add.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				addSynonym();
			}
		});
		this.addSynonymButton.setToolTipText("Add Synonym");
		toolBar.add(this.addSynonymButton);
		this.deleteSynonymButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-remove.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedSynonym();
			}
		});
		this.deleteSynonymButton.setToolTipText("Delete Synonym");
		toolBar.add(this.deleteSynonymButton);
		toolBar.setFloatable(false);
		return toolBar;
	}

	private class SynonymsTableFormat implements WritableTableFormat<Synonym>, AdvancedTableFormat<Synonym> {
		
		@Override
		public boolean isEditable(Synonym synonym, int column) {
			return true;
		}

		@Override
		public Synonym setColumnValue(Synonym synonym, Object editedValue, int column) {
			synonym.setLabel(editedValue.toString());
			return synonym;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public String getColumnName(int column) {
			return "Synonym";
		}

		@Override
		public Object getColumnValue(Synonym synonym, int column) {
			return synonym.getLabel();
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return String.class;
		}

		@Override
		public Comparator<?> getColumnComparator(int column) {
			return GlazedLists.comparableComparator();
		}

	}

	@SuppressWarnings("unused")
	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
