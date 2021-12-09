import java.io.InputStream;
import java.util.HashMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.function.library.print;
import org.apache.jena.vocabulary.VCARD;

public class TestJena {
	
	
	public static void main(String[] args) {
		
		String inputFileName = "example1.rdf";
		
		// create an empty model
		 Model model = ModelFactory.createDefaultModel();

		// RDFDataMgr for import
		InputStream in = RDFDataMgr.open(inputFileName);
		if (in == null) {
		    throw new IllegalArgumentException(
		                "File: " + inputFileName + " not found");
		}

		// read the RDF/XML file
		model.read(in, null);

		// write it to standard out
		model.write(System.out);
		
		HashMap<String, String> nameSpacePrefixes = (HashMap<String, String>) model.getNsPrefixMap();
		System.out.println(nameSpacePrefixes.get("people"));
		
		String fullNameString= extractObject(model, nameSpacePrefixes.get("people")+"SheikhMahad", VCARD.FN.toString());
		System.out.println(fullNameString);
		
	}
	
	public static String extractObject(Model model, String subjectURI, String predicateURI) {
		Resource subject = model.getResource(subjectURI);
		Property predicate = model.getProperty(predicateURI);
		
		return subject.getProperty(predicate).toString();
	}

}
