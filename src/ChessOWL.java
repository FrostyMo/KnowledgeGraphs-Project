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
import java.util.Random;

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
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
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
	
	static HashMap<String, String> nameSpaceHashMap;
	
	public static void main(String[] args) throws IOException, CsvException, ParseException {

		Main.CreateGamesList(Main.CSV_FILE_PATH);
		Main.CreatePlayersList(Main.CSV_FILE_PATH_1);
		Main.CreateContinentsList(Main.CSV_FILE_PATH_2);
		
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
		nameSpaceHashMap = new HashMap<>();
		// load classes and individuals
		// that are non repeating e.g Rules, Categories etc
		loadClasses();
		
		
		// get the prefixes
		HashMap<String, String> uriHashMap = (HashMap<String, String>) chessModel.getNsPrefixMap();
		


		
		// Create a class from game
		OntClass gameClass = chessModel.getOntClass(MAR.NS_GAME+"Game");
		OntClass gameCategory = chessModel.getOntClass(MAR.NS_GAMECATEGORY+"GameCategory");
		
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
//				System.out.println("public static final String " + myOntProperty.getLocalName().toString().toUpperCase() 
//						+ " = \"" + myOntProperty.getLocalName() + "\";" );
			}
			
		}
		
		
		OntClass personClass = chessModel.getOntClass(MAR.NS_PERSON+"Person");
		
//		getDataProperties(gameClass);
		createCountryIndividuals();
		
		createGameIndividuals(MAR.NS_GAME, gameClass);
//		createGameIndividuals(MAR.NS_PERSON, personClass);
		createPlayerIndividuals();
		
		
//		chessModel.write(System.out);
		writeToFile(fileName, chessModel);
