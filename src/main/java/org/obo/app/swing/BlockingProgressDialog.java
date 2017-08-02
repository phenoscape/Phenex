package org.obo.app.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.jdesktop.swingworker.SwingWorker;

/**
 * A modal dialog which displays a message and an indeterminate progress bar
 * while a executing a SwingWorker object.  The dialog closes and returns when the SwingWorker
 * finishes executing.
 * 
 * @author Jim Balhoff
 */
public class BlockingProgressDialog<T, V> extends JDialog {

	private final SwingWorker<T, V> worker;
	private String message;

	public BlockingProgressDialog(SwingWorker<T, V> worker, String message) {
		super(new JFrame(), true);
		this.worker = worker;
		this.message = message;
		this.worker.addPropertyChangeListener(new WorkerListener());
		this.initializeInterface();
	}

	/**
	 * Display the dialog and execute the SwingWorker. Returns when the 
	 * SwingWorker completes execution.
	 */
	public void run() {
		this.worker.execute();
		this.setVisible(true);
	}

	private void initializeInterface() {
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.setResizable(false);
		this.setLayout(new GridBagLayout());
		// surrounding with html tags makes the JLabel wrap its text
		final JLabel label = new JLabel("<HTML>" + this.message + "</HTML>");
		final GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.insets = new Insets(11, 11, 11, 11);
		labelConstraints.fill = GridBagConstraints.BOTH;
		labelConstraints.weightx = 1.0;
		labelConstraints.weighty = 1.0;
		this.add(label, labelConstraints);
		final GridBagConstraints progressConstraints = new GridBagConstraints();
		progressConstraints.gridy = 1;
		progressConstraints.fill = GridBagConstraints.HORIZONTAL;
		progressConstraints.insets = new Insets(11, 11, 11, 11);
		final JProgressBar progress = new JProgressBar();
		progress.setIndeterminate(true);
		this.add(progress, progressConstraints);
	}

	private class WorkerListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("state".equals(evt.getPropertyName()) && worker.getState().equals(SwingWorker.StateValue.DONE)) {
				BlockingProgressDialog.this.setVisible(false);
			}
		}

	}

}
