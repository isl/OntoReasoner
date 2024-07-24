package src.main.java;

import java.io.StringReader;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;


public class OntologyIdentifier {
    private OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM );

    public String getRdfFormat(String extension) {
        String format = OntologyReasoner.langs.get(extension.toLowerCase());
        if (format == null) {
            throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }
        return format;
    }

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
