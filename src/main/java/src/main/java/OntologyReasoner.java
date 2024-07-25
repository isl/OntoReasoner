package src.main.java;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class for reasoning over ontologies, listing classes and properties,
 * and identifying relationships between them.
 */
public class OntologyReasoner {

    /**
     * Ontology model used for reading and manipulating RDF data.
     */
    private OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

    /**
     * HashMap storing RDF formats associated with file extensions.
     */
    protected static final HashMap<String, String> langs = new HashMap<String, String>();

    // Static initializer block to populate the langs map with supported RDF formats
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

    /**
     * Loads a schema file into the model and returns all declared namespaces with their prefixes.
     *
     * @param schemaFile The file containing the schema.
     * @return A map where the key is the namespace prefix and the value is the namespace URI.
     * @throws FileNotFoundException If the schema file cannot be found.
     */
    
    public Map<String, String> initiateModel(File schemaFile) throws FileNotFoundException {
        InputStream targetStream = new FileInputStream(schemaFile);
        
        // Get the file extension to determine the format
        String filePath = schemaFile.getPath();
        String extension = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();

        // Check if the file extension is supported
        if (!OntologyReasoner.langs.keySet().contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("The given file extension (" + extension + ") is not supported. "
                    + "The list of accepted file extensions is " + OntologyReasoner.langs.keySet());
        }

        // Read the schema file into the model
        model.read(targetStream, null, langs.get(extension));
        
        // Retrieve the namespace prefixes and URIs
        Map<String, String> nsPrefixMap = model.getNsPrefixMap();

        return nsPrefixMap;
    }

    /**
     * Lists all classes defined in the ontology model.
     *
     * @return a collection of URIs of all classes
     */
    public Collection<String> listClasses() {
        ExtendedIterator<OntClass> classIter = this.model.listClasses();
        Set<String> classSet = new HashSet<>();
        while (classIter.hasNext()) {
            classSet.add(classIter.next().getURI());
        }
        return classSet;
    }

    /**
     * Lists all properties defined in the ontology model.
     *
     * @return a collection of URIs of all properties
     */
    public Collection<String> listProperties() {
        ExtendedIterator<OntProperty> propIter = this.model.listOntProperties();
        Set<String> propertySet = new HashSet<>();
        while (propIter.hasNext()) {
            propertySet.add(propIter.next().getURI());
        }
        return propertySet;
    }

    /**
     * Lists all properties that have a specific class as their range.
     *
     * @param className the URI of the class to check as the range
     * @return a collection of URIs of properties with the specified class as their range
     */
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

    /**
     * Lists all classes that can be the range of properties where the specified class is the domain.
     *
     * @param className the URI of the class to check as the domain
     * @return a collection of URIs of classes that can be the range of properties with the specified class as the domain
     */
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