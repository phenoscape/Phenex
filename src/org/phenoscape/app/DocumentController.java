package org.phenoscape.app;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

/**
 * A general class managing reading and writing of document files and the data loaded from those files.
 * @author Jim Balhoff
 */
public abstract class DocumentController {
  
  private File currentFile;
  
  public DocumentController() {
    this.setWindowTitle(null);
  }
  
  public void open() {
    final JFileChooser fileChooser = new JFileChooser();
    final int result = fileChooser.showOpenDialog(this.getWindow());
    if (result == JFileChooser.APPROVE_OPTION) {
      final File file = fileChooser.getSelectedFile();
      final boolean success = this.readData(file);
      if (success) { this.setCurrentFile(file); }
      else { this.runFileReadErrorMessage(); }
    }
  }
  
  public void importFile() {
    final JFileChooser fileChooser = new JFileChooser();
    final int result = fileChooser.showOpenDialog(this.getWindow());
    if (result == JFileChooser.APPROVE_OPTION) {
      final File file = fileChooser.getSelectedFile();
      final boolean success = this.readData(file);
      if (success) { this.setCurrentFile(null); }
      else { this.runFileReadErrorMessage(); }
    }
  }
  
  public void save() {
    if (this.getCurrentFile() == null) {
      this.saveAs();
    } else {
      this.writeData(this.getCurrentFile());
    }
  }
  
  public void saveAs() {
    final JFileChooser fileChooser = new JFileChooser();
    final int result = fileChooser.showSaveDialog(this.getWindow());
    if (result == JFileChooser.APPROVE_OPTION) {
      final File file = fileChooser.getSelectedFile();
      final boolean success = this.writeData(file);
      if (success) { this.setCurrentFile(file); }
      else { this.runFileWriteErrorMessage(); }
    }
  }

  public File getCurrentFile() {
    return this.currentFile;
  }
  
  public void setCurrentFile(File aFile) {
    this.currentFile = aFile;
    this.setWindowTitle(aFile);
  }
  
  public abstract boolean readData(File aFile);
  
  public abstract boolean writeData(File aFile);
  
  public abstract JFrame getWindow();
  
  public abstract String getAppName();
  
  private void setWindowTitle(File aFile) {
    final JFrame window = this.getWindow();
    if (window != null) {
      CrossPlatform.setTitleOnWindowForFile(window, aFile, this.getAppName());
    }
  }
  
  private void runFileReadErrorMessage() {
    log().error("Failed to load file data");
    //TODO gui message
  }
  
  private void runFileWriteErrorMessage() {
    log().error("Failed to write file data");
    //TODO gui message
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
