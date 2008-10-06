package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.Comparator;

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

public class TaxonTableComponent extends PhenoscapeGUIComponent {

  private JButton addTaxonButton;
  private JButton deleteTaxonButton;

  public TaxonTableComponent(String id, PhenoscapeController controller) {
    super(id, controller);
  }

  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }
  
  private void addTaxon() {
    this.getController().getDataSet().newTaxon();
  }
  
  private void deleteSelectedTaxon() {
    final Taxon taxon = this.getSelectedTaxon();
    if (taxon != null) { this.getController().getDataSet().removeTaxon(taxon); }
  }
  
  private Taxon getSelectedTaxon() {
    final EventList<Taxon> selected = this.getController().getTaxaSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
    }
  }
  
  private void updateButtonStates() {
    this.deleteTaxonButton.setEnabled(this.getSelectedTaxon() != null);
  }

  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    final EventTableModel<Taxon> taxaTableModel = new EventTableModel<Taxon>(this.getController().getSortedTaxa(), new TaxaTableFormat());
    final JTable taxaTable = new BugWorkaroundTable(taxaTableModel);
    taxaTable.setSelectionModel(this.getController().getTaxaSelectionModel());
    taxaTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
    taxaTable.setDefaultRenderer(OBOObject.class, new TermRenderer("None"));
    taxaTable.getColumnModel().getColumn(0).setCellEditor(this.createAutocompleteEditor(this.getController().getOntologyController().getTaxonTermSet().getTerms()));
    taxaTable.putClientProperty("Quaqua.Table.style", "striped");
    new TableColumnPrefsSaver(taxaTable, this.getClass().getName());
    final TableComparatorChooser<Taxon> sortChooser = new TableComparatorChooser<Taxon>(taxaTable, this.getController().getSortedTaxa(), false);
    sortChooser.addSortActionListener(new SortDisabler());
    this.add(new JScrollPane(taxaTable), BorderLayout.CENTER);
    this.add(this.createToolBar(), BorderLayout.NORTH);
    this.getController().getTaxaSelectionModel().addListSelectionListener(new TaxonSelectionListener());
  }
  
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar();
    try {
      this.addTaxonButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
          public void actionPerformed(ActionEvent e) {
            addTaxon();
          }
        });
      this.addTaxonButton.setToolTipText("Add Taxon");
      toolBar.add(this.addTaxonButton);
      this.deleteTaxonButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
          public void actionPerformed(ActionEvent e) {
            deleteSelectedTaxon();
          }
        });
      this.deleteTaxonButton.setToolTipText("Delete Taxon");
      toolBar.add(this.deleteTaxonButton);
    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    toolBar.setFloatable(false);
    return toolBar;
  }

  private class TaxaTableFormat implements WritableTableFormat<Taxon>, AdvancedTableFormat<Taxon> {

    public boolean isEditable(Taxon taxon, int column) {
      return true;
    }

    public Taxon setColumnValue(Taxon taxon, Object editedValue, int column) {
      switch (column) {
      case 0: taxon.setValidName((OBOClass)editedValue); break;
      case 1: taxon.setPublicationName(editedValue.toString()); break;
      case 2: taxon.setComment(editedValue.toString()); break;
      }
      return taxon;
    }

    public int getColumnCount() {
      return 3;
    }

    public String getColumnName(int column) {
      switch (column) {
      case 0: return "Valid Taxon";
      case 1: return "Publication Taxon";
      case 2: return "Comment";
      default: return null;
      }
    }

    public Object getColumnValue(Taxon taxon, int column) {
      switch (column) {
      case 0: return taxon.getValidName();
      case 1: return taxon.getPublicationName();
      case 2: return taxon.getComment();
      default: return null;
      }
    }

    public Class<?> getColumnClass(int column) {
      switch (column) {
      case 0: return OBOObject.class;
      case 1: return String.class;
      case 2: return String.class;
      default: return null;
      }
    }

    public Comparator<?> getColumnComparator(int column) {
      switch (column) {
      case 0: return GlazedLists.comparableComparator();
      case 1: return Strings.getNaturalComparator();
      case 2: return Strings.getNaturalComparator();
      default: return null;
      }
    }
    
  }
  
  private class TaxonSelectionListener implements ListSelectionListener {
    
    public TaxonSelectionListener() {
      updateButtonStates();
    }

    public void valueChanged(ListSelectionEvent e) {
      updateButtonStates();
    }
    
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
