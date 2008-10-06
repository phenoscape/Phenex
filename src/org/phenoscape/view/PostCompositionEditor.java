package org.phenoscape.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.model.TermSet;
import org.phenoscape.swing.TablePopupListener;

import phenote.datamodel.OboUtil;
import phenote.gui.BugWorkaroundTable;
import phenote.util.FileUtil;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

public class PostCompositionEditor extends PhenoscapeGUIComponent {
  
  private OBOClass genus;
  private EventList<Differentia> diffs = new BasicEventList<Differentia>();
  private EventSelectionModel<Differentia> selectionModel = new EventSelectionModel<Differentia>(diffs);
  private final TermSet termSet;
  private JComboBox genusBox;
  private JButton addDifferentiaButton;
  private JButton deleteDifferentiaButton;
  private JTable diffTable;
  private DifferentiaTableFormat tableFormat;
  private TablePopupListener popupListener;
  
  public PostCompositionEditor(String id, PhenoscapeController controller, TermSet terms) {
    super(id, controller);
    this.termSet = terms;
  }
  
  public PostCompositionEditor(PhenoscapeController controller, TermSet terms) {
    this("", controller, terms);
    this.initializeInterface();
  }
  
  @Override
  public void init() {
    super.init();
    this.initializeInterface();    
  }
  
  public void addDifferentia() {
    this.diffs.add(new Differentia());
  }
  
  public void deleteSelectedDifferentia() {
    this.selectionModel.getSelected().clear();
  }
  
  private void updateGenus() {
    this.genus = (OBOClass)(this.genusBox.getSelectedItem());
  }
  
  public OBOClass getTerm() {
    for (Differentia diff : this.diffs) {
      if (!diff.isComplete()) {
        this.diffs.remove(diff);
      }
    }
    if (this.diffs.isEmpty()) {
      return this.genus;
    } else {
      final OboUtil util = OboUtil.initPostCompTerm(this.genus);
      for (Differentia diff : this.diffs) {
        util.addRelDiff(diff.getRelation(), diff.getTerm());
      }
      return util.getPostCompTerm();
    }
  }
 
  public void setTerm(OBOClass aTerm) {
    this.diffs.clear();
    if ((aTerm != null) && (OboUtil.isPostCompTerm(aTerm))) {
      this.genus = OboUtil.getGenusTerm(aTerm);
      for(Link link : OboUtil.getAllDifferentia(aTerm)) {
        final Differentia diff = new Differentia();
        diff.setRelation(link.getType());
        final LinkedObject parent = link.getParent();
        if (parent instanceof OBOClass) {
          diff.setTerm((OBOClass)parent);
        } else {
          log().error("Differentia is not an OBOClass: " + parent);
        }
        this.diffs.add(diff);
      }
    } else {
      this.genus = aTerm;
    }
    this.genusBox.setSelectedItem((OBOObject)this.genus);
    //this.genusBox.getModel().setSelectedItem((OBOObject)this.genus);
    log().debug("Combo box has: " + this.genusBox.getSelectedItem());
  }

