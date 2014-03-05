package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.util.Collections;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.apache.batik.ext.swing.GridBagConstants;
import org.obo.app.swing.BugWorkaroundTable;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.Association;
import org.phenoscape.model.AssociationSupport;
import org.phenoscape.model.MatrixCell;
import org.phenoscape.model.MatrixCellSelectionListener;
import org.phenoscape.model.MultipleState;
import org.phenoscape.model.State;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

public class StateSupportComponent extends PhenoscapeGUIComponent {

	private JTabbedPane panel;
	private CellSelectionListener cellListener = new CellSelectionListener();

	public StateSupportComponent(String id, PhenexController controller) {
		super(id, controller);
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();
		this.getController().addMatrixCellSelectionListener(this.cellListener);
	}

	@Override
	public void cleanup() {
		this.getController().removeMatrixCellSelectionListener(cellListener);
		super.cleanup();
	}

	private void initializeInterface() {
		this.setLayout(new BorderLayout());
		this.panel = new JTabbedPane();
		this.add(this.panel, BorderLayout.CENTER);
	}

	private void clearInterface() {
		log().debug("Clearing panel");
		this.panel.removeAll();
		this.panel.validate();
	}

	private void displaySupportForCell(MatrixCell cell) {

		//		final GridBagConstraints constraints = new GridBagConstraints();
		//		constraints.fill = GridBagConstants.HORIZONTAL;
		//		constraints.gridx = 0;
		//		constraints.gridy = 0;
		for (State state : this.stateValuesForCell(cell)) {
			this.panel.addTab(state.getSymbol(), new JScrollPane(this.createSupportTable(cell, state)));
		}
		this.panel.validate();
	}

	private Set<AssociationSupport> supportsForStateAssignment(MatrixCell cell, State state) {
		final Association assoc = new Association(cell.getTaxon().getNexmlID(), cell.getCharacter().getNexmlID(), state.getNexmlID());
		return this.getController().getDataSet().getAssociationSupport().get(assoc);
	}

	private JTable createSupportTable(MatrixCell cell, State state) {
		final Set<AssociationSupport> supports = supportsForStateAssignment(cell, state);
		final JTable supportTable = new BugWorkaroundTable(new EventTableModel<AssociationSupport>(GlazedLists.eventList(supports), new SupportTableFormat()));
		supportTable.putClientProperty("Quaqua.Table.style", "striped");
		return supportTable;
	}

	private Set<State> stateValuesForCell(MatrixCell cell) {
		final State state = this.getController().getDataSet().getStateForTaxon(cell.getTaxon(), cell.getCharacter());
		final Set<State> states;
		if (state instanceof MultipleState) {
			states = ((MultipleState)state).getStates();
		} else if (state == null) {
			states = Collections.emptySet();
		}
		else {
			states = Collections.singleton(state);
		}
		return states;
	}

	private class CellSelectionListener implements MatrixCellSelectionListener {

		@Override
		public void matrixCellWasSelected(MatrixCell cell, PhenexController controller) {
			clearInterface();
			if (cell != null) {
				displaySupportForCell(cell);
			}
		}

	}

	private static class SupportTableFormat implements TableFormat<AssociationSupport> {

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			switch(column) {
			case 0: return "State Description"; 
			case 1: return "Source";
			default: return null;
			}

		}

		@Override
		public Object getColumnValue(AssociationSupport support, int column) {
			switch(column) {
			case 0: return support.getDescriptionText(); 
			case 1: return support.getDescriptionSource();
			default: return null;
			}
		}

	}

}
