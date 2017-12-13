package org.obo.annotation.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.obo.annotation.base.OBOUtil;
import org.obo.annotation.base.OBOUtil.Differentium;
import org.obo.annotation.base.TermSet;
import org.obo.app.swing.AutocompleteField;
import org.obo.app.swing.BugWorkaroundTable;
import org.obo.app.swing.TablePopupListener;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

public class PostCompositionEditor extends AbstractGUIComponent {

	private OBOClass genus;
	private EventList<Differentium> diffs = new BasicEventList<Differentium>();
	private EventSelectionModel<Differentium> selectionModel = new EventSelectionModel<Differentium>(diffs);
	private final TermSet termSet;
	private final TermSet relationsSet;
	private final TermSet differentiaTermSet;
	private final OntologyCoordinator coordinator;
	private AutocompleteField<OBOObject> genusBox;
	private JButton addDifferentiaButton;
	private JButton deleteDifferentiaButton;
	private JTable diffTable;
	private DifferentiaTableFormat tableFormat;
	private TablePopupListener popupListener;

	public PostCompositionEditor(String id, TermSet genusTerms, TermSet relations, TermSet differentiaTerms, OntologyCoordinator coordinator) {
		super(id);
		this.termSet = genusTerms;
		this.relationsSet = relations;
		this.differentiaTermSet = differentiaTerms;
		this.coordinator = coordinator;
	}

	public PostCompositionEditor(String id, TermSet terms, TermSet relations, OntologyCoordinator coordinator) {
		this(id, terms, relations, terms, coordinator);
	}

	public PostCompositionEditor(TermSet terms, TermSet relations, TermSet differentiaTerms, OntologyCoordinator coordinator) {
		this("", terms, relations, differentiaTerms, coordinator);
		this.initializeInterface();
	}

	public PostCompositionEditor(TermSet terms, TermSet relations, OntologyCoordinator coordinator) {
		this("", terms, relations, coordinator);
		this.initializeInterface();
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();    
	}

	public void addDifferentia() {
		this.diffs.add(new Differentium());
	}

	public void deleteSelectedDifferentia() {
		this.selectionModel.getSelected().clear();
	}

	private void updateGenus() {
		this.genus = (OBOClass)(this.genusBox.getValue());
	}

	public OBOClass getTerm() {
		for (Differentium diff : this.diffs) {
			if (!diff.isComplete()) {
				this.diffs.remove(diff);
			}
		}
		if (this.diffs.isEmpty()) {
			return this.genus;
		} else {
			return OBOUtil.createPostComposition(this.genus, this.diffs);
		}
	}

	public void setTerm(OBOClass aTerm) {
		this.diffs.clear();
		if ((aTerm != null) && (OBOUtil.isPostCompTerm(aTerm))) {
			this.genus = OBOUtil.getGenusTerm(aTerm);
			for (Link link : OBOUtil.getAllDifferentia(aTerm)) {
				final Differentium diff = new Differentium();
				diff.setRelation(link.getType());
				final LinkedObject parent = link.getParent();
				if (parent instanceof OBOClass) {
					diff.setTerm((OBOClass)parent);
				} else {
					log().error("Differentia is not an OBOClass: " + parent);
				}
				this.diffs.add(diff);
			}
		} else {
			this.genus = aTerm;
		}
		this.genusBox.setValue((OBOObject)this.genus);
		if (this.diffs.isEmpty()) {
			this.diffs.add(new Differentium());
		}
	}

