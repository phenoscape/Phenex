package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.apache.log4j.Logger;
import org.obo.annotation.view.TermRenderer;
import org.obo.app.swing.BugWorkaroundTable;
import org.obo.app.swing.PlaceholderRenderer;
import org.obo.app.swing.SortDisabler;
import org.obo.app.util.EverythingEqualComparator;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.Association;
import org.phenoscape.model.Character;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.MatrixCell;
import org.phenoscape.model.MultipleState;
import org.phenoscape.model.MultipleState.MODE;
import org.phenoscape.model.State;
import org.phenoscape.model.Taxon;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.eekboom.utils.Strings;

public class CharacterMatrixComponent extends PhenoscapeGUIComponent {

	private EventTableModel<Taxon> headerModel;
	private EventTableModel<Taxon> matrixTableModel;
	private JTable matrixTable;
	private DefaultCellEditor popupEditor;
	private DefaultCellEditor quickEditor;
	private final SortedList<Taxon> sortedTaxa = new SortedList<Taxon>(this.getController().getDataSet().getTaxa(), new EverythingEqualComparator<Taxon>());
	private final EventList<State> allStates;
	private static enum TaxonDisplay {
		PUBLICATION_NAME { @Override public String toString() { return "Display Publication Name"; }},
		VALID_NAME { @Override public String toString() { return "Display Valid Name"; }},
		MATRIX_NAME { @Override public String toString() { return "Display Matrix Name"; }}
	}
	private static enum CharacterDisplay {
		CHARACTER_NUMBER { @Override public String toString() { return "Display Character Number"; }},
		CHARACTER_DESCRIPTION { @Override public String toString() { return "Display Character Description"; }}
	}
	private static enum StateDisplay {
		STATE_SYMBOL { @Override public String toString() { return "Display State Symbol"; }},
		STATE_DESCRIPTION { @Override public String toString() { return "Display State Description"; }}
	}
	private TaxonDisplay taxonOption = TaxonDisplay.VALID_NAME;
	private CharacterDisplay characterOption = CharacterDisplay.CHARACTER_NUMBER;
	private StateDisplay stateOption = StateDisplay.STATE_SYMBOL;

	public CharacterMatrixComponent(String id, PhenexController controller) {
		super(id, controller);
		this.allStates = new CollectionList<Character, State>(this.getController().getDataSet().getCharacters(),
				new CollectionList.Model<Character, State>() {
			@Override
			public List<State> getChildren(Character parent) {
				return parent.getStates();
			}
		} 
				);
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();
	}

