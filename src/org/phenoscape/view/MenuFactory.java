package org.phenoscape.view;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.framework.ViewMenu;
import org.phenoscape.app.CrossPlatform;
import org.phenoscape.model.PhenexController;

import phenote.gui.actions.ResponderChainAction;
import edu.stanford.ejalbert.BrowserLauncher;
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
        final JMenuItem showMenu = bbopViewMenu.getItem(1);
        perspectivesMenu.setText("Window");
        showMenu.setText("View");
        menus.add(showMenu);
        menus.add(perspectivesMenu);
        menus.add(this.createHelpMenu());
        return menus;
    }

    private JMenuItem createFileMenu() {
        final JMenu menu = new JMenu("File");
        final Action openAction = new AbstractAction("Open...") {
            public void actionPerformed(ActionEvent e) { controller.open(); }
        };
        openAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(new JMenuItem(openAction));
        final Action mergeTaxaAction = new AbstractAction("Tab-delimited Taxa...") {
            public void actionPerformed(ActionEvent e) { controller.openMergeTaxa(); }
        };
        final Action mergeCharactersAction = new AbstractAction("Tab-delimited Characters...") {
            public void actionPerformed(ActionEvent e) { controller.openMergeCharacters(); }
        };
        final Action mergeNEXUSAction = new AbstractAction("NEXUS Data...") {
            public void actionPerformed(ActionEvent e) { controller.openMergeNEXUS(); }
        };
        final Action mergeNeXMLAction = new AbstractAction("NeXML Data...") {
            public void actionPerformed(ActionEvent e) { controller.openMergeNeXML(); }
        };
        final JMenu mergeMenu = new JMenu("Merge");
        mergeMenu.add(new JMenuItem(mergeTaxaAction));
        mergeMenu.add(new JMenuItem(mergeCharactersAction));
        mergeMenu.add(new JMenuItem(mergeNEXUSAction));
        mergeMenu.add(new JMenuItem(mergeNeXMLAction));
        menu.add(mergeMenu);
        menu.addSeparator();
        final Action saveAction = new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) { controller.save(); }
        };
        saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(new JMenuItem(saveAction));
        final Action saveAsAction = new AbstractAction("Save As...") {
            public void actionPerformed(ActionEvent e) { controller.saveAs(); }
        };
        saveAsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
        menu.add(new JMenuItem(saveAsAction));
        if (CrossPlatform.shouldPutExitInFileMenu()) {
            menu.addSeparator();
            final Action exitAction = new AbstractAction("Exit") {
                public void actionPerformed(ActionEvent e) { GUIManager.exit(0); }
            };
            menu.add(new JMenuItem(exitAction));
        }
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
        return menu;
    }

    private JMenuItem createHelpMenu() {
        final JMenu menu = new JMenu("Help");
        final Action homepageAction = new AbstractAction("Phenex Homepage") {
            public void actionPerformed(ActionEvent e) {
                try {
                    (new BrowserLauncher()).openURLinBrowser("http://www.phenoscape.org/wiki/Phenex");
                } catch (BrowserLaunchingInitializingException e1) {
                    log().error("Unable to open URL in browser", e1);
                    e1.printStackTrace();
                } catch (UnsupportedOperatingSystemException e1) {
                    log().error("Unable to open URL in browser", e1);
                }
            }
        };
        final Action trackerAction = new AbstractAction("Submit Bug Report or Feature Request...") {
            public void actionPerformed(ActionEvent e) {
                try {
                    (new BrowserLauncher()).openURLinBrowser("https://sourceforge.net/tracker/?func=add&group_id=76834&atid=1116214");
                } catch (BrowserLaunchingInitializingException e1) {
                    log().error("Unable to open URL in browser", e1);
                    e1.printStackTrace();
                } catch (UnsupportedOperatingSystemException e1) {
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
