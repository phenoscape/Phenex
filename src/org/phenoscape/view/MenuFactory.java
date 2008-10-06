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

import org.bbop.framework.GUIManager;
import org.bbop.framework.ViewMenu;
import org.phenoscape.app.CrossPlatform;
import org.phenoscape.model.PhenoscapeController;

import phenote.gui.actions.ResponderChainAction;

public class MenuFactory {
  
  private final PhenoscapeController controller;
  
  public MenuFactory(PhenoscapeController controller) {
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
    final Action mergeMatrixAction = new AbstractAction("NEXUS Matrix...") {
      public void actionPerformed(ActionEvent e) { controller.openMergeNEXUS(); }
    };
    final JMenu mergeMenu = new JMenu("Merge");
    mergeMenu.add(new JMenuItem(mergeTaxaAction));
    mergeMenu.add(new JMenuItem(mergeCharactersAction));
    mergeMenu.add(new JMenuItem(mergeMatrixAction));
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
      final Action exitAction = new AbstractAction("Exit"){
        public void actionPerformed(ActionEvent e) { GUIManager.exit(0); }
      };
      menu.add(new JMenuItem(exitAction));
    }
    return menu;
  }
  
  private JMenuItem createEditMenu() {
    final JMenu menu = new JMenu("Edit");
    final Action undoAction = new ResponderChainAction("undo", "Undo");
    undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    undoAction.setEnabled(false); //TODO add Undo support
    menu.add(new JMenuItem(undoAction));
    final Action redoAction = new ResponderChainAction("redo", "Redo");
    redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
    redoAction.setEnabled(false); //TODO add Redo support
    menu.add(new JMenuItem(redoAction));
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

}
