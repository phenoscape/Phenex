package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.State;
import org.phenoscape.model.TermSet;
import org.phenoscape.swing.PlaceholderRenderer;
import org.phenoscape.swing.PopupListener;

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

public class PhenotypeTableComponent extends PhenoscapeGUIComponent {

  private JButton addPhenotypeButton;
  private JButton deletePhenotypeButton;
  private PopupListener tablePopup;
  private PhenotypesTableFormat tableFormat;
  private JTable phenotypesTable;
  
  public PhenotypeTableComponent(String id, PhenoscapeController controller) {
    super(id, controller);
  }
  
  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }

  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    this.tableFormat = new PhenotypesTableFormat();
    final EventTableModel<Phenotype> phenotypesTableModel = new EventTableModel<Phenotype>(this.getController().getPhenotypesForCurrentStateSelection(), this.tableFormat);
    this.phenotypesTable = new BugWorkaroundTable(phenotypesTableModel);
    this.phenotypesTable.setSelectionModel(this.getController().getCurrentPhenotypesSelectionModel());
    this.phenotypesTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
    this.phenotypesTable.setDefaultRenderer(OBOObject.class, new TermRenderer("None"));
    for (int i = 0; i < this.phenotypesTable.getColumnCount(); i++) {
      final TableCellEditor editor = this.tableFormat.getColumnEditor(i);
      if (editor != null) { this.phenotypesTable.getColumnModel().getColumn(i).setCellEditor(editor); }
    }
    this.phenotypesTable.putClientProperty("Quaqua.Table.style", "striped");
    new TableColumnPrefsSaver(this.phenotypesTable, this.getClass().getName());
    this.tablePopup = new PopupListener(this.createTablePopupMenu());
    this.phenotypesTable.addMouseListener(this.tablePopup);
    final TableComparatorChooser<Phenotype> sortChooser = new TableComparatorChooser<Phenotype>(this.phenotypesTable, this.getController().getPhenotypesForCurrentStateSelection(), false);
    sortChooser.addSortActionListener(new SortDisabler());
    this.add(new JScrollPane(this.phenotypesTable), BorderLayout.CENTER);
    this.add(this.createToolBar(), BorderLayout.NORTH);
    this.getController().getCurrentStatesSelectionModel().addListSelectionListener(new StateSelectionListener());
    this.getController().getCurrentPhenotypesSelectionModel().addListSelectionListener(new PhenotypeSelectionListener());
  }
  
  private void addPhenotype() {
    final State state = this.getSelectedState();
    if (state != null) {
      final Phenotype phenotype = state.newPhenotype();
      phenotype.setEntity(this.getAutofillEntity());
    }
  }
  
  private OBOClass getAutofillEntity() {
    for (State state : this.getController().getStatesForCurrentCharacterSelection()) {
      for (Phenotype phenotype : state.getPhenotypes()) {
        if (phenotype.getEntity() != null) {
          return phenotype.getEntity();
        }
      }
    }
    return null;
  }
  
  private void deleteSelectedPhenotype() {
    final State state = this.getSelectedState();
    if (state != null) {
      final Phenotype phenotype = this.getSelectedPhenotype();
      if (phenotype != null) { state.removePhenotype(phenotype); }
    }
  }
  
  private State getSelectedState() {
    final EventList<State> selected = this.getController().getCurrentStatesSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
    }
  }
  
  private Phenotype getSelectedPhenotype() {
    final EventList<Phenotype> selected = this.getController().getCurrentPhenotypesSelectionModel().getSelected();
    if (selected.size() == 1) {
      return selected.get(0);
    } else {
      return null;
    }
  }
  
  private void stateSelectionDidChange() {
    final String unselectedTitle = "Phenotypes";
    final String selectedPrefix = "Phenotypes for State: ";
    final List<State> states = this.getController().getCurrentStatesSelectionModel().getSelected();
    if (states.isEmpty()) {
      this.updatePanelTitle(unselectedTitle);
    } else {
      this.updatePanelTitle(selectedPrefix + states.get(0));
    }
    this.updateButtonStates();
  }
  
  private void phenotypeSelectionDidChange() {
    this.updateButtonStates();
  }
  
  private void updateButtonStates() {
    this.addPhenotypeButton.setEnabled(this.getSelectedState() != null);
    this.deletePhenotypeButton.setEnabled(this.getSelectedPhenotype() != null);
  }
  
  private void runPostCompositionForTermAtPoint(Point p) {
    final int column = this.phenotypesTable.getTableHeader().columnAtPoint(p);
    final int row = this.phenotypesTable.rowAtPoint(p);
    if (!this.tableFormat.getColumnClass(column).equals(OBOObject.class)) return;
    final Phenotype phenotype = this.getController().getPhenotypesForCurrentStateSelection().get(row);
    final OBOClass term = (OBOClass)(this.tableFormat.getColumnValue(phenotype, column));
    final PostCompositionEditor pce = new PostCompositionEditor(this.getController(), this.tableFormat.getColumnTermSet(column));
    pce.setTerm(term);
    final int result = pce.runPostCompositionDialog();
    if (result == JOptionPane.OK_OPTION) {
      this.tableFormat.setColumnValue(phenotype, pce.getTerm(), column);
    }
  }

  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar();
    try {
      this.addPhenotypeButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
          public void actionPerformed(ActionEvent e) {
            addPhenotype();
          }
        });
      this.addPhenotypeButton.setToolTipText("Add Phenotype");
      toolBar.add(this.addPhenotypeButton);
      this.deletePhenotypeButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
          public void actionPerformed(ActionEvent e) {
            deleteSelectedPhenotype();
          }
        });
      this.deletePhenotypeButton.setToolTipText("Delete Phenotype");
      toolBar.add(this.deletePhenotypeButton);
    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    toolBar.setFloatable(false);
    return toolBar;
  }
  
  private JPopupMenu createTablePopupMenu() {
    final JPopupMenu menu = new JPopupMenu();
    menu.add(new AbstractAction("Edit Post-composed Term") {
      public void actionPerformed(ActionEvent e) {
        runPostCompositionForTermAtPoint(tablePopup.getLocation());
      }
    });
    return menu;
  }
  
  private class PhenotypesTableFormat implements WritableTableFormat<Phenotype>, AdvancedTableFormat<Phenotype> {
    
    public boolean isEditable(Phenotype phenotype, int column) {
      return true;
    }

    public Phenotype setColumnValue(Phenotype phenotype, Object editedValue, int column) {
      switch (column) {
      case 0: phenotype.setEntity((OBOClass)editedValue); break;
      case 1: phenotype.setQuality((OBOClass)editedValue); break;
      case 2: phenotype.setRelatedEntity((OBOClass)editedValue); break;
      case 3: phenotype.setCount((Integer)editedValue); break;
      case 4: phenotype.setMeasurement((Float)editedValue); break;
      case 5: phenotype.setUnit((OBOClass)editedValue); break;
      case 6: phenotype.setComment(editedValue.toString()); break;
      }
      return phenotype;
    }

    public int getColumnCount() {
      return 7;
    }

    public String getColumnName(int column) {
      switch (column) {
      case 0: return "Entity";
      case 1: return "Quality";
      case 2: return "Related Entity";
      case 3: return "Count";
      case 4: return "Measurement";
      case 5: return "Unit";
      case 6: return "Comment";
      default: return null;
      }
    }
    
    public TermSet getColumnTermSet(int column) {
      switch (column) {
      case 0: return getController().getOntologyController().getEntityTermSet();
      case 1: return getController().getOntologyController().getQualityTermSet();
      case 2: return getController().getOntologyController().getRelatedEntityTermSet();
      case 3: return null;
      case 4: return null;
      case 5: return getController().getOntologyController().getUnitTermSet();
      case 6: return null;
      default: return null;
      }
    }
    
    public TableCellEditor getColumnEditor(int column) {
      switch (column) {
      case 0: return createAutocompleteEditor(this.getColumnTermSet(column).getTerms());
      case 1: return createAutocompleteEditor(this.getColumnTermSet(column).getTerms());
      case 2: return createAutocompleteEditor(this.getColumnTermSet(column).getTerms());
      case 3: return null;
      case 4: return null;
      case 5: return createAutocompleteEditor(this.getColumnTermSet(column).getTerms());
      case 6: return null;
      default: return null;
      }
    }

    public Object getColumnValue(Phenotype phenotype, int column) {
      switch (column) {
      case 0: return phenotype.getEntity();
      case 1: return phenotype.getQuality();
      case 2: return phenotype.getRelatedEntity();
      case 3: return phenotype.getCount();
      case 4: return phenotype.getMeasurement();
      case 5: return phenotype.getUnit();
      case 6: return phenotype.getComment();
      default: return null;
      }
    }

    public Class<?> getColumnClass(int column) {
      switch (column) {
      case 0: return OBOObject.class;
      case 1: return OBOObject.class;
      case 2: return OBOObject.class;
      case 3: return Integer.class;
      case 4: return Float.class;
      case 5: return OBOObject.class;
      case 6: return String.class;
      default: return null;
      }
    }

    public Comparator<?> getColumnComparator(int column) {
      switch (column) {
      case 0: return GlazedLists.comparableComparator();
      case 1: return GlazedLists.comparableComparator();
      case 2: return GlazedLists.comparableComparator();
      case 3: return GlazedLists.comparableComparator();
      case 4: return GlazedLists.comparableComparator();
      case 5: return GlazedLists.comparableComparator();
      case 6: return Strings.getNaturalComparator();
      default: return null;
      }
    }
    
  }
  
  private class StateSelectionListener implements ListSelectionListener {
    
    public StateSelectionListener() {
      stateSelectionDidChange();
    }

    public void valueChanged(ListSelectionEvent e) {
      stateSelectionDidChange();      
    }
    
  }
  
  private class PhenotypeSelectionListener implements ListSelectionListener {
    
    public PhenotypeSelectionListener() {
      phenotypeSelectionDidChange();
    }

    public void valueChanged(ListSelectionEvent e) {
      phenotypeSelectionDidChange();      
    }
    
  }
    
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
