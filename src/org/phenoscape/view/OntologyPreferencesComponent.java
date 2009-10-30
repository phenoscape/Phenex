package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.phenoscape.model.OntologySource;
import org.phenoscape.model.UserOntologyConfiguration;
import org.phenoscape.swing.BugWorkaroundTable;
import org.phenoscape.swing.PlaceholderRenderer;

import phenote.gui.TableColumnPrefsSaver;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.eekboom.utils.Strings;

public class OntologyPreferencesComponent extends AbstractGUIComponent {

    private JButton addURLButton;
    private JButton deleteURLButton;
    private JButton applyButton;
    private JButton revertButton;
    private final EventList<OntologySource> sources = new BasicEventList<OntologySource>();
    private final EventSelectionModel<OntologySource> sourcesSelectionModel = new EventSelectionModel<OntologySource>(sources);
    private final List<OntologySource> oldSources = new ArrayList<OntologySource>();
    private final UserOntologyConfiguration config;

    //TODO enable/disable apply & revert buttons
    public OntologyPreferencesComponent(String id, UserOntologyConfiguration configuration) {
        super(id);
        this.config = configuration;
    }

    @Override
    public void init() {
        super.init();
        final List<OntologySource> storedSources = this.config.getStoredSources();
        if (storedSources != null) {
            this.sources.addAll(storedSources);
        }
        this.cloneContents(this.sources, this.oldSources);
        this.initializeInterface();
    }

    private void addSource() {
        this.sources.add(new OntologySource());
    }

    private void deleteSelectedSource() {
        final OntologySource source = this.getSelectedSource();
        if (source != null) { this.sources.remove(source); }
    }

    private void applyChanges() {
        this.config.storeSources(this.sources);
        this.cloneContents(this.sources, this.oldSources);
        //TODO tell user they will need to relaunch to see changes
    }

    private void revertChanges() {
        this.cloneContents(this.oldSources, this.sources);
    }

    private OntologySource getSelectedSource() {
        final EventList<OntologySource> selected = this.sourcesSelectionModel.getSelected();
        if (selected.size() == 1) {
            return selected.get(0);
        } else {
            return null;
        }
    }

    private void cloneContents(List<OntologySource> original, List<OntologySource> destination) {
        destination.clear();
        for (OntologySource item : original) {
            destination.add(item.copy());
        }
    }

    private void initializeInterface() {
        this.setLayout(new BorderLayout());
        final EventTableModel<OntologySource> sourcesTableModel = new EventTableModel<OntologySource>(this.sources, new OntologySourcesTableFormat());
        final JTable sourcesTable = new BugWorkaroundTable(sourcesTableModel);
        sourcesTable.setSelectionModel(this.sourcesSelectionModel);
        sourcesTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
        sourcesTable.putClientProperty("Quaqua.Table.style", "striped");
        new TableColumnPrefsSaver(sourcesTable, this.getClass().getName());
        //final TableComparatorChooser<Character> sortChooser = new TableComparatorChooser<Character>(charactersTable, this.getController().getSortedCharacters(), false);
        //sortChooser.addSortActionListener(new SortDisabler());
        this.add(new JScrollPane(sourcesTable), BorderLayout.CENTER);
        this.add(this.createToolBar(), BorderLayout.NORTH);
        this.add(this.createSavePanel(), BorderLayout.SOUTH);
    }

    private JToolBar createToolBar() {
        final JToolBar toolBar = new JToolBar();
        this.addURLButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-add.png"))) {
            public void actionPerformed(ActionEvent e) {
                addSource();
            }
        });
        this.addURLButton.setToolTipText("Add Source URL");
        toolBar.add(this.addURLButton);
        this.deleteURLButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-remove.png"))) {
            public void actionPerformed(ActionEvent e) {
                deleteSelectedSource();
            }
        });
        this.deleteURLButton.setToolTipText("Delete Source URL");
        toolBar.add(this.deleteURLButton);
        toolBar.setFloatable(false);
        return toolBar;
    }

    private JPanel createSavePanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        this.revertButton = new JButton(new AbstractAction("Revert") {
            public void actionPerformed(ActionEvent e) {
                revertChanges();
            }
        });
        this.revertButton.setToolTipText("Revert Changes");
        panel.add(this.revertButton, BorderLayout.WEST);
        this.applyButton = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                applyChanges();
            }
        });
        this.applyButton.setToolTipText("Apply Changes");
        panel.add(this.applyButton, BorderLayout.EAST);
        return panel;
    }

    private class OntologySourcesTableFormat implements WritableTableFormat<OntologySource>, AdvancedTableFormat<OntologySource> {

        public boolean isEditable(OntologySource source, int column) {
            return true;
        }

        public OntologySource setColumnValue(OntologySource source, Object editedValue, int column) {
            switch(column) {
            case 0: source.setLabel(editedValue.toString()); break;
            case 1:
                try {
                    source.setURL(new URL(editedValue.toString()));
                } catch (MalformedURLException e) {
                    log().error("User entered bad URL");
                } break;
            }
            return source;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int column) {
            switch(column) {
            case 0: return "Label";
            case 1: return "URL";
            }
            return null;
        }

        public Object getColumnValue(OntologySource source, int column) {
            switch(column) {
            case 0: return source.getLabel();
            case 1: return source.getURL();
            }
            return null;
        }

        public Class<?> getColumnClass(int column) {
            switch(column) {
            case 0: return String.class;
            case 1: return URL.class;
            }
            return null;
        }

        public Comparator<?> getColumnComparator(int column) {
            switch(column) {
            case 0: return Strings.getNaturalComparator();
            case 1: return new Comparator<URL>() {
                public int compare(URL o1, URL o2) {
                    return Strings.compareNatural(o1.toString(), o2.toString());
                }
            };
            }
            return null;
        }

    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
