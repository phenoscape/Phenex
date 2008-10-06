package org.phenoscape.model;

import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOSession;
import org.obo.util.TermUtil;

public class AnatomyTermFilter implements TermFilter {

  private final OBOSession session;
  private static final String[] INCLUDES = {
    "TAO:0001668", //anatomical space
    "TAO:0001478", //anatomical cluster
    "TAO:0000548", //musculature system
    "TAO:0000272", //respiratory system
    "TAO:0000282", //sensory system
    "TAO:0000434", //skeletal system
    "TAO:0000496", //compound organ
    "TAO:0001622", //odontode
    "TAO:0001306", //pharyngeal arch
    "TAO:0001625", //tooth
    "TAO:0001114", //head
    "TAO:0001117", //post-vent region
    "TAO:0000292", //surface structure
    "TAO:0001115", //trunk
    "TAO:0000089", //actinotrichium
    "TAO:0005145", //muscle
    "TAO:0007009", //nerve
    "TAO:0000135", //notochord
    "TAO:0001641", //portion of connective tissue
    "TAO:0005143", //dentine
    "TAO:0005142", //enameloid
    "TAO:0001621", //otolith
    "PATO:0000462", //absent
    "PATO:0000467", //present
    "PATO:0000052", //shape
    "PATO:0000117", //size
    "PATO:0001447", //calcified
    "PATO:0001449", //cartilaginous
    "PATO:0001840", //compressed
    "PATO:0001847", //constricted
    "PATO:0001821", //imperforate
    "PATO:0001448", //ossified
    "PATO:0000649", //perforated
    "PATO:0001987", //saccular
    "PATO:0001480", //spongy
    "PATO:0001851", //swollen
    "PATO:0000150", //texture
    "PATO:0000133", //angle
    "PATO:0000610", //open
    "PATO:0000608", //closed
    "PATO:0000137", //orientation
    "PATO:0001608", //patchy
    "PATO:0000140", //placement
    "PATO:0001032", //position
    "PATO:0001512", //punctate
    "PATO:0000965", //symmetry
    "PATO:0000040", //distance
    "PATO:0001645", //protruding into
    "PATO:0001646", //protruding out of
    "PATO:0001631", //relational spatial quality
    "PATO:0001452", //relational structural quality
    "PATO:0000053", //count
    "PATO:0001647", //relational shape quality
    "PATO:0000014", //color
    "PATO:0000019", //color pattern
    "PATO:0000017", //color saturation
    "PATO:0000016", //color brightness
    "PATO:0001301", //opacity
    "PATO:0000020" //relative color
  };
  
  private static final String[] EXCLUDES = {
    "PATO:0001907", //botryoidal
    "PATO:0001978", //cut
    "PATO:0001984", //decurrent
    "PATO:0000945", //epinastic
    "PATO:0000949", //gasciated
    "PATO:0001882", //limaciform
    "PATO:0001356", //pleomorphic
    "PATO:0001357", //pulvinate
    "PATO:0001864", //sphericality
    "PATO:0000585", //hypotrophic
    "PATO:0001623", //atrophied
    "PATO:0001780", //dystrophic
    "PATO:0001940", //gigantic
    "PATO:0000584", //hypertrophic
    "PATO:0001643", //stubby
    "PATO:0000588", //vestigial
    "PATO:0001606", //greasy
    "PATO:0001370", //viscid
    "PATO:0000613", //disoriented
    "PATO:0000614", //oriented
    "PATO:0001629", //aggregated
    "PATO:0001932", //alternate placement
    "PATO:0001474", //anteverted
    "PATO:0000619", //crowded
    "PATO:0001953", //decussate
    "PATO:0001852", //dislocated
    "PATO:0001630", //dispersed
    "PATO:0001952", //distichous
    "PATO:0001597", //everted
    "PATO:0000623", //exserted
    "PATO:0001856", //introverted
    "PATO:0000625", //inverted
    "PATO:0000626", //lateralized
    "PATO:0000627", //localised
    "PATO:0000628", //mislocalised
    "PATO:0000629", //misrouted
    "PATO:0000631", //prostrate
    "PATO:0000633", //uncrowded
    "PATO:0000635", //unlocalised
    "PATO:0001951", //whorled
    "PATO:0001476", //decreased position
    "PATO:0001687", //elevation
    "PATO:0001475", //increased position
    "PATO:0001933", //opposite
    "PATO:0001477", //retracted
    "PATO:0001324", //bilateral symmetry
    "PATO:0001325", //radial symmetry
    "PATO:0000650", //supernumerary
    "PATO:0001473" //duplicated
  };

  public AnatomyTermFilter(OBOSession session) {
    this.session = session;
  }

  public boolean include(OBOObject obj) {
    final OBOClass term = (OBOClass)obj;
    //only filter out stuff from anatomy ontology or PATO
    final OBOClass teleostAnatomicalEntity = (OBOClass)(this.session.getObject("TAO:0100000"));
    final OBOClass quality = (OBOClass)(this.session.getObject("PATO:0000001"));
    if (!term.equals(teleostAnatomicalEntity) && !TermUtil.hasAncestor(term, teleostAnatomicalEntity) && !term.equals(quality) && !TermUtil.hasAncestor(term, quality)) {
      return true;
    }
    for (String includeID : INCLUDES) {
      if (this.equalsOrHasAncestor(term, includeID)) {
        for (String excludeID : EXCLUDES) {
          if (this.equalsOrHasAncestor(term, excludeID)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  private boolean equalsOrHasAncestor(OBOClass term, String ancestorID) {
    final OBOClass ancestor = (OBOClass)(this.session.getObject(ancestorID));
    if (term.equals(ancestor)) {
      return true;
    } else if (TermUtil.hasAncestor(term, ancestor)) {
      return true;
    }
    return false;
  }

}
