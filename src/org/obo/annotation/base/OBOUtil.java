package org.obo.annotation.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.obo.app.model.AbstractPropertyChangeObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBORestriction;
import org.obo.datamodel.TermSubset;
import org.obo.datamodel.impl.OBOClassImpl;
import org.obo.datamodel.impl.OBORestrictionImpl;

public class OBOUtil {

    public static OBOClass createPostComposition(OBOClass genus, List<Differentium> differentia) {
        final String id = createPostcompositionID(genus, differentia);
        final String name = createPostcompositionName(genus, differentia);
        final OBOClass postComposition = new OBOClassImpl(name, id);
        final OBORestrictionImpl genusRelation = new OBORestrictionImpl(postComposition, OBOProperty.IS_A, genus);
        genusRelation.setCompletes(true);
        postComposition.addParent(genusRelation);
        for (Differentium differentium : differentia) {
            final OBORestrictionImpl differentiumRelation = new OBORestrictionImpl(postComposition, differentium.getRelation(), differentium.getTerm());
            differentiumRelation.setCompletes(true);
            postComposition.addParent(differentiumRelation);
        }
        return postComposition;
    }

    private static String createPostcompositionID(OBOClass genus, List<Differentium> differentia) {
        final StringBuffer sb = new StringBuffer();
        sb.append(genus.getID());
        for (Differentium differentium : differentia) {
            sb.append(createDifferentiumID(differentium));
        }
        return sb.toString();
    }

    private static String createPostcompositionName(OBOClass genus, List<Differentium> differentia) {
        final StringBuffer sb = new StringBuffer();
        sb.append(genus.getName());
        for (Differentium differentium : differentia) {
            sb.append(createDifferentiumName(differentium));
        }
        return sb.toString();
    }

    private static String relationDifferentiumFormat(String r, String d) {
        return "^"+r+"("+d+")";
    }

    private static String createDifferentiumID(Differentium differentium) {
        return relationDifferentiumFormat(differentium.getRelation().getID(), differentium.getTerm().getID());
    }

    private static String createDifferentiumName(Differentium differentium) {
        return relationDifferentiumFormat(differentium.getRelation().getName(), differentium.getTerm().getName());
    }

    /** for non post comp returns term itself */
    public static OBOClass getGenusTerm(OBOClass term) {
        if (isPostCompTerm(term)) {
            for (Object o : term.getParents()) {
                OBORestriction r = (OBORestriction)o;
                if (r.getCompletes() && r.getType().equals(OBOProperty.IS_A))
                    return (OBOClass)r.getParent(); // check downcast?
            }
            // error msg?
        }
        return term;
    }

    public static boolean isPostCompTerm(OBOObject term) {
        if (term.getID().indexOf('^') < 0) { return false; }
        for (Link l : term.getParents()) {
            if (isLinkToDiff(l)) { return true; }
        }
        return false;
    }

    public static boolean isLinkToDiff(Link l) {
        if (!isOboRestriction(l)) return false;
        return isLinkToDiff(getOboRestriction(l));
    }

    public static boolean isLinkToDiff(OBORestriction r) {
        // i guess there is a case where isa is completes - but why?
        return r.getCompletes() && !r.getType().equals(OBOProperty.IS_A);
    }

    public static boolean isOboRestriction(Link l) {
        return l instanceof OBORestriction;
    }

    public static OBORestriction getOboRestriction(Link l) {
        if (!isOboRestriction(l)) return null;
        return (OBORestriction)l;
    }

    public static List<Link> getAllDifferentia(OBOClass postComp) {
        final List<Link> restrictions = new ArrayList<Link>();
        for (Link l : postComp.getParents()) {
            if (isLinkToDiff(l)) restrictions.add(l);
        }
        return restrictions;
    }

    /**
     * Finds and returns the closest attribute ancestor for a value term from PATO.
     * Returns the input term if it is itself an attribute, or null if no attribute is found.
     */
    public static OBOClass getAttributeForValue(OBOClass valueTerm) {
        final Set<TermSubset> categories = valueTerm.getSubsets();
        final Set<String> categoryNames = new HashSet<String>();
        for (TermSubset category : categories) {
            categoryNames.add(category.getName());
        }
        if (categoryNames.contains("attribute_slim")) {
            return valueTerm;
        } else if ((categoryNames.contains("value_slim"))) {
            return getAttributeForValue(getIsaParentForTerm(valueTerm));
        }
        return null;
    }

    /**
     * Returns the "is_a" parent for an OBO term, or null if one is not found.
     */
    public static OBOClass getIsaParentForTerm(OBOClass term) {
        final Collection<Link> parents = term.getParents();
        for (Link link : parents) {
            if (link.getType().getName().equals("is_a")) {
                return (OBOClass)(link.getParent());
            }
        }
        return null;
    }

    //    if (ObjectUtils.equals(this.validName, validName)) return;
    //    final OBOClass oldValue = this.validName;
    //    this.validName = validName;
    //    this.firePropertyChange(VALID_NAME, oldValue, validName);

    public static class Differentium extends AbstractPropertyChangeObject {

        public static final String RELATION = "relation";
        public static final String TERM = "term";
        private OBOProperty relation;
        private OBOClass term;

        public OBOProperty getRelation() {
            return this.relation;
        }

        public void setRelation(OBOProperty relation) {
            if (ObjectUtils.equals(this.relation, relation)) return;
            final OBOProperty oldValue = this.relation;
            this.relation = relation;
            this.firePropertyChange(RELATION, oldValue, relation);
        }

        public OBOClass getTerm() {
            return this.term;
        }

        public void setTerm(OBOClass term) {
            if (ObjectUtils.equals(this.term, term)) return;
            final OBOClass oldValue = this.term;
            this.term = term;
            this.firePropertyChange(TERM, oldValue, term);
        }

        public boolean isComplete() {
            return (this.relation != null) && (this.term != null);
        }

        @Override
        public Class<?> getClass(String propertyKey) throws UndefinedKeyException {
            if (propertyKey.equals(TERM)) {
                return OBOClass.class;
            } else if (propertyKey.equals(RELATION)) {
                return OBOProperty.class;
            } else {
                return super.getClass(propertyKey);
            }
        }

    }

}
