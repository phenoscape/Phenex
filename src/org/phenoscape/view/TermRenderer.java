package org.phenoscape.view;

import java.awt.Component;

import javax.swing.JTable;

import org.obo.datamodel.OBOObject;
import org.phenoscape.swing.PlaceholderRenderer;

/**
 * A table cell renderer for displaying cell values which are ontology terms (OBOClass).
 * The term name is displayed, and the term ID is provided as a tooltip.
 * @author Jim Balhoff
 */
public class TermRenderer extends PlaceholderRenderer {

  public TermRenderer(String placeholder) {
    super(placeholder);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value instanceof OBOObject) {
      final OBOObject term = (OBOObject)value;
      this.setToolTipText(term.getID());
      return super.getTableCellRendererComponent(table, term.getName(), isSelected, hasFocus, row, column);
    } else {
      this.setToolTipText(null);
      return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }  
  }
  
}
