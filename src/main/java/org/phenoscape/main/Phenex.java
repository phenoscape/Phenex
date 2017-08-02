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
		@Override
		public void run() {
			GUIManager.getManager().addStartupTask(new PhenexStartupTask());
			GUIManager.getManager().start();
		}
	}

	public static String version() {
		final Package p = Phenex.class.getPackage();
		return p.getImplementationVersion();
	}

}
