package org.phenoscape.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A table cell renderer which displays greyed-out placeholder text when the cell value is 
 * empty.
 * @author Jim Balhoff
 */
public class PlaceholderRenderer extends DefaultTableCellRenderer {
  
  private final String placeholder;

  /**
   * Create a new PlaceholderRenderer which displays the given text as a placeholder value. 
   * @param placeholder The placeholder text to display.
   */
  public PlaceholderRenderer(String placeholder) {
    super();
    this.placeholder = placeholder;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if ((value == null) || (value.equals(""))) {
      final Component component = super.getTableCellRendererComponent(table, this.placeholder, isSelected, hasFocus, row, column);
      component.setForeground(Color.GRAY);
      return component;
    } else {
      final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if (isSelected) {
        component.setForeground(UIManager.getColor("Table.selectionForeground"));
      } else {
        component.setForeground(UIManager.getColor("Table.foreground"));
      }
      return component;
    }
  }

}
