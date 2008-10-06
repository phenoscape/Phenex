package org.phenoscape.swing;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A PlaceholderText displays greyed-out placeholder text in a textfield with
 * an empty value. When the user clicks into the field to begin editing, the 
 * placeholder text will disappear.
 * @author Jim Balhoff
 */
public class PlaceholderText {
  
  private final JTextField textField;
  private final FocusListener focusListener = new FieldFocusListener();
  private final DocumentListener docListener = new FieldDocumentListener();
  private String placeholder;
  private String previousText = "";
  private Color previousColor;
  private boolean settingPlaceholder = false;

  /**
   * Create a new PlaceholderText.
   * @param textField The field in which to show placeholder text.
   * @param placeholder The placeholder text to display when the field is empty.
   */
  public PlaceholderText(JTextField textField, String placeholder) {
    this.textField = textField;
    this.placeholder = placeholder;
    this.textField.addFocusListener(this.focusListener);
    this.textField.getDocument().addDocumentListener(this.docListener);
  }
 
  /**
   * @return The placeholder text displayed by this object.
   */
  public String getPlaceholder() {
    return this.placeholder;
  }

  /**
   * @param placeholder The placeholder text this object should display.
   */
  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
  }
  
  /**
   * Tells this object to remove any listeners from its textfield.
   */
  public void dispose() {
    this.textField.removeFocusListener(this.focusListener);
    this.textField.getDocument().removeDocumentListener(this.docListener);
  }
  
  private class FieldFocusListener implements FocusListener {
    
    public void focusGained(FocusEvent e) {
      textField.setForeground(previousColor);
      textField.setText(previousText);
    }

    public void focusLost(FocusEvent e) {
      previousText = textField.getText();
      previousColor = textField.getForeground();
      if (previousText.length() == 0) {
        settingPlaceholder = true;
        textField.setForeground(Color.GRAY);
        textField.setText(placeholder);
        settingPlaceholder = false;
      }
    }
  }
  
  private class FieldDocumentListener implements DocumentListener {

    public void changedUpdate(DocumentEvent e) {
      this.textChanged();
    }

    public void insertUpdate(DocumentEvent e) {
      this.textChanged();
      }

    public void removeUpdate(DocumentEvent e) {
      this.textChanged();
      }
    
    private void textChanged() {
      if (!settingPlaceholder) {
        previousText = textField.getText();
      }
    }
    
  }

}
