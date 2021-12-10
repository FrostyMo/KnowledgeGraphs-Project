import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType;
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
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.logging.log4j.core.config.status.StatusConfiguration;

import com.github.andrewoma.dexx.collection.ArrayList;
import com.opencsv.exceptions.CsvException;

import jdk.dynalink.beans.StaticClass;



public class ChessOWL {

	static OntModel chessModel;
	static HashMap<String, Property> gameDataPropHashMap;
	static HashMap<String, Property> gameObjectPropHashMap;
	
	static HashMap<String, Property> playerDataPropHashMap;
	static HashMap<String, Property> playerObjectPropHashMap;
	
	static HashMap<String, OntClass> allClassesHashMap;
	static HashMap<String, Property> allDataPropertiesHashMap;
	static HashMap<String, Property> allObjectPropertiesHashMap;
	
	static HashMap<String, Individual> oneTimeIndividuals;
	
	public static void main(String[] args) throws IOException, CsvException, ParseException {

		Main.CreateGamesList(Main.CSV_FILE_PATH);
		Main.CreatePlayersList(Main.CSV_FILE_PATH_1);
		
		
		// No inference
		chessModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		chessModel.read("chess_latest.owl", "RDF/XML");
//		ExtendedIterator<OntClass> classesExtendedIterator = chessModel.listClasses();
		
		
		
		gameDataPropHashMap = new HashMap<>();
		playerDataPropHashMap = new HashMap<>();
		allClassesHashMap = new HashMap<>();
		oneTimeIndividuals = new HashMap<>();
		gameObjectPropHashMap  = new HashMap<>();
		playerObjectPropHashMap  = new HashMap<>();
		allDataPropertiesHashMap = new HashMap<>();
		allObjectPropertiesHashMap = new HashMap<>();
		// load classes and individuals
		// that are non repeating e.g Rules, Categories etc
		loadClasses();
		
		
		// get the prefixes
		HashMap<String, String> prefixesHashMap = (HashMap<String, String>) chessModel.getNsPrefixMap();

		// our current namespace is mar
		String NS = prefixesHashMap.get("mar");

		// get mar:gameID property
		Property rdfTypeProperty = chessModel.getProperty(NS+MAR.GAME_ID);

		// Create a class from game
		OntClass gameClass = chessModel.getOntClass(NS+"Game");
		OntClass gameCategory = chessModel.getOntClass(NS+"GameCategory");
		
		for (ExtendedIterator<OntClass> classesExtendedIterator = gameCategory.listSubClasses(); classesExtendedIterator.hasNext();) {
			System.out.println(classesExtendedIterator.next().getLocalName());
		}

		String fileName = "chess2.rdf";
		
		for (Iterator<OntProperty> iterator = chessModel.listAllOntProperties(); iterator.hasNext();) {
			OntProperty myOntProperty = iterator.next();
			if (!myOntProperty.toString().contains("Property")) {
				if (myOntProperty.isDatatypeProperty()) {
					allDataPropertiesHashMap.put(myOntProperty.getLocalName(), myOntProperty);
				}
				else if(myOntProperty.isObjectProperty()){
					allObjectPropertiesHashMap.put(myOntProperty.getLocalName(), myOntProperty);
				}
				System.out.println("public static final String " + myOntProperty.getLocalName().toString().toUpperCase() + " = \"" + myOntProperty.getLocalName() + "\";" );
			}
			
		}
		
		
//		getDataProperties(gameClass);
		createGameIndividuals(NS, gameClass);
		
//		chessModel.write(System.out);
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
	static ArrayList<Individual> createGameIndividuals(String NamespaceURI, OntClass classOfIndividual) throws ParseException {
	    
		ArrayList<Individual> individuals = new ArrayList<>();
		
	    
	    // Get all the data properties of classOfIndividual
	    ArrayList<OntProperty> dataProperties = getProperties(classOfIndividual);
	    
	    int size = 0;
	    if (classOfIndividual.getLocalName().equals("Game")) {
	    	size = Main.GamesArray.size();
	    }
	    else if (classOfIndividual.getLocalName().equals("Person")) {
	    	size = Main.PlayersArray.size();
	    }
	    
	    // Create an individual using NS and individual name prefix
    	// Then just append current iteration to it
	    for (Integer i =0; i < size; i++) {
	    	
	    	// ***** Example: createIndividual("mar" + "game" + "1", Game) *****
	    	// mar:game1, mar:game2, mar:game3...
	    	Individual individual = chessModel.createIndividual(NamespaceURI + classOfIndividual.getLocalName().toLowerCase() + i.toString(), classOfIndividual);
	    	
	    	
	    	// give it a type NamedIndividual for protege
	    	individual.addRDFType(OWL2.NamedIndividual);
	    	
	    	
	    	// If the class is Game
		    // load the game properties
		    if (classOfIndividual.getLocalName().equals("Game")) {
		    	
		    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		    	individual.addLiteral(gameDataPropHashMap.get(MAR.GAME_ID), 
		    						  Long.parseUnsignedLong(Main.GamesArray.get(i).game_id));
		    	
		    	Date parsed = f.parse(Main.GamesArray.get(i).start_time);
		    	Calendar cal = Calendar.getInstance();
		    	cal.setTime(parsed);
		    	
		    	XSDDateTime date = new XSDDateTime(cal);
		    	individual.addLiteral(gameDataPropHashMap.get(MAR.START_DATE_TIME), date);
		    	
		    	parsed = f.parse(Main.GamesArray.get(i).end_time);
		    	cal.clear();
		    	cal.setTime(parsed);
		    	date = new XSDDateTime(cal);
		    	individual.addLiteral(gameDataPropHashMap.get(MAR.END_DATE_TIME), date);
		    	
		    	String ruleIndividualName =  Main.GamesArray.get(i).rules.toLowerCase();
//		    	System.out.println(ruleIndividualName);
		    	individual.addProperty(gameObjectPropHashMap.get(MAR.RULES_OF_CHESS),oneTimeIndividuals.get(ruleIndividualName));
		    	
		    	String categoryIndividualName =  Main.GamesArray.get(i).time_class.toLowerCase();
//		    	System.out.println(gameObjectPropHashMap.get(MAR.GAMECATEGORY));
		    	
		    	individual.addProperty(gameObjectPropHashMap.get(MAR.HAS_GAME_CATEGORY),oneTimeIndividuals.get(categoryIndividualName));
		    	
		    	// create game moves individual
		    	Individual individualMoves = chessModel.createIndividual(NamespaceURI + MAR.GAMEMOVES.toLowerCase() + i.toString(), allClassesHashMap.get(MAR.GAMEMOVES));
		    	individualMoves.addRDFType(OWL2.NamedIndividual);
		    	individualMoves.addLiteral(allDataPropertiesHashMap.get(MAR.MOVESLISTBLACK), Main.GamesArray.get(i).BlackMoves);
		    	individualMoves.addLiteral(allDataPropertiesHashMap.get(MAR.MOVESLISTWHITE), Main.GamesArray.get(i).WhiteMoves);
		    	individualMoves.addLiteral(allDataPropertiesHashMap.get(MAR.NUMOFBLACKMOVES), Integer.parseInt(Main.GamesArray.get(i).BlackMovesNum));
		    	individualMoves.addLiteral(allDataPropertiesHashMap.get(MAR.NUMOFWHITEMOVES), Integer.parseInt(Main.GamesArray.get(i).WhiteMovesNum));
		    	individualMoves.addLiteral(allDataPropertiesHashMap.get(MAR.NUMOFMOVES), Integer.parseInt(Main.GamesArray.get(i).TMoves));
		    	individual.addProperty(gameObjectPropHashMap.get(MAR.HAS_MOVES),individualMoves);
		    	
		    	// create results
		    	Individual individualResultWhite = chessModel.createIndividual(NamespaceURI + MAR.RESULT.toLowerCase() + i.toString() + "W", allClassesHashMap.get(MAR.RESULT));
		    	Individual individualResultBlack = chessModel.createIndividual(NamespaceURI + MAR.RESULT.toLowerCase() + i.toString() + "B", allClassesHashMap.get(MAR.RESULT));
		    	individualResultWhite.addRDFType(OWL2.NamedIndividual);
		    	individualResultBlack.addRDFType(OWL2.NamedIndividual);
		    	
		    	individualResultWhite.addLiteral(allDataPropertiesHashMap.get(MAR.RESULTCOLOR),"white");
		    	individualResultWhite.addLiteral(allDataPropertiesHashMap.get(MAR.RESULTTYPE),Main.GamesArray.get(i).white_result);
		    	individualResultBlack.addLiteral(allDataPropertiesHashMap.get(MAR.RESULTCOLOR),"black");
		    	individualResultBlack.addLiteral(allDataPropertiesHashMap.get(MAR.RESULTTYPE),Main.GamesArray.get(i).white_result);
		    	individual.addProperty(gameObjectPropHashMap.get(MAR.HASRESULT),individualResultWhite);
		    	individual.addProperty(gameObjectPropHashMap.get(MAR.HASRESULT),individualResultBlack);
//		    	chessModel.getIndividual(allClassesHashMap.get(ruleClassName));
//		    	allClassesHashMap.get(ruleClassName).getNameSpace() + ruleClassName.toString().toLowerCase()
 		    }
		    
		    
		    // Else If the class is Player
		    // load the player properties
		    else if (classOfIndividual.getLocalName().equals("Person")) {
		    	
		    }
		    individuals.append(individual);
		}
	    
	    return individuals;
	}
	
	static ArrayList<OntProperty> getProperties(OntClass myClass){
		ArrayList<OntProperty> DataTypeProperties = new ArrayList<>();
		
		for (Iterator<OntProperty> iterator = myClass.listDeclaredProperties(false); iterator.hasNext();) {
			OntProperty myOntProperty = iterator.next();
			if (!myOntProperty.toString().contains("Property")) {
				if (myOntProperty.isDatatypeProperty()) {
					System.out.println("Data Property: " + myOntProperty.getLocalName() + " " + myOntProperty.getRange().getLocalName());
//					DataTypeProperties.append(myOntProperty);
					if (myClass.getLocalName().equals("Game")) {
						gameDataPropHashMap.put(myOntProperty.getLocalName(), myOntProperty);
					}
					else if (myClass.getLocalName().equals("Person")) {
						playerDataPropHashMap.put(myOntProperty.getLocalName(), myOntProperty);
					}
				}
				else if (myOntProperty.isObjectProperty()){
					System.out.println("Object Property: " + myOntProperty.getLocalName() + " " + myOntProperty.getRange().getLocalName());
//					DataTypeProperties.append(myOntProperty);
					if (myClass.getLocalName().equals("Game")) {
						gameObjectPropHashMap.put(myOntProperty.getLocalName(), myOntProperty);
					}
					else if (myClass.getLocalName().equals("Person")) {
						playerObjectPropHashMap.put(myOntProperty.getLocalName(), myOntProperty);
					}
				}	
			}
		}
		return DataTypeProperties;
	}
	
	static boolean containsRepeatingClasses(String className) {
		switch (className){
			case MAR.GAME:
				return true;
			case MAR.GAMEMOVES:
				return true;
			case MAR.PLAYER:
				return true;
			case MAR.RESULT:
				return true;
			case MAR.PERSON:
				return true;
			case MAR.BLITZGAME:
				return true;
			case MAR.BULLETGAME:
				return true;
			case MAR.RAPIDGAME:
				return true;
			case MAR.DAILYGAME:
				return true;
			case MAR.LAZYGAME:
				return true;
			case MAR.HIGHLYACTIVEPLAYER:
				return true;
			case MAR.RATING:
				return true;
			case MAR.BLITZRATING:
				return true;
			case MAR.RAPIDRATING:
				return true;
			case MAR.STANDARDRATING:
				return true;
				
		}
		return false;
	}
	
	static void loadClasses() {
		for (ExtendedIterator<OntClass> classesExtendedIterator = chessModel.listClasses(); classesExtendedIterator.hasNext();) {
			OntClass m_Class = classesExtendedIterator.next();
			
			if (m_Class.isURIResource()) {
//				System.out.println(m_Class );
				allClassesHashMap.put(m_Class.getLocalName(), m_Class);
			}
			
		}
		for (Object objectName : allClassesHashMap.keySet()) {
			
			if (!containsRepeatingClasses(objectName.toString())) {
				System.out.print(objectName );
				System.out.print(" -> ");
				System.out.println(allClassesHashMap.get(objectName));
				Individual individual = chessModel.createIndividual(allClassesHashMap.get(objectName).getNameSpace() + objectName.toString().toLowerCase(), allClassesHashMap.get(objectName));
				individual.addRDFType(OWL2.NamedIndividual);
				System.out.println("1 :" + individual.getLocalName());
				oneTimeIndividuals.put(individual.getLocalName(), individual);
			}
			
//			System.out.println("public static final String " + objectName.toString().toUpperCase() + " = \"" + objectName + "\";" );
		}
	}
	
	
}
