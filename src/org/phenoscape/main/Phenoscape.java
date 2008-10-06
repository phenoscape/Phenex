package org.phenoscape.main;

import javax.swing.SwingUtilities;

import org.bbop.framework.GUIManager;

public class Phenoscape {

  /**
   * Start the Phenoscape version of Phenote.
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new PhenoscapeRunnable());
  }

  private static class PhenoscapeRunnable implements Runnable {
    public void run() {
      GUIManager.getManager().addStartupTask(new PhenoscapeStartupTask());
      GUIManager.getManager().start();
    }
  }

}
