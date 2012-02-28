package org.phenoscape.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.phenoscape.controller.PhenexController;
import org.phenoscape.model.PhenotypeProposal;
import org.phenoscape.model.State;

import ca.odell.glazedlists.EventList;

public class PhenotypeProposalComponent extends PhenoscapeGUIComponent {

	private JTextField entityField;
	private JTextField locatorField;
	private JTextField qualityField;
	private JTextField relatedEntityField;

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
		final JPanel textPanel = new JPanel(new GridBagLayout());
		//textPanel.add(new JLabel("<HTML><B>Parsed Description</B></HTML>"));
		textPanel.setBorder(BorderFactory.createTitledBorder("Parsed Description"));
		this.add(textPanel, constraints);
		constraints.gridx += 1;
		final JPanel phenotypePanel = new JPanel(new GridBagLayout());
		this.add(phenotypePanel, constraints);
		//phenotypePanel.add(new JLabel("<HTML><B>Proposed Phenotype</B></HTML>"));
		phenotypePanel.setBorder(BorderFactory.createTitledBorder("Proposed Phenotype"));

		final GridBagConstraints descriptionConstraints = new GridBagConstraints();
		descriptionConstraints.gridx = 0;
		descriptionConstraints.gridy = 0;
		descriptionConstraints.anchor = GridBagConstraints.EAST;
		textPanel.add(new JLabel("Entity:"), descriptionConstraints);
		descriptionConstraints.gridy += 1;
		textPanel.add(new JLabel("Entity Locator:"), descriptionConstraints);
		descriptionConstraints.gridy += 1;
		textPanel.add(new JLabel("Quality:"), descriptionConstraints);
		descriptionConstraints.gridy += 1;
		textPanel.add(new JLabel("Related Entity:"), descriptionConstraints);
		descriptionConstraints.anchor = GridBagConstraints.CENTER;
		descriptionConstraints.fill = GridBagConstraints.HORIZONTAL;
		descriptionConstraints.weightx = 1;
		descriptionConstraints.gridy = 0;
		descriptionConstraints.gridx = 1;
		this.entityField  = new JTextField();
		this.entityField.setEditable(false);
		textPanel.add(this.entityField, descriptionConstraints);
		this.locatorField = new JTextField();
		this.locatorField.setEditable(false);
		descriptionConstraints.gridy += 1;
		textPanel.add(this.locatorField, descriptionConstraints);
		this.qualityField = new JTextField();
		this.qualityField.setEditable(false);
		descriptionConstraints.gridy += 1;
		textPanel.add(this.qualityField, descriptionConstraints);
		descriptionConstraints.gridy += 1;
		this.relatedEntityField = new JTextField();
		this.relatedEntityField.setEditable(false);
		textPanel.add(this.relatedEntityField, descriptionConstraints);
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
		final String unselectedTitle = "Phenotypes";
		final String selectedPrefix = "Phenotype Proposal for State: ";
		final List<State> states = this.getController().getCurrentStatesSelectionModel().getSelected();
		if (states.isEmpty()) {
			this.updatePanelTitle(unselectedTitle);
		} else {
			final State state = states.get(0);
			this.updatePanelTitle(selectedPrefix + state);
			final PhenotypeProposal proposal = state.getProposal();
			this.entityField.setText(proposal.getEntityText());
			this.locatorField.setText(proposal.getEntityLocatorText());
			this.qualityField.setText(proposal.getQualityText());
			this.relatedEntityField.setText(proposal.getQualityModifierText());
		}
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

}
