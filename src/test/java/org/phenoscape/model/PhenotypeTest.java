package org.phenoscape.model;

import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.impl.OBOClassImpl;

public class PhenotypeTest {
    
    @Test
    public void testEquality() {
        final OBOClass term = new OBOClassImpl("FAKE:42");
        final Phenotype phenotype1 = new Phenotype();
        phenotype1.setComment("comment");
        phenotype1.setEntity(term);
        phenotype1.setCount(42);
        final Phenotype phenotype2 = new Phenotype();
        phenotype2.setComment("comment");
        phenotype2.setEntity(term);
        phenotype2.setCount(42);
        Assert.assertEquals("Phenotypes should be equal", phenotype1, phenotype2);
        final List<Phenotype> list1 = Collections.singletonList(phenotype1);
        final List<Phenotype> list2 = Collections.singletonList(phenotype2);
        Assert.assertEquals("Equality should work in lists", list1, list2);
        phenotype1.setCount(null);
        phenotype2.setCount(0);
        Assert.assertFalse("Null and zero should not be equal", phenotype1.equals(phenotype2));
        phenotype1.setCount(0);
        phenotype2.setRelatedEntity(term);
        Assert.assertFalse("Phenotypes should not be equal", phenotype1.equals(phenotype2));
    }

}