//		printHashMap(uriHashMap);
//		System.out.println("SADSADSADSA");
//		printHashMap(nameSpaceHashMap);
		printHashMap(Main.CountryCodeContinentHashMap);
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
	// the class of Indivdual
	static ArrayList<Individual> createGameIndividuals(String NamespaceURI, OntClass classOfIndividual) throws ParseException {
	    
		ArrayList<Individual> individuals = new ArrayList<>();
		
	    
		getProperties(classOfIndividual);
	    
	    
	    // Create an individual using NS and individual name prefix
    	// Then just append current iteration to it
	    for (Integer i =0; i < Main.GamesArray.size(); i++) {
	   
	    	
	    	// ***** Example: createIndividual("mar" + "game" + "1", Game) *****
	    	// mar:game1, mar:game2, mar:game3...
	    	Individual individual = chessModel.createIndividual(MAR.NS_GAME + classOfIndividual.getLocalName().toLowerCase() + i.toString(), classOfIndividual);
	    	
	    	
	    	// give it a type NamedIndividual for protege
	    	individual.addRDFType(OWL2.NamedIndividual);
	    	
	    	individual.addLiteral(allDataPropertiesHashMap.get(MAR.GAMEID), 
					  Long.parseUnsignedLong(Main.GamesArray.get(i).game_id));

	    	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    	Date parsed = f.parse(Main.GamesArray.get(i).start_time);
	    	Calendar cal = Calendar.getInstance();
	    	cal.setTime(parsed);
	    	
	    	XSDDateTime date = new XSDDateTime(cal);
	    	individual.addLiteral(gameDataPropHashMap.get(MAR.STARTDATETIME), date);
	    	
	    	parsed = f.parse(Main.GamesArray.get(i).end_time);
	    	cal.clear();
	    	cal.setTime(parsed);
	    	date = new XSDDateTime(cal);
	    	individual.addLiteral(gameDataPropHashMap.get(MAR.ENDDATETIME), date);
	    	
	    	String ruleIndividualName =  Main.GamesArray.get(i).rules.toLowerCase();
//		    	System.out.println(ruleIndividualName);
	    	individual.addProperty(gameObjectPropHashMap.get(MAR.RULES_OF_CHESS),oneTimeIndividuals.get(ruleIndividualName));
	    	
	    	String categoryIndividualName =  Main.GamesArray.get(i).time_class.toLowerCase();
//		    	System.out.println(gameObjectPropHashMap.get(MAR.GAMECATEGORY));
	    	
	    	individual.addProperty(gameObjectPropHashMap.get(MAR.HASGAMECATEGORY),oneTimeIndividuals.get(categoryIndividualName));
	    	
	    	// create game moves individual
	    	Individual individualMoves = chessModel.createIndividual(NamespaceURI + MAR.GAMEMOVES.toLowerCase() + i.toString(), allClassesHashMap.get(MAR.GAMEMOVES));
	    	individualMoves.addRDFType(OWL2.NamedIndividual);
	    	individualMoves.addLiteral(allDataPropertiesHashMap.get(MAR.MOVESLISTBLACK), Main.GamesArray.get(i).BlackMoves);
	    	individualMoves.addLiteral(allDataPropertiesHashMap.get(MAR.MOVESLISTWHITE), Main.GamesArray.get(i).WhiteMoves);
	    	individualMoves.addLiteral(allDataPropertiesHashMap.get(MAR.NUMOFBLACKMOVES), Integer.parseInt(Main.GamesArray.get(i).BlackMovesNum));
	    	individualMoves.addLiteral(allDataPropertiesHashMap.get(MAR.NUMOFWHITEMOVES), Integer.parseInt(Main.GamesArray.get(i).WhiteMovesNum));
	    	individualMoves.addLiteral(allDataPropertiesHashMap.get(MAR.NUMOFMOVES), Integer.parseInt(Main.GamesArray.get(i).TMoves));
	    	individual.addProperty(gameObjectPropHashMap.get(MAR.HASMOVES),individualMoves);
	    	
	    	// create results
	    	Individual individualResultWhite = chessModel.createIndividual(NamespaceURI + MAR.RESULT.toLowerCase() + i.toString() + "W", allClassesHashMap.get(MAR.RESULT));
	    	Individual individualResultBlack = chessModel.createIndividual(NamespaceURI + MAR.RESULT.toLowerCase() + i.toString() + "B", allClassesHashMap.get(MAR.RESULT));
	    	individualResultWhite.addRDFType(OWL2.NamedIndividual);
	    	individualResultBlack.addRDFType(OWL2.NamedIndividual);
	    	
	    	individualResultWhite.addLiteral(allDataPropertiesHashMap.get(MAR.RESULTCOLOR),"white");
	    	individualResultWhite.addLiteral(allDataPropertiesHashMap.get(MAR.RESULTTYPE),Main.GamesArray.get(i).white_result);
	    	individualResultBlack.addLiteral(allDataPropertiesHashMap.get(MAR.RESULTCOLOR),"black");
	    	individualResultBlack.addLiteral(allDataPropertiesHashMap.get(MAR.RESULTTYPE),Main.GamesArray.get(i).black_result);
	    	individual.addProperty(gameObjectPropHashMap.get(MAR.HASRESULT),individualResultWhite);
	    	individual.addProperty(gameObjectPropHashMap.get(MAR.HASRESULT),individualResultBlack);

 		    
		    individuals.append(individual);
		}
	    
	    return individuals;
	}
	
	static void createPlayerIndividuals() {
		
		getProperties(allClassesHashMap.get(MAR.PERSON));
		
		for (Integer i =0; i < Main.PlayersArray.size(); i++) {
			Individual individual = chessModel.createIndividual(MAR.NS_PERSON + MAR.PERSON.toLowerCase() + i.toString(), allClassesHashMap.get(MAR.PERSON));
	    	
	    	// give it a type NamedIndividual for protege
	    	individual.addRDFType(OWL2.NamedIndividual);
	    	
	    	String birthName = Main.PlayersArray.get(i).firstName + Main.PlayersArray.get(i).lastName;
	    	String gender = Main.PlayersArray.get(i).Gender;
	    	String inactive_flag = Main.PlayersArray.get(i).Inactive_flag;
	    	String title = Main.PlayersArray.get(i).Title;
	    	
	    	individual.addLiteral(playerDataPropHashMap.get(MAR.FIDEID), Integer.parseInt(Main.PlayersArray.get(i).FideID));
	    	individual.addLiteral(playerDataPropHashMap.get(MAR.BIRTHDATE), Double.parseDouble(Main.PlayersArray.get(i).Year_of_birth));
	    	individual.addLiteral(playerDataPropHashMap.get(MAR.BIRTHNAME), birthName);
	    	individual.addLiteral(playerDataPropHashMap.get(MAR.GENDER), gender);
	    	individual.addLiteral(playerDataPropHashMap.get(MAR.INACTIVEFLAG), inactive_flag);
	    	individual.addLiteral(playerDataPropHashMap.get(MAR.DBOTITLE), title);
	    	
	    	// GENERATE A RANDOM NUMBER FROM
	    	// 0 TO 13
	    	individual.addLiteral(playerDataPropHashMap.get(MAR.PLAYEDAGAINSTN_GM), new Random().nextInt(13));
	    	
	    	
	    	System.out.print(title);
	    	
	    	

	    	individual.addProperty(playerObjectPropHashMap.get(MAR.DBPTITLE),oneTimeIndividuals.get(title.toLowerCase()));
	    	
	    	String country = Main.PlayersArray.get(i).Federation;

	    	individual.addProperty(allObjectPropertiesHashMap.get(MAR.DBOCOUNTRY), oneTimeIndividuals.get(country));
	    	
	    	Individual individualRatingBlitz = chessModel.createIndividual(MAR.NS_BLITZRATING + MAR.BLITZRATING.toLowerCase() + i.toString(), allClassesHashMap.get(MAR.BLITZRATING));
	    	individualRatingBlitz.addRDFType(OWL2.NamedIndividual);
	    	Double bRating = Double.parseDouble(Main.PlayersArray.get(i).Blitz_rating);
	    	individualRatingBlitz.addLiteral(allDataPropertiesHashMap.get(MAR.HASRATINGVALUE),bRating);
	    	
	    	Individual individualRatingRapid = chessModel.createIndividual(MAR.NS_RAPIDRATING + MAR.RAPIDRATING.toLowerCase() + i.toString(), allClassesHashMap.get(MAR.RAPIDRATING));
	    	individualRatingRapid.addRDFType(OWL2.NamedIndividual);
	    	Double rRating = Double.parseDouble(Main.PlayersArray.get(i).Rapid_rating);
	    	individualRatingRapid.addLiteral(allDataPropertiesHashMap.get(MAR.HASRATINGVALUE),rRating);
	    	
	    	Individual individualRatingStandard = chessModel.createIndividual(MAR.NS_STANDARDRATING + MAR.STANDARDRATING.toLowerCase() + i.toString(), allClassesHashMap.get(MAR.STANDARDRATING));
	    	individualRatingStandard.addRDFType(OWL2.NamedIndividual);
	    	Double sRating = Double.parseDouble(Main.PlayersArray.get(i).Standard_Rating);
	    	individualRatingStandard.addLiteral(allDataPropertiesHashMap.get(MAR.HASRATINGVALUE),sRating);
	    	
	    	individual.addProperty(allObjectPropertiesHashMap.get(MAR.HASPLAYERRATING), individualRatingBlitz);
	    	individual.addProperty(allObjectPropertiesHashMap.get(MAR.HASPLAYERRATING), individualRatingRapid);
	    	individual.addProperty(allObjectPropertiesHashMap.get(MAR.HASPLAYERRATING), individualRatingStandard);
		    int gameIndex=0;
    		for (Game game : Main.GamesArray) {
//    			System.out.println(game.black_usernameID + "-" + game.white_usernameID + "-" + Main.PlayersArray.get(i).FideID);
    			if (game.black_usernameID.equals(Main.PlayersArray.get(i).FideID)) {
    				Individual gameIndividual = chessModel.getIndividual(MAR.NS_GAME + MAR.GAME.toLowerCase() + gameIndex);
    				individual.addProperty(allObjectPropertiesHashMap.get(MAR.PLAYEDASBLACK), gameIndividual);
    				gameIndividual.addProperty(allObjectPropertiesHashMap.get(MAR.PLAYEDASBLACKBY), individual);
    			}
    			else if (game.white_usernameID.equals(Main.PlayersArray.get(i).FideID)) {
    				Individual gameIndividual = chessModel.getIndividual(MAR.NS_GAME + MAR.GAME.toLowerCase() + gameIndex);
    				individual.addProperty(allObjectPropertiesHashMap.get(MAR.PLAYEDASWHITE), gameIndividual);
    				gameIndividual.addProperty(allObjectPropertiesHashMap.get(MAR.PLAYEDASWHITEBY), individual);
    				
    			}
    			
    			gameIndex++;
    		}
	    	
	    	
		}
		
		
	}
	
	// CREATE ALL THE COUNTRIES USING CSV EXTRACTED ARRAY
	// ADD TO ONETIME INDIVIDUALS
	// CONVERT THOSE CONTINENT CLASSES TO ENUMERATED CLASSES
	static void createCountryIndividuals() {

		RDFList europeList = chessModel.createList();
		RDFList asiaList = chessModel.createList();
		RDFList oceaniaList = chessModel.createList();
		RDFList southAmericaList = chessModel.createList();
		RDFList northAmericaList = chessModel.createList();
		RDFList africaList = chessModel.createList();
		
		for (Continent continent  : Main.continentsArray) {
			String code = continent.Alpha3Code;
			Individual country = chessModel.createIndividual(MAR.NS_COUNTRY + code, allClassesHashMap.get(MAR.COUNTRY));
			Literal label = chessModel.createLiteral( continent.CountryName, "en" );
			country.addLabel(label);
			country.addProperty(allObjectPropertiesHashMap.get(MAR.DBOCONTINENT), oneTimeIndividuals.get(continent.Continent.toLowerCase()));
			oneTimeIndividuals.put(continent.Alpha3Code, country);
			
			// ADD TO CONTINENT LIST
			// ACCORDING TO DATASET
			switch(continent.Continent) {
				case MAR.AFRICA:{
					africaList = africaList.with(country);
					break;
				}
				case MAR.EUROPE:{
					europeList = europeList.with(country);
					break;
				}
				case MAR.ASIA:{
					asiaList = asiaList.with(country);
					break;
				}
				case MAR.SOUTH_AMERICA:{
					southAmericaList = southAmericaList.with(country);
					break;
				}
				case MAR.NORTH_AMERICA:{
					northAmericaList = northAmericaList.with(country);
					break;
				}
				case MAR.OCEANIA:{
					oceaniaList = oceaniaList.with(country);
					break;
				}
				
			
			}
		}
		// CONVERT THE CLASSES TO ENUMERATED
		// BY PROVIDING AN RDFLIST
		allClassesHashMap.get(MAR.AFRICA).convertToEnumeratedClass(africaList);
		allClassesHashMap.get(MAR.EUROPE).convertToEnumeratedClass(europeList);
		allClassesHashMap.get(MAR.ASIA).convertToEnumeratedClass(asiaList);
		allClassesHashMap.get(MAR.SOUTH_AMERICA).convertToEnumeratedClass(southAmericaList);
		allClassesHashMap.get(MAR.NORTH_AMERICA).convertToEnumeratedClass(northAmericaList);
		allClassesHashMap.get(MAR.OCEANIA).convertToEnumeratedClass(oceaniaList);
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
			case MAR.COUNTRY:
				return true;
//			case MAR.CONTINENT:
//				return true;
//			case MAR.ASIA:
//				return true;
//			case MAR.AFRICA:
//				return true;
//			case MAR.NORTH_AMERICA:
//				return true;
//			case MAR.SOUTH_AMERICA:
//				return true;
//			case MAR.OCEANIA:
//				return true;
//			case MAR.EUROPE:
//				return true;
				
		}
		return false;
	}
	
	static void loadClasses() {
		for (ExtendedIterator<OntClass> classesExtendedIterator = chessModel.listClasses(); classesExtendedIterator.hasNext();) {
			OntClass m_Class = classesExtendedIterator.next();
			
			if (m_Class.isURIResource()) {
//				System.out.println(m_Class );
				
				
				// SAVE ALL THE CLASSES NAMES AS STATIC STRINGS
				
				if (m_Class.getLocalName().equals("")) {
//					System.out.println("public static final String " + m_Class.getLabel("en").toUpperCase() + " = \"" + m_Class.getLabel("en").toUpperCase() + "\";" );
					allClassesHashMap.put(m_Class.getLabel("en"), m_Class);
					nameSpaceHashMap.put(m_Class.getLabel("en"), m_Class.getNameSpace());
					System.out.println("public static final String NS_" + m_Class.getLabel("en").toUpperCase() + " = \"" + m_Class.getNameSpace() + "\";" );
				}
				else {
//					System.out.println("public static final String " + m_Class.getLocalName().toString().toUpperCase() + " = \"" + m_Class.getLocalName() + "\";" );
					allClassesHashMap.put(m_Class.getLocalName(), m_Class);
					nameSpaceHashMap.put(m_Class.getLocalName(), m_Class.getNameSpace());
					System.out.println("public static final String NS_" + m_Class.getLocalName().toString().toUpperCase() + " = \"" + m_Class.getNameSpace() + "\";" );
				}
				// SAVE ALL THE NAMESPACES AS STATIC STRINGS
				
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
