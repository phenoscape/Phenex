package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
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

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.model.Specimen;
import org.phenoscape.model.Taxon;
import org.phenoscape.swing.PlaceholderRenderer;

import phenote.gui.BugWorkaroundTable;
import phenote.gui.SortDisabler;
import phenote.gui.TableColumnPrefsSaver;
import phenote.util.FileUtil;
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

  public SpecimenTableComponent(String id, PhenoscapeController controller) {
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
    this.addSpecimenButton.setEnabled(this.getSelectedTaxon() != null);
    this.deleteSpecimenButton.setEnabled(this.getSelectedSpecimen() != null);
  }
  
  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    final EventTableModel<Specimen> specimensTableModel = new EventTableModel<Specimen>(this.getController().getSpecimensForCurrentTaxonSelection(), new SpecimensTableFormat());
    final JTable specimensTable = new BugWorkaroundTable(specimensTableModel);
    specimensTable.setSelectionModel(this.getController().getCurrentSpecimensSelectionModel());
    specimensTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
    specimensTable.setDefaultRenderer(OBOObject.class, new TermRenderer("None"));
    specimensTable.getColumnModel().getColumn(0).setCellEditor(this.createAutocompleteEditor(this.getController().getOntologyController().getCollectionTermSet().getTerms()));
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
    try {
      this.addSpecimenButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
        public void actionPerformed(ActionEvent e) {
          addSpecimen();
        }
      });
      this.addSpecimenButton.setToolTipText("Add Specimen");
      toolBar.add(this.addSpecimenButton);
      
      this.duplicateSpecimenButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-duplicate.png"))) {
        public void actionPerformed(ActionEvent e) {
          duplicateSelectedSpecimen();
        }
      });
      this.duplicateSpecimenButton.setToolTipText("Duplicate Specimen");
      toolBar.add(this.duplicateSpecimenButton);
      
      this.deleteSpecimenButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
        public void actionPerformed(ActionEvent e) {
          deleteSelectedSpecimen();
        }
      });
      this.deleteSpecimenButton.setToolTipText("Delete Specimen");
      toolBar.add(this.deleteSpecimenButton);
    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    toolBar.setFloatable(false);
    return toolBar;
  }
  
  private class SpecimensTableFormat implements WritableTableFormat<Specimen>, AdvancedTableFormat<Specimen> {

    public boolean isEditable(Specimen specimen, int column) {
      return true;
    }

    public Specimen setColumnValue(Specimen specimen, Object editedValue, int column) {
      switch(column) {
      case 0: specimen.setCollectionCode((OBOClass)editedValue); break;
      case 1: specimen.setCatalogID(editedValue.toString()); break;
      }
      updateObjectForGlazedLists(specimen, getController().getSpecimensForCurrentTaxonSelection());
      return specimen;
    }

    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int column) {
      switch(column) {
      case 0: return "Collection";
      case 1: return "Catalog ID";
      default: return null;
      }
    }

    public Object getColumnValue(Specimen specimen, int column) {
      switch(column) {
      case 0: return specimen.getCollectionCode();
      case 1: return specimen.getCatalogID();
      default: return null;
      }
    }

    public Class<?> getColumnClass(int column) {
      switch(column) {
      case 0: return OBOObject.class;
      case 1: return String.class;
      default: return null;
      }
    }

    public Comparator<?> getColumnComparator(int column) {
      switch(column) {
      case 0: return GlazedLists.comparableComparator();
      case 1: return Strings.getNaturalComparator();
      default: return null;
      }
    }
    
  }
  
  private class TaxonSelectionListener implements ListSelectionListener {
    
    public TaxonSelectionListener() {
      taxonSelectionDidChange();
    }

    public void valueChanged(ListSelectionEvent e) {
      taxonSelectionDidChange();      
    }
    
  }
  
 private class SpecimenSelectionListener implements ListSelectionListener {
    
    public SpecimenSelectionListener() {
      specimenSelectionDidChange();
    }

    public void valueChanged(ListSelectionEvent e) {
      specimenSelectionDidChange();      
    }
    
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
