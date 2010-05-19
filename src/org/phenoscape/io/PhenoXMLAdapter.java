package org.phenoscape.io;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bioontologies.obd.schema.pheno.BearerDocument.Bearer;
import org.bioontologies.obd.schema.pheno.DescriptionDocument.Description;
import org.bioontologies.obd.schema.pheno.MeasurementDocument.Measurement;
import org.bioontologies.obd.schema.pheno.PhenotypeCharacterDocument.PhenotypeCharacter;
import org.bioontologies.obd.schema.pheno.PhenotypeDocument.Phenotype;
import org.bioontologies.obd.schema.pheno.QualifierDocument.Qualifier;
import org.bioontologies.obd.schema.pheno.QualityDocument.Quality;
import org.bioontologies.obd.schema.pheno.RelatedEntityDocument.RelatedEntity;
import org.bioontologies.obd.schema.pheno.TyperefDocument.Typeref;
import org.bioontologies.obd.schema.pheno.UnitDocument.Unit;
import org.obo.annotation.base.OBOUtil;
import org.obo.annotation.base.OBOUtil.Differentium;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.DanglingClassImpl;
import org.obo.datamodel.impl.DanglingPropertyImpl;

public class PhenoXMLAdapter {

    private final Set<String> danglers = new HashSet<String>();
    private final Set<String> secondaryIDs = new HashSet<String>();
    private final OBOSession session;

    public PhenoXMLAdapter(OBOSession session) {
        this.session = session;
    }

    public boolean didCreateDanglers() {
        return !this.danglers.isEmpty();
    }

    public Collection<String> getDanglersList() {
        return this.danglers;
    }

    public boolean didMigrateSecondaryIDs() {
        return !this.secondaryIDs.isEmpty();
    }

    public Collection<String> getMigratedSecondaryIDsList() {
        return this.secondaryIDs;
    }

    public static PhenotypeCharacter createPhenotypeCharacter(org.phenoscape.model.Phenotype phenoCharacter) {
        if ((phenoCharacter.getEntity() == null) && (phenoCharacter.getQuality() == null)) {
            return null;
        }
        final PhenotypeCharacter phenotypeCharacter = PhenotypeCharacter.Factory.newInstance();
        if (phenoCharacter.getEntity() != null) {
            final Bearer bearer = phenotypeCharacter.addNewBearer();
            bearer.setTyperef(getTyperefForTerm(phenoCharacter.getEntity()));
        }
        if (phenoCharacter.getQuality() != null) {
            final Quality quality = phenotypeCharacter.addNewQuality();
            quality.setTyperef(getTyperefForTerm(phenoCharacter.getQuality()));
            if (phenoCharacter.getRelatedEntity() != null) {
                final RelatedEntity relatedEntity = quality.addNewRelatedEntity();
                relatedEntity.setTyperef(getTyperefForTerm(phenoCharacter.getRelatedEntity()));
            }
            if (phenoCharacter.getCount() != null) {
                quality.setCount(BigInteger.valueOf(phenoCharacter.getCount()));
            }
            if (phenoCharacter.getMeasurement() != null) {
                final Measurement measurement = quality.addNewMeasurement();
                measurement.setValue(phenoCharacter.getMeasurement());
                if (phenoCharacter.getUnit() != null) {
                    final Unit unit = measurement.addNewUnit();
                    unit.setTyperef(getTyperefForTerm(phenoCharacter.getUnit()));
                }
            }
        }
        if (phenoCharacter.getComment() != null) {
            final Description description = phenotypeCharacter.addNewDescription();
            description.setStringValue(phenoCharacter.getComment());
        }
        return phenotypeCharacter;
    }

    public static Phenotype createPhenotype(List<PhenotypeCharacter> phenotypeCharacters) {
        final Phenotype phenotype = Phenotype.Factory.newInstance();
        phenotype.setPhenotypeCharacterArray(phenotypeCharacters.toArray(new PhenotypeCharacter[] {}));
        return phenotype;
    }

    public List<org.phenoscape.model.Phenotype> parsePhenotype(Phenotype phenotype) {
        final List<org.phenoscape.model.Phenotype> newPhenotypes = new ArrayList<org.phenoscape.model.Phenotype>();
        for (PhenotypeCharacter pc : phenotype.getPhenotypeCharacterList()) {
            newPhenotypes.add(parsePhenotypeCharacter(pc));
        }
        return newPhenotypes;
    }

