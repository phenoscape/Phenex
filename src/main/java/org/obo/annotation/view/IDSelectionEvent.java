package org.obo.annotation.view;

import java.util.EventObject;


public class IDSelectionEvent extends EventObject {

	private String id=null;
	private String fieldType=null; //to be used temporarily for the field type, 
	//to determine the appropriate window for display?

  IDSelectionEvent(Object source, String id, String type) {
    super(source);
    this.id = id;
    this.fieldType = type;
  }
  
  public String getID() {
  	return id;
  }
  
  public String getType() {
  	return fieldType;
  }


}
