package org.phenoscape.swing;

import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.text.Document;

public class TabActionTextField extends JTextField {

  public TabActionTextField() {
    super();
  }

  public TabActionTextField(Document doc, String text, int columns) {
    super(doc, text, columns);
  }

  public TabActionTextField(int columns) {
    super(columns);
  }

  public TabActionTextField(String text, int columns) {
    super(text, columns);
  }

  public TabActionTextField(String text) {
    super(text);
  }

  @Override
  protected void processFocusEvent(FocusEvent e) {
    super.processFocusEvent(e);
    if ((!e.isTemporary()) && (e.getID() == FocusEvent.FOCUS_LOST)) {
      this.fireActionPerformed();
    }
  }

}
