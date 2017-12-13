package org.obo.annotation.view;

import org.oboedit.gui.components.GraphViewCanvas;
import org.oboedit.gui.factory.GraphViewFactory;

public class PhenoteGraphViewFactory extends GraphViewFactory {

    public PhenoteGraphViewFactory() {
    }

    @Override
    public String getID() {
        return "GRAPH_VIEW";
    }

    @Override
    public GraphViewCanvas doCreateComponent(String id) {
        return new GraphViewCanvas(id);
    }

    @Override
    public String getName() {
        return "Graph View";
    }

    @Override
    public FactoryCategory getCategory() {
        return FactoryCategory.ONTOLOGY;
    }

    @Override
    public String getHelpTopicID() {
        return "Graph_Viewer";
    }
}
