package org.phenoscape.view;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.obo.app.util.Collections;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.Character;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.State;
import org.phenoscape.util.AnnotationConsistencyChecker;
import org.phenoscape.util.ConsistencyIssue;

import ca.odell.glazedlists.EventList;

public class AnnotationCheckerComponent extends PhenoscapeGUIComponent {

	private JEditorPane warningsField;
	private AnnotationConsistencyChecker checker;

	public AnnotationCheckerComponent(String id, PhenexController controller) {
		super(id, controller);
	}

	@Override
	public void init() {
		super.init();
		this.checker = new AnnotationConsistencyChecker(this.getController().getOntologyCoordinator().getOBOSession());
		this.initializeInterface();
	}

	private void initializeInterface() {
		this.warningsField = new JEditorPane();
		this.warningsField.setContentType("text/html");
		this.warningsField.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		this.warningsField.setEditable(false);
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(this.warningsField), BorderLayout.CENTER);
		this.getController().getCurrentPhenotypesSelectionModel().addListSelectionListener(new PhenotypeSelectionListener());
	}

	private void phenotypeSelectionDidChange() {
		final Phenotype phenotype = this.getSelectedPhenotype();
		final State state = this.getSelectedState();
		final Character character = this.getSelectedCharacter();
		if (phenotype == null) {
			return;
		} else {
			final Collection<ConsistencyIssue> issues = this.checker.checkPhenotype(phenotype, state, character);
			final List<String> errors = new ArrayList<String>();
			for (ConsistencyIssue issue : issues) {
				errors.add("<b>Warning:</b> " + issue.getIssue());
			}
			if (errors.isEmpty()) {
				this.warningsField.setText("");
			} else {
				this.warningsField.setText("<html>" + Collections.join(errors, "<br>") + "</html>");	
			}
		}
	}

	private Phenotype getSelectedPhenotype() {
		final EventList<Phenotype> selected = this.getController().getCurrentPhenotypesSelectionModel().getSelected();
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

	private Character getSelectedCharacter() {
		final EventList<Character> selected = this.getController().getCharactersSelectionModel().getSelected();
		if (selected.size() == 1) {
			return selected.get(0);
		} else {
			return null;
		}
	}

	private class PhenotypeSelectionListener implements ListSelectionListener {

		public PhenotypeSelectionListener() {
			phenotypeSelectionDidChange();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			phenotypeSelectionDidChange();      
		}

	}

}
