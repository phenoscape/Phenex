package org.phenoscape.main;

import javax.swing.SwingUtilities;

import org.bbop.framework.GUIManager;

public class Phenex {

  /**
   * Start the Phenoscape version of Phenote.
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new PhenexRunnable());
  }

  private static class PhenexRunnable implements Runnable {
    public void run() {
      GUIManager.getManager().addStartupTask(new PhenexStartupTask());
      GUIManager.getManager().start();
    }
  }

}
