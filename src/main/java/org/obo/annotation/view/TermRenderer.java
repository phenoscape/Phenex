package org.obo.annotation.view;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;

import org.obo.annotation.base.OBOUtil;
import org.obo.app.swing.PlaceholderRenderer;
import org.obo.datamodel.DanglingObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;

/**
 * A table cell renderer for displaying cell values which are ontology terms (OBOClass).
 * The term name is displayed, and the term ID is provided as a tooltip.
 * @author Jim Balhoff
 */
public class TermRenderer extends PlaceholderRenderer {

	private static final Color THE_COLOR_PURPLE = new Color(133, 0, 175);

	public TermRenderer(String placeholder) {
		super(placeholder);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof OBOObject) {
			final OBOObject term = (OBOObject)value;
			this.setToolTipText(term.getID());
			final Component component = super.getTableCellRendererComponent(table, (term.getName() != null ? term.getName() : term.getID()), isSelected, hasFocus, row, column);
			if (isDangling(term)) {
				component.setForeground(Color.BLUE);
				this.setToolTipText("Dangling Term: " + term.getID());
			}
			if (isProvisional(term)) {
				component.setForeground(THE_COLOR_PURPLE);
				this.setToolTipText("Provisional Term: " + term.getID());
			}
			if (term.isObsolete()) {
				component.setForeground(Color.RED);
				this.setToolTipText("Obsolete Term: " + term.getID());
			}
			return component;
		} else {
			this.setToolTipText(null);
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}  
	}

	public static boolean isDangling(OBOObject term) {
		if (OBOUtil.isPostCompTerm(term)) {
			if (isDangling(OBOUtil.getGenusTerm((OBOClass)term))) {
				return true;
			} else {
				for (Link link : OBOUtil.getAllDifferentia((OBOClass)term)) {
					final LinkedObject parent = link.getParent();
					if (!(parent instanceof OBOClass)) continue;
					final OBOClass differentium = (OBOClass)parent;
					if (isDangling(differentium)) {
						return true;
					}
				}
			}
		} else {
			return term instanceof DanglingObject;
		}
		return false;
	}

	public static boolean isProvisional(OBOObject term) {
		if (term.getNamespace() != null) {
			return term.getNamespace().equals(new Namespace("bioportal_provisional"));	
		} else {
			return false;
		}
	}

}
