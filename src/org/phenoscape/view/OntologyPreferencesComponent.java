package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
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
    private final EventList<Source> sources = new BasicEventList<Source>();
    private final EventSelectionModel<Source> sourcesSelectionModel = new EventSelectionModel<Source>(sources);

    public OntologyPreferencesComponent(String id) {
        super(id);
    }

    @Override
    public void init() {
        super.init();
        //TODO load current sources into list
        //
        this.initializeInterface();
    }

    private void addSource() {
        this.sources.add(new Source());
    }

    private void deleteSelectedSource() {
        final Source source = this.getSelectedSource();
        if (source != null) { this.sources.remove(source); }
    }

    private Source getSelectedSource() {
        final EventList<Source> selected = this.sourcesSelectionModel.getSelected();
        if (selected.size() == 1) {
            return selected.get(0);
        } else {
            return null;
        }
    }

    private void initializeInterface() {
        this.setLayout(new BorderLayout());
        final EventTableModel<Source> sourcesTableModel = new EventTableModel<Source>(this.sources, new OntologySourcesTableFormat());
        final JTable sourcesTable = new BugWorkaroundTable(sourcesTableModel);
        sourcesTable.setSelectionModel(this.sourcesSelectionModel);
        sourcesTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
        sourcesTable.putClientProperty("Quaqua.Table.style", "striped");
        new TableColumnPrefsSaver(sourcesTable, this.getClass().getName());
        //final TableComparatorChooser<Character> sortChooser = new TableComparatorChooser<Character>(charactersTable, this.getController().getSortedCharacters(), false);
        //sortChooser.addSortActionListener(new SortDisabler());
        this.add(new JScrollPane(sourcesTable), BorderLayout.CENTER);
        this.add(this.createToolBar(), BorderLayout.NORTH);
        this.add(this.createSaveToolBar(), BorderLayout.SOUTH);
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
    
    private JToolBar createSaveToolBar() {
        final JToolBar toolBar = new JToolBar();
        this.revertButton = new JButton(new AbstractAction("Revert") {
            public void actionPerformed(ActionEvent e) {
                //deleteSelectedSource();
            }
        });
        this.revertButton.setToolTipText("Revert Changes");
        toolBar.add(this.revertButton);
        this.applyButton = new JButton(new AbstractAction("Apply") {
            public void actionPerformed(ActionEvent e) {
                //addSource();
            }
        });
        this.applyButton.setToolTipText("Apply Changes");
        toolBar.add(this.applyButton);
        toolBar.setFloatable(false);
        return toolBar;
    }

    private class OntologySourcesTableFormat implements WritableTableFormat<Source>, AdvancedTableFormat<Source> {

        public boolean isEditable(Source source, int column) {
            return true;
        }

        public Source setColumnValue(Source source, Object editedValue, int column) {
            switch(column) {
            case 0: source.setLabel(editedValue.toString());
            try {
                source.setURL(new URL(editedValue.toString()));
            } catch (MalformedURLException e) {
                //TODO check correctness of URL in a cell editor instead
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

        public Object getColumnValue(Source source, int column) {
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

    private static class Source {

        private String label;
        private URL url;

        public String getLabel() {
            return this.label;
        }

        public void setLabel(String newLabel) {
            this.label = newLabel;
        }

        public URL getURL() {
            return this.url;
        }

        public void setURL(URL newURL) {
            this.url = newURL;
        }

    }

    private Preferences getPrefsRoot() {
        return Preferences.userNodeForPackage(this.getClass()).node("ontologyConfig");
    }


    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
