package org.obo.annotation.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBORestriction;
import org.obo.datamodel.TermSubset;
import org.obo.datamodel.impl.OBOClassImpl;
import org.obo.datamodel.impl.OBORestrictionImpl;
import org.obo.util.TermUtil;


/** changing this from static to non-static so can build post comp object from
    multiple rel-diffs */

public class OBOUtil {

  private static final Logger LOG = Logger.getLogger(OBOUtil.class);
  private OBOClass postCompTerm;
  private String id;
  private String name;
  private boolean hasRelAndDiff=false;


//   /** used by OntologyManager */
//   public static OBOClass makePostCompTerm(OBOClass genus, OBOProperty rel,
//                                           OBOClass diff) {
//     String nm = pcString(genus.getName(),rel.getName(),diff.getName());
//     String id = pcString(genus.getID(),rel.getID(),diff.getID());
//     OBOClass postCompTerm = new OBOClassImpl(nm,id);
//     OBOProperty ISA = OBOProperty.IS_A;
//     OBORestrictionImpl gRel = new OBORestrictionImpl(postCompTerm,ISA,genus);
//     gRel.setCompletes(true); // post comp flag
//     postCompTerm.addParent(gRel);
//     OBORestrictionImpl dRel = new OBORestrictionImpl(postCompTerm,rel,diff);
//     dRel.setCompletes(true); // post comp
//     postCompTerm.addParent(dRel);
//     return postCompTerm;
//   }

  public static OBOClass makePostCompTerm(OBOClass genus, OBOProperty rel,
                                          OBOClass diff) {
    OBOUtil u = initPostCompTerm(genus);
    u.addRelDiff(rel,diff);
    return u.getPostCompTerm();
  }


  /** even if genus is null returns an oboutil - should it return null if genus null? ex? */
  public static OBOUtil initPostCompTerm(OBOClass genus) {
    OBOUtil ou = new OBOUtil();
    if (genus!=null) ou.addGenus(genus);
    return ou;
  }


  private void addGenus(OBOClass genus) {
  	if (genus==null) {
  	  LOG.error("Genus is null, cant add to postcomp");
  	  return;
  	}
    id = genus.getID();
    name = genus.getName();
    postCompTerm = new OBOClassImpl(name,id);
    OBOProperty ISA = OBOProperty.IS_A;
    OBORestrictionImpl gRel = new OBORestrictionImpl(postCompTerm,ISA,genus);
    gRel.setCompletes(true); // post comp flag
    postCompTerm.addParent(gRel);
  }

  public void addRelDiff(OBOProperty rel,OBOClass diff) {
    if (postCompTerm==null){
      LOG.error("cant add rel diff, post comp term is null");
      return;
    }
    OBORestrictionImpl dRel = new OBORestrictionImpl(postCompTerm,rel,diff);
    dRel.setCompletes(true); // post comp
    postCompTerm.addParent(dRel);
    name += relDiffString(rel.getName(),diff.getName());
    postCompTerm.setName(name);
    id += relDiffString(rel.getID(),diff.getID());
    // just for now
    ((OBOClassImpl)postCompTerm).setID(id);
    hasRelAndDiff = true;
  }

  public boolean hasRelAndDiff() { return hasRelAndDiff; }

  public OBOClass getPostCompTerm() { return postCompTerm; }

  private static String pcString(String g, String r, String d) {
    // for now hard wire to part_of
    return g + relDiffString(r,d);
  }

  private static String relDiffString(String r, String d) {
    return "^"+r+"("+d+")";
  }

  /** for non post comp returns term itself */
  public static OBOClass getGenusTerm(OBOClass term) {
    if (isPostCompTerm(term)) {
      for (Object o : term.getParents()) {
        OBORestriction r = (OBORestriction)o;
        if (r.completes() && r.getType().equals(OBOProperty.IS_A))
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
    return r.completes() && !r.getType().equals(OBOProperty.IS_A);
  }

  public static boolean isOboRestriction(Link l) {
    return l instanceof OBORestriction;
  }

  public static OBORestriction getOboRestriction(Link l) {
    if (!isOboRestriction(l)) return null;
    return (OBORestriction)l;
  }
  
  /** Return number of rel-diffs a term has */
  public static int getNumOfDiffs(OBOClass term) {
    if (term == null) return 0;
    int i=0;
    for (Link l : term.getParents())
      if (isLinkToDiff(l)) i++;
    return i;
  }


  /** Assumes theres only one term with diffRel, returns the 1st one it finds
      null if none found - ex? */
  public static OBOClass getDifferentiaTerm(OBOClass postComp,
                                            OBOProperty diffRel) {
    if (!isPostCompTerm(postComp)) return null; // ex?
    // loop thru parents looking for diffRel
    for (Link l : postComp.getParents()) {
      OBOProperty rel = l.getType();
      if (rel.equals(diffRel)) {
        LinkedObject lo = l.getParent();
        if (TermUtil.isClass(lo))
          return TermUtil.getClass(lo);
      }
    }
    return null; // diff not found - OboEx?
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

}
