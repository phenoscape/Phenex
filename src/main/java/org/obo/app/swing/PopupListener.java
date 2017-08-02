package org.obo.app.swing;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

public class PopupListener extends MouseAdapter {
    private final JPopupMenu menu;
    private Point p;

    public PopupListener(JPopupMenu popupMenu) {
        this.menu = popupMenu;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    public Point getLocation() {
        return this.p;
    }

    protected void maybeShowPopup(MouseEvent e) {
        p = e.getPoint();
        if (e.isPopupTrigger()) {
            this.menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

}