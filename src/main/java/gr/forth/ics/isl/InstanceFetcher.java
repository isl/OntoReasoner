package gr.forth.ics.isl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for fetching instances and their associated classes from an ontology model.
 */
public class InstanceFetcher {

    /**
     * Ontology model used for reading and manipulating RDF data.
     */
    private OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

    /**
     * Logger for logging messages and errors.
     */
    private static final Logger logger = LoggerFactory.getLogger(InstanceFetcher.class);

    /**
     * Constructs an InstanceFetcher and initializes the ontology model with the given file contents and extension.
     *
     * @param fileContents the contents of the ontology file as a string
     * @param extension    the file extension indicating the RDF format
     * @throws IllegalArgumentException if the given file extension is not supported
     */
    public InstanceFetcher(String fileContents, String extension) {
        if (!OntologyReasoner.langs.keySet().contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("The given file extension (" + extension + ") is not supported. "
                    + "The list of accepted file extensions is " + OntologyReasoner.langs.keySet());
        }
        model.read(new StringReader(fileContents), null, OntologyReasoner.langs.get(extension.toLowerCase()));
    }

    /**
     * Retrieves all unique class URIs from the ontology model.
     *
     * @return a collection of class URIs
     */
    public Collection<String> getClassUris() {
        Set<String> retCollection = new HashSet<>();
        String selectQuery = "SELECT DISTINCT ?class "
                + "WHERE { "
                + "?subject <" + RDF.type + "> ?class "
                + "}";
        QueryExecution qe = QueryExecutionFactory.create(selectQuery, this.model);
        ResultSet results = qe.execSelect();
        while (results.hasNext()) {
            QuerySolution result = results.next();
            retCollection.add(result.get("class").toString());
        }
        return retCollection;
    }

    /**
     * Retrieves all instance URIs and their labels for a given class URI.
     *
     * @param classUri the URI of the class
     * @return a collection of pairs containing instance URIs and their labels
     */
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

    /**
     * Retrieves all classes and their instances along with the instance labels.
     *
     * @return a multimap containing class URIs as keys and pairs of instance URIs and labels as values
     */
    public Multimap<String, Pair<String, String>> getClassAndInstanceUris() {
        Multimap<String, Pair<String, String>> classAndInstances = ArrayListMultimap.create();
        Collection<String> classUris = getClassUris();
        for (String classUri : classUris) {
            Collection<Pair<String, String>> instances = getInstanceUris(classUri);
            classAndInstances.putAll(classUri, instances);
        }
        return classAndInstances;
    }

    public static void main(String[] args) {
        String filePath = "examples/instance.rdf";
        String fileExtension = ".rdf";

        StringBuilder fileContents = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                fileContents.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error("Error", e);
            return;
        }

        InstanceFetcher fetcher = new InstanceFetcher(fileContents.toString(), fileExtension);

        Collection<String> classUris = fetcher.getClassUris();
        System.out.println("Class URIs:");
        for (String uri : classUris) {
            System.out.println(uri);
        }

        System.out.println("\nInstance URIs:");
        for (String classUri : classUris) {
            Collection<Pair<String, String>> instanceUris = fetcher.getInstanceUris(classUri);
            for (Pair<String, String> instance : instanceUris) {
                System.out.println("Instance: " + instance.getLeft() + ", Label: " + instance.getRight());
            }
        }

        Multimap<String, Pair<String, String>> classAndInstanceUris = fetcher.getClassAndInstanceUris();
        System.out.println("\nClass and Instance URIs:");
        for (String classUri : classAndInstanceUris.keySet()) {
            Collection<Pair<String, String>> instances = classAndInstanceUris.get(classUri);
            for (Pair<String, String> instance : instances) {
                System.out.println("Class: " + classUri + ", Instance: " + instance.getLeft() + ", Label: " + instance.getRight());
            }
        }
    }
}