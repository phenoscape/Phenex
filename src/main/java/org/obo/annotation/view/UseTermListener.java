package org.obo.annotation.view;

/** im not sure if this belongs in selection package - a use term listener listens
    for UseTermEvents - this is from the term browser saying basically the user wants
    to use this term - in the main window that means editing the model, in the postcomp
    window that means setting the text either for genus of differentia (which waits
    for an ok to actually edit the model */

public interface UseTermListener {
  public void useTerm(UseTermEvent e);
}
