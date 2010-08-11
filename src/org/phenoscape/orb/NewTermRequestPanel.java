package org.phenoscape.orb;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.obo.annotation.base.OBOUtil.Differentium;
import org.obo.annotation.view.OntologyCoordinator;
import org.obo.annotation.view.TermAutocompleteFieldFactory;
import org.obo.annotation.view.TermRenderer;
import org.obo.app.swing.AutocompleteField;
import org.obo.app.swing.BugWorkaroundTable;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;
import org.obo.util.TermUtil;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

public class NewTermRequestPanel extends AbstractGUIComponent {
    
    private AutocompleteField<OBOObject> parentBox;
    private EventList<Differentium> links = new BasicEventList<Differentium>();
    private EventSelectionModel<Differentium> selectionModel = new EventSelectionModel<Differentium>(links);
    private LinksTableFormat tableFormat;
    private JTable linksTable;
    private JButton addLinkButton;
    private JButton deleteLinkButton;
    private final OntologyCoordinator ontologyCoordinator;
    private final ORBTerm orbTerm = new ORBTerm();

    public NewTermRequestPanel(String id, OntologyCoordinator coordinator) {
        super(id);
        this.ontologyCoordinator = coordinator;
    }
    
    public NewTermRequestPanel(OntologyCoordinator coordinator) {
        this("", coordinator);
    }

    @Override
    public void init() {
        super.init();
        this.initializeInterface();    
    }
    
    public ORBTerm getTerm() {
        return this.orbTerm;
    }
    
    public void addLink() {
        this.links.add(new Differentium());
    }

    public void deleteSelectedLink() {
        this.selectionModel.getSelected().clear();
    }
    
    private void updateParent() {
        this.orbTerm.setParent((OBOClass)(this.parentBox.getValue()));
    }

    private void initializeInterface() {
        this.setLayout(new GridBagLayout());
        final GridBagConstraints labelConstraints = new GridBagConstraints();
        this.add(new JLabel("Parent:"), labelConstraints);
        final GridBagConstraints comboConstraints = new GridBagConstraints();
        comboConstraints.gridx = 1;
        comboConstraints.fill = GridBagConstraints.HORIZONTAL;
        comboConstraints.weightx = 1.0;
        this.parentBox = TermAutocompleteFieldFactory.createAutocompleteBox(this.toOBOObjects(TermUtil.getTerms(this.ontologyCoordinator.getOBOSession())), this.ontologyCoordinator);
        this.parentBox.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                updateParent();
            }
        });
        this.add(this.parentBox, comboConstraints);
        final GridBagConstraints postComposeGenusConstraints = new GridBagConstraints();
        postComposeGenusConstraints.gridx = 2;
        this.tableFormat = new LinksTableFormat();
        final EventTableModel<Differentium> model = new EventTableModel<Differentium>(this.links, this.tableFormat);
        this.linksTable = new BugWorkaroundTable(model);
        this.linksTable.setSelectionModel(this.selectionModel);
        this.linksTable.setDefaultRenderer(OBOObject.class, new TermRenderer("None"));
        this.linksTable.putClientProperty("Quaqua.Table.style", "striped");
        this.linksTable.getColumnModel().getColumn(0).setCellEditor(this.tableFormat.getColumnEditor(0));
        this.linksTable.getColumnModel().getColumn(1).setCellEditor(this.tableFormat.getColumnEditor(1));
        final GridBagConstraints tableConstraints = new GridBagConstraints();
        tableConstraints.gridy = 1;
        tableConstraints.gridwidth = 3;
        tableConstraints.fill = GridBagConstraints.BOTH;
        tableConstraints.weighty = 1.0;
        this.add(new JScrollPane(linksTable), tableConstraints);
        final GridBagConstraints toolbarConstraints = new GridBagConstraints();
        toolbarConstraints.gridy = 2;
        toolbarConstraints.gridwidth = 2;
        toolbarConstraints.fill = GridBagConstraints.HORIZONTAL;
        toolbarConstraints.weightx = 1.0;
        this.add(this.createToolBar(), toolbarConstraints);
    }
    
    private JToolBar createToolBar() {
        final JToolBar toolBar = new JToolBar();
        this.addLinkButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-add.png"))) {
            public void actionPerformed(ActionEvent e) {
                addLink();
            }
        });
        this.addLinkButton.setToolTipText("Add Differentia");
        toolBar.add(this.addLinkButton);
        this.deleteLinkButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-remove.png"))) {
            public void actionPerformed(ActionEvent e) {
                deleteSelectedLink();
            }
        });
        this.deleteLinkButton.setToolTipText("Delete Differentia");
        toolBar.add(this.deleteLinkButton);
        toolBar.setFloatable(false);
        return toolBar;
    }
    
    private class LinksTableFormat implements WritableTableFormat<Differentium>, AdvancedTableFormat<Differentium> {

        public boolean isEditable(Differentium diff, int column) {
            return true;
        }

        public TableCellEditor getColumnEditor(int column) {
            switch (column) {
            case 0: return TermAutocompleteFieldFactory.createAutocompleteEditor(toOBOObjects(TermUtil.getRelationshipTypes(ontologyCoordinator.getOBOSession())), ontologyCoordinator);
            case 1: return TermAutocompleteFieldFactory.createAutocompleteEditor(toOBOObjects(TermUtil.getTerms(ontologyCoordinator.getOBOSession())), ontologyCoordinator);
            default: return null;
            }
        }

        public Differentium setColumnValue(Differentium diff, Object editedValue, int column) {
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
            case 1: return "Target Term";
            }
            return null;
        }

        public Object getColumnValue(Differentium diff, int column) {
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
    
    @SuppressWarnings("unchecked")
    private Collection<OBOObject> toOBOObjects(Collection<?> terms) {
        return (Collection<OBOObject>)terms;
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
