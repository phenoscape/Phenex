package org.obo.annotation.view;

import org.oboedit.gui.components.GraphViewCanvas;
import org.oboedit.gui.factory.GraphViewFactory;

public class PhenoteGraphViewFactory extends GraphViewFactory {

	public PhenoteGraphViewFactory() {
	}
	
	public String getID() {
//		return "GRAPH_DAG_VIEW";
		return "GRAPH_VIEW";
	}
	
	public GraphViewCanvas doCreateComponent(String id) {
		return new GraphViewCanvas(id);
	}

	public String getName() {
		return "Graph View";
	}
	
	
	public FactoryCategory getCategory() {
		return FactoryCategory.ONTOLOGY;
	}

	@Override
	public String getHelpTopicID() {
		return "Graph_Viewer";
	}
}
