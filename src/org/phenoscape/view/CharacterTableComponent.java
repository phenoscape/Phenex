package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.obo.app.swing.BugWorkaroundTable;
import org.obo.app.swing.PlaceholderRenderer;
import org.obo.app.swing.SortDisabler;
import org.obo.app.swing.TableColumnPrefsSaver;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.MultipleState;
import org.phenoscape.model.MultipleState.MODE;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.eekboom.utils.Strings;

public class CharacterTableComponent extends PhenoscapeGUIComponent {

	private JButton addCharacterButton;
	private JButton deleteCharacterButton;

	public CharacterTableComponent(String id, PhenexController controller) {
		super(id, controller);
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();
	}

	public void consolidateSelectedCharacters() {
		//FIXME some of this logic should be moved down into the model
		final List<Character> characters = Collections.unmodifiableList(this.getSelectedCharacters());
		if (characters.size() > 1) {
			final DataSet data = this.getController().getDataSet();
			final Character newCharacter = data.newCharacter();;
			final List<String> labels = new ArrayList<String>();
			final List<String> comments = new ArrayList<String>();
			final List<String> figures = new ArrayList<String>();
			final List<String> discussions = new ArrayList<String>();
			for (Character character : characters) {
				labels.add(character.getLabel());
				comments.add(character.getComment());
				figures.add(character.getFigure());
				discussions.add(character.getDiscussion());
				newCharacter.addStates(character.getStates());
			}
			newCharacter.setLabel(StringUtils.stripToNull(org.obo.app.util.Collections.join(labels, "; ")));
			newCharacter.setComment(StringUtils.stripToNull(org.obo.app.util.Collections.join(comments, "; ")));
			newCharacter.setFigure(StringUtils.stripToNull(org.obo.app.util.Collections.join(figures, "; ")));
			newCharacter.setDiscussion(StringUtils.stripToNull(org.obo.app.util.Collections.join(discussions, "; ")));
			for (Taxon taxon : data.getTaxa()) {
				final Set<State> statesForTaxon = new HashSet<State>();
				for (Character character : characters) {
					final State stateValue = data.getStateForTaxon(taxon, character);
					if (stateValue instanceof MultipleState) {
						statesForTaxon.addAll(((MultipleState)stateValue).getStates());
					} else if (stateValue != null) {
						statesForTaxon.add(stateValue);
					}
				}
				final State newStateValue;
				if (statesForTaxon.size() > 1) {
					if (statesForTaxon.contains(null)) {
						log().debug("We created a null state?");
						log().debug(statesForTaxon);
					}
					newStateValue = new MultipleState(statesForTaxon, MODE.POLYMORPHIC);
				} else if (statesForTaxon.size() == 1) {
					newStateValue = statesForTaxon.iterator().next();
				} else {
					newStateValue = null;
				}
				data.setStateForTaxon(taxon, newCharacter, newStateValue);
			}
			data.removeCharacters(characters);
			int i = 0;
			for (State state : newCharacter.getStates()) {
				state.setSymbol("" + i);
				i += 1;
			}
		}
	}

	private void initializeInterface() {
		this.setLayout(new BorderLayout());
		final EventTableModel<Character> charactersTableModel = new EventTableModel<Character>(this.getController().getSortedCharacters(), new CharactersTableFormat());
		final JTable charactersTable = new BugWorkaroundTable(charactersTableModel);
		charactersTable.setSelectionModel(this.getController().getCharactersSelectionModel());
		charactersTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
		charactersTable.putClientProperty("Quaqua.Table.style", "striped");
		new TableColumnPrefsSaver(charactersTable, this.getClass().getName());
		final TableComparatorChooser<Character> sortChooser = new TableComparatorChooser<Character>(charactersTable, this.getController().getSortedCharacters(), false);
		sortChooser.addSortActionListener(new SortDisabler());
		this.add(new JScrollPane(charactersTable), BorderLayout.CENTER);
		this.add(this.createToolBar(), BorderLayout.NORTH);
		this.getController().getCharactersSelectionModel().addListSelectionListener(new CharacterSelectionListener());
	}

	private void addCharacter() {
		this.getController().getDataSet().newCharacter();
	}

	private void deleteSelectedCharacter() {
		final List<Character> characters = Collections.unmodifiableList(this.getSelectedCharacters());
		final DataSet data = this.getController().getDataSet();
		data.getCharacters().removeAll(characters);
	}

	private List<Character> getSelectedCharacters() {
		return this.getController().getCharactersSelectionModel().getSelected();
	}

	private void updateButtonStates() {
		this.deleteCharacterButton.setEnabled(!this.getSelectedCharacters().isEmpty());
	}

	private void selectFirstState() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (!getController().getStatesForCurrentCharacterSelection().isEmpty()) {
					getController().getCurrentStatesSelectionModel().setSelectionInterval(0, 0);
				}
			}
		});
	}

	private JToolBar createToolBar() {
		final JToolBar toolBar = new JToolBar();
		this.addCharacterButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-add.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				addCharacter();
			}
		});
		this.addCharacterButton.setToolTipText("Add Character");
		toolBar.add(this.addCharacterButton);
		this.deleteCharacterButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-remove.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedCharacter();
			}
		});
		this.deleteCharacterButton.setToolTipText("Delete Character");
		toolBar.add(this.deleteCharacterButton);
		toolBar.setFloatable(false);
		return toolBar;
	}

	private class CharactersTableFormat implements WritableTableFormat<Character>, AdvancedTableFormat<Character> {

		@Override
		public boolean isEditable(Character character, int column) {
			return column != 0;
		}

		@Override
		public Character setColumnValue(Character character, Object editedValue, int column) {
			switch(column) {
			case 0: break;
			case 1: character.setLabel(editedValue.toString()); break;
			case 2: character.setComment(editedValue.toString()); break;
			case 3: character.setFigure(editedValue.toString()); break;
			case 4: character.setDiscussion(editedValue.toString()); break;
			}
			return character;
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public String getColumnName(int column) {
			switch(column) {
			case 0: return " ";
			case 1: return "Character Description";
			case 2: return "Comment";
			case 3: return "Figure";
			case 4: return "Discussion";
			default: return null;
			}
		}

		@Override
		public Object getColumnValue(Character character, int column) {
			switch(column) {
			case 0: return getController().getDataSet().getCharacters().indexOf(character) + 1;
			case 1: return character.getLabel();
			case 2: return character.getComment();
			case 3: return character.getFigure();
			case 4: return character.getDiscussion();
			default: return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch(column) {
			case 0: return Integer.class;
			case 1: return String.class;
			case 2: return String.class;
			case 3: return String.class;
			case 4: return String.class;
			default: return null;
			}
		}

		@Override
		public Comparator<?> getColumnComparator(int column) {
			switch(column) {
			case 0: return GlazedLists.comparableComparator();
			case 1: return Strings.getNaturalComparator();
			case 2: return Strings.getNaturalComparator();
			case 3: return Strings.getNaturalComparator();
			case 4: return Strings.getNaturalComparator();
			default: return null;
			}
		}

	}

	private class CharacterSelectionListener implements ListSelectionListener {

		public CharacterSelectionListener() {
			updateButtonStates();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			updateButtonStates();
			selectFirstState();
		}

	}

	@Override
	protected Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
