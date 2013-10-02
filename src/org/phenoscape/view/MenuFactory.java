package org.phenoscape.view;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.framework.ViewMenu;
import org.obo.app.controller.DocumentController.AutosaveChangeListener;
import org.obo.app.swing.ResponderChainAction;
import org.obo.app.util.CrossPlatform;
import org.phenoscape.controller.PhenexController;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingExecutionException;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

public class MenuFactory {

	private final PhenexController controller;

	public MenuFactory(PhenexController controller) {
		this.controller = controller;
	}

	public Collection<? extends JMenuItem> createMenus() {
		Collection<JMenuItem> menus = new ArrayList<JMenuItem>();
		menus.add(this.createFileMenu());
		menus.add(this.createEditMenu());
		final ViewMenu bbopViewMenu = new ViewMenu();
		final JMenuItem perspectivesMenu = bbopViewMenu.getItem(0);
		final JMenuItem viewMenu = bbopViewMenu.getItem(1);
		perspectivesMenu.setText("Window");
		viewMenu.setText("View");
		menus.add(viewMenu);
		menus.add(this.createORBMenu());
		menus.add(perspectivesMenu);
		menus.add(this.createHelpMenu());
		return menus;
	}

	private JMenuItem createFileMenu() {
		final JMenu menu = new JMenu("File");
		final Action openAction = new AbstractAction("Open...") {
			@Override
			public void actionPerformed(ActionEvent e) { controller.open(); }
		};
		openAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(new JMenuItem(openAction));
		final Action importPhenotypeProposals = new AbstractAction("Phenotype proposals...") {
			@Override
			public void actionPerformed(ActionEvent e) { controller.openImportPhenotypeProposals(); }
		};
		final JMenu mergeMenu = new JMenu("Import");
		mergeMenu.add(new JMenuItem(importPhenotypeProposals));
		menu.add(mergeMenu);
		menu.addSeparator();
		final Action saveAction = new AbstractAction("Save") {
			@Override
			public void actionPerformed(ActionEvent e) { controller.save(); }
		};
		saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(new JMenuItem(saveAction));
		final Action saveAsAction = new AbstractAction("Save As...") {
			@Override
			public void actionPerformed(ActionEvent e) { controller.saveAs(); }
		};
		saveAsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
		menu.add(new JMenuItem(saveAsAction));
		final Action exportExcelAction = new AbstractAction("Export for Excel...") {
			@Override
			public void actionPerformed(ActionEvent e) { controller.exportToExcel(); }
		};
		menu.add(new JMenuItem(exportExcelAction));
		final Action exportBiocreativeAction = new AbstractAction("Export for Charaparser Evaluation...") {
			@Override
			public void actionPerformed(ActionEvent e) { controller.exportForCharaparserEvaluation(); }
		};
		menu.add(new JMenuItem(exportBiocreativeAction));
		if (CrossPlatform.shouldPutExitInFileMenu()) {
			menu.addSeparator();
			final Action exitAction = new AbstractAction("Exit") {
				@Override
				public void actionPerformed(ActionEvent e) { GUIManager.exit(0); }
			};
			menu.add(new JMenuItem(exitAction));
		}
		menu.addSeparator();
		final Action enableAutosaveAction = new AbstractAction("Enable Autosave") {
			@Override
			public void actionPerformed(ActionEvent e) { controller.toggleAutosave(); }  

		};
		final JCheckBoxMenuItem autosave = new JCheckBoxMenuItem(enableAutosaveAction);
		autosave.setSelected(controller.getShouldAutosave());
		controller.addAutosaveChangeListener(new AutosaveChangeListener() {
			@Override
			public void autosaveSettingChanged(boolean shouldAutosave) {
				autosave.setSelected(shouldAutosave);
			}
		});
		menu.add(autosave);
		return menu;
	}

	private JMenuItem createEditMenu() {
		final JMenu menu = new JMenu("Edit");
		final Action undo = this.controller.getUndoController().getUndoAction();
		undo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(new JMenuItem(undo));
		final Action redo = this.controller.getUndoController().getRedoAction();
		redo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
		menu.add(new JMenuItem(redo));
		menu.addSeparator();
		final Action cutAction = new ResponderChainAction("cut", "Cut");
		cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(new JMenuItem(cutAction));
		final Action copyAction = new ResponderChainAction("copy", "Copy");
		copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(new JMenuItem(copyAction));
		final Action pasteAction = new ResponderChainAction("paste", "Paste");
		pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(new JMenuItem(pasteAction));
		final Action selectAllAction = new ResponderChainAction("selectAll", "Select All");
		selectAllAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(new JMenuItem(selectAllAction));
		menu.addSeparator();
		final Action consolidateSelectedCharactersAction = new ResponderChainAction("consolidateSelectedCharacters", "Consolidate Selected Characters");
		menu.add(new JMenuItem(consolidateSelectedCharactersAction));
		final Action consolidateSelectedStatesAction = new ResponderChainAction("consolidateSelectedStates", "Consolidate Selected States");
		menu.add(new JMenuItem(consolidateSelectedStatesAction));
		final Action newCharacterWithSelectedStatesAction = new ResponderChainAction("createNewCharacterWithSelectedStates", "New Character With Selected States");
		menu.add(new JMenuItem(newCharacterWithSelectedStatesAction));
		menu.addSeparator();
		final Action generateTreeAction = new AbstractAction("Generate Tree From Taxonomy Ontology") {
			@Override
			public void actionPerformed(ActionEvent e) { controller.generateTree(); }
		};
		menu.add(new JMenuItem(generateTreeAction));
		return menu;
	}

	private JMenuItem createORBMenu() {
		final JMenu menu = new JMenu("ORB");
		final Action termRequestAction = new AbstractAction("Request New Term...") {
			@Override
			public void actionPerformed(ActionEvent e) { controller.runORBTermRequest(); }
		};
		menu.add(new JMenuItem(termRequestAction));
		return menu;
	}

	private JMenuItem createHelpMenu() {
		final JMenu menu = new JMenu("Help");
		final Action homepageAction = new AbstractAction("Phenex Homepage") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					(new BrowserLauncher(null)).openURLinBrowser("http://www.phenoscape.org/wiki/Phenex");
				} catch (BrowserLaunchingInitializingException e1) {
					log().error("Unable to open URL in browser", e1);
					e1.printStackTrace();
				} catch (UnsupportedOperatingSystemException e1) {
					log().error("Unable to open URL in browser", e1);
				} catch (BrowserLaunchingExecutionException e1) {
					log().error("Unable to open URL in browser", e1);
				}
			}
		};
		final Action trackerAction = new AbstractAction("Submit Bug Report or Feature Request...") {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					(new BrowserLauncher(null)).openURLinBrowser("https://sourceforge.net/tracker/?func=add&group_id=76834&atid=1116214");
				} catch (BrowserLaunchingInitializingException e1) {
					log().error("Unable to open URL in browser", e1);
					e1.printStackTrace();
				} catch (UnsupportedOperatingSystemException e1) {
					log().error("Unable to open URL in browser", e1);
				} catch (BrowserLaunchingExecutionException e1) {
					log().error("Unable to open URL in browser", e1);
				}
			}
		};
		menu.add(homepageAction);
		menu.add(trackerAction);
		return menu;
	}

	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
