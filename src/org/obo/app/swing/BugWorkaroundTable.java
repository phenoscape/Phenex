package org.obo.app.swing;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

/**
 * This table provides some workarounds to behaviors in Sun's JTable.
 * @author Jim Balhoff
 */
public class BugWorkaroundTable extends JTable {

	private final Set<Component> verifierAdded = new HashSet<Component>();
	/**
	 * This input verifier cancels editing on the table cell editor when the user
	 * clicks elsewhere in the window.  This addresses a bug in which the edited value
	 * was being applied to a newly selected item in another (master) table, instead
	 * of the edited one.
	 */
	private final InputVerifier verifier = new InputVerifier() {

		@Override
		public boolean verify(JComponent input) {
			return true;
		}

		@Override
		public boolean shouldYieldFocus(JComponent input) {
			final TableCellEditor editor = BugWorkaroundTable.this.getCellEditor();
			if (editor != null) editor.cancelCellEditing();
			return true;
		}            

	};

	public BugWorkaroundTable() {
		super();
		this.init();
	}

	public BugWorkaroundTable(TableModel dm) {
		super(dm);
		this.init();
	}

	public BugWorkaroundTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
		this.init();
	}

	public BugWorkaroundTable(int numRows, int numColumns) {
		super(numRows, numColumns);
		this.init();
	}

	public BugWorkaroundTable(Vector<?> rowData, Vector<?> columnNames) {
		super(rowData, columnNames);
		this.init();
	}

	public BugWorkaroundTable(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
		this.init();
	}

	public BugWorkaroundTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
		this.init();
	}

	private void init() {
		// make sure any cell editors take focus, so that:
		// 1. user can start typing immediately (especially for JComboBox)
		// 2. editor has focus and its verifier will be called when user clicks elsewhere
		this.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (BugWorkaroundTable.this.isEditing()) {
					BugWorkaroundTable.this.getEditorComponent().requestFocusInWindow();
				}
			}
			@Override
			public void focusLost(FocusEvent e) {}
		});
	}


	/**
	 * JTable incorrectly begins editing of table cells when various modifier keys are pressed.  This 
	 * results in bizarre behavior when trying to select or copy rows.
	 * See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4820794
	 * @see javax.swing.JTable#processKeyBinding(javax.swing.KeyStroke, java.awt.event.KeyEvent, int, boolean)
	 */
	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		if (e.getKeyCode() == KeyEvent.VK_CAPS_LOCK) {
			return false;
		}
		// from http://lists.apple.com/archives/Java-dev/2004/Dec/msg00283.html
		boolean retValue = false;
		if (e.getKeyCode()!=KeyEvent.VK_META || e.getKeyCode()!=KeyEvent.VK_CONTROL || e.getKeyCode()!=KeyEvent.VK_ALT) {
			if (e.isControlDown() || e.isMetaDown() || e.isAltDown()) {
				InputMap map = this.getInputMap(condition);
				ActionMap am = getActionMap();
				if (map != null && am != null && isEnabled()) {
					Object binding = map.get(ks);
					Action action = (binding == null) ? null : am.get(binding);
					if (action != null) {
						SwingUtilities.notifyAction(action, ks, e, this, e.getModifiers());
						retValue = false;
					}
					else {
						try {
							JComponent ancestor = (JComponent)
									SwingUtilities.getAncestorOfClass(Class.forName("javax.swing.JComponent"), this);
							ancestor.dispatchEvent(e);
						}
						catch (ClassNotFoundException fr) {
							log().error(fr.toString());
						}
					}
				}
				else {
					retValue = super.processKeyBinding(ks, e, condition, pressed);
				}
			}
			else {
				retValue = super.processKeyBinding(ks, e, condition, pressed);
			}
		}
		return retValue;
	}

	/** 
	 * Most convenient spot to make sure any table cell editor has the input verifier.
	 */
	@Override
	public Component getEditorComponent() {
		final Component component = super.getEditorComponent();
		if ((component != null) && (!this.verifierAdded.contains(component))) {
			this.verifierAdded.add(component);
			((JComponent) component).setInputVerifier(this.verifier);
		}
		return component;
	}

	/**
	 * Take this opportunity to request focus, so that input verifiers elsewhere will
	 * be triggered.
	 */
	@Override
	public boolean editCellAt(int row, int column, EventObject e) {
		this.requestFocusInWindow();
		return super.editCellAt(row, column, e);
	}

	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
