package org.phenoscape.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

public class WindowSizePrefsSaver {
  
  private final Window window;
  private final String autoSaveName;
  private final WindowWindowListener windowListener = new WindowWindowListener();
  private final WindowComponentListener componentListener = new WindowComponentListener();
  
  public WindowSizePrefsSaver(Window window, String autoSaveName) {
    this.window = window;
    this.autoSaveName = autoSaveName;
    this.window.addWindowListener(this.windowListener);
    this.window.addComponentListener(this.componentListener);
    this.sizeWindow();
  }
  
  /**
   * Tells this object to remove any listeners from its window.
   */
  public void dispose() {
    this.window.removeWindowListener(this.windowListener);
  }
  
  private void sizeWindow() {
    //TODO need to verify window is on screen and not too big
    if (!this.hasWindowPrefs()) {
      log().debug("Window prefs not found");
      this.window.setLocationRelativeTo(null);
    } else {
      this.window.setLocation(this.getWindowLocationPref());
      this.window.setSize(this.getWindowSizePref());
    }
  }
  
  private void saveWindowPrefs() {
    this.getSizeNode().putInt("width", this.window.getWidth());
    this.getSizeNode().putInt("height", this.window.getHeight());
    this.getLocationNode().putInt("x", this.window.getLocation().x);
    this.getLocationNode().putInt("y", this.window.getLocation().y);
  }
  
  private boolean hasWindowPrefs() {
    final Dimension size = this.getWindowSizePref();
    final Point location = this.getWindowLocationPref();
    return (size.height > -1) && (size.width > -1) && (location.x > -1) && (location.y > -1);
  }
  
  private Dimension getWindowSizePref() {
    return new Dimension(this.getSizeNode().getInt("width", -1), this.getSizeNode().getInt("height", -1));
  }
  
  private Point getWindowLocationPref() {
    return new Point(this.getLocationNode().getInt("x", -1), this.getLocationNode().getInt("y", -1));
  }
  
  private Preferences getSizeNode() {
    return this.getPrefsRoot().node("size");
  }
  
  private Preferences getLocationNode() {
    return this.getPrefsRoot().node("location");
  }
  
  private Preferences getPrefsRoot() {
    return Preferences.userNodeForPackage(this.getClass()).node(this.autoSaveName);
  }
  
  private class WindowWindowListener implements WindowListener {

    public void windowActivated(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowClosing(WindowEvent e) { saveWindowPrefs(); }
    
    public void windowDeactivated(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}
    
  }
  
  private class WindowComponentListener implements ComponentListener {

    public void componentHidden(ComponentEvent e) {}

    public void componentMoved(ComponentEvent e) { saveWindowPrefs(); }

    public void componentResized(ComponentEvent e) { saveWindowPrefs(); }

    public void componentShown(ComponentEvent e) {}
    
  }
    
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
