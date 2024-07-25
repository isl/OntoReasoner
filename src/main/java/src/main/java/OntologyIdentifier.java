package src.main.java;

import java.io.StringReader;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;

/**
 * Class for identifying ontologies and checking if a file is an RDF schema.
 */
public class OntologyIdentifier {

    /**
     * Ontology model used for reading and manipulating RDF data.
     */
    private OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

    /**
     * Retrieves the RDF format based on the given file extension.
     *
     * @param extension the file extension
     * @return the RDF format corresponding to the extension
     * @throws IllegalArgumentException if the file extension is not supported
     */
    public String getRdfFormat(String extension) {
        String format = OntologyReasoner.langs.get(extension.toLowerCase());
        if (format == null) {
            throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }
        return format;
    }

    /**
     * Checks if the given file contents represent an RDF schema.
     *
     * @param fileContents the contents of the file
     * @param extension the file extension
     * @return true if the file is an RDF schema, false otherwise
     * @throws IllegalArgumentException if the file extension is not supported
     */
    public boolean isSchema(String fileContents, String extension) {
        if (!OntologyReasoner.langs.keySet().contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("The given file extension (" + extension + ") is not supported. "
                    + "The list of accepted file extensions is " +  OntologyReasoner.langs.keySet());
        }
        model.read(new StringReader(fileContents), null, OntologyReasoner.langs.get(extension.toLowerCase()));
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
}