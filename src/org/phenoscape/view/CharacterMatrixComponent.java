package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.apache.log4j.Logger;
import org.phenoscape.model.Character;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;
import org.phenoscape.swing.PlaceholderRenderer;

import phenote.gui.BugWorkaroundTable;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

public class CharacterMatrixComponent extends PhenoscapeGUIComponent {

  private EventTableModel<Taxon> headerModel;
  private EventTableModel<Taxon> matrixTableModel;
  private final EventList<State> allStates;
  private static enum TaxonDisplay {
    PUBLICATION_NAME { public String toString() { return "Display Publication Name"; }},
    VALID_NAME { public String toString() { return "Display Valid Name"; }}
  }
  private static enum CharacterDisplay {
    CHARACTER_NUMBER { public String toString() { return "Display Character Number"; }},
    CHARACTER_DESCRIPTION { public String toString() { return "Display Character Description"; }}
  }
  private static enum StateDisplay {
    STATE_SYMBOL { public String toString() { return "Display State Symbol"; }},
    STATE_DESCRIPTION { public String toString() { return "Display State Description"; }}
  }
  private TaxonDisplay taxonOption = TaxonDisplay.VALID_NAME;
  private CharacterDisplay characterOption = CharacterDisplay.CHARACTER_NUMBER;
  private StateDisplay stateOption = StateDisplay.STATE_SYMBOL;

