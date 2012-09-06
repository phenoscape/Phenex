package org.obo.app.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.ComboPopup;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventComboBoxModel;

public class AutocompleteField<T> extends JComponent {

	private final AutocompleteComboBox comboBox = new AutocompleteComboBox();
	private final List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private AutocompleteSearcher<T> searcher;
	private final EventList<SearchHit<T>> completeList = new BasicEventList<SearchHit<T>>();
	private final ComboBoxModel comboBoxModel = new EventComboBoxModel<SearchHit<T>>(completeList);
	private Action action;
	private T currentValue;
	private boolean changingCompletionList = false;
	private boolean externallySettingText = false;
	private boolean hasBeenEdited = false;
	private Object lastHighlightedItem = null;

	public AutocompleteField(AutocompleteSearcher<T> searcher) {
		this.searcher = searcher;
		this.comboBox.setEditable(true);
		this.comboBox.setEditor(new AutocompleteEditor());
		this.comboBox.setModel(this.comboBoxModel);
		final TextFieldListener textFieldListener = new TextFieldListener();
		this.getEditorField().getDocument().addDocumentListener(textFieldListener);
		this.comboBox.addActionListener(new AutocompleteActionListener());
		this.comboBox.setRenderer(new AutocompleteRenderer());
		this.getListComponent().addListSelectionListener(new CompletionListListener());
		this.getListComponent().setVerifyInputWhenFocusTarget(false);
		this.setLayout(new BorderLayout());
		this.add(this.comboBox, BorderLayout.CENTER);
	}

	public AutocompleteSearcher<T> getSearcher() {
		return this.searcher;
	}

	public void setSearcher(AutocompleteSearcher<T> newSearcher) {
		this.searcher = newSearcher;
	}

	public void setValue(T value) {
		this.hasBeenEdited = false;
		lastHighlightedItem = null;
		this.internallySetValue(value);
	}

	private void internallySetValue(T value) {
		this.currentValue = value;
		this.externallySettingText = true;
		this.comboBox.setSelectedItem(null);
		getEditorField().setText(this.getSearcher().toString(value));
	}

	public T getValue() {
		return this.currentValue;
	}

	public void addActionListener(ActionListener l) {
		this.actionListeners.add(l);
	}

	public void removeActionListener(ActionListener l) {
		this.actionListeners.remove(l);
	}

	public void setAction(Action a) {
		if (this.action != null) {
			this.removeActionListener(this.action);
		}
		this.action = a;
		this.addActionListener(this.action);
	}

	public Action getAction() {
		return this.action;
	}

	public JList getListComponent() {
		final ComboPopup popup = this.getComboPopup(this.getComboBox());
		if (popup != null) {
			return popup.getList();
		}
		return null;
	}

