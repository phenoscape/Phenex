package org.phenoscape.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

public class AutocompleteCellEditor<T> implements TableCellEditor {

    private final AutocompleteField<T> acField;
    private final List<CellEditorListener> listeners = new ArrayList<CellEditorListener>();
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

    public void addCellEditorListener(CellEditorListener l) {
        this.listeners.add(l);
    }

    public void cancelCellEditing() {
        this.value = this.originalValue;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (CellEditorListener listener : listeners) {
                    listener.editingCanceled(new ChangeEvent(this));
                }
            }
        });
    }

    public Object getCellEditorValue() {
        return this.value;
    }

    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) { 
            return ((MouseEvent)anEvent).getClickCount() >= this.clickCountToStart;
        }
        return true;
    }

    public void removeCellEditorListener(CellEditorListener l) {
        this.listeners.remove(l);
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    public boolean stopCellEditing() {
        for (CellEditorListener listener : listeners) {
            final CellEditorListener cel = listener;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    cel.editingStopped(new ChangeEvent(this));
                }
            });
        }
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
            for (CellEditorListener listener : listeners) {
                final CellEditorListener cel = listener;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        cel.editingStopped(new ChangeEvent(this));
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
