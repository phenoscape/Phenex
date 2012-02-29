package org.phenoscape.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.obo.annotation.base.OBOUtil;
import org.obo.annotation.base.OBOUtil.Differentium;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.util.TermUtil;
import org.oboedit.controller.SelectionManager;
import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.Phenotype;
import org.phenoscape.model.PhenotypeProposal;
import org.phenoscape.model.PhenotypeProposal.ResolvedStatus;
import org.phenoscape.model.State;

import ca.odell.glazedlists.EventList;

public class PhenotypeProposalComponent extends PhenoscapeGUIComponent {

	private TermPanel entityPanel = new TermPanel();
	private TermPanel entityLocatorPanel = new TermPanel();
	private TermPanel qualityPanel = new TermPanel();
	private TermPanel relatedEntityPanel = new TermPanel();
	private JPanel phenotypePanel;

	public PhenotypeProposalComponent(String id, PhenexController controller) {
		super(id, controller);
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();
	}

	private void initializeInterface() {
		this.getController().getCurrentStatesSelectionModel().addListSelectionListener(new StateSelectionListener());
		this.setLayout(new GridBagLayout());
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridy = 0;
		constraints.gridx = 0;
		constraints.gridwidth = 3;
		this.phenotypePanel = new JPanel(new GridBagLayout());
		phenotypePanel.setBorder(BorderFactory.createTitledBorder("Proposed Phenotype"));
		this.updatePanelColorAndTitle();
		this.add(phenotypePanel, constraints);

		final JButton rejectButton = new JButton(new AbstractAction("Reject Phenotype") {

			@Override
			public void actionPerformed(ActionEvent event) {
				rejectProposal();
			}
		});
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridwidth = 1;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.gridy = 1;
		constraints.gridx = 1;
		constraints.fill = GridBagConstraints.NONE;
		this.add(rejectButton, constraints);
		final JButton acceptButton = new JButton(new AbstractAction("Accept and Edit Phenotype") {

			@Override
			public void actionPerformed(ActionEvent event) {
				createAndAddPhenotype();
				acceptProposal();
			}
		});
		constraints.gridx = 2;
		this.add(acceptButton, constraints);

		final GridBagConstraints phenotypeConstraints = new GridBagConstraints();
		phenotypeConstraints.gridx = 0;
		phenotypeConstraints.gridy = 0;
		phenotypeConstraints.anchor = GridBagConstraints.EAST;
		phenotypePanel.add(new JLabel("Entity:"), phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(new JLabel("Entity Locator:"), phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(new JLabel("Quality:"), phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(new JLabel("Related Entity:"), phenotypeConstraints);
		phenotypeConstraints.anchor = GridBagConstraints.CENTER;
		phenotypeConstraints.fill = GridBagConstraints.HORIZONTAL;
		phenotypeConstraints.weightx = 1;
		phenotypeConstraints.gridy = 0;
		phenotypeConstraints.gridx = 1;
		phenotypeConstraints.gridy = 0;

		phenotypePanel.add(entityPanel, phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(entityLocatorPanel, phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(qualityPanel, phenotypeConstraints);
		phenotypeConstraints.gridy += 1;
		phenotypePanel.add(relatedEntityPanel, phenotypeConstraints);
	}

	private State getSelectedState() {
		final EventList<State> selected = this.getController().getCurrentStatesSelectionModel().getSelected();
		if (selected.size() == 1) {
			return selected.get(0);
		} else {
			return null;
		}
	}

	private void stateSelectionDidChange() {
		this.updatePanelColorAndTitle();
		final String unselectedTitle = "Phenotypes";
		final String selectedPrefix = "Phenotype Proposal for State: ";
		final State state = this.getSelectedState();
		if (state == null) {
			this.updatePanelTitle(unselectedTitle);
		} else {
			this.updatePanelTitle(selectedPrefix + state);
			this.clearFields();
			final PhenotypeProposal proposal = state.getProposal();
			if (proposal != null) {
				this.entityPanel.setTerms(proposal.getEntities());
				this.entityLocatorPanel.setTerms(proposal.getEntityLocators());
				this.qualityPanel.setTerms(proposal.getProcessedQualities(this.getController().getOntologyController().getOBOSession()));
				this.relatedEntityPanel.setTerms(proposal.getQualityModifiers());

				SelectionManager.selectTerms(this, this.collectTerms(proposal));
			}

		}
	}

	private Set<LinkedObject> collectTerms(PhenotypeProposal proposal) {
		final Set<LinkedObject> terms = new HashSet<LinkedObject>();
		terms.addAll(proposal.getEntities());
		terms.addAll(proposal.getEntityLocators());
		terms.addAll(proposal.getQualities());
		terms.addAll(proposal.getQualityModifiers());
		if (proposal.getNegatedQualityParent() != null) {
			terms.add(proposal.getNegatedQualityParent());
		}
		final Set<LinkedObject> ancestors = new HashSet<LinkedObject>();
		boolean first = true;
		for (LinkedObject term : terms) {
			if (first) {
				ancestors.addAll(TermUtil.getAncestors(term, null));
			} else {
				ancestors.retainAll(TermUtil.getAncestors(term, null));
			}
		}
		terms.addAll(ancestors);
		return terms;
	}

	private void createAndAddPhenotype() {
		final State state = this.getSelectedState();
		if (state != null) {
			final Phenotype phenotype = new Phenotype();
			final OBOClass entity = this.entityPanel.getSelectedTerm();
			final OBOClass entityLocator = this.entityLocatorPanel.getSelectedTerm();
			if (entity != null && entityLocator != null) {
				final Differentium diff = new Differentium();
				diff.setRelation((OBOProperty)(this.getController().getOntologyController().getOBOSession().getObject("OBO_REL:part_of")));
				diff.setTerm(entityLocator);
				final OBOClass composition = OBOUtil.createPostComposition(entity, Collections.singletonList(diff));
				phenotype.setEntity(composition);
			} else if (entity != null) {
				phenotype.setEntity(entity);
			} else{
				phenotype.setEntity(entityLocator);
			}
			phenotype.setQuality(this.qualityPanel.getSelectedTerm());
			phenotype.setRelatedEntity(this.relatedEntityPanel.getSelectedTerm());
			if (phenotype.getEntity() != null || phenotype.getQuality() != null || phenotype.getRelatedEntity() != null) {
				state.addPhenotype(phenotype);
			}
		}
	}

	private void updatePanelColorAndTitle() {
		if (this.phenotypePanel != null) {
			final TitledBorder border = (TitledBorder)(this.phenotypePanel.getBorder());
			final State state = this.getSelectedState();
			if (state != null && state.getProposal() != null) {
				final PhenotypeProposal proposal = state.getProposal();
				log().debug("Setting title color");
				switch (proposal.getStatus()) {
				case PROPOSED: border.setTitleColor(Color.BLUE); border.setTitle("Proposed Phenotype"); break;
				case ACCEPTED: border.setTitleColor(Color.GREEN); border.setTitle("Proposed Phenotype - accepted"); break;
				case REJECTED: border.setTitleColor(Color.RED); border.setTitle("Proposed Phenotype - rejected"); break;
				}
			} else {
				border.setTitle("No Proposed Phenotype");
				border.setTitleColor(Color.BLACK);	
			}
			this.phenotypePanel.repaint();
		}
	}

	private void acceptProposal() {
		this.getSelectedState().getProposal().setStatus(ResolvedStatus.ACCEPTED);
		this.updatePanelColorAndTitle();
	}

	private void rejectProposal() {
		this.getSelectedState().getProposal().setStatus(ResolvedStatus.REJECTED);
		this.updatePanelColorAndTitle();
	}

	private void clearFields() {
		this.entityPanel.setTerms(Collections.<OBOClass>emptyList());
		this.entityLocatorPanel.setTerms(Collections.<OBOClass>emptyList());
		this.qualityPanel.setTerms(Collections.<OBOClass>emptyList());
		this.relatedEntityPanel.setTerms(Collections.<OBOClass>emptyList());
	}

	private class StateSelectionListener implements ListSelectionListener {

		public StateSelectionListener() {
			stateSelectionDidChange();
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			stateSelectionDidChange();      
		}

	}

	private static class TermPanel extends JPanel {

		private JComponent termComponent;
		private List<OBOClass> terms;

		public TermPanel() {
			this.setLayout(new BorderLayout());
			this.setTerms(Collections.<OBOClass>emptyList());
		}

		public void setTerms(List<OBOClass> terms) {
			this.terms = terms;
			this.removeAll();
			this.termComponent = null;
			if (this.terms.size() > 1) {
				this.termComponent = new JComboBox(this.terms.toArray());
			} else if (terms.size() == 1) {
				final JTextField field = new JTextField(terms.get(0).getName()); 
				this.termComponent = field;
				field.setEditable(false);

			} else {
				final JTextField field = new JTextField(); 
				this.termComponent = field;
				field.setEditable(false);
				field.setEnabled(false);
			}
			this.add(termComponent, BorderLayout.CENTER);
		}

		public OBOClass getSelectedTerm() {
			if (this.terms.size() > 1) {
				return (OBOClass)(((JComboBox)this.termComponent).getSelectedItem());
			} else if (this.terms.size() == 1) {
				return this.terms.get(0);
			} else {
				return null;
			}
		}

	}

}
