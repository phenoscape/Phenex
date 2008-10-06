package org.phenoscape.app;

import java.io.File;

import javax.swing.JFrame;

/**
 * A collection of static methods providing platform-specific functionality.
 * @author Jim Balhoff
 */
public class CrossPlatform {
  
  public static enum Platform { MAC, WINDOWS, UNIX; }
  
  public static Platform getCurrentPlatform() {
    final String name = System.getProperty("os.name");
    if (name.startsWith("Mac")) {
      return Platform.MAC;
    } else if (name.startsWith("Windows")) {
      return Platform.WINDOWS;
    } else {
      return Platform.UNIX;
    }
  }
  
  /**
   * Don't add "Exit" to the File menu on Mac.
   * Instead there is "Quit" under the automatic app menu.
   */
  public static boolean shouldPutExitInFileMenu() {
    return !getCurrentPlatform().equals(Platform.MAC);
  }
  
  /**
   * Don't put the application name in the window title on Mac.
   * Instead it is visible in the menubar.
   */
  public static boolean shouldPutAppNameInWindowTitle() {
    return !getCurrentPlatform().equals(Platform.MAC);
  }
  
  /**
   * Titles this window appropriately to represent the given file, or as untitled 
   * if the file is null.  Also adds a proxy document icon on supported platforms (Mac OS X).
   */
  public static void setTitleOnWindowForFile(JFrame window, File file, String appName) {
    final String docName = file != null ? file.getName() : "Untitled";
    final String windowTitle;
    if (CrossPlatform.shouldPutAppNameInWindowTitle()) {
      windowTitle = docName + " - " + appName;
    } else {
      windowTitle = docName;
    }
    window.setTitle(windowTitle);
    window.getRootPane().putClientProperty("Window.documentFile", file);
  }
  
  /**
   * Returns a folder suitable for storing per-user application support files.
   * This is unrelated to the storage location for java.util.prefs.
   */
  public static File getUserPreferencesFolder(String name) {
    final String homePath = System.getProperty("user.home");
    switch(getCurrentPlatform()) {
    // it would be much better to find a supported API for obtaining the Application Support folder
    case MAC: return new File(homePath, "Library/Application Support/" + name);
    case WINDOWS: return new File(homePath, name);
      // UNIX behavior is default
    default: return new File(homePath, "." + name.toLowerCase());
    }
  }

}
