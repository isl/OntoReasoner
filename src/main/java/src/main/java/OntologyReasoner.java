package src.main.java;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class OntologyReasoner {
    private OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM );
    protected static final HashMap<String, String> langs = new HashMap<String, String>();

    static {
        langs.put(".ttl", "Turtle");
        langs.put(".nt", "N-TRIPLES");
        langs.put(".nq", "N-Quads");
        langs.put(".trig", "TriG");
        langs.put(".rdf", "RDF/XML");
        langs.put(".rdfs", "RDF/XML");
        langs.put(".owl", "RDF/XML");
        langs.put(".jsonld", "JSON-LD");
        langs.put(".trdf", "RDF Thrift");
        langs.put(".rt", "RDF Thrift");
        langs.put(".rj", "RDF/JSON");
        langs.put(".trix", "TriX");
    }

    public Collection<String> listClasses(){
        ExtendedIterator<OntClass> classIter=this.model.listClasses();
        Set<String> classSet=new HashSet<>();
        while(classIter.hasNext())
            classSet.add(classIter.next().getURI());
        return classSet;
    }

    public Collection<String> listProperties(){
        ExtendedIterator<OntProperty> propIter=this.model.listOntProperties();
        Set<String> propertySet=new HashSet<>();
        while(propIter.hasNext())
            propertySet.add(propIter.next().getURI());
        return propertySet;
    }

    public Collection<String> listPropertiesWithRange(String className) {
        ExtendedIterator<OntProperty> propIter = this.model.listOntProperties();
        Set<String> propertySet = new HashSet<>();
        while (propIter.hasNext()) {
            OntProperty property = propIter.next();
            if (property.getRange() != null && property.getRange().getURI().equals(className)) {
                propertySet.add(property.getURI());
            }
        }
        return propertySet;
    }

    public Collection<String> listClassesThatCanBeRangeOfClass(String className) {
        Set<String> classSet = new HashSet<>();
        ExtendedIterator<OntProperty> propIter = this.model.listOntProperties();
        while (propIter.hasNext()) {
            OntProperty property = propIter.next();
            if (property.getRange() != null && property.getRange().getURI().equals(className)) {
                ExtendedIterator<? extends OntResource> domainClasses = property.listDomain();
                while (domainClasses.hasNext()) {
                    OntResource domainClass = domainClasses.next();
                    if (domainClass.getURI() != null) {
                        classSet.add(domainClass.getURI());
                    }
                }
            }
        }
        return classSet;
    }    
}
