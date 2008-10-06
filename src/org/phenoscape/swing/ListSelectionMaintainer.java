package org.phenoscape.swing;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventSelectionModel;

/**
 * This class listens to changes to an EventList and its associated EventSelectionModel. 
 * It attempts to prevent an empty selection in the selection model, by selecting the next 
 * item after one is deleted, and selecting newly inserted items. 
 * 
 * @author Jim Balhoff
 */
public class ListSelectionMaintainer<T> {

  private final EventList<T> list;
  private final EventSelectionModel<T> selectionModel;

  public ListSelectionMaintainer(EventList<T> list, EventSelectionModel<T> selectionModel) {
    this.list = list;
    this.selectionModel = selectionModel;
    this.list.addListEventListener(new ListListener<T>());
  }
  
  private void selectIndex(int index) {
    if (index < this.list.size()) {
      selectionModel.setSelectionInterval(index, index);
    }
  }

  private class ListListener<E> implements ListEventListener<E> {

    public void listChanged(final ListEvent<E> listChanges) {
      int index = -1;
      while (listChanges.hasNext()) {
        listChanges.next();
        if ((listChanges.getType() == ListEvent.UPDATE) || (listChanges.isReordering())) {
          return;
        }
        index = listChanges.getIndex();
      }
      final int selectionIndex = (index > (list.size() - 1)) ? (list.size() - 1) : index;
      if (selectionIndex > -1) {
        // must use invokeLater because the selection model will not have found out about the inserted item yet
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            selectIndex(selectionIndex);
          }
        });
      }
    }

  }

  @SuppressWarnings("unused")
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
