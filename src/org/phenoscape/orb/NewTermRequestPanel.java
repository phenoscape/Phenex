package org.phenoscape.orb;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.obo.annotation.view.OntologyCoordinator;
import org.obo.annotation.view.TermAutocompleteFieldFactory;
import org.obo.annotation.view.TermRenderer;
import org.obo.app.swing.AutocompleteField;
import org.obo.app.swing.BugWorkaroundTable;
import org.obo.app.swing.TabActionTextField;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.phenoscape.controller.PhenexController;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

public class NewTermRequestPanel extends AbstractGUIComponent {

	private JTextField preferredLabelField;
	private AutocompleteField<OBOObject> parentBox;
	private EventList<Synonym> synonyms = new BasicEventList<Synonym>();
	private EventSelectionModel<Synonym> selectionModel = new EventSelectionModel<Synonym>(synonyms);
	{
		selectionModel.setSelectionMode(EventSelectionModel.SINGLE_SELECTION);
	}
	private SynonymsTableFormat tableFormat;
	private JTable synonymsTable;
	private JButton addLinkButton;
	private JButton deleteLinkButton;
	private final PhenexController controller;
	private final ORBTerm orbTerm = new ORBTerm();

	public NewTermRequestPanel(String id, PhenexController controller) {
		super(id);
		this.controller = controller;
	}

	public NewTermRequestPanel(PhenexController controller) {
		this("", controller);
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();    
	}

	public ORBTerm getTerm() {
		return this.orbTerm;
	}

	public void addSynonym() {
		final Synonym newSynonym = new Synonym();
		this.synonyms.add(newSynonym);
		final int index = this.synonyms.indexOf(newSynonym);
		this.selectionModel.setSelectionInterval(index, index);
	}

	public void deleteSelectedLink() {
		this.selectionModel.getSelected().clear();
	}
	
	private void updateTermLabel() {
		this.orbTerm.setLabel(this.preferredLabelField.getText());
	}

	private void updateParent() {
		this.orbTerm.setParent((OBOClass)(this.parentBox.getValue()));
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
				updateTermLabel();
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
		this.parentBox.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateParent();
			}
		});
		this.add(this.parentBox, constraints);
		this.tableFormat = new SynonymsTableFormat();
		final EventTableModel<Synonym> model = new EventTableModel<Synonym>(this.synonyms, this.tableFormat);
		this.synonymsTable = new BugWorkaroundTable(model);
		this.synonymsTable.setSelectionModel(this.selectionModel);
		this.synonymsTable.setDefaultRenderer(OBOObject.class, new TermRenderer("None"));
		this.synonymsTable.putClientProperty("Quaqua.Table.style", "striped");
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
	}

	private JToolBar createToolBar() {
		final JToolBar toolBar = new JToolBar();
		this.addLinkButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-add.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				addSynonym();
			}
		});
		this.addLinkButton.setToolTipText("Add Differentia");
		toolBar.add(this.addLinkButton);
		this.deleteLinkButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-remove.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedLink();
			}
		});
		this.deleteLinkButton.setToolTipText("Delete Differentia");
		toolBar.add(this.deleteLinkButton);
		toolBar.setFloatable(false);
		return toolBar;
	}

	private class SynonymsTableFormat implements WritableTableFormat<Synonym>, AdvancedTableFormat<Synonym> {

		public TableCellEditor getColumnEditor(int column) {
			return new DefaultCellEditor(new JTextField());
		}
		
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

	@SuppressWarnings("unchecked")
	private Collection<OBOObject> toOBOObjects(Collection<?> terms) {
		return (Collection<OBOObject>)terms;
	}

	private class Synonym implements Comparable<Synonym> {

		private String label;

		public String getLabel() {
			return this.label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		@Override
		public int compareTo(Synonym other) {
			return this.getLabel().compareTo(other.getLabel());
		}

	}

	@SuppressWarnings("unused")
	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
