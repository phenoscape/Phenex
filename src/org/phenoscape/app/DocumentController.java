package org.phenoscape.app;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.phenoscape.model.UndoController;
import org.phenoscape.model.UndoController.UnsavedChangesListener;

/**
 * A general class managing reading and writing of document files and the data loaded from those files.
 * @author Jim Balhoff
 */
public abstract class DocumentController {

    private File currentFile;
    private UndoController undo;
    private final DirtyDocumentIndicator dirtyIndicator = new DirtyDocumentIndicator();

    public DocumentController() {
        this.setWindowTitle(null);
    }

    public void open() {
        if (!this.canCloseDocument()) return;
        final JFileChooser fileChooser = new JFileChooser();
        final FileFilter filter = new FileFilter() {
            
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().endsWith("." + getDefaultFileExtension());
            }

            @Override
            public String getDescription() {
                return "NeXML Files";
            }
        };
        fileChooser.setFileFilter(filter);
        final int result = fileChooser.showOpenDialog(this.getWindow());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            try {
                this.readData(file);
                this.setCurrentFile(file);
                this.getUndoController().discardAllEdits();
                this.getUndoController().markChangesSaved();
            } catch (IOException e) {
                log().error("Failed to load file data", e);
                this.runFileReadErrorMessage(file, e.getLocalizedMessage());
            }
        }
    }

    public void importFile() {
        if (!this.canCloseDocument()) return;
        final JFileChooser fileChooser = new JFileChooser();
        final int result = fileChooser.showOpenDialog(this.getWindow());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            try {
                this.readData(file);
                this.setCurrentFile(null);
                this.getUndoController().discardAllEdits();
                this.getUndoController().markChangesSaved();
            } catch (IOException e) {
                log().error("Failed to load file data", e);
                this.runFileReadErrorMessage(file, e.getLocalizedMessage());
            }
        }
    }

    public void save() {
        if (this.getCurrentFile() == null) {
            this.saveAs();
        } else {
            try {
                this.writeData(this.getCurrentFile());
                this.getUndoController().markChangesSaved();
            } catch (IOException e) {
                log().error("Unable to save file", e);
                this.runFileWriteErrorMessage(this.getCurrentFile(), e.getLocalizedMessage());
            }
        }
    }

    public void saveAs() {
        final JFileChooser fileChooser = new JFileChooser();
        final int result = fileChooser.showSaveDialog(this.getWindow());
        if (result == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            final String suffix = "." + this.getDefaultFileExtension();
            final File correctedFile;
            if (file.getName().endsWith(suffix)) {
                correctedFile = file;
            } else {
                correctedFile = new File(file.getPath() + suffix);
            }
            if (correctedFile.exists()) {
                String[] options = {"Replace", "Cancel"};
                final int replace = JOptionPane.showOptionDialog(this.getWindow(), "\"" + correctedFile.getName() + "\" already exists. Do you want to replace it?  Replacing it will overwrite its current contents.", null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (replace == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            try {
                this.writeData(correctedFile);
                this.setCurrentFile(correctedFile);
                this.getUndoController().markChangesSaved();
            } catch (IOException e) {
                log().error("Unable to save file", e);
                this.runFileWriteErrorMessage(file, e.getLocalizedMessage());
            }
        }
    }

    public File getCurrentFile() {
        return this.currentFile;
    }

    public void setCurrentFile(File aFile) {
        this.currentFile = aFile;
        this.setWindowTitle(aFile);
    }

    public abstract void readData(File aFile) throws IOException;

    public abstract void writeData(File aFile) throws IOException;
    
    public UndoController getUndoController() {
        return this.undo;
    }
    
    public void setUndoController(UndoController controller) {
        if (this.undo != null) {
            this.undo.removeUnsavedChangesListener(this.dirtyIndicator);
        }
        this.undo = controller;
        this.undo.addUnsavedChangesListener(this.dirtyIndicator);
    }
    
    public abstract JFrame getWindow();

    public abstract String getAppName();
    
    public abstract String getDefaultFileExtension();
    
    public boolean canCloseDocument() {
        if (this.getUndoController().hasUnsavedChanges()) {
            return this.runUnsavedChangesDialog();
        } else {
            return true;
        }
    }
    
    private void setWindowTitle(File aFile) {
        final JFrame window = this.getWindow();
        if (window != null) {
            CrossPlatform.setTitleOnWindowForFile(window, aFile, this.getAppName());
        }
    }

    private void runFileReadErrorMessage(File file, String error) {
        JOptionPane.showMessageDialog(null, "Failed to open document \"" + file.getName() + "\".  The following error occurred:\n\n" + error, "Error Reading File", JOptionPane.ERROR_MESSAGE);
    }

    private void runFileWriteErrorMessage(File file, String error) {
        final String[] options = {"Save to New Location...", "Cancel"};
        final int result = JOptionPane.showOptionDialog(null, "Failed to save document \"" + file.getName() + "\".  The following error occurred:\n\n" + error, "Error Writing File", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        if (result == JOptionPane.YES_OPTION) {
            this.saveAs();
        }
    }
    
    private boolean runUnsavedChangesDialog() {
        final String[] options = {"Save", "Don't Save", "Cancel"};
        String message = "You have unsaved changes.  Would you like to save before closing" + ((this.getCurrentFile() == null) ? "" : (" \"" + this.getCurrentFile().getName() + "\"")) + "?";
        final int result =  JOptionPane.showOptionDialog(this.getWindow(), message, "", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (result == JOptionPane.YES_OPTION) {
            this.save();
            return this.canCloseDocument();
        } else if (result == JOptionPane.CANCEL_OPTION) {
            return false;
        } else if (result == JOptionPane.NO_OPTION) {
            return true;
        }
        return false;
    }
    
    private class DirtyDocumentIndicator implements UnsavedChangesListener {

        public void setUnsavedChanges(boolean unsaved) {
            CrossPlatform.setWindowModified(getWindow(), unsaved);
        }

    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}