	public int runPostCompositionDialog(Component parentComponent) {
		if (this.genusBox == null) {
			this.init();
		}
		this.setPreferredSize(new Dimension(300, 200));
		return JOptionPane.showConfirmDialog(parentComponent, this, "Post-composition Editor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	}

	private void runPostCompositionForTermAtPoint(Point p) {
		final int column = this.diffTable.getTableHeader().columnAtPoint(p);
		final int row = this.diffTable.rowAtPoint(p);
		if (!this.tableFormat.getColumnClass(column).equals(OBOObject.class)) return;
		final Differentium differentia = this.diffs.get(row);
		final OBOClass term = (OBOClass)(this.tableFormat.getColumnValue(differentia, column));
		final PostCompositionEditor pce = new PostCompositionEditor(this.termSet, this.relationsSet, this.differentiaTermSet, this.coordinator);
		pce.setTerm(term);
		final int result = pce.runPostCompositionDialog(this);
		if (result == JOptionPane.OK_OPTION) {
			this.tableFormat.setColumnValue(differentia, pce.getTerm(), column);
		}
	}

	private void initializeInterface() {
		this.setLayout(new GridBagLayout());
		final GridBagConstraints labelConstraints = new GridBagConstraints();
		this.add(new JLabel("Genus:"), labelConstraints);
		final GridBagConstraints comboConstraints = new GridBagConstraints();
		comboConstraints.gridx = 1;
		comboConstraints.fill = GridBagConstraints.HORIZONTAL;
		comboConstraints.weightx = 1.0;
		this.genusBox = TermAutocompleteFieldFactory.createAutocompleteBox(this.termSet, this.coordinator);
		this.genusBox.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateGenus();
			}
		});
		this.add(this.genusBox, comboConstraints);
		final GridBagConstraints postComposeGenusConstraints = new GridBagConstraints();
		postComposeGenusConstraints.gridx = 2;
		this.tableFormat = new DifferentiaTableFormat();
		final EventTableModel<Differentium> model = new EventTableModel<Differentium>(this.diffs, this.tableFormat);
		this.diffTable = new BugWorkaroundTable(model);
		this.diffTable.setSelectionModel(this.selectionModel);
		this.diffTable.setDefaultRenderer(OBOObject.class, new TermRenderer("None"));
		this.diffTable.putClientProperty("Quaqua.Table.style", "striped");
		this.diffTable.getColumnModel().getColumn(0).setCellEditor(this.tableFormat.getColumnEditor(0));
		this.diffTable.getColumnModel().getColumn(1).setCellEditor(this.tableFormat.getColumnEditor(1));
		final GridBagConstraints tableConstraints = new GridBagConstraints();
		tableConstraints.gridy = 1;
		tableConstraints.gridwidth = 3;
		tableConstraints.fill = GridBagConstraints.BOTH;
		tableConstraints.weighty = 1.0;
		this.add(new JScrollPane(diffTable), tableConstraints);
		final GridBagConstraints toolbarConstraints = new GridBagConstraints();
		toolbarConstraints.gridy = 2;
		toolbarConstraints.gridwidth = 2;
		toolbarConstraints.fill = GridBagConstraints.HORIZONTAL;
		toolbarConstraints.weightx = 1.0;
		this.add(this.createToolBar(), toolbarConstraints);
		this.popupListener = new TablePopupListener(this.createTablePopupMenu(), this.diffTable);
		this.popupListener.setPopupColumns(Arrays.asList(new Integer[] {1}));
		this.diffTable.addMouseListener(this.popupListener);
	}

	private JToolBar createToolBar() {
		final JToolBar toolBar = new JToolBar();
		this.addDifferentiaButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-add.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				addDifferentia();
			}
		});
		this.addDifferentiaButton.setToolTipText("Add Differentia");
		toolBar.add(this.addDifferentiaButton);
		this.deleteDifferentiaButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-remove.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedDifferentia();
			}
		});
		this.deleteDifferentiaButton.setToolTipText("Delete Differentia");
		toolBar.add(this.deleteDifferentiaButton);
		toolBar.setFloatable(false);
		return toolBar;
	}

	private JPopupMenu createTablePopupMenu() {
		final JPopupMenu menu = new JPopupMenu();
		menu.add(new AbstractAction("Create Post-composed Term") {
			@Override
			public void actionPerformed(ActionEvent e) {
				runPostCompositionForTermAtPoint(popupListener.getLocation());
			}
		});
		return menu;
	}

	private class DifferentiaTableFormat implements WritableTableFormat<Differentium>, AdvancedTableFormat<Differentium> {

		@Override
		public boolean isEditable(Differentium diff, int column) {
			return true;
		}

		public TableCellEditor getColumnEditor(int column) {
			switch (column) {
			case 0: return TermAutocompleteFieldFactory.createAutocompleteEditor(relationsSet, coordinator);
			case 1: return TermAutocompleteFieldFactory.createAutocompleteEditor(differentiaTermSet, coordinator);
			default: return null;
			}
		}

		@Override
		public Differentium setColumnValue(Differentium diff, Object editedValue, int column) {
			switch(column) {
			case 0: diff.setRelation((OBOProperty)editedValue); break;
			case 1: diff.setTerm((OBOClass)editedValue); break;
			}
			return diff;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			switch(column) {
			case 0: return "Relationship";
			case 1: return "Differentia";
			}
			return null;
		}

		@Override
		public Object getColumnValue(Differentium diff, int column) {
			switch(column) {
			case 0: return diff.getRelation();
			case 1: return diff.getTerm();
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return OBOObject.class;
		}

		@Override
		public Comparator<?> getColumnComparator(int column) {
			return GlazedLists.comparableComparator();
		}

	}

	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
