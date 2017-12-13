package org.phenoscape.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.obo.annotation.base.OBOUtil;
import org.obo.annotation.base.OBOUtil.Differentium;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;

/**
 * A class for creating serializable representations of OBOClasses (terms or post-compositions) for use in copy/paste.
 */
public class TermTransferObject implements Serializable {

    private final Object genus;
    private final List<SerializableDifferentium> serializableDifferentia = new ArrayList<SerializableDifferentium>();

    public TermTransferObject(OBOClass term) {
        if ((term != null) && (OBOUtil.isPostCompTerm(term))) {
            final OBOClass genusTerm = OBOUtil.getGenusTerm(term);
            if (OBOUtil.isPostCompTerm(genusTerm)) {
                this.genus = new TermTransferObject(genusTerm);
            } else {
                this.genus = genusTerm.getID();
            }
            for (Link link : OBOUtil.getAllDifferentia(term)) {
                final SerializableDifferentium diff = new SerializableDifferentium();
                diff.setRelation(link.getType().getID());
                final LinkedObject parent = link.getParent();
                if (parent instanceof OBOClass) {
                    diff.setTerm(new TermTransferObject((OBOClass)parent));
                } else {
                    log().error("Differentium is not an OBOClass: " + parent);
                }
                this.serializableDifferentia.add(diff);
            }
        } else {
            this.genus = term.getID();
        }
    }

    public OBOClass getTerm(OBOSession session) {
        if (this.serializableDifferentia.isEmpty()) {
            return this.getGenus(session);
        } else {
            final OBOClass genusTerm = this.getGenus(session);
            final List<Differentium> differentia = new ArrayList<Differentium>();
            for (SerializableDifferentium diff : this.serializableDifferentia) {
                differentia.add(this.convert(diff, session));
            }
            return OBOUtil.createPostComposition(genusTerm, differentia);
        }
    }

    private OBOClass getGenus(OBOSession session) {
        if (this.genus instanceof String) {
            return (OBOClass)(session.getObject((String)this.genus));
        } else {
            return ((TermTransferObject)this.genus).getTerm(session);
        }
    }

    private Differentium convert(SerializableDifferentium diff, OBOSession session) {
        final Differentium differentium = new Differentium();
        differentium.setRelation((OBOProperty)(session.getObject(diff.getRelation())));
        differentium.setTerm(diff.getTerm().getTerm(session));
        return differentium;
    }

    private static class SerializableDifferentium implements Serializable {

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
