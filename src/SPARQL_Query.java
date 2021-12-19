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
		
		String queryString1 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
			+"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
			+"PREFIX mar: <http://www.semanticweb.org/mominsalar/ontologies/2021/10/mar#>\n"
			+"PREFIX dbo: <https://dbpedia.org/ontology/>\n"
			+"PREFIX bif: <http://www.openlinksw.com/schemas/bif#>\n"
			+"SELECT ?Players ?Category ?startDateTime ?endDateTime ?playTimen\n"
			+"WHERE {\n"
			+ "   ?Players mar:playedGame ?Game .\n"
			+ "   ?Game mar:hasGameCategory ?Category .\n"
			+ "   ?Game dbo:startDateTime ?startDateTime .\n"
			 +"   ?Game dbo:endDateTime ?endDateTime .\n"
			+ "   BIND ( (?endDateTime) - (?startDateTime) as ?playTime ) .\n"
			+ "   # 0Years 0Months 0Days... less than 40 minutes\n"
			+ "   FILTER (?playTime < \"P0Y0M0DT0H40M00.000S\"^^xsd:duration) .\n";
		
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