	protected void fireActionPerformed() {
		if (!this.hasBeenEdited) return;
		for (ActionListener l : this.actionListeners) {
			l.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "value changed"));
		}
	}

	@Override
	public void setInputVerifier(InputVerifier inputVerifier) {
		this.getEditorField().setInputVerifier(inputVerifier);
	}

	protected JComboBox getComboBox() {
		return this.comboBox;
	}

	private void queryWithInput() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				getSearcher().setSearch(getEditorField().getText());
				final List<SearchHit<T>> matches = getSearcher().getMatches();
				if (!matches.isEmpty()) {
					//TODO maybe autofill textbox?  only if starts with input?
				}
				changingCompletionList = true;
				completeList.clear();
				completeList.addAll(matches.size() > 50 ? matches.subList(0, 50) : matches); //TODO there is a magic number here
				changingCompletionList = false;
				comboBox.setPopupVisible(false);
				comboBox.showPopup();
			}
		});
	}

	private void setValueWithInput(SearchHit<T> hit) {
		this.internallySetValue(hit.getHit());
		this.fireActionPerformed();
	}

	private void setValueWithTextInput(String text) {
		final SearchHit<T> hit;
		// if there is more than one exact hit for the text we want the one we had before
		if (this.getSearcher().isSame(text, this.currentValue)) {
			hit = this.getSearcher().getAsHit(this.currentValue);
		} else {
			hit = this.getSearcher().getExactHit(text);
		}
		if (hit != null) {
			comboBox.setSelectedItem(hit);
			this.setValueWithInput(hit);
		} else if (text.trim().equals("")) { 
			this.internallySetValue(null);
			this.fireActionPerformed();
		} else {
			this.comboBox.setForeground(Color.RED);
			this.getEditorField().setForeground(Color.RED);
		}
	}

	private class TextFieldListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
			this.textChanged();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			this.textChanged();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			this.textChanged();
		}

		private void textChanged() {
			comboBox.setForeground(Color.BLACK);
			getEditorField().setForeground(Color.BLACK);
			if (externallySettingText) {
				return;
			}
			hasBeenEdited = true;
			comboBox.setPotentialValue(null);
			queryWithInput();
		}

	}

	private JTextField getEditorField() {
		final Component component = this.comboBox.getEditor().getEditorComponent();
		if (component instanceof JTextField) {
			return (JTextField)component;
		} else {
			log().fatal("Combobox doesn't use a JTextField for editor.");
			return null;
		}
	}

	protected ComboPopup getComboPopup(JComboBox comboBox) {
		final AccessibleContext ac = comboBox.getAccessibleContext();
		for (int i = 0; i < ac.getAccessibleChildrenCount(); i++) {
			final Accessible a = ac.getAccessibleChild(i);
			if (a instanceof ComboPopup) { return (ComboPopup)a; }
		}
		log().error("Can't retrieve popup from combobox; can't do mouse overs");
		return null;
	}

	private class AutocompleteComboBox extends JComboBox {

		private Object potentialValue = null;

		public AutocompleteComboBox() {
			super();
		}

		public Object getPotentialValue() {
			return this.potentialValue;
		}

		public void setPotentialValue(Object object) {
			this.potentialValue = object;
		}

		@Override
		public void setSelectedItem(Object anObject) {
			this.potentialValue = anObject;
			super.setSelectedItem(anObject);
		}

		@Override
		public void firePopupMenuWillBecomeVisible() {
			hasBeenEdited = true;
			super.firePopupMenuWillBecomeVisible();
		}

		@Override
		public void setInputVerifier(InputVerifier inputVerifier) {
			getEditorField().setInputVerifier(inputVerifier);
		}

	}

	private class AutocompleteEditor extends BasicComboBoxEditor {

		public AutocompleteEditor() {
			super();
			this.editor = new AutocompleteTextField();
			this.correctInputMapForEditorField(this.editor);
		}

		@Override
		public void setItem(Object anObject) {
			return;
		}

		/**
		 * This method replaces the AutoTextFieldEditor's InputMap with the same one
		 * the current look and feel would use.  This was a problem for keyboard selection
		 * when using the Quaqua look and feel.
		 */
		private void correctInputMapForEditorField(JTextField field) {
			final Component systemComboBoxEditor = new JComboBox().getEditor().getEditorComponent();
			if (systemComboBoxEditor instanceof JComponent) {
				final InputMap map = ((JComponent)systemComboBoxEditor).getInputMap();
				SwingUtilities.replaceUIInputMap(field, JComponent.WHEN_FOCUSED, map);
			}
		}

	}

	private class AutocompleteTextField extends JTextField {

		@Override
		public void setText(String t) {
			if (changingCompletionList) return;
			super.setText(t);
			externallySettingText = false;
		}

	}

	private class AutocompleteActionListener implements ActionListener {

		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("comboBoxEdited") || e.getModifiers() != 0) {
				final Object selectedItem = comboBox.getSelectedItem();
				final Object potentialValue = lastHighlightedItem;
				if (potentialValue instanceof SearchHit) { //TODO need to check for null pv!!!
					final SearchHit<T> hit = (SearchHit<T>)potentialValue; 
					comboBox.setSelectedItem(hit);
					setValueWithInput(hit);
				} else if ((potentialValue == null) && (selectedItem instanceof String)){
					setValueWithTextInput((String)selectedItem);
				}
			}
		}

	}

	private class CompletionListListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent event) {
			final Object source = event.getSource();
			if (source instanceof JList) {
				final JList menu = (JList)source;
				try {
					final Object item = menu.getSelectedValue();
					if (item != null) {
						lastHighlightedItem = item;
					}
				} catch (IndexOutOfBoundsException e) {
					// for some reason sometimes the menu selection is not valid
				}
			} else {
				log().error("Source of combobox mouse over event is not JList");
			}
		}

	}

	private static class AutocompleteRenderer extends BasicComboBoxRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			final SearchHit<?> hit = (SearchHit<?>)value;
			final String displayValue = "<html>" + hit.getMatchText() + " <i><font color=\"gray\" size=\"small\">" + hit.getMatchType().getName() + "</font></i></html>";
			return super.getListCellRendererComponent(list, displayValue, index, isSelected,cellHasFocus);
		}

	}

	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
