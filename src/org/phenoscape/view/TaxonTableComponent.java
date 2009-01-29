package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.phenoscape.model.PhenexController;
import org.phenoscape.model.Taxon;
import org.phenoscape.swing.PlaceholderRenderer;

import phenote.gui.BugWorkaroundTable;
import phenote.gui.SortDisabler;
import phenote.gui.TableColumnPrefsSaver;
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
    private EventTableModel<Taxon> taxaTableModel;

    public TaxonTableComponent(String id, PhenexController controller) {
        super(id, controller);
    }

    @Override
    public void init() {
        super.init();
        this.initializeInterface();
    }

    public void copy() {
        final StringBuffer buffer = new StringBuffer();
        final int columnCount = this.taxaTableModel.getColumnCount();
        for (int row = 0; row < this.getController().getSortedTaxa().size(); row++) {
            if (this.getController().getTaxaSelectionModel().isSelectedIndex(row)) {
                for (int column = 0; column < columnCount; column++) {
                    final Object value = this.taxaTableModel.getValueAt(row, column);
                    buffer.append(value != null ? value : "");
                    if ((column + 1) < columnCount) {
                        buffer.append("\t");
                    }
                }
                buffer.append(System.getProperty("line.separator"));
            }
        }
        if (buffer.length() > 0) {
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            final StringSelection stringSelection = new StringSelection(buffer.toString());
            clipboard.setContents(stringSelection, null);
        }
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

    private void selectFirstSpecimen() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (!getController().getSpecimensForCurrentTaxonSelection().isEmpty()) {
                    getController().getCurrentSpecimensSelectionModel().setSelectionInterval(0, 0);
                }
            }
        });
    }

    private void initializeInterface() {
        this.setLayout(new BorderLayout());
        this.taxaTableModel = new EventTableModel<Taxon>(this.getController().getSortedTaxa(), new TaxaTableFormat());
        final JTable taxaTable = new BugWorkaroundTable(this.taxaTableModel);
        taxaTable.setSelectionModel(this.getController().getTaxaSelectionModel());
        taxaTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
        taxaTable.setDefaultRenderer(OBOObject.class, new TermRenderer("None"));
        taxaTable.getColumnModel().getColumn(1).setCellEditor(this.createAutocompleteEditor(this.getController().getOntologyController().getTaxonTermSet().getTerms()));
        taxaTable.putClientProperty("Quaqua.Table.style", "striped");
        taxaTable.getActionMap().getParent().remove("copy");
        taxaTable.getActionMap().getParent().remove("paste");
        new TableColumnPrefsSaver(taxaTable, this.getClass().getName());
        final TableComparatorChooser<Taxon> sortChooser = new TableComparatorChooser<Taxon>(taxaTable, this.getController().getSortedTaxa(), false);
        sortChooser.addSortActionListener(new SortDisabler());
        this.add(new JScrollPane(taxaTable), BorderLayout.CENTER);
        this.add(this.createToolBar(), BorderLayout.NORTH);
        this.getController().getTaxaSelectionModel().addListSelectionListener(new TaxonSelectionListener());
    }

    private JToolBar createToolBar() {
        final JToolBar toolBar = new JToolBar();
        this.addTaxonButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-add.png"))) {
            public void actionPerformed(ActionEvent e) {
                addTaxon();
            }
        });
        this.addTaxonButton.setToolTipText("Add Taxon");
        toolBar.add(this.addTaxonButton);
        this.deleteTaxonButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-remove.png"))) {
            public void actionPerformed(ActionEvent e) {
                deleteSelectedTaxon();
            }
        });
        this.deleteTaxonButton.setToolTipText("Delete Taxon");
        toolBar.add(this.deleteTaxonButton);
        toolBar.setFloatable(false);
        return toolBar;
    }

    private class TaxaTableFormat implements WritableTableFormat<Taxon>, AdvancedTableFormat<Taxon> {

        public boolean isEditable(Taxon taxon, int column) {
            return column != 0;
        }

        public Taxon setColumnValue(Taxon taxon, Object editedValue, int column) {
            switch (column) {
            case 0: break;
            case 1: taxon.setValidName((OBOClass)editedValue); break;
            case 2: taxon.setPublicationName(editedValue.toString()); break;
            case 3: taxon.setComment(editedValue.toString()); break;
            case 4: taxon.setMatrixTaxonName(editedValue.toString()); break;
            }
            return taxon;
        }

        public int getColumnCount() {
            return 5;
        }

        public String getColumnName(int column) {
            switch (column) {
            case 0: return " ";
            case 1: return "Valid Taxon";
            case 2: return "Publication Taxon";
            case 3: return "Comment";
            case 4: return "Matrix Taxon";
            default: return null;
            }
        }

        public Object getColumnValue(Taxon taxon, int column) {
            switch (column) {
            case 0: return getController().getDataSet().getTaxa().indexOf(taxon) + 1;
            case 1: return taxon.getValidName();
            case 2: return taxon.getPublicationName();
            case 3: return taxon.getComment();
            case 4: return taxon.getMatrixTaxonName();
            default: return null;
            }
        }

        public Class<?> getColumnClass(int column) {
            switch (column) {
            case 0: return Integer.class;
            case 1: return OBOObject.class;
            case 2: return String.class;
            case 3: return String.class;
            case 4: return String.class;
            default: return null;
            }
        }

        public Comparator<?> getColumnComparator(int column) {
            switch (column) {
            case 0: return GlazedLists.comparableComparator();
            case 1: return GlazedLists.comparableComparator();
            case 2: return Strings.getNaturalComparator();
            case 3: return Strings.getNaturalComparator();
            case 4: return Strings.getNaturalComparator();
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
            selectFirstSpecimen();
        }

    }

    @SuppressWarnings("unused")
    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
