package org.phenoscape.bridge;

import static org.junit.Assert.*;

import org.junit.Test;
import org.obd.model.Node;
import org.obo.datamodel.Dbxref;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.PropertyValue;

public class ZfinObdBridgeTest {

	private String ZEBRAFISH_ANATOMY = "zebrafish_anatomy";
	private String TELEOST_ANATOMY = "teleost_anatomy";
	@Test
	public void testGetOboSession() {
		String id = "01234";
		System.out.println(id.matches("[A-Z]+:[0-9]+"));

	}
}
