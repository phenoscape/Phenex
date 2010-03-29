package org.obo.annotation.view;

import java.awt.SystemColor;

import net.infonode.docking.properties.DockingWindowProperties;
import net.infonode.docking.properties.FloatingWindowProperties;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;

public class NativeDockingTheme extends DockingWindowsTheme {
    
    private RootWindowProperties rootWindowProperties = new ShapedGradientDockingTheme().getRootWindowProperties();
    
    public NativeDockingTheme() {
        super();
        this.initialize();
    }

    @Override
    public String getName() {
        return "Native Docking Theme";
    }

    @Override
    public RootWindowProperties getRootWindowProperties() {
        return this.rootWindowProperties;
    }
    
    private void initialize() {
        this.configureDockingWindowProperties();
        this.configureFloatingWindowProperties();
    }

    private void configureDockingWindowProperties() {
        final DockingWindowProperties properties = this.getRootWindowProperties().getDockingWindowProperties();
        // this is the front focused tab title
        properties.getTabProperties().getFocusedProperties().getComponentProperties().setForegroundColor(SystemColor.textText);
        // this is an unfocused tab title when tabbed behind another
        properties.getTabProperties().getTitledTabProperties().getNormalProperties().getComponentProperties().setForegroundColor(SystemColor.textInactiveText);
        // this is an unfocused tab title when not tabbed with another
        properties.getTabProperties().getTitledTabProperties().getHighlightedProperties().getComponentProperties().setForegroundColor(SystemColor.textInactiveText);
    }

    private void configureFloatingWindowProperties() {
        final FloatingWindowProperties properties = this.getRootWindowProperties().getFloatingWindowProperties();
        properties.setUseFrame(true);
    }

}
