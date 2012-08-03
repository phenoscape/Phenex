package org.obo.annotation.view;

import java.util.Collection;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.obo.annotation.base.TermSet;
import org.obo.app.swing.AutocompleteCellEditor;
import org.obo.app.swing.AutocompleteField;
import org.obo.app.swing.SearchHit;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;

public class TermAutocompleteFieldFactory {

    /**
     * Update SelectionManager with current term selection.  This allows components
     * like the Term Info panel to display information about the term.
     */
    private static void updateGlobalTermSelection(Object source, OBOClass term, SelectionManager selectionManager) {
        selectionManager.selectTerm(source, term, false);
    }

    public static AutocompleteField<OBOObject> createAutocompleteBox(TermSet terms, OntologyCoordinator coordinator) {
        final AutocompleteField<OBOObject> ac =  new AutocompleteField<OBOObject>(new TermSearcher(terms));
        ac.getListComponent().addListSelectionListener(new CompletionListListener(coordinator.getSelectionManager()));
        return ac;
    }

    public static TableCellEditor createAutocompleteEditor(TermSet terms, OntologyCoordinator coordinator) {
        return new AutocompleteCellEditor<OBOObject>(createAutocompleteBox(terms, coordinator));
    }

    private static class CompletionListListener implements ListSelectionListener {

        private SelectionManager selectionManager;

        public CompletionListListener(SelectionManager selectionManager) {
            this.selectionManager = selectionManager;
        }

        public void valueChanged(ListSelectionEvent event) {
            final Object source = event.getSource();
            if (source instanceof JList) {
                final JList menu = (JList)source;
                try {
                    final Object value = menu.getSelectedValue();
                    if ((value instanceof SearchHit<?>) && (((SearchHit<?>)value).getHit() instanceof OBOClass)) {
                        updateGlobalTermSelection(this, (OBOClass)((SearchHit<?>)value).getHit(), this.selectionManager);
                    } else {
                        // sometimes the selection is a String instead
                    }
                } catch (IndexOutOfBoundsException e) {
                    // for some reason sometimes the menu selection is not valid
                }
            } else {
                log().error("Source of combobox mouse over event is not JList");
            }
        }

    }

    private static Logger log() {
        return Logger.getLogger(TermAutocompleteFieldFactory.class);
    }

}
