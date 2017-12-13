package org.phenoscape.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.obo.util.TermUtil;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.Taxon;

public class TreeBuilder {

	/**
	 * @return Map<nodeID, parentID>
	 */
	public static Map<LinkedObject, LinkedObject> buildTree(DataSet data, OBOSession session) {
		final Set<LinkedObject> allAncestors = new HashSet<LinkedObject>();
		for (Taxon taxon : data.getTaxa()) {
			if (taxon.getValidName() != null) {
				allAncestors.addAll(TermUtil.getisaAncestors(taxon.getValidName(), true));
			}
		}
		final Map<LinkedObject, LinkedObject> treeNodes = new HashMap<LinkedObject, LinkedObject>();
		if (!allAncestors.isEmpty()) {
			final Collection<OBOClass> roots = TermUtil.getRoots(session);
			logger().debug(roots);
			roots.retainAll(allAncestors);
			if (roots.size() != 1) {
				logger().error("Should only be one taxon root.");
			}
			final LinkedObject taxonRoot = TermUtil.getRoot(allAncestors.iterator().next());
			logger().debug("Root taxon: " + taxonRoot);
			final Set<LinkedObject> retainTaxa = new HashSet<LinkedObject>();
			for (Taxon taxon : data.getTaxa()) {
				if (taxon.getValidName() != null) {
					retainTaxa.add(taxon.getValidName());
				}
			}
			processNode(taxonRoot, treeNodes, allAncestors, null, retainTaxa);
		}
		return treeNodes;
	}

	private static void processNode(LinkedObject node, Map<LinkedObject, LinkedObject> treeNodes, Set<LinkedObject> allAncestors, LinkedObject parent, Collection<LinkedObject> retainTaxa) {
		final Set<LinkedObject> children = new HashSet<LinkedObject>(TermUtil.getChildren(node));
		children.retainAll(allAncestors);
		if ((children.size() == 1) && (!retainTaxa.contains(node))) { //retain needed internal nodes
			processNode(children.iterator().next(), treeNodes, allAncestors, parent, retainTaxa);
		} else {
			if (parent != null) {
				treeNodes.put(node, parent);
			}
			for (LinkedObject child : children) {
				processNode(child, treeNodes, allAncestors, node, retainTaxa);
			}
		}
	}

	private static Logger logger() {
		return Logger.getLogger(TreeBuilder.class);
	}

}
