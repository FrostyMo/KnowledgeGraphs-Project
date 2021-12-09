import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.jena.ext.xerces.xs.datatypes.XSDateTime;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.library.print;
import org.apache.jena.sparql.util.DateTimeStruct.DateTimeParseException;
import org.apache.jena.vocabulary.OWL2;

import com.github.andrewoma.dexx.collection.ArrayList;
import com.opencsv.exceptions.CsvException;

public class ChessOWL {

	static OntModel chessModel;
	static HashMap<String, Property> dataPropHashMap;
	public static void main(String[] args) throws IOException, CsvException, ParseException {
		
//		Main createTbox = new Main();
		Main.CreateGamesList(Main.CSV_FILE_PATH);
		Main.CreatePlayersList(Main.CSV_FILE_PATH_1);
		
		
		// No inference
		chessModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		chessModel.read("chess.owl", "RDF/XML");
		
		dataPropHashMap = new HashMap<>();
		
		// get the prefixes
		HashMap<String, String> prefixesHashMap = (HashMap<String, String>) chessModel.getNsPrefixMap();

		// our current namespace is mar
		String NS = prefixesHashMap.get("mar");

		// get mar:gameID property
		Property rdfTypeProperty = chessModel.getProperty(NS+MARDictionary.GAME_ID);

		// Create a class from game
		OntClass gameClass = chessModel.getOntClass(NS+"Game");


		String fileName = "chess2.rdf";
		
		
		
		getDataProperties(gameClass);
		createGameIndividuals(NS, "game", gameClass);
		
		chessModel.write(System.out);
		writeToFile(fileName, chessModel);
		
	}
	
	
	// write ontology to a file
	static void writeToFile(String fileName, OntModel model) throws IOException {
		FileWriter out = new FileWriter( fileName );
		try {
			model.write( out, "RDF/XML-ABBREV" );
		}
		finally {
		   try {
		       out.close();
		   }
		   catch (IOException closeException) {
		       // ignore
		   }
		}
	}
	
	// print the hashmap with string string key values
	static void printHashMap(HashMap<String, String> hashMap) {
		for (String name: hashMap.keySet()) {
		    String key = name.toString();
		    String value = hashMap.get(name).toString();
		    System.out.println(key + " " + value);
		}
	}
	
	// This function receives 
	// a URI e.g http://momin/path/to/uri/mar#
	// an individualNamePrefix e.g Game (1,2,3... will be appended to it)
	// a listOfIndividuals e.g GamesArray/PlayersArray
	// the class of Indivdual
	static ArrayList<Individual> createGameIndividuals(String NamespaceURI, String individualNamePrefix, OntClass classOfIndividual) throws ParseException {
	    
		ArrayList<Individual> individuals = new ArrayList<>();
		
	    
	    // Get all the data properties of classOfIndividual
	    ArrayList<OntProperty> dataProperties = getDataProperties(classOfIndividual);
	    
	    int size = 0;
	    if (classOfIndividual.getLocalName().equals("Game")) {
	    	size = Main.GamesArray.size();
	    }
	    else if (classOfIndividual.getLocalName().equals("Player")) {
	    	size = Main.PlayersArray.size();
	    }
	    
	    // Create an individual using NS and individual name prefix
    	// Then just append current iteration to it
	    for (Integer i =0; i < size; i++) {
	    	
	    	// ***** Example: createIndividual("mar" + "game" + "1", Game) *****
	    	// mar:game1, mar:game2, mar:game3...
	    	Individual individual = chessModel.createIndividual(NamespaceURI + individualNamePrefix + i.toString(), classOfIndividual);
	    	
	    	
	    	// give it a type NamedIndividual for protege
	    	individual.addRDFType(OWL2.NamedIndividual);
	    	
	    	
	    	// If the class is Game
		    // load the game properties
		    if (classOfIndividual.getLocalName().equals("Game")) {
		    	DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		    	
		    	
//		    	Calendar calendar = Calendar.getInstance();
//		    	calendar.setTime(dateTime);
//		    	
//		    	XSDateTime(calendar);
		    	individual.addLiteral(dataPropHashMap.get(MARDictionary.GAME_ID), 
		    						  Long.parseUnsignedLong(Main.GamesArray.get(i).game_id));
		    	
		    	LocalDateTime dateTime = LocalDateTime.from(f.parse(Main.GamesArray.get(i).start_time));
		    	individual.addLiteral(dataPropHashMap.get(MARDictionary.START_DATE_TIME), dateTime);
		    	
		    	dateTime = LocalDateTime.from(f.parse(Main.GamesArray.get(i).end_time));
		    	individual.addLiteral(dataPropHashMap.get(MARDictionary.END_DATE_TIME), dateTime);

		    }
		    
		    
		    // Else If the class is Player
		    // load the player properties
		    else if (classOfIndividual.getLocalName().equals("Player")) {
		    	
		    }
		    individuals.append(individual);
		}
	    
	    return individuals;
	}
	
	static ArrayList<OntProperty> getDataProperties(OntClass myClass){
		ArrayList<OntProperty> DataTypeProperties = new ArrayList<>();
		
		for (Iterator<OntProperty> iterator = myClass.listDeclaredProperties(false); iterator.hasNext();) {
			OntProperty myOntProperty = iterator.next();
			if (myOntProperty.isDatatypeProperty() && !myOntProperty.toString().contains("Property")) {
				System.out.println(myOntProperty.getLocalName() + " " + myOntProperty.getRange().getLocalName());
				DataTypeProperties.append(myOntProperty);
				dataPropHashMap.put(myOntProperty.getLocalName(), myOntProperty);
			}
			
		}
		
		return DataTypeProperties;
	}

	
}
