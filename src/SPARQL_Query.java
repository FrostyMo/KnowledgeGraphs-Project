import java.io.InputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;


public class SPARQL_Query {

	public static void main(String[] args) {
		
		String inputFileName = "example1.rdf";
		
		Model model = readFile(inputFileName);
		
		
		String queryString = " SELECT ?Fname ?famName ?givenName\n"
				+ "				WHERE\n"
				+ "				{\n"
				+ "				  ?person <http://www.w3.org/2001/vcard-rdf/3.0#FN> ?Fname .\n"
				+ "				  ?person <http://www.w3.org/2001/vcard-rdf/3.0#N> ?bNode .\n"
				+ "				  ?bNode <http://www.w3.org/2001/vcard-rdf/3.0#Family> ?famName .\n"
				+ "				  ?bNode <http://www.w3.org/2001/vcard-rdf/3.0#Given> ?givenName .\n"
				+ "				}\n";
		
		Query query = QueryFactory.create(queryString);
		
		try(QueryExecution qExecution = QueryExecutionFactory.create(query, model)){
			
			ResultSet resultSet = qExecution.execSelect();
//			ResultSetFormatter.out(System.out, resultSet, query);
			
			while(resultSet.hasNext()) {
				QuerySolution solution = resultSet.nextSolution();
				
				Literal l1 = solution.getLiteral("?Fname");
				Literal l2 = solution.getLiteral("?famName");
				Literal l3 = solution.getLiteral("?givenName");
				
				System.out.println("Full Name: " + l1 + "\nFamily Name: " + l2 + "\nGiven Name: " + l3);
			}
			
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
	
	
	// Read file in RDF/XML and return a model
	public static Model readFile(String inputFileName) {
		Model model = ModelFactory.createDefaultModel();
		InputStream in = RDFDataMgr.open(inputFileName);
		
		if (in == null) {
		    throw new IllegalArgumentException(
		                "File: " + inputFileName + " not found");
		}
		model.read(in, null);
		
		return model;
	}

}
