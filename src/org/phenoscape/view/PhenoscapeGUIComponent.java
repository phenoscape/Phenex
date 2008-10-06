package org.phenoscape.view;

import java.util.Collection;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.ComponentManager;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.swing.AutoCompleteSupport;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

public class PhenoscapeGUIComponent extends AbstractGUIComponent {
  
  private final PhenoscapeController controller;

  public PhenoscapeGUIComponent(String id, PhenoscapeController controller) {
    super(id);
    this.controller = controller;
  }
  
  /**
   * Most interface components will need access to data.  The controller is the gatekeeper
   * to the data model.
   */
  public PhenoscapeController getController() {
    return this.controller;
  }
  
  /**
   * Prod an event list to send change notifications for an object it contains.
   * This will cause interface objects displaying items in that list to display the
   * changed value.  This is a Glazed Lists convention.
   */
  protected <T> void updateObjectForGlazedLists(T anObject, EventList<T> aList) {
    final int index = aList.indexOf(anObject);
    if (index > -1) { aList.set(index, anObject); }
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
  
  protected JComboBox createAutocompleteBox(Collection<OBOObject> terms) {
    return this.createAutocompleteBox(terms, false);
  }
  
  private JComboBox createAutocompleteBox(Collection<OBOObject> terms, boolean cellEditor) {
    final JComboBox comboBox = new JComboBox();
    comboBox.putClientProperty("JComboBox.isTableCellEditor", cellEditor);
    final AutoCompleteSupport<OBOObject> acs = AutoCompleteSupport.install(comboBox, GlazedLists.eventList(terms), new TermFilterator());
    acs.setValidItemClass(OBOObject.class);
    final ComboPopup popup = this.getComboPopup(comboBox);
    if (popup != null) { popup.getList().addListSelectionListener(new CompletionListListener()); }
    return comboBox;
  }
  
  protected TableCellEditor createAutocompleteEditor(Collection<OBOObject> terms) {
    final JComboBox comboBox = this.createAutocompleteBox(terms, true);
    final DefaultCellEditor editor = new DefaultCellEditor(comboBox);
    editor.setClickCountToStart(2);
    return editor;
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
          if (value instanceof OBOClass) {
            updateGlobalTermSelection((OBOClass)value);
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
