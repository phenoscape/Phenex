package org.obo.annotation.view;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOObject;


/**
 * @author Nicole Washington
 * 
 * This is the toolbar to be displayed along with the {@link TermInfo2} panel.
 * The termName element gets informed by what is currently displayed in the
 * TermInfo panel.
 * 
 *
 */
public class TermInfoToolbar extends JToolBar {

    private static final Logger LOG =  Logger.getLogger(TermInfoToolbar.class);
    public static final int BUTTON_HEIGHT = 30;
    private static final int TERM_INFO_DEFAULT_WIDTH=350;
    private UseTermListener useTermListener;
    private OBOObject currentOboClass = null;
    private final int BACKBUTTONINDEX = 0;
    private final int FORWARDBUTTONINDEX = 1;
    private final int FAVBUTTONINDEX = 2;
    private final int USETERMBUTTONINDEX = 3;
    private final int GETANNOTATIONSBUTTONINDEX = 4;

    private Vector<JButton> buttons;
    private JTextArea termField;

    public void hideToolbar() {
        setVisible(false);
    }

    public void showToolbar() {
        setVisible(true);
    }

    public void setTermFieldText(OBOObject oboClass) {
        termField.setText(oboClass.getName());
        currentOboClass = oboClass;
    }

    public TermInfoToolbar() {
        super("TermInfoToolbar");
        init();
    }

    private void init() {

        buttons = new Vector<JButton>();

        //Standard things to do for browser
        //The actions ought to be created elsewhere, yeah?
        //    Action useTermAction = new UseTermAction();    


        JButton favoritesButton = new JButton();
        JButton useTermButton = new JButton();

        /*****************************************/
        favoritesButton.setToolTipText("Favorites");


        useTermButton.addActionListener(new UseTermActionListener());
        useTermButton.setToolTipText("Use Term");

        termField =  new JTextArea();
        termField.setFont(new Font("Arial", Font.BOLD, 12));
        termField.setWrapStyleWord(true);
        termField.setLineWrap(true);
        //    termField.setContentType("text/html");
        termField.setText("(no term selected)");
        termField.setPreferredSize(new Dimension(TERM_INFO_DEFAULT_WIDTH-(buttons.size()*BUTTON_HEIGHT),BUTTON_HEIGHT));
        termField.setMinimumSize(new Dimension(TERM_INFO_DEFAULT_WIDTH-(buttons.size()*BUTTON_HEIGHT),BUTTON_HEIGHT));
        //    termField.setMaximumSize(new Dimension((TERM_INFO_DEFAULT_WIDTH-(buttons.size()*BUTTON_HEIGHT)),BUTTON_HEIGHT));
        termField.setEditable(false);  

        add(termField,0);
        addSeparator();

        JButton tempButton = null;
        for (int i=0; i<buttons.size(); i++) {
            tempButton = (JButton)buttons.elementAt(i);
            if (tempButton.getIcon() != null) {
                tempButton.setText(""); //an icon-only button
                tempButton.setPreferredSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
                tempButton.setMinimumSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
                tempButton.setMaximumSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
            }
            add(tempButton);
        }

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        setRollover(true);
        showToolbar();
    }

    /** use term listener comes from selection events, and it listens for useTerm
      events, this is how UseTerm button sets terms in char field guis.
      I think this should ignore setting to null, which right now is what
      happens from normal select events (as opposed to mouse over events) */
    public void setUseTermListener (UseTermListener utl) {
        if (utl == null) return; // ignore nulling, keep previous nonnull (??)
        useTermListener = utl;
    }

    UseTermListener getUseTermListener() { return useTermListener; }

    private class UseTermActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //      System.out.println("curent term for use term="+currentOboClass);
            UseTermListener utl = useTermListener;
            if (utl == null) return;
            if (currentOboClass == null) return; // shouldnt happen
            utl.useTerm(new UseTermEvent(TermInfoToolbar.this,currentOboClass));
        }
    }

    public void setButtonStatus(String button, boolean enabled) {
        if (button.equals("forward")) {
            ((JButton)buttons.get(FORWARDBUTTONINDEX)).setEnabled(enabled);
        } else if (button.equals("back")) {
            ((JButton)buttons.get(BACKBUTTONINDEX)).setEnabled(enabled);
        } else if (button.equals("annotations")) {
            ((JButton)buttons.get(GETANNOTATIONSBUTTONINDEX)).setEnabled(enabled);
        }

    }

}


