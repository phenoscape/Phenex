package org.phenoscape.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;
import org.jdesktop.xswingx.PromptSupport;
import org.obo.app.swing.TabActionTextField;
import org.phenoscape.util.ProvisionalTermUtil;

public class ORBPreferencesComponent extends AbstractGUIComponent {

	private JTextField apikeyField;
	private JTextField useridField;

	public ORBPreferencesComponent(String id) {
		super(id);
	}

	@Override
	public void init() {
		super.init();
		this.initializeInterface();
	}

	private void initializeInterface() {
		this.setLayout(new GridBagLayout());
		this.apikeyField = new TabActionTextField();
		PromptSupport.setPrompt("None", this.apikeyField);
		this.apikeyField.setBackground(new JTextField().getBackground());
		this.apikeyField.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ProvisionalTermUtil.setAPIKey(StringUtils.stripToNull(apikeyField.getText()));
			}
		});
		this.useridField = new TabActionTextField();
		PromptSupport.setPrompt("None", this.useridField);
		this.useridField.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ProvisionalTermUtil.setUserID(StringUtils.stripToNull(useridField.getText()));
			}
		});
		final GridBagConstraints apikeyLabelConstraints = new GridBagConstraints();
		apikeyLabelConstraints.anchor = GridBagConstraints.EAST;
		this.add(new JLabel("Bioportal API key:"), apikeyLabelConstraints);
		final GridBagConstraints apikeyFieldConstraints = new GridBagConstraints();
		apikeyFieldConstraints.gridx = 1;
		apikeyFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		apikeyFieldConstraints.weightx = 1.0;
		this.add(this.apikeyField, apikeyFieldConstraints);

		final GridBagConstraints useridLabelConstraints = new GridBagConstraints();
		useridLabelConstraints.anchor = GridBagConstraints.EAST;
		useridLabelConstraints.gridy = 1;
		this.add(new JLabel("Bioportal user ID:"), useridLabelConstraints);
		final GridBagConstraints useridFieldConstraints = new GridBagConstraints();
		useridFieldConstraints.gridx = 1;
		useridFieldConstraints.gridy = 1;
		useridFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		useridFieldConstraints.weightx = 1.0;
		this.add(this.useridField, useridFieldConstraints);
		this.setPreferredSize(new Dimension(400, 100));
		this.updateInterface();
	}

	private void updateInterface() {
		this.apikeyField.setText(ProvisionalTermUtil.getAPIKey());
		this.useridField.setText(ProvisionalTermUtil.getUserID());
	}

	@SuppressWarnings("unused")
	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

}
