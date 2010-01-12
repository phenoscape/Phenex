package org.obo.annotation.base;

import java.io.Serializable;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * A location from which to download a document containing ontology terms.  The OntologySource can also be given a user-friendly label.
 * @author Jim Balhoff
 */
public class OntologySource implements Cloneable, Serializable {

    private String label;
    private URL url;
    
    public OntologySource() {
        super();
    }
    
    public OntologySource(String label, URL url) {
        super();
        this.label = label;
        this.url = url;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String newLabel) {
        this.label = newLabel;
    }

    public URL getURL() {
        return this.url;
    }

    public void setURL(URL newURL) {
        this.url = newURL;
    }

    /**
     * @return a new OntologySource object with the same label and URL as this one.
     */
    public OntologySource copy() {
        try {
            return (OntologySource)(this.clone());
        } catch (CloneNotSupportedException e) {
            //should never happen
            log().error("Source object not cloneable", e);
        }
        return null;
    }

    private Logger log() {
        return Logger.getLogger(this.getClass());
    }

}