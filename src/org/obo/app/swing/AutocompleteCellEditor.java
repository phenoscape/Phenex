package org.obo.app.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

public class AutocompleteCellEditor<T> extends AbstractCellEditor implements TableCellEditor {

    private final AutocompleteField<T> acField;
    private Object originalValue = null;
    private Object value = null;
    private int clickCountToStart = 2;

    public AutocompleteCellEditor(AutocompleteField<T> autocompleteField) {
        this.acField = autocompleteField;
        this.acField.getComboBox().putClientProperty("JComboBox.isTableCellEditor", true);
        this.acField.getComboBox().setBorder(null); //this corrects the editor height on Windows
        this.acField.addActionListener(new AutocompleteBoxActionListener());
    }

    @SuppressWarnings("unchecked")
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.originalValue = value;
        this.value = value;
        this.acField.setValue((T)value);
        return this.acField.getComboBox();
    }

    @Override
    public void cancelCellEditing() {
        this.value = this.originalValue;
        super.cancelCellEditing();
    }

    public Object getCellEditorValue() {
        return this.value;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) { 
            return ((MouseEvent)anEvent).getClickCount() >= this.clickCountToStart;
        }
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    
    public void setClickCountToStart(int count) {
        this.clickCountToStart = count;
    }
    
    public int getClickCountToStart() {
        return this.clickCountToStart;
    }
    
    private class AutocompleteBoxActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            value = acField.getValue();
            stopCellEditing();
        }
    }
    
    @SuppressWarnings("unused")
    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