  public int runPostCompositionDialog() {
    if (this.genusBox == null) {
      this.init();
    }
    this.setPreferredSize(new Dimension(300, 200));
    return JOptionPane.showConfirmDialog(null, this, "Post-composition Editor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
  }
  
  private void runPostCompositionForGenus() {
    final PostCompositionEditor pce = new PostCompositionEditor(this.getController(), this.termSet);
    pce.setTerm(this.genus);
    final int result = pce.runPostCompositionDialog();
    if (result == JOptionPane.OK_OPTION) {
      this.genusBox.setSelectedItem(pce.getTerm());
    }
  }
  
  private void runPostCompositionForTermAtPoint(Point p) {
    final int column = this.diffTable.getTableHeader().columnAtPoint(p);
    final int row = this.diffTable.rowAtPoint(p);
    if (!this.tableFormat.getColumnClass(column).equals(OBOObject.class)) return;
    final Differentia differentia = this.diffs.get(row);
    final OBOClass term = (OBOClass)(this.tableFormat.getColumnValue(differentia, column));
    final PostCompositionEditor pce = new PostCompositionEditor(this.getController(), this.termSet);
    pce.setTerm(term);
    final int result = pce.runPostCompositionDialog();
    if (result == JOptionPane.OK_OPTION) {
      this.tableFormat.setColumnValue(differentia, pce.getTerm(), column);
    }
  }
  
  private void initializeInterface() {
    this.setLayout(new GridBagLayout());
    final GridBagConstraints labelConstraints = new GridBagConstraints();
    this.add(new JLabel("Genus:"), labelConstraints);
    final GridBagConstraints comboConstraints = new GridBagConstraints();
    comboConstraints.gridx = 1;
    comboConstraints.fill = GridBagConstraints.HORIZONTAL;
    comboConstraints.weightx = 1.0;
    this.genusBox = this.createAutocompleteBox(this.termSet.getTerms());
    this.genusBox.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        updateGenus();
      }
    });
    this.add(this.genusBox, comboConstraints);
    final GridBagConstraints postComposeGenusConstraints = new GridBagConstraints();
    postComposeGenusConstraints.gridx = 2;
    final JButton postComposeGenusButton = new JButton();
    postComposeGenusButton.setAction(new AbstractAction("PC...") {
      public void actionPerformed(ActionEvent e) {
        runPostCompositionForGenus();
      }
    });
    //this.add(postComposeGenusButton, postComposeGenusConstraints);
    this.tableFormat = new DifferentiaTableFormat();
    final EventTableModel<Differentia> model = new EventTableModel<Differentia>(this.diffs, this.tableFormat);
    this.diffTable = new BugWorkaroundTable(model);
    this.diffTable.setSelectionModel(this.selectionModel);
    this.diffTable.setDefaultRenderer(OBOObject.class, new TermRenderer("None"));
    this.diffTable.putClientProperty("Quaqua.Table.style", "striped");
    this.diffTable.getColumnModel().getColumn(0).setCellEditor(this.tableFormat.getColumnEditor(0));
    this.diffTable.getColumnModel().getColumn(1).setCellEditor(this.tableFormat.getColumnEditor(1));
    final GridBagConstraints tableConstraints = new GridBagConstraints();
    tableConstraints.gridy = 1;
    tableConstraints.gridwidth = 3;
    tableConstraints.fill = GridBagConstraints.BOTH;
    tableConstraints.weighty = 1.0;
    this.add(new JScrollPane(diffTable), tableConstraints);
    final GridBagConstraints toolbarConstraints = new GridBagConstraints();
    toolbarConstraints.gridy = 2;
    toolbarConstraints.gridwidth = 2;
    toolbarConstraints.fill = GridBagConstraints.HORIZONTAL;
    toolbarConstraints.weightx = 1.0;
    this.add(this.createToolBar(), toolbarConstraints);
    this.popupListener = new TablePopupListener(this.createTablePopupMenu(), this.diffTable);
    this.popupListener.setPopupColumns(Arrays.asList(new Integer[] {1}));
    this.diffTable.addMouseListener(this.popupListener);
  }
  
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar();
    try {
      this.addDifferentiaButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
          public void actionPerformed(ActionEvent e) {
            addDifferentia();
          }
        });
      this.addDifferentiaButton.setToolTipText("Add Differentia");
      toolBar.add(this.addDifferentiaButton);
      this.deleteDifferentiaButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
          public void actionPerformed(ActionEvent e) {
            deleteSelectedDifferentia();
          }
        });
      this.deleteDifferentiaButton.setToolTipText("Delete Differentia");
      toolBar.add(this.deleteDifferentiaButton);
    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    toolBar.setFloatable(false);
    return toolBar;
  }
  
  private JPopupMenu createTablePopupMenu() {
    final JPopupMenu menu = new JPopupMenu();
    menu.add(new AbstractAction("Create Post-composed Term") {
      public void actionPerformed(ActionEvent e) {
        runPostCompositionForTermAtPoint(popupListener.getLocation());
      }
    });
    return menu;
  }
  
  private static class Differentia {
    
    private OBOProperty relation;
    private OBOClass term;
    
    public OBOProperty getRelation() {
      return this.relation;
    }
    
    public void setRelation(OBOProperty relation) {
      this.relation = relation;
    }
    
    public OBOClass getTerm() {
      return this.term;
    }
    
    public void setTerm(OBOClass term) {
      this.term = term;
    }
    
    public boolean isComplete() {
      return (this.relation != null) && (this.term != null);
    }
    
  }
 
  private class DifferentiaTableFormat implements WritableTableFormat<Differentia>, AdvancedTableFormat<Differentia> {

    public boolean isEditable(Differentia diff, int column) {
      return true;
    }
    
    public TableCellEditor getColumnEditor(int column) {
      switch (column) {
      case 0: return createAutocompleteEditor(getController().getOntologyController().getRelationsTermSet().getTerms());
      case 1: return createAutocompleteEditor(termSet.getTerms());
      default: return null;
      }
    }

    public Differentia setColumnValue(Differentia diff, Object editedValue, int column) {
      switch(column) {
      case 0: diff.setRelation((OBOProperty)editedValue); break;
      case 1: diff.setTerm((OBOClass)editedValue); break;
      }
      return diff;
    }

    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int column) {
      switch(column) {
      case 0: return "Relationship";
      case 1: return "Differentia";
      }
      return null;
    }

    public Object getColumnValue(Differentia diff, int column) {
      switch(column) {
      case 0: return diff.getRelation();
      case 1: return diff.getTerm();
      }
      return null;
    }

    public Class<?> getColumnClass(int column) {
      return OBOObject.class;
    }

    public Comparator<?> getColumnComparator(int column) {
      return GlazedLists.comparableComparator();
    }
  
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
