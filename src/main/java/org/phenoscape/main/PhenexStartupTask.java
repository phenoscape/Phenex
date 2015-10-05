package org.phenoscape.main;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.GUIManager;
import org.bbop.framework.GUITask;
import org.bbop.framework.VetoableShutdownListener;
import org.bbop.framework.dock.LayoutDriver;
import org.bbop.framework.dock.idw.IDWDriver;
import org.jdesktop.swingworker.SwingWorker;
import org.obo.annotation.base.UserOntologyConfiguration;
import org.obo.annotation.view.LogViewComponentFactory;
import org.obo.annotation.view.NativeDockingTheme;
import org.obo.annotation.view.PhenoteOntologyTreeEditorFactory;
import org.obo.annotation.view.SelectionBridge;
import org.obo.annotation.view.TermInfoComponentFactory;
import org.obo.app.swing.BlockingProgressDialog;
import org.obo.app.swing.WindowSizePrefsSaver;
import org.obo.app.util.CrossPlatform;
import org.oboedit.gui.factory.SearchComponentFactory;
import org.oboedit.gui.factory.SearchResultsComponentFactory;
import org.oboedit.gui.tasks.DefaultGUIStartupTask;
import org.phenoscape.controller.OntologyController;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.view.AnnotationCheckerComponentFactory;
import org.phenoscape.view.CharacterMatrixComponentFactory;
import org.phenoscape.view.CharacterTableComponentFactory;
import org.phenoscape.view.ConsistencyReviewComponentFactory;
import org.phenoscape.view.DataSetComponentFactory;
import org.phenoscape.view.MenuFactory;
import org.phenoscape.view.ORBPreferencesComponentFactory;
import org.phenoscape.view.OntologyPreferencesComponentFactory;
import org.phenoscape.view.PhenotypeProposalComponentFactory;
import org.phenoscape.view.PhenotypeTableComponentFactory;
import org.phenoscape.view.SpecimenTableComponentFactory;
import org.phenoscape.view.StateSupportComponentFactory;
import org.phenoscape.view.StateTableComponentFactory;
import org.phenoscape.view.TaxonTableComponentFactory;

/**
 * This startup task does all the work of starting up Phenex.
 * @author Jim Balhoff
 */
public class PhenexStartupTask extends DefaultGUIStartupTask {

	private PhenexController controller;
	private UserOntologyConfiguration ontologyConfiguration;

	@Override
	protected Collection<GUIComponentFactory<?>> getDefaultComponentFactories() {
		Collection<GUIComponentFactory<?>> factories = new ArrayList<GUIComponentFactory<?>>();
		factories.add(new DataSetComponentFactory(this.controller));
		factories.add(new CharacterTableComponentFactory(this.controller));
		factories.add(new StateTableComponentFactory(this.controller));
		factories.add(new PhenotypeTableComponentFactory(this.controller));
		factories.add(new AnnotationCheckerComponentFactory(this.controller));
		factories.add(new ConsistencyReviewComponentFactory(this.controller));
		factories.add(new StateSupportComponentFactory(this.controller));
		factories.add(new PhenotypeProposalComponentFactory(this.controller));
		factories.add(new TaxonTableComponentFactory(this.controller));
		factories.add(new SpecimenTableComponentFactory(this.controller));
		factories.add(new CharacterMatrixComponentFactory(this.controller));
		factories.add(new OntologyPreferencesComponentFactory(this.ontologyConfiguration));
		factories.add(new ORBPreferencesComponentFactory());
		factories.add(new TermInfoComponentFactory(this.controller.getOntologyCoordinator()));
		//factories.add(new PhenoteOntologyTreeEditorFactory());
		factories.add(new SearchComponentFactory() {
			@Override
			public FactoryCategory getCategory() {
				return FactoryCategory.ONTOLOGY;
			}
		});
		factories.add(new SearchResultsComponentFactory());
		factories.add(new LogViewComponentFactory());
		return factories;
	}

	@Override
	protected void configureLogging() {
		//logging is configured via the log4j config file in the classpath
	}

