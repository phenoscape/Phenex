package org.obo.annotation.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.obo.datamodel.OBOClass;

/** Controller for term & character selection */
public class SelectionManager {

	// private static SelectionManager singleton;
	private static Map<String, SelectionManager> groupToSelMan = new HashMap<String, SelectionManager>();

	private List<TermSelectionListener> termListenerList;
	private List<IDSelectionListener> IDListenerList;

	public static void reset() {
		groupToSelMan.clear();
	}

	public SelectionManager() {
		termListenerList = new ArrayList<TermSelectionListener>(5);
		IDListenerList = new ArrayList<IDSelectionListener>(5);
	}

	// TERM SELECTION

	public void addTermSelectionListener(TermSelectionListener l) {
		termListenerList.add(l);
	}

	public void removeTermSelectionListener(TermSelectionListener l) {
		termListenerList.remove(l);
	}
	
	// ID SELECTION
	public void addIDSelectionListener(IDSelectionListener l) {
		IDListenerList.add(l);
	}

	public void removeIDSelectionListener(IDSelectionListener l) {
		IDListenerList.remove(l);
	}

	// void addCharacterSelectionListener(CharacterSelectionListener l) {}

	public void selectHistoryTerm(Object source, OBOClass oboClass,
			UseTermListener l) {
		boolean isMouseOver = false;
		boolean isHyperlink = true;
		TermSelectionEvent e = makeTermEvent(source, oboClass, l, isMouseOver,
				isHyperlink);
		fireTermSelect(e);
	}

	public void selectMouseOverTerm(Object source, OBOClass oboClass,
			UseTermListener l) {
		boolean isMouseOver = true;
		boolean isHyperlink = false;
		TermSelectionEvent e = makeTermEvent(source, oboClass, l, isMouseOver,
				isHyperlink);
		fireTermSelect(e);
	}

  
  /** select oboClass, if its not a hyperlink then fireTermSelect
   if it is a hyperlink and its not getting fired its unclear to me what the point
   of this is??? - MG */
	public void selectTerm(Object source, OBOClass oboClass, boolean isHyperlink) {
		boolean isMouseOver = false;
		// System.out.println("ishyperlink="+isHyperlink);
		TermSelectionEvent e = makeTermEvent(source, oboClass, null,
				isMouseOver, isHyperlink);
		if (!isHyperlink) // and if it is a hyperlink that whats the point???
			fireTermSelect(e);
	}
	
	public void selectID(Object source, String id, String type) {
		IDSelectionEvent e = makeIDEvent(source, id, type);
			fireIDSelect(e);
	}

	private void fireIDSelect(IDSelectionEvent e) {
		// need to make a copy of the term listener list to avoid
		// co-modification problems
		List<IDSelectionListener> temp = new ArrayList<IDSelectionListener>(
				IDListenerList);
		Iterator<IDSelectionListener> it = temp.iterator();
		while (it.hasNext())
			it.next().IDSelected(e);
	}
	
	private void fireTermSelect(TermSelectionEvent e) {
		// need to make a copy of the term listener list to avoid
		// co-modification problems
		List<TermSelectionListener> temp = new ArrayList<TermSelectionListener>(
				termListenerList);
		Iterator<TermSelectionListener> it = temp.iterator();
		while (it.hasNext())
			it.next().termSelected(e);
	}

	// void selectTerm(String termName) {} ???

	private TermSelectionEvent makeTermEvent(Object src, OBOClass oc,
			UseTermListener l, boolean mouse, boolean link) {
		return new TermSelectionEvent(src, oc, l, mouse, link);
	}

	private IDSelectionEvent makeIDEvent(Object src, String id, String type) {
		return new IDSelectionEvent(src, id, type);
	}

}
