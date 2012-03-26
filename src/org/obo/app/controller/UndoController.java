package org.obo.app.controller;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.apache.log4j.Logger;

/**
 * A wrapper over javax.swing.undo.UndoableEditSupport and javax.swing.undo.UndoManager 
 * which provides a unified interface for managing undo and redo for an application. This 
 * class provides undo and redo actions which can be used in menu items and also provides 
 * support for tracking whether there are unsaved edits.
 * @author Jim Balhoff
 */
public class UndoController {

    private final UndoableEditSupport undoSupport = new UndoableEditSupport();
    private final UndoManager undoManager = new UndoManager();
    private final Action undo;
    private final Action redo;
    private int dirtyStack = 0;
    private boolean undoCleans = true;
    private List<UnsavedChangesListener> unsavedChangesListeners = new ArrayList<UnsavedChangesListener>();
    private int ignoreStack = 0;

    public UndoController() {
        this.undoSupport.addUndoableEditListener(this.undoManager);
        this.undo = new AbstractAction("Undo") {
            public void actionPerformed(ActionEvent e) {
                undoManager.undo();
                updateUndoRedoActions();
                undid();
            }
        };
        this.redo = new AbstractAction("Redo") {
            public void actionPerformed(ActionEvent e) {
                undoManager.redo();
                updateUndoRedoActions();
                redid();
            }
        };
        this.updateUndoRedoActions();
    }

    public Action getUndoAction() {
        return this.undo;
    }

    public Action getRedoAction() {
        return this.redo;
    }

    public void discardAllEdits() {
        this.undoManager.discardAllEdits();
        this.updateUndoRedoActions();
        this.dirtyStack = 0;
    }

    public void markChangesSaved() {
        this.dirtyStack = 0;
        this.fireUnsavedChangesChanged();
    }

    public boolean hasUnsavedChanges() {
        return this.dirtyStack != 0;
    }

    public void postEdit(UndoableEdit e) {
        if (this.ignoreStack == 0) {
            this.undoSupport.postEdit(e);
            this.updateUndoRedoActions();
            this.edited();
        }
    }

    public void beginIgnoringEdits() {
        this.ignoreStack++;
    }

    public void endIgnoringEdits() {
        this.ignoreStack--;
    }

    private void updateUndoRedoActions() {
        this.undo.setEnabled(this.undoManager.canUndo());
        this.undo.putValue(Action.NAME, this.undoManager.getUndoPresentationName());
        this.redo.setEnabled(this.undoManager.canRedo());
        this.redo.putValue(Action.NAME, this.undoManager.getRedoPresentationName());
    }

    private void undid() {
        if (this.hasUnsavedChanges()) {
            if (this.undoCleans) {
                this.dirtyStack -= 1;
            } else {
                this.dirtyStack += 1;
            }
        } else {
            this.undoCleans = false;
            this.dirtyStack += 1;
        }
        this.fireUnsavedChangesChanged();
    }

    private void redid() {
        if (this.hasUnsavedChanges()) {
            if (this.undoCleans) {
                this.dirtyStack += 1;
            } else {
                this.dirtyStack -= 1;
            }
        } else {
            this.undoCleans = true;
            this.dirtyStack += 1;
        }
        this.fireUnsavedChangesChanged();
    }

    private void edited() {
        if (this.hasUnsavedChanges()) {
            if (this.undoCleans) {
                this.dirtyStack += 1;
            } else {
                // this should prevent dirtyStack from ever reaching 0
                this.dirtyStack = 1;
            }
        } else {
            this.undoCleans = true;
            this.dirtyStack += 1;
        }
        this.fireUnsavedChangesChanged();
    }

    public interface UnsavedChangesListener {

        public void setUnsavedChanges(boolean unsaved);
    }

    public void addUnsavedChangesListener(UnsavedChangesListener listener) {
        this.unsavedChangesListeners.add(listener);
    }

    public void removeUnsavedChangesListener(UnsavedChangesListener listener) {
        this.unsavedChangesListeners.remove(listener);
    }

    private void fireUnsavedChangesChanged() {
        for (UnsavedChangesListener listener : this.unsavedChangesListeners) {
            listener.setUnsavedChanges(this.hasUnsavedChanges());
        }
    }

    @SuppressWarnings("unused")
    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