	@Override
	protected void configureUI() {
		try {
			final String lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
			if (CrossPlatform.getCurrentPlatform().equals(CrossPlatform.Platform.MAC)) {
				// We are running on Mac OS X - use the Quaqua look and feel
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
			} else {
				// We are on some other platform, use the system look and feel
				UIManager.setLookAndFeel(lookAndFeelClassName);
			}
		} catch (ClassNotFoundException e) {
			log().error("Look and feel class not found", e);
		} catch (InstantiationException e) {
			log().error("Could not instantiate look and feel", e);
		} catch (IllegalAccessException e) {
			log().error("Error setting look and feel", e);
		} catch (UnsupportedLookAndFeelException e) {
			log().error("Look and feel not supported", e);
		}
	}

	@Override
	protected void configureSystem() {
		super.configureSystem();
		this.ontologyConfiguration = new UserOntologyConfiguration();
		final SwingWorker<OntologyController, Void> ontologyLoader = new SwingWorker<OntologyController, Void>() {
			@Override
			protected OntologyController doInBackground() {
				return new OntologyController(ontologyConfiguration);
			}
		};
		// you would expect that displaying the progress dialog would make the splash screen go away, but it doesn't
		this.flashJFrameToMakeSplashScreenGoAway();
		final BlockingProgressDialog<OntologyController, Void> dialog = new BlockingProgressDialog<OntologyController, Void>(ontologyLoader, "Phenex is checking for ontology updates.  It may take some time to download and configure ontologies.");
		dialog.setTitle("Launching " + this.getAppName());
		dialog.setSize(400, 150);
		dialog.setLocationRelativeTo(null);
		dialog.run();
		try {
			this.controller = new PhenexController(ontologyLoader.get());
		} catch (InterruptedException e) {
			log().fatal("Failed to create ontology controller", e);
			GUIManager.exit(1);
		} catch (ExecutionException e) {
			log().fatal("Failed to create ontology controller", e);
			GUIManager.exit(1);
		}
		this.controller.setAppName(this.getAppName());
	}

	@Override
	protected String getAppID() {
		return "Phenex";
	}

	@Override
	protected String getAppName() {
		return "Phenex";
	}

	@Override
	protected Action getAboutAction() {
		//TODO make an about panel
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		};
	}

	@Override
	protected JFrame createFrame() {
		final JFrame frame = super.createFrame();
		frame.setTitle(getAppName());
		// the window prefs saver is not currently working because the BBOP MainFrame is trying to be too smart
		new WindowSizePrefsSaver(frame, this.getClass().getName() + "mainwindow");
		return frame;
	}

	@Override
	protected void showFrame() {
		// BBOP centers and makes frame a certain size - we are overriding this
		GUIManager.getManager().getFrame().setVisible(true);
	}

	@Override
	protected LayoutDriver createLayoutDriver() {
		final LayoutDriver driver = super.createLayoutDriver();
		if (driver instanceof IDWDriver) {
			((IDWDriver)driver).setCustomTheme(new NativeDockingTheme());
		}
		driver.setSaveLayoutOnExit(false);
		return driver;
	}

	@Override
	protected String getPerspectiveResourceDir() {
		return "org/phenoscape/view/layouts";
	}

	@Override
	protected String getDefaultPerspectiveResourcePath() {
		if (getPerspectiveResourceDir() != null)
			return getPerspectiveResourceDir() + "/default.idw";
		else
			return null;
	}

	@Override
	public File getPrefsDir() {
		return CrossPlatform.getUserPreferencesFolder(this.getAppID());
	}

	@Override
	protected void installSystemListeners() {
		GUIManager.addVetoableShutdownListener(new VetoableShutdownListener() {
			@Override
			public boolean willShutdown() {
				return controller.canCloseDocument();
			}
		});
	}

	@Override
	protected void doOtherInstallations() {
		super.doOtherInstallations();
		new SelectionBridge(this.controller.getOntologyCoordinator().getSelectionManager()).install();
	}

	@Override
	protected Collection<? extends JMenuItem> getDefaultMenus() {
		return (new MenuFactory(this.controller)).createMenus();
	}

	@Override
	protected Collection<GUITask> getDefaultTasks() {
		// OBO-Edit startup task adds some things we don't want
		// hopefully none of these tasks are needed for operations in Phenex
		return new ArrayList<GUITask>();
	}

	private void flashJFrameToMakeSplashScreenGoAway() {
		final JFrame frame = new JFrame();
		frame.setVisible(true);
		frame.setVisible(false);
	}

	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
