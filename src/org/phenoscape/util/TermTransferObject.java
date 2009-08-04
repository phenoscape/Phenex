package org.phenoscape.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;

import phenote.datamodel.OboUtil;

/**
 * A class for creating serializable representations of OBOClasses (terms or post-compositions) for use in copy/paste.
 */
public class TermTransferObject implements Serializable {
    
    private final Object genus;
    private final List<Differentium> differentia = new ArrayList<Differentium>();
    
    public TermTransferObject(OBOClass term) {
        if ((term != null) && (OboUtil.isPostCompTerm(term))) {
            final OBOClass genusTerm = OboUtil.getGenusTerm(term);
            if (OboUtil.isPostCompTerm(genusTerm)) {
                this.genus = new TermTransferObject(genusTerm);
            } else {
                this.genus = genusTerm.getID();
            }
            for (Link link : OboUtil.getAllDifferentia(term)) {
                final Differentium diff = new Differentium();
                diff.setRelation(link.getType().getID());
                final LinkedObject parent = link.getParent();
                if (parent instanceof OBOClass) {
                    diff.setTerm(new TermTransferObject((OBOClass)parent));
                } else {
                    log().error("Differentia is not an OBOClass: " + parent);
                }
                this.differentia.add(diff);
            }
        } else {
            this.genus = term.getID();
        }
    }
    
    public OBOClass getTerm(OBOSession session) {
        if (this.differentia.isEmpty()) {
            return this.getGenus(session);
        } else {
            final OboUtil util = OboUtil.initPostCompTerm(this.getGenus(session));
            for (Differentium diff : this.differentia) {
                util.addRelDiff((OBOProperty)(session.getObject(diff.getRelation())), diff.getTerm().getTerm(session));
            }
            return util.getPostCompTerm();
        }
    }
    
    private OBOClass getGenus(OBOSession session) {
        if (this.genus instanceof String) {
            return (OBOClass)(session.getObject((String)this.genus));
        } else {
            return ((TermTransferObject)this.genus).getGenus(session);
        }
    }
    
    private static class Differentium implements Serializable {

        private String relation;
        private TermTransferObject term;

        public String getRelation() {
            return this.relation;
        }

        public void setRelation(String relation) {
            this.relation = relation;
        }

        public TermTransferObject getTerm() {
            return this.term;
        }

        public void setTerm(TermTransferObject term) {
            this.term = term;
        }

    }
    
    private Logger log() {
        return Logger.getLogger(this.getClass());
    }
    
}
