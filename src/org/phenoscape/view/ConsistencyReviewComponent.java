package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.obo.app.swing.BugWorkaroundTable;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.Character;
import org.phenoscape.util.AnnotationConsistencyChecker;
import org.phenoscape.util.ConsistencyIssue;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.eekboom.utils.Strings;

public class ConsistencyReviewComponent extends PhenoscapeGUIComponent {

	private ConsistencyTableFormat tableFormat;
	private JTable table;
	private JButton refreshButton;
	private EventList<ConsistencyIssue> issues = new BasicEventList<ConsistencyIssue>();
	private AnnotationConsistencyChecker checker;

	public ConsistencyReviewComponent(String id, PhenexController controller) {
		super(id, controller);
		this.initializeInterface();
	}

	private void initializeInterface() {
		this.checker = new AnnotationConsistencyChecker(this.getController().getOntologyCoordinator().getOBOSession());
		this.setLayout(new BorderLayout());
		this.tableFormat = new ConsistencyTableFormat();
		final EventTableModel<ConsistencyIssue> phenotypesTableModel = new EventTableModel<ConsistencyIssue>(this.issues, this.tableFormat);
		this.table = new BugWorkaroundTable(phenotypesTableModel);
		this.add(new JScrollPane(this.table), BorderLayout.CENTER);
		this.add(this.createToolBar(), BorderLayout.NORTH);
	}

	private JToolBar createToolBar() {
		final JToolBar toolBar = new JToolBar();
		this.refreshButton = new JButton(new AbstractAction(null, new ImageIcon(this.getClass().getResource("/org/phenoscape/view/images/list-add.png"))) {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		this.refreshButton.setToolTipText("Refresh");
		toolBar.add(this.refreshButton);
		toolBar.setFloatable(false);
		return toolBar;
	}

	private void refresh() {
		this.issues.clear();
		for (Character character : this.getController().getDataSet().getCharacters()) {
			this.issues.addAll(checker.checkCharacter(character));
		}
	}

	private class ConsistencyTableFormat implements AdvancedTableFormat<ConsistencyIssue> {

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0: return "";
			case 1: return "Character";
			case 2: return "State";
			case 3: return "Issue";
			default: return null;
			}
		}

		@Override
		public Object getColumnValue(ConsistencyIssue issue, int column) {
			switch (column) {
			case 0: return getController().getDataSet().getCharacters().indexOf(issue.getCharacter()) + 1;
			case 1: return issue.getCharacter().getLabel();
			case 2: return issue.getState() == null ? "" : issue.getState().getLabel();
			case 3: return issue.getIssue();
			default: return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int column) {
			if (column == 0) {
				return Integer.class;
			} else {
				return String.class;	
			}
		}

		@Override
		public Comparator<?> getColumnComparator(int column) {
			if (column == 0) {
				return GlazedLists.comparableComparator();
			} else {
				return Strings.getNaturalComparator();	
			}
		}

	}

}
