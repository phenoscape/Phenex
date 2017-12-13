package org.obo.annotation.view;

import java.util.EventObject;

import org.obo.datamodel.OBOClass;

public class TermSelectionEvent extends EventObject {

  private OBOClass oboClass;
  private UseTermListener useTermListener;
  private boolean isMouseOverEvent;
  private boolean isHyperlinkEvent;

  TermSelectionEvent(Object source, OBOClass oboClass, UseTermListener utl, 
		  boolean isMouse, boolean isHyperlink) {
    super(source);
    this.oboClass = oboClass;
    useTermListener = utl;
    isMouseOverEvent = isMouse;
    isHyperlinkEvent = isHyperlink;
  }

  public OBOClass getOboClass() { return oboClass; }

  public UseTermListener getUseTermListener() { return useTermListener; }
  
  public boolean isMouseOverEvent() { return isMouseOverEvent; }
  
  public boolean isHyperlinkEvent() { return isHyperlinkEvent; }
}
