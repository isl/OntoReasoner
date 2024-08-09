package gr.forth.ics.isl;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * Class for reasoning over ontologies, listing classes and properties,
 * and identifying relationships between them.
 */
public class OntologyReasoner {

    /**
     * Ontology model used for reading and manipulating RDF data.
     */
    private OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    private static OntModel modelAll = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

    /**
     * HashMap storing RDF formats associated with file extensions.
     */
    protected static final HashMap<String, String> langs = new HashMap<String, String>();


    public void setModel(OntModel model) {
        this.model = model;
    }


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
     * Lists all classes defined in the ontology model.
     *
     * @return a collection of URIs of all classes
     */
    public Collection<String> getAllClasses() {
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
    public Collection<String> getAllProperties() {
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
    public Collection<String> listProperties(String className) {
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
    public Collection<String> listObjects(String className) {
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
     * Reads the content of a schema file provided as a URL and returns true if the schema is valid.
     * 
     * @param modelNS the URL of the schema file.
     * @return true if the schema is valid, false otherwise.
     */
    public boolean initiateModel(String modelNS) {
        String ext = modelNS.substring(modelNS.lastIndexOf("."));
        Model baseModel = ModelFactory.createDefaultModel();
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, baseModel);

        try {
            model.read(modelNS, langs.get(ext));
        } catch (Exception e) {
            System.err.println("Error reading model: " + e.getMessage());
            return false;
        }

        // Check if the model is consistent
        ValidityReport validity = model.validate();
        boolean isValid = validity.isValid();
        if (isValid) {
            modelAll.addSubModel(model);
        }

        return isValid;
    }

    /**
    * Loads an ontology from a URL and checks its validity.
    *
    * @param modelNS The URL of the ontology file.
    * @param extension The file extension of the ontology.
    * @return {@code true} if the model is valid (consistent); {@code false} otherwise.
    * @throws org.apache.jena.shared.JenaException If there is an issue with reading the model or an invalid file format.
    */
    public boolean initiateModelUrl(String modelNS, String extension) {
        disableLogging();
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, (Model) null);
    
        try {
            model.setDerivationLogging(false);
            model.read(modelNS, langs.get(extension));
        } catch (org.apache.jena.shared.JenaException e) {
            if (e.getMessage().contains("java.io.IOException")) {
                throw new org.apache.jena.shared.JenaException("Connection refused to connect: " + e.getMessage());
            } else if (e.toString().contains("org.apache.jena.shared.SyntaxError")) {
                throw new org.apache.jena.shared.SyntaxError("Wrong file format for extension: " + extension);
            } else {
                throw new org.apache.jena.shared.JenaException("Error: " + e.getMessage());
            }
        }

        model.prepare();
        OntModel tmp = modelAll;
        modelAll = model;
        modelAll.addSubModel(tmp);
    
        // Check if the model is consistent
        ValidityReport validity = model.validate();
        return validity.isValid();
    }
    
    // Method to disable logging
    public static void disableLogging() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger("ROOT");
        rootLogger.setLevel(Level.OFF);
    }

    /**
    * Creates an ontology model using the provided file content and checks its validity.
    *
    * @param fileContent The content of the ontology file.
    * @param extension The file extension of the ontology.
    * @return {@code true} if the model is valid (consistent); {@code false} otherwise.
    * @throws org.apache.jena.shared.JenaException If there is an issue with reading the model or an invalid file format.
    */
    public boolean initiateModelFileContent(String fileContent, String extension) {
        disableLogging();

        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, (Model) null);
        InputStream in = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

        try {
            model.setDerivationLogging(false);
            model.read(in, langs.get(extension));
        } catch (org.apache.jena.shared.JenaException e) {
            if (e.getMessage().contains("java.io.IOException")) {
                throw new org.apache.jena.shared.JenaException("Connection refused to connect: " + e.getMessage());
            } else if (e.toString().contains("org.apache.jena.shared.SyntaxError")) {
                throw new org.apache.jena.shared.SyntaxError("Wrong file format for extension: " + extension);
            } else {
                throw new org.apache.jena.shared.JenaException("Error: " + e.getMessage());
            }
        }

    model.prepare();
    OntModel tmp = modelAll;
    modelAll = model;
    modelAll.addSubModel(tmp);

    ValidityReport validity = model.validate();
    return validity.isValid();
    }

}