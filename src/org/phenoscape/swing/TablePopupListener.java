package org.phenoscape.swing;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

public class TablePopupListener extends PopupListener {
  
  private final JTable table;
  private final Collection<Integer> columns = new ArrayList<Integer>();
  
  public TablePopupListener(JPopupMenu popupMenu, JTable table) {
    super(popupMenu);
    this.table = table;
  }
  
  public void setPopupColumns(Collection<Integer> columns) {
    this.columns.clear();
    this.columns.addAll(columns);
  }

  @Override
  protected void maybeShowPopup(MouseEvent e) {
    final int column = this.table.getTableHeader().columnAtPoint(e.getPoint());
    if (this.columns.contains(new Integer(column))) {
      super.maybeShowPopup(e);;
    }
  }
  
  

}