    public org.phenoscape.model.Phenotype parsePhenotypeCharacter(PhenotypeCharacter pc) {
        final org.phenoscape.model.Phenotype newPhenotype = new org.phenoscape.model.Phenotype();
        if ((pc.getBearer() != null) && (pc.getBearer().getTyperef() != null)) {
            newPhenotype.setEntity(this.getTermForTyperef(pc.getBearer().getTyperef()));
        }

        if (!pc.getQualityList().isEmpty()) {
            // we only load the first quality for now
            final Quality quality = pc.getQualityList().get(0);
            if (quality.getTyperef() != null) {
                newPhenotype.setQuality(this.getTermForTyperef(quality.getTyperef()));
            }
            if (!quality.getRelatedEntityList().isEmpty()) {
                //we only use one related entity for now
                final RelatedEntity e2 = quality.getRelatedEntityList().get(0);
                if (e2.getTyperef() != null) {
                    newPhenotype.setRelatedEntity(this.getTermForTyperef(e2.getTyperef()));
                }
            }
            if (quality.getCount() != null) {
                newPhenotype.setCount(quality.getCount().intValue());
            }
            if (!quality.getMeasurementList().isEmpty()) {
                // we only use one measurement for now
                final Measurement measurement = quality.getMeasurementList().get(0);
                newPhenotype.setMeasurement(measurement.getValue());
                if ((measurement.getUnit() != null) && (measurement.getUnit().getTyperef() != null)) {
                    newPhenotype.setUnit(this.getTermForTyperef(measurement.getUnit().getTyperef()));
                }

            }
        }
        if (pc.getDescription() != null) {
            newPhenotype.setComment(pc.getDescription().getStringValue());
        }
        return newPhenotype;
    }

    private static Typeref getTyperefForTerm(OBOClass term) {
        final Typeref tr = Typeref.Factory.newInstance();
        if (OBOUtil.isPostCompTerm(term)) {
            tr.setAbout(OBOUtil.getGenusTerm(term).getID());
            for (Link link : OBOUtil.getAllDifferentia(term)) {
                final LinkedObject parent = link.getParent();
                if (!(parent instanceof OBOClass)) continue;
                final OBOClass differentia = (OBOClass)parent;
                final Qualifier qualifier = tr.addNewQualifier();
                qualifier.setRelation(link.getType().getID());
                qualifier.addNewHoldsInRelationTo().setTyperef(getTyperefForTerm(differentia));
            }
        } else {
            tr.setAbout(term.getID());
        }
        return tr;
    }

    private OBOClass getTermForTyperef(Typeref tr) {
        final OBOClass genus = this.getTerm(tr.getAbout());
        if (tr.sizeOfQualifierArray() > 0) {
            // need to create post-comp
            final List<Differentium> differentia = new ArrayList<Differentium>();
            for (Qualifier qualifier : tr.getQualifierList()) {
                final OBOProperty relation = this.getRelation(qualifier.getRelation());
                final OBOClass term = this.getTermForTyperef(qualifier.getHoldsInRelationTo().getTyperef());
                final Differentium differentium = new Differentium();
                differentium.setRelation(relation);
                differentium.setTerm(term);
                differentia.add(differentium);
            }
            return OBOUtil.createPostComposition(genus, differentia);
        } else {
            return genus;
        }
    }

    private OBOClass getTerm(String id) {
        final IdentifiedObject term = this.session.getObject(id);
        if (term instanceof OBOClass) {
            return (OBOClass)term;
        } else {
            final OBOClass altTerm = this.findTermByAltID(id);
            if (altTerm != null) {
                return altTerm;
            } else {
                log().warn("Term not found; creating dangler for " + id);
                this.danglers.add(id);
                final OBOClass dangler = new DanglingClassImpl(id.trim());
                return dangler;
            }
        }
    }

    private OBOClass findTermByAltID(String id) {
        final Collection<IdentifiedObject> terms = this.session.getObjects();
        for (IdentifiedObject object : terms) {
            if (object instanceof OBOClass) {
                final OBOClass term = (OBOClass)object;
                if (term.getSecondaryIDs().contains(id)) {
                    this.secondaryIDs.add(id);
                    return term;
                }
            }
        }
        return null;
    }

    private OBOProperty getRelation(String id) {
        final IdentifiedObject relation = this.session.getObject(id);
        if (relation instanceof OBOProperty) {
            return (OBOProperty)relation;
        } else {
            log().warn("Property not found; creating dangler for " + id);
            this.danglers.add(id);
            final OBOProperty dangler = new DanglingPropertyImpl(id);
            return dangler;
        }
    }

    private static Logger log() {
        return Logger.getLogger(PhenoXMLAdapter.class);
    }

}