	private void initializeInterface() {
		this.setLayout(new BorderLayout());
		this.headerModel = new EventTableModel<Taxon>(this.sortedTaxa, new HeaderTableFormat());
		final JTable headerTable = new BugWorkaroundTable(this.headerModel);
		headerTable.putClientProperty("Quaqua.Table.style", "striped");
		headerTable.setDefaultRenderer(Taxon.class, new TaxonRenderer());
		headerTable.getColumnModel().getColumn(0).setMaxWidth(40);
		final TableComparatorChooser<Taxon> sortChooser = new TableComparatorChooser<Taxon>(headerTable, this.sortedTaxa, false);
		sortChooser.addSortActionListener(new SortDisabler());
		this.matrixTableModel = new EventTableModel<Taxon>(this.sortedTaxa, new MatrixTableFormat());
		this.matrixTable = new BugWorkaroundTable(this.matrixTableModel);
		this.matrixTable.setCellSelectionEnabled(true);
		this.matrixTable.setDefaultRenderer(Object.class, new PlaceholderRenderer("None"));
		this.matrixTable.setDefaultRenderer(State.class, new StateCellRenderer());
		final JComboBox statesBox = new JComboBox();
		statesBox.setRenderer(new StateListRenderer());
		this.popupEditor = new PopupStateCellEditor(statesBox);
		this.popupEditor.setClickCountToStart(2);
		this.quickEditor = new QuickStateCellEditor(new JTextField());
		this.matrixTable.setDefaultEditor(State.class, this.popupEditor);
		this.matrixTable.putClientProperty("Quaqua.Table.style", "striped");
		this.matrixTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.getController().getDataSet().getCharacters().addListEventListener(new ListEventListener<Character>() {
			@Override
			public void listChanged(ListEvent<Character> listChanges) {
				matrixTableModel.fireTableStructureChanged();
			}
		});
		this.allStates.addListEventListener(new ListEventListener<State>() {
			@Override
			public void listChanged(ListEvent<State> listChanges) {
				matrixTableModel.fireTableDataChanged();
			}
		});
		final JScrollPane headerScroller = new JScrollPane(headerTable);
		final JScrollPane matrixScroller = new JScrollPane(matrixTable);
		headerScroller.getVerticalScrollBar().setModel(matrixScroller.getVerticalScrollBar().getModel());
		headerScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		headerScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		matrixScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, headerScroller, matrixScroller);
		splitPane.setDividerLocation(150);
		splitPane.setDividerSize(3);
		this.add(splitPane, BorderLayout.CENTER);
		this.add(this.createToolBar(), BorderLayout.SOUTH);
		this.getController().getDataSet().addPropertyChangeListener(DataSet.MATRIX_CELL, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				matrixTableModel.fireTableDataChanged();
			}
		});
		this.matrixTable.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateSelectedCell();
			}
		});
		this.matrixTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateSelectedCell();
			}
		});
	}

	private void updateSelectedCell() {
		final Taxon selectedTaxon;
		if (this.matrixTable.getSelectedRow() != -1) {
			selectedTaxon = this.matrixTableModel.getElementAt(this.matrixTable.getSelectedRow());
		} else {
			selectedTaxon = null;
		}
		final Character selectedCharacter;
		if (this.matrixTable.getSelectedColumn() != -1) {
			selectedCharacter = this.getCharacter(this.matrixTable.getSelectedColumn());
		} else {
			selectedCharacter = null;
		}
		if (selectedTaxon != null && selectedCharacter != null) {
			this.getController().setSelectedMatrixCell(new MatrixCell(selectedTaxon, selectedCharacter));
		} else {
			this.getController().setSelectedMatrixCell(null);
		}
	}

	private JToolBar createToolBar() {
		final JToolBar toolBar = new JToolBar();
		final JComboBox taxonBox = new JComboBox(TaxonDisplay.values());
		taxonBox.setSelectedItem(this.taxonOption);
		taxonBox.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				taxonOption = (TaxonDisplay)(taxonBox.getSelectedItem());
				headerModel.fireTableDataChanged();
			}
		});
		final JComboBox characterBox = new JComboBox(CharacterDisplay.values());
		characterBox.setSelectedItem(this.characterOption);
		characterBox.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				characterOption = (CharacterDisplay)(characterBox.getSelectedItem());
				matrixTableModel.fireTableStructureChanged();
			}
		});
		final JComboBox stateBox = new JComboBox(StateDisplay.values());
		stateBox.setSelectedItem(this.stateOption);
		stateBox.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stateOption = (StateDisplay)(stateBox.getSelectedItem());
				matrixTableModel.fireTableDataChanged();
			}
		});
		final JCheckBox editorTypeCheckBox = new JCheckBox("Use quick editor");
		editorTypeCheckBox.setSelected(false);
		editorTypeCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					matrixTable.setDefaultEditor(State.class, quickEditor);
				} else {
					matrixTable.setDefaultEditor(State.class, popupEditor);
				}
			}});
		toolBar.add(taxonBox);
		toolBar.add(characterBox);
		toolBar.add(stateBox);
		toolBar.add(editorTypeCheckBox);
		toolBar.setFloatable(false);
		return toolBar;
	}

	private Character getCharacter(int index) {
		return getController().getDataSet().getCharacters().get(index);
	}

	private class HeaderTableFormat implements AdvancedTableFormat<Taxon> {

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0: return Integer.class;
			case 1: return Taxon.class;
			default: return null;
			}
		}

		@Override
		public Comparator<?> getColumnComparator(int column) {
			switch (column) {
			case 0: return GlazedLists.comparableComparator();
			case 1: return new Comparator<Taxon>() {
				@Override
				public int compare(Taxon o1, Taxon o2) {
					if (taxonOption.equals(TaxonDisplay.VALID_NAME)) {
						return GlazedLists.comparableComparator().compare(o1.getValidName(), o2.getValidName());
					} else if (taxonOption.equals(TaxonDisplay.PUBLICATION_NAME)){
						return Strings.getNaturalComparator().compare(o1.getPublicationName(), o2.getPublicationName());
					} else { // MATRIX_NAME
						return Strings.getNaturalComparator().compare(o1.getMatrixTaxonName(), o2.getMatrixTaxonName());
					}
				}
			};
			default: return null;
			}
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			switch(column) {
			case 0: return " ";
			case 1: return "Taxon";
			default: return null;
			}
		}

		@Override
		public Object getColumnValue(Taxon taxon, int column) {
			switch(column) {
			case 0: return getController().getDataSet().getTaxa().indexOf(taxon) + 1;
			case 1: return taxon;
			default: return null;
			}
		}

	}

	private class MatrixTableFormat implements AdvancedTableFormat<Taxon>, WritableTableFormat<Taxon> {

		@Override
		public int getColumnCount() {
			return getController().getDataSet().getCharacters().size();
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return State.class;
		}

		@Override
		public Comparator<?> getColumnComparator(int column) {
			return null;
		}

		@Override
		public String getColumnName(int column) {
			if (characterOption.equals(CharacterDisplay.CHARACTER_DESCRIPTION)) {
				return getCharacter(column).toString();
			} else if (characterOption.equals(CharacterDisplay.CHARACTER_NUMBER)) {
				return "" + (column + 1);
			}
			return null;
		}

		@Override
		public Object getColumnValue(Taxon taxon, int column) {
			return getController().getDataSet().getStateForTaxon(taxon, getCharacter(column));
		}

		@Override
		public boolean isEditable(Taxon baseObject, int column) {
			return true;
		}

		@Override
		public Taxon setColumnValue(Taxon taxon, Object editedValue, int column) {
			getController().getDataSet().setStateForTaxon(taxon, getCharacter(column), (State)editedValue);
			return taxon;
		}

	}

	private class StateCellRenderer extends PlaceholderRenderer {

		public StateCellRenderer() {
			super("?");
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			final State state = (State)value;
			if (state != null) {
				final Object newValue;
				if (stateOption.equals(StateDisplay.STATE_SYMBOL)) {
					newValue = state.getSymbol() != null ? state.getSymbol() : "#";
				}  else {
					newValue = state.getLabel() != null ? state.getLabel() : "untitled";
				}
				return super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
			} else {
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}

		}

	}

	private class StateListRenderer extends BasicComboBoxRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			final Object newValue = value != null ? value : "?";
			return super.getListCellRendererComponent(list, newValue, index, isSelected, cellHasFocus);
		}

	}

	private class PopupStateCellEditor extends DefaultCellEditor {

		private final DefaultComboBoxModel model = new DefaultComboBoxModel();

		public PopupStateCellEditor(JComboBox comboBox) {
			super(comboBox);
			comboBox.setModel(model);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			final Character character = getController().getDataSet().getCharacters().get(column);
			this.model.removeAllElements();
			this.model.addElement(null);
			for (State state : character.getStates()) {
				this.model.addElement(state);
			}
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}

	}

	private class QuickStateCellEditor extends DefaultCellEditor {

		private List<State> states = new ArrayList<State>();
		private State originalValue;
		private State currentValue;
		private final JTextField field;
		private boolean invalid = false;

		public QuickStateCellEditor(JTextField textField) {
			super(textField);
			this.field = textField;
			this.field.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			textField.getDocument().addDocumentListener(new FieldListener());
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			final Character character = getController().getDataSet().getCharacters().get(column);
			this.states = character.getStates();
			final State state = (State)value;
			final Object newValue = state != null ? state.getSymbol() : null;
			this.originalValue = state;
			this.currentValue = state;
			final Component component = super.getTableCellEditorComponent(table, newValue, isSelected, row, column);
			this.field.selectAll();
			return component;
		}

		@Override
		public void cancelCellEditing() {
			this.currentValue = this.originalValue;
			super.cancelCellEditing();
		}

		@Override
		public Object getCellEditorValue() {
			return this.currentValue;
		}

		@Override
		public boolean stopCellEditing() {
			if (this.getInvalid()) {
				return false;
			}
			return super.stopCellEditing();
		}

		private boolean getInvalid() {
			return this.invalid;
		}

		private void setInvalid(boolean value) {
			this.invalid = value;
			if (this.invalid) {
				field.setForeground(Color.RED);
			} else {
				field.setForeground(Color.BLACK);
			}
		}

		private class FieldListener implements DocumentListener {

			@Override
			public void changedUpdate(DocumentEvent e) { this.documentChanged(); }

			@Override
			public void insertUpdate(DocumentEvent e) { this.documentChanged(); }

			@Override
			public void removeUpdate(DocumentEvent e) { this.documentChanged(); }

			private void documentChanged() {
				final String text = field.getText();
				if ((text == null) || (text.equals(""))) {
					currentValue = null;
					setInvalid(false);
				} else {
					final State foundState = this.interpretEntry(text);
					if (foundState != null) {
						currentValue = foundState;
						setInvalid(false);
					} else {
						setInvalid(true);
					}
				}
			}

			private State interpretEntry(String text) {
				log().debug("Interpret entry: " + text);
				if (text.contains("&")) {
					final Set<State> multipleStates = this.interpretStateSymbols(text.split("&"));
					if (multipleStates != null) {
						return new MultipleState(multipleStates, MODE.POLYMORPHIC);
					} else {
						return null;
					}
				} else if (text.contains("/")) {
					final Set<State> multipleStates = this.interpretStateSymbols(text.split("/"));
					if (multipleStates != null) {
						return new MultipleState(multipleStates, MODE.UNCERTAIN);
					} else {
						return null;
					}
				} else {
					for (State state : states) {
						if (text.equals(state.getSymbol())) {
							return state;
						}
					}
				}
				return null;
			}

			private Set<State> interpretStateSymbols(String[] symbols) {
				if (symbols.length == 1) {
					return null;
				}
				final Set<State> multipleStates = new HashSet<State>();
				for (String symbol : symbols) {
					for (State state : states) {
						if (symbol.equals(state.getSymbol())) {
							multipleStates.add(state);
						}
					}
				}
				if (symbols.length == multipleStates.size()) {
					return multipleStates;
				} else {
					return null;
				}

			}

		}

	}

	private class TaxonRenderer extends TermRenderer {

		public TaxonRenderer() {
			super("untitled");
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (taxonOption.equals(TaxonDisplay.VALID_NAME)) {
				final Object newValue = value != null ? ((Taxon)value).getValidName() : value;
				return super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
			} else {
				final Object newValue;
				if (value != null) {
					newValue = taxonOption.equals(TaxonDisplay.PUBLICATION_NAME) ? ((Taxon)value).getPublicationName() : ((Taxon)value).getMatrixTaxonName();
				} else {
					newValue = value;
				}
				return super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
			}
		}

	}

	@Override
	protected Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
