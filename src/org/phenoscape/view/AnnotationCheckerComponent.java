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
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.TermSubset;
import org.obo.filters.LinkFilter;
import org.obo.filters.LinkFilterImpl;
import org.obo.util.TermUtil;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.Phenotype;

import ca.odell.glazedlists.EventList;

public class AnnotationCheckerComponent extends PhenoscapeGUIComponent {

	private JEditorPane warningsField;
	private static final String STRUCTURE = "PATO:0000141";
	private static final String POSITION = "PATO:0000140";
	private static final String SIZE = "PATO:0000117";

	public AnnotationCheckerComponent(String id, PhenexController controller) {
		super(id, controller);
	}

	@Override
	public void init() {
		super.init();
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
		if (phenotype == null) {

		} else {
			final List<String> errors = this.checkForErrors(phenotype);
			if (errors.isEmpty()) {
				this.warningsField.setText("");
			} else {
				this.warningsField.setText("<html>" + Collections.join(errors, "\n\n") + "</html>");	
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

	private List<String> checkForErrors(Phenotype phenotype) {
		final List<String> errors = new ArrayList<String>();
		final OBOSession session = this.getController().getOntologyController().getOBOSession();
		TermSubset relation_slim = null;
		for (TermSubset subset : session.getSubsets()) {
			if ((subset.getName() != null) && (subset.getName().equals("relational_slim"))) {
				relation_slim = subset;
			}
		}
		if (phenotype.getQuality() == null) {
			errors.add("<b>Warning:</b> No quality has been entered.");
		}
		if (relation_slim != null) {
			if (phenotype.getQuality() != null) {
				if (phenotype.getQuality().getSubsets().contains(relation_slim)) {
					if (phenotype.getRelatedEntity() == null) {
						errors.add("<b>Warning:</b> Relational quality has been used without a related entity.");
					}
				} else {
					if ((phenotype.getRelatedEntity() != null) && (!this.isOptionallyRelationalQuality(phenotype.getQuality()))) {
						errors.add("<b>Warning:</b> Related entity requires a relational quality.");
					}
				}
			}

		}
		return errors;
	}
	
	private boolean isOptionallyRelationalQuality(OBOClass quality) {
		if ((quality.getID().equals(STRUCTURE)) || (quality.getID().equals(POSITION))) {
			return true;
		} else {
			final OBOSession session = this.getController().getOntologyController().getOBOSession();
			final OBOClass size = (OBOClass)(session.getObject(SIZE));
			final LinkFilter filter = new LinkFilterImpl((OBOProperty)(session.getObject("OBO_REL:is_a")));
			final Collection<LinkedObject> sizes = TermUtil.getDescendants(size, true, filter);
			return sizes.contains(quality);
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
