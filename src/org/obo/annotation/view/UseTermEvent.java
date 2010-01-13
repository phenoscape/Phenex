package org.obo.annotation.view;

import java.util.EventObject;

import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;

/** im not sure if this belongs in selection package - a use term listener listens
    for UseTermEvents - this is from the term browser saying basically the user wants
    to use this term - in the main window that means editing the model, in the postcomp
    window that means setting the text either for genus of differentia (which waits
    for an ok to actually edit the model */

public class UseTermEvent extends EventObject {
  private OBOObject oboObject;
  //private OBOClass term;
  public UseTermEvent(Object source,OBOClass term) {
    super(source);
    //this.term = term;
    oboObject = term;
  }
  public UseTermEvent(Object source, OBOObject obj) {
    super(source);
    oboObject = obj;
  }
  /** if possible phase this out for getObject? */
  public OBOClass getTerm() {
    if (isTerm()) return (OBOClass)oboObject;
    return null;
  }
  public boolean isTerm() { return oboObject instanceof OBOClass; }
  public OBOObject getObject() { return oboObject; }
}
