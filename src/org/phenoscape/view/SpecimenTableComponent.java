package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.obo.annotation.view.TermAutocompleteFieldFactory;
import org.obo.annotation.view.TermRenderer;
import org.obo.app.swing.BugWorkaroundTable;
import org.obo.app.swing.PlaceholderRenderer;
import org.obo.app.swing.SortDisabler;
import org.obo.app.swing.TableColumnPrefsSaver;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.Taxon;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.eekboom.utils.Strings;

public class SpecimenTableComponent extends PhenoscapeGUIComponent {

	private JButton addSpecimenButton;
	private JButton duplicateSpecimenButton;
	private JButton deleteSpecimenButton;

	public SpecimenTableComponent(String id, PhenexController controller) {
		super(id, controller);
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();
	}

	private void addSpecimen() {
		final Taxon taxon = this.getSelectedTaxon();
		if (taxon != null) { taxon.newSpecimen(); }
	}

	private void duplicateSelectedSpecimen() {
		final Taxon taxon = this.getSelectedTaxon();
		if (taxon != null) {
			final Specimen specimen = this.getSelectedSpecimen();
			if (specimen != null) {
				taxon.addSpecimen(new Specimen(specimen));
			}
		}
	}

	private void deleteSelectedSpecimen() {
		final Taxon taxon = this.getSelectedTaxon();
		if (taxon != null) {
			final Specimen specimen = this.getSelectedSpecimen();
			if (specimen != null) { taxon.removeSpecimen(specimen); }
		}
	}

	private Taxon getSelectedTaxon() {
		final EventList<Taxon> selected = this.getController().getTaxaSelectionModel().getSelected();
		if (selected.size() == 1) {
			return selected.get(0);
		} else {
			return null;
		}
	}

	private Specimen getSelectedSpecimen() {
		final EventList<Specimen> selected = this.getController().getCurrentSpecimensSelectionModel().getSelected();
		if (selected.size() == 1) {
			return selected.get(0);
		} else {
			return null;
		}
	}

	private void taxonSelectionDidChange() {
		final String unselectedTitle = "Specimens";
		final String selectedPrefix = "Specimens for Taxon: ";
		final List<Taxon> taxa = this.getController().getTaxaSelectionModel().getSelected();
		if (taxa.isEmpty()) {
			this.updatePanelTitle(unselectedTitle);
		} else {
			final Taxon taxon = taxa.get(0);
			this.updatePanelTitle(selectedPrefix + taxon);
		}
		this.updateButtonStates();
	}

	private void specimenSelectionDidChange() {
		this.updateButtonStates();
	}

	private void updateButtonStates() {
		this.addSpecimenButton.setEnabled((this.getSelectedTaxon() != null));
		this.duplicateSpecimenButton.setEnabled((this.getSelectedSpecimen() != null) && (this.getSelectedTaxon() != null));
		this.deleteSpecimenButton.setEnabled((this.getSelectedSpecimen() != null) && (this.getSelectedTaxon() != null));
	}

	private void initializeInterface() {
		this.setLayout(new BorderLayout());
		final EventTableModel<Specimen> specimensTableModel = new EventTableModel<Specimen>(this.getController().getSpecimensForCurrentTaxonSelection(), new SpecimensTableFormat());
		final JTable specimensTable = new BugWorkaroundTable(specimensTableModel);
		specimensTable.setSelectionModel(this.getController().getCurrentSpecimensSelectionModel());
		specimensTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
		specimensTable.setDefaultRenderer(OBOObject.class, new TermRenderer("None"));
		specimensTable.getColumnModel().getColumn(0).setCellEditor(TermAutocompleteFieldFactory.createAutocompleteEditor(this.getController().getOntologyController().getCollectionTermSet().getTerms(), getController().getOntologyCoordinator()));
		specimensTable.putClientProperty("Quaqua.Table.style", "striped");
		new TableColumnPrefsSaver(specimensTable, this.getClass().getName());
		final TableComparatorChooser<Specimen> sortChooser = new TableComparatorChooser<Specimen>(specimensTable, this.getController().getSpecimensForCurrentTaxonSelection(), false);
		sortChooser.addSortActionListener(new SortDisabler());
		this.add(new JScrollPane(specimensTable), BorderLayout.CENTER);
		this.add(this.createToolBar(), BorderLayout.NORTH);
		this.getController().getTaxaSelectionModel().addListSelectionListener(new TaxonSelectionListener());
		this.getController().getCurrentSpecimensSelectionModel().addListSelectionListener(new SpecimenSelectionListener());
	}

	private JToolBar createToolBar() {
		final JToolBar toolBar = new JToolBar();
		this.addSpecimenButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-add.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				addSpecimen();
			}
		});
		this.addSpecimenButton.setToolTipText("Add Specimen");
		toolBar.add(this.addSpecimenButton);

		this.duplicateSpecimenButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-duplicate.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				duplicateSelectedSpecimen();
			}
		});
		this.duplicateSpecimenButton.setToolTipText("Duplicate Specimen");
		toolBar.add(this.duplicateSpecimenButton);

		this.deleteSpecimenButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-remove.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedSpecimen();
			}
		});
		this.deleteSpecimenButton.setToolTipText("Delete Specimen");
		toolBar.add(this.deleteSpecimenButton);
		toolBar.setFloatable(false);
		return toolBar;
	}

	private class SpecimensTableFormat implements WritableTableFormat<Specimen>, AdvancedTableFormat<Specimen> {

		@Override
		public boolean isEditable(Specimen specimen, int column) {
			return true;
		}

		@Override
		public Specimen setColumnValue(Specimen specimen, Object editedValue, int column) {
			switch(column) {
			case 0: specimen.setCollectionCode((OBOClass)editedValue); break;
			case 1: specimen.setCatalogID(StringUtils.stripToNull(editedValue.toString())); break;
			case 2: specimen.setComment(StringUtils.stripToNull(editedValue.toString())); break;
			}
			return specimen;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int column) {
			switch(column) {
			case 0: return "Collection";
			case 1: return "Catalog ID";
			case 2: return "Comment";
			default: return null;
			}
		}

		@Override
		public Object getColumnValue(Specimen specimen, int column) {
			switch(column) {
			case 0: return specimen.getCollectionCode();
			case 1: return specimen.getCatalogID();
			case 2: return specimen.getComment();
			default: return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch(column) {
			case 0: return OBOObject.class;
			case 1: return String.class;
			case 2: return String.class;
			default: return null;
			}
		}

		@Override
		public Comparator<?> getColumnComparator(int column) {
			switch(column) {
			case 0: return GlazedLists.comparableComparator();
			case 1: return Strings.getNaturalComparator();
			case 2: return Strings.getNaturalComparator();
			default: return null;
			}
		}

	}

	private class TaxonSelectionListener implements ListSelectionListener {

		public TaxonSelectionListener() {
			taxonSelectionDidChange();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			taxonSelectionDidChange();      
		}

	}

	private class SpecimenSelectionListener implements ListSelectionListener {

		public SpecimenSelectionListener() {
			specimenSelectionDidChange();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			specimenSelectionDidChange();      
		}

	}

}
