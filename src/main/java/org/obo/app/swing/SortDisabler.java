package org.obo.app.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ca.odell.glazedlists.swing.TableComparatorChooser;

public class SortDisabler implements ActionListener {

	private boolean previousSortWasReverse = false;
	private int previouslySortedColumn = -1;

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source instanceof TableComparatorChooser<?>) {
			this.turnOffSortingIfNeeded((TableComparatorChooser<?>)source);
		}
	}

	private void turnOffSortingIfNeeded(TableComparatorChooser<?> sorter) {
		final int sortedColumn = (sorter.getSortingColumns().isEmpty()) ? -1 : sorter.getSortingColumns().get(0);
		if (this.previousSortWasReverse && this.previouslySortedColumn == sortedColumn) {
			this.previousSortWasReverse = false;
			sorter.clearComparator();
		}
		if (sortedColumn > -1) {
			this.previousSortWasReverse = sorter.isColumnReverse(sortedColumn);
			this.previouslySortedColumn = sortedColumn;
		}
	}

}
