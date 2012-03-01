package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.obo.app.swing.BugWorkaroundTable;
import org.obo.app.swing.PlaceholderRenderer;
import org.obo.app.swing.SortDisabler;
import org.obo.app.swing.TableColumnPrefsSaver;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.Character;
import org.phenoscape.model.State;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.eekboom.utils.Strings;

public class StateTableComponent extends PhenoscapeGUIComponent {

    private JButton addStateButton;
    private JButton deleteStateButton;

    public StateTableComponent(String id, PhenexController controller) {
        super(id, controller);
    }

    @Override
    public void init() {
        super.init();
        this.initializeInterface();
    }

    private void initializeInterface() {
        this.setLayout(new BorderLayout());
        final EventTableModel<State> statesTableModel = new EventTableModel<State>(this.getController().getStatesForCurrentCharacterSelection(), new StatesTableFormat());
        final JTable statesTable = new BugWorkaroundTable(statesTableModel);
        statesTable.setSelectionModel(this.getController().getCurrentStatesSelectionModel());
        statesTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
        statesTable.putClientProperty("Quaqua.Table.style", "striped");
        new TableColumnPrefsSaver(statesTable, this.getClass().getName());
        final TableComparatorChooser<State> sortChooser = new TableComparatorChooser<State>(statesTable, this.getController().getStatesForCurrentCharacterSelection(), false);
        sortChooser.addSortActionListener(new SortDisabler());
        this.add(new JScrollPane(statesTable), BorderLayout.CENTER);
        this.add(this.createToolBar(), BorderLayout.NORTH);
        this.getController().getCharactersSelectionModel().addListSelectionListener(new CharacterSelectionListener());
        this.getController().getCurrentStatesSelectionModel().addListSelectionListener(new StateSelectionListener());
    }

    private void addState() {
        final Character character = this.getSelectedCharacter();
        if (character != null) { character.newState(); }
    }

    private void deleteSelectedState() {
        final Character character = this.getSelectedCharacter();
        if (character != null) {
            final State state = this.getSelectedState();
            if (state != null) { character.removeState(state); }
        }
    }

    private Character getSelectedCharacter() {
        final EventList<Character> selected = this.getController().getCharactersSelectionModel().getSelected();
        if (selected.size() == 1) {
            return selected.get(0);
        } else {
            return null;
        }
    }

    private State getSelectedState() {
        final EventList<State> selected = this.getController().getCurrentStatesSelectionModel().getSelected();
        if (selected.size() == 1) {
            return selected.get(0);
        } else {
            return null;
        }
    }

    private void characterSelectionDidChange() {
        final String unselectedTitle = "States";
        final String selectedPrefix = "States for Character: ";
        final List<Character> characters = this.getController().getCharactersSelectionModel().getSelected();
        if (characters.isEmpty()) {
            this.updatePanelTitle(unselectedTitle);
        } else {
            this.updatePanelTitle(selectedPrefix + characters.get(0));
        }
        this.updateButtonStates();
    }

    private void stateSelectionDidChange() {
        this.updateButtonStates();
        this.selectFirstPhenotype();
    }

    private void updateButtonStates() {
        this.addStateButton.setEnabled(this.getSelectedCharacter() != null);
        this.deleteStateButton.setEnabled(this.getSelectedState() != null);
    }

    private void selectFirstPhenotype() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (!getController().getPhenotypesForCurrentStateSelection().isEmpty()) {
                    getController().getCurrentPhenotypesSelectionModel().setSelectionInterval(0, 0);
                }
            }
        });
    }

    private JToolBar createToolBar() {
        final JToolBar toolBar = new JToolBar();
        this.addStateButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-add.png"))) {
            public void actionPerformed(ActionEvent e) {
                addState();
            }
        });
        this.addStateButton.setToolTipText("Add State");
        toolBar.add(this.addStateButton);
        this.deleteStateButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-remove.png"))) {
            public void actionPerformed(ActionEvent e) {
                deleteSelectedState();
            }
        });
        this.deleteStateButton.setToolTipText("Delete State");
        toolBar.add(this.deleteStateButton);
        toolBar.setFloatable(false);
        return toolBar;
    }

    private class StatesTableFormat implements WritableTableFormat<State>, AdvancedTableFormat<State> {

        public boolean isEditable(State state, int column) {
            return true;
        }

        public State setColumnValue(State state, Object editedValue, int column) {
            switch(column) {
            case 0: state.setSymbol(editedValue.toString()); break;
            case 1: state.setLabel(editedValue.toString()); break;
            case 2: state.setComment(editedValue.toString()); break;
            case 3: state.setFigure(editedValue.toString()); break;
            }
            return state;
        }

        public int getColumnCount() {
            return 4;
        }

        public String getColumnName(int column) {
            switch(column) {
            case 0: return "Symbol";
            case 1: return "State Description";
            case 2: return "Comment";
            case 3: return "Figure";
            default: return null;
            }
        }

        public Object getColumnValue(State state, int column) {
            switch(column) {
            case 0: return state.getSymbol();
            case 1: return state.getLabel();
            case 2: return state.getComment();
            case 3: return state.getFigure();
            default: return null;
            }
        }

        public Class<?> getColumnClass(int column) {
            switch(column) {
            case 0: return String.class;
            case 1: return String.class;
            case 2: return String.class;
            case 3: return String.class;
            default: return null;
            }
        }

        public Comparator<?> getColumnComparator(int column) {
            switch(column) {
            case 0: return Strings.getNaturalComparator();
            case 1: return Strings.getNaturalComparator();
            case 2: return Strings.getNaturalComparator();
            case 3: return Strings.getNaturalComparator();
            default: return null;
            }
        }

    }

    private class CharacterSelectionListener implements ListSelectionListener {

        public CharacterSelectionListener() {
            characterSelectionDidChange();
        }

        public void valueChanged(ListSelectionEvent e) {
            characterSelectionDidChange();      
        }

    }

    private class StateSelectionListener implements ListSelectionListener {

        public StateSelectionListener() {
            stateSelectionDidChange();
        }

        public void valueChanged(ListSelectionEvent e) {
            stateSelectionDidChange();      
        }

    }
    
}
