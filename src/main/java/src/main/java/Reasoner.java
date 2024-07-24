/*package src.main.java;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ArrayList;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;



public class Reasoner {
    private OntModel model;
    
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
    public Reasoner(String rdfsFilePath, String rdfFilePath){
        this.model= ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        FileManager.get().readModel(this.model,rdfsFilePath);
        FileManager.get().readModel(this.model, rdfFilePath);
    }

    public Reasoner(Collection<String>filePaths){
        this.model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        for(String filePath : filePaths){
            FileManager.get().readModel(this.model, filePath);
        }
    }

    public String getRdfFormat(String extension) {
        String format = langs.get(extension.toLowerCase());
        if (format == null) {
            throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }
        return format;
    }

    public boolean isSchema(String fileContents, String extension) {
        if (!Reasoner.langs.keySet().contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("The given file extension (" + extension + ") is not supported. "
                    + "The list of accepted file extensions is " + Reasoner.langs.keySet());
        }
        model.read(new StringReader(fileContents), null, Reasoner.langs.get(extension.toLowerCase()));
        String query = "ASK "
                + "WHERE { "
                + "?s ?p ?type. "
                + "FILTER("
                + "?type=<https://www.w3.org/1999/02/22-rdf-syntax-ns#Class> || "
                + "?type=<http://www.w3.org/1999/02/22-rdf-syntax-ns#Class> || "
                + "?type=<https://www.w3.org/2000/01/rdf-schema#Class> ||  "
                + "?type=<http://www.w3.org/2000/01/rdf-schema#Class> || "
                + "?type=<http://www.w3.org/2002/07/owl#Class> ||  "
                + "?type=<https://www.w3.org/2002/07/owl#Class> || "
                + "?type=<https://www.w3.org/1999/02/22-rdf-syntax-ns#Property> || "
                + "?type=<http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> || "
                + "?type=<https://www.w3.org/2000/01/rdf-schema#Property> ||  "
                + "?type=<http://www.w3.org/2000/01/rdf-schema#Property> || "
                + "?type=<http://www.w3.org/2002/07/owl#Property> ||  "
                + "?type=<https://www.w3.org/2002/07/owl#Property>) "
                + "}";
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        return qe.execAsk();
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


    public Collection<String> listInstancesOfClass(String className) {
        OntClass ontClass = this.model.getOntClass(className);
        Set<String> instanceSet = new HashSet<>();
        if (ontClass != null) {
            System.out.println("Class found: " + className);
            ExtendedIterator<? extends OntResource> instances = ontClass.listInstances();
            while (instances.hasNext()) {
                OntResource instance = instances.next();
                if (instance.getURI() != null) {
                    instanceSet.add(instance.getURI());
                } else {
                    instanceSet.add(instance.getLocalName());
                }
            }
        } else {
            System.out.println("Class not found: " + className);
        }
        return instanceSet;
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

    public Collection<String> listClassesWithInstances() {
        ExtendedIterator<OntClass> classIter = this.model.listClasses();
        Set<String> classWithInstancesSet = new HashSet<>();
        while (classIter.hasNext()) {
            OntClass ontClass = classIter.next();
            if (ontClass.listInstances().hasNext()) {
                classWithInstancesSet.add(ontClass.getURI());
            }
        }
        return classWithInstancesSet;
    }

        public Collection<Pair<String, String>> getInstanceUris(String classUri) {
        Collection<Pair<String, String>> instancesWithLabels = new ArrayList<>();
        
        Resource classResource = model.createResource(classUri);
        Property rdfType = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Property rdfsLabel = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
        ExtendedIterator<Individual> individuals = model.listIndividuals(classResource);
        while (individuals.hasNext()) {
            Individual individual = individuals.next();
            String instanceUri = individual.getURI();
            String label = "";
            if (individual.hasProperty(rdfsLabel)) {
                label = individual.getProperty(rdfsLabel).getString();
            }
            instancesWithLabels.add(new ImmutablePair<>(instanceUri, label));
        }
        
        return instancesWithLabels;
    }

    public static void main(String []args){
        Reasoner reasoner=new Reasoner("schema.rdfs","instance.rdf");
        String extension = ".ttl";
        String filecontent ="";
                
        boolean isSchemaResult = reasoner.isSchema(filecontent, extension);
        System.out.println("Schema: " + isSchemaResult);

        String rdfFormat = reasoner.getRdfFormat(extension);
        System.out.println("RDF format for extension " + extension + " is " + rdfFormat);

        System.out.println("\nClasses:");
        reasoner.listClasses().forEach( c -> System.out.println(c));

        System.out.println("\nProperties:");
        reasoner.listProperties().forEach( p -> System.out.println(p));

        System.out.println("\nInstances of Class:");
        reasoner.listInstancesOfClass("http://www.cidoc-crm.org/cidoc-crm/E4_Period").forEach(i -> System.out.println(i));

        System.out.println("\nProperties with range:");
        reasoner.listPropertiesWithRange("http://www.cidoc-crm.org/cidoc-crm/E18_Physical_Thing").forEach(r -> System.out.println(r));

        System.out.println("\nClasses that can be Range of Class:");
        reasoner.listClassesThatCanBeRangeOfClass("http://www.cidoc-crm.org/cidoc-crm/E4_Period").forEach(s -> System.out.println(s));

        System.out.println("\nClasses with Instances");
        reasoner.listClassesWithInstances().forEach(z -> System.out.println(z));

        System.out.println("\ngetInstance");
        reasoner.getInstanceUris("http://www.cidoc-crm.org/cidoc-crm/E65_Creation").forEach(e -> System.out.println(e));
        
    }
}*/