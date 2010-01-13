package org.phenoscape.view;

import java.util.Collection;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.ComponentManager;
import org.obo.annotation.view.TermSearcher;
import org.obo.app.swing.AutocompleteCellEditor;
import org.obo.app.swing.AutocompleteField;
import org.obo.app.swing.SearchHit;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.phenoscape.controller.PhenexController;

public class PhenoscapeGUIComponent extends AbstractGUIComponent {

    private final PhenexController controller;

    public PhenoscapeGUIComponent(String id, PhenexController controller) {
        super(id);
        this.controller = controller;
    }

    /**
     * Most interface components will need access to data.  The controller is the gatekeeper
     * to the data model.
     */
    public PhenexController getController() {
        return this.controller;
    }

    /**
     * Update SelectionManager with current term selection.  This allows components
     * like the Term Info panel to display information about the term.
     */
    protected void updateGlobalTermSelection(OBOClass term) {
        this.getController().getPhenoteSelectionManager().selectTerm(this, term, false);
    }

    /**
     * Change the title for the tab of this component;
     */
    protected void updatePanelTitle(String title) {
        ComponentManager.getManager().setLabel(this, title);
    }

    protected AutocompleteField<OBOObject> createAutocompleteBox(Collection<OBOObject> terms) {
        final AutocompleteField<OBOObject> ac =  new AutocompleteField<OBOObject>(new TermSearcher(terms));
        ac.getListComponent().addListSelectionListener(new CompletionListListener());
        return ac;
    }

    protected TableCellEditor createAutocompleteEditor(Collection<OBOObject> terms) {
        return new AutocompleteCellEditor<OBOObject>(this.createAutocompleteBox(terms));
    }

    protected ComboPopup getComboPopup(JComboBox comboBox) {
        final AccessibleContext ac = comboBox.getAccessibleContext();
        for (int i = 0; i < ac.getAccessibleChildrenCount(); i++) {
            final Accessible a = ac.getAccessibleChild(i);
            if (a instanceof ComboPopup) { return (ComboPopup)a; }
        }
        log().error("Can't retrieve popup from combobox; can't do mouse overs");
        return null;
    }

    protected class CompletionListListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent event) {
            final Object source = event.getSource();
            if (source instanceof JList) {
                final JList menu = (JList)source;
                try {
                    final Object value = menu.getSelectedValue();
                    if ((value instanceof SearchHit<?>) && (((SearchHit<?>)value).getHit() instanceof OBOClass)) {
                        updateGlobalTermSelection((OBOClass)((SearchHit<?>)value).getHit());
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

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
