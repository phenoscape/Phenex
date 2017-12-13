package org.phenoscape.view;

import org.bbop.framework.AbstractComponentFactory;

public class ORBPreferencesComponentFactory extends AbstractComponentFactory<ORBPreferencesComponent> {

	@Override
	public org.bbop.framework.GUIComponentFactory.FactoryCategory getCategory() {
		return FactoryCategory.CONFIG;
	}

	@Override
	public String getID() {
		return "phenex_orb_preferences";
	}

	@Override
	public String getName() {
		return "ORB Connection Settings";
	}

	@Override
	public ORBPreferencesComponent doCreateComponent(String id) {
		return new ORBPreferencesComponent(id);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public boolean getPreferSeparateWindow() {
		return true;
	}

}
