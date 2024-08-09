import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.forth.ics.isl.OntologyReasoner;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestReasonner {

    private OntologyReasoner reasoner;
    private OntModel model;

    @BeforeEach
    public void setUp() {
        model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        model.createClass("http://www.cidoc-crm.org/cidoc-crm/E4_Period");
        model.createClass("http://www.cidoc-crm.org/cidoc-crm/E1_CRM_Entity");
        model.createClass("http://www.cidoc-crm.org/cidoc-crm/E8_Acquisition");

        model.createOntProperty("http://www.cidoc-crm.org/cidoc-crm/P147_curated");
        model.createOntProperty("http://www.cidoc-crm.org/cidoc-crm/P148_has_component");

        reasoner = new OntologyReasoner();
        reasoner.setModel(model);
    }

    @Test
    public void testGetAllClasses() {

        Collection<String> classes = reasoner.getAllClasses();
        assertEquals(3, classes.size());
        assertTrue(classes.contains("http://www.cidoc-crm.org/cidoc-crm/E4_Period"));
        assertTrue(classes.contains("http://www.cidoc-crm.org/cidoc-crm/E1_CRM_Entity"));
        assertTrue(classes.contains("http://www.cidoc-crm.org/cidoc-crm/E8_Acquisition"));
    }

    @Test
    public void testGetAllProperties() {
        Collection<String> properties = reasoner.getAllProperties();
        assertEquals(2, properties.size());
        assertTrue(properties.contains("http://www.cidoc-crm.org/cidoc-crm/P147_curated"));
        assertTrue(properties.contains("http://www.cidoc-crm.org/cidoc-crm/P148_has_component"));
    }
}