  public CharacterMatrixComponent(String id, PhenoscapeController controller) {
    super(id, controller);
    this.allStates = new CollectionList<Character, State>(this.getController().getDataSet().getCharacters(),
        new CollectionList.Model<Character, State>() {
      public List<State> getChildren(Character parent) {
        return parent.getStates();
      }
    } 
    );
  }

  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }

  private void initializeInterface() {
    this.setLayout(new BorderLayout());
    this.headerModel = new EventTableModel<Taxon>(this.getController().getDataSet().getTaxa(), new HeaderTableFormat());
    final JTable headerTable = new BugWorkaroundTable(this.headerModel);
    headerTable.putClientProperty("Quaqua.Table.style", "striped");
    headerTable.setDefaultRenderer(Taxon.class, new TaxonRenderer());
    this.matrixTableModel = new EventTableModel<Taxon>(this.getController().getDataSet().getTaxa(), new MatrixTableFormat());
    final JTable matrixTable = new BugWorkaroundTable(this.matrixTableModel);
    matrixTable.setCellSelectionEnabled(true);
    matrixTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
    matrixTable.setDefaultRenderer(State.class, new StateCellRenderer());
    final JComboBox statesBox = new JComboBox();
    statesBox.setRenderer(new StateListRenderer());
    final StateCellEditor editor = new StateCellEditor(statesBox);
    editor.setClickCountToStart(2);
    matrixTable.setDefaultEditor(State.class, editor);
    matrixTable.putClientProperty("Quaqua.Table.style", "striped");
    matrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    this.getController().getDataSet().getCharacters().addListEventListener(new ListEventListener<Character>() {
      public void listChanged(ListEvent<Character> listChanges) {
        matrixTableModel.fireTableStructureChanged();
      }
    });
    this.allStates.addListEventListener(new ListEventListener<State>() {
      public void listChanged(ListEvent<State> listChanges) {
        matrixTableModel.fireTableDataChanged();
      }
    });
    final JScrollPane headerScroller = new JScrollPane(headerTable);
    final JScrollPane matrixScroller = new JScrollPane(matrixTable);
    headerScroller.getVerticalScrollBar().setModel(matrixScroller.getVerticalScrollBar().getModel());
    headerScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    headerScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    matrixScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, headerScroller, matrixScroller);
    splitPane.setDividerLocation(150);
    splitPane.setDividerSize(3);
    this.add(splitPane, BorderLayout.CENTER);
    this.add(this.createToolBar(), BorderLayout.SOUTH);
  }
  
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar();
    final JComboBox taxonBox = new JComboBox(TaxonDisplay.values());
    taxonBox.setSelectedItem(this.taxonOption);
    taxonBox.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        taxonOption = (TaxonDisplay)(taxonBox.getSelectedItem());
        headerModel.fireTableDataChanged();
      }
    });
    final JComboBox characterBox = new JComboBox(CharacterDisplay.values());
    characterBox.setSelectedItem(this.characterOption);
    characterBox.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        characterOption = (CharacterDisplay)(characterBox.getSelectedItem());
        matrixTableModel.fireTableStructureChanged();
      }
    });
    final JComboBox stateBox = new JComboBox(StateDisplay.values());
    stateBox.setSelectedItem(this.stateOption);
    stateBox.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        stateOption = (StateDisplay)(stateBox.getSelectedItem());
        matrixTableModel.fireTableDataChanged();
      }
    });
    toolBar.add(taxonBox);
    toolBar.add(characterBox);
    toolBar.add(stateBox);
    toolBar.setFloatable(false);
    return toolBar;
  }
  
  private class HeaderTableFormat implements AdvancedTableFormat<Taxon> {

    public Class<?> getColumnClass(int column) {
      return Taxon.class;
    }

    public Comparator<?> getColumnComparator(int column) {
      return null;
    }

    public int getColumnCount() {
      return 1;
    }

    public String getColumnName(int column) {
      return "Taxon";
    }

    public Object getColumnValue(Taxon taxon, int column) {
      return taxon;
    }
    
  }
  
  private class MatrixTableFormat implements AdvancedTableFormat<Taxon>, WritableTableFormat<Taxon> {

    public int getColumnCount() {
      return getController().getDataSet().getCharacters().size();
    }

    public Class<?> getColumnClass(int column) {
      return State.class;
    }

    public Comparator<?> getColumnComparator(int column) {
      return null;
    }

    public String getColumnName(int column) {
      if (characterOption.equals(CharacterDisplay.CHARACTER_DESCRIPTION)) {
        return this.getCharacter(column).toString();
      } else if (characterOption.equals(CharacterDisplay.CHARACTER_NUMBER)) {
        return "" + (column + 1);
      }
      return null;
    }

    public Object getColumnValue(Taxon taxon, int column) {
      return getController().getDataSet().getStateForTaxon(taxon, this.getCharacter(column));
    }

    public boolean isEditable(Taxon baseObject, int column) {
      return true;
    }

    public Taxon setColumnValue(Taxon taxon, Object editedValue, int column) {
      getController().getDataSet().setStateForTaxon(taxon, this.getCharacter(column), (State)editedValue);
      return taxon;
    }
    
    private Character getCharacter(int index) {
      return getController().getDataSet().getCharacters().get(index);
    }
    
  }
  
  private class StateCellRenderer extends PlaceholderRenderer {

    public StateCellRenderer() {
      super("?");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      final State state = (State)value;
      if (state != null) {
        final Object newValue;
        if (stateOption.equals(StateDisplay.STATE_SYMBOL)) {
          newValue = state.getSymbol() != null ? state.getSymbol() : "#";
        }  else {
          newValue = state.getLabel() != null ? state.getLabel() : "untitled";
        }
        return super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
      } else {
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }

    }

  }
  
  private class StateListRenderer extends BasicComboBoxRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      final Object newValue = value != null ? value : "?";
      return super.getListCellRendererComponent(list, newValue, index, isSelected, cellHasFocus);
    }
    
  }
  
  private class StateCellEditor extends DefaultCellEditor {
    
    private final DefaultComboBoxModel model = new DefaultComboBoxModel();

    public StateCellEditor(JComboBox comboBox) {
      super(comboBox);
      comboBox.setModel(model);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      final Character character = getController().getDataSet().getCharacters().get(column);
      this.model.removeAllElements();
      this.model.addElement(null);
      for (State state : character.getStates()) {
        this.model.addElement(state);
      }
      return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

  }
  
  private class TaxonRenderer extends TermRenderer {

    public TaxonRenderer() {
      super("untitled");
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if (taxonOption.equals(TaxonDisplay.VALID_NAME)) {
        final Object newValue = value != null ? ((Taxon)value).getValidName() : value;
        return super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
      } else {
        final Object newValue = value != null ? ((Taxon)value).getPublicationName() : value;
        return super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
      }
    }
    
  }
  
  @SuppressWarnings("unused")
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
