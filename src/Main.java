import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;


public class Main {
	public static ArrayList<Game> GamesArray;
	public static ArrayList<Player> PlayersArray;
	public static ArrayList<Continent> continentsArray;
	
	public static HashMap<String, Player> PlayersHashMap;
	public static HashMap<String, Game> GamesHashMap;
	public static HashMap<String, String> CountryCodeContinentHashMap;
	
	public static final String CSV_FILE_PATH = "./gamesFinal1.csv";
	public static final String CSV_FILE_PATH_1= "./womenChessPlayers.csv";
	public static final String CSV_FILE_PATH_2= "./CountryCodes.csv";
			

//	public static void main(String[] args) throws IOException, CsvException
//	{
//		System.out.println("Creating Games Object List\n");
//		CreateGamesList(CSV_FILE_PATH);
//		System.out.println("Games List Size = " + GamesArray.size());
//		System.out.println("__________________Created_______________________");
//		
//		System.out.println("Creating Players Object List\n");
//		CreatePlayersList(CSV_FILE_PATH_1);
//		System.out.println("Players List Size = " + PlayersArray.size());
//		System.out.println("__________________Created_______________________\n");
//		
//	}
	public static void CreatePlayersList (String file) throws IOException, CsvException{
		
		PlayersArray = new ArrayList<Player>();
		PlayersHashMap = new HashMap<>();
	
		// Create an object of filereader class
		// with CSV file as a parameter.	
		FileReader filereader = new FileReader(file);
		// create csvReader object
		// and skip first Line
		
		CSVReader csvReader = new CSVReaderBuilder(filereader)
								.withSkipLines(1)
								.build();
		List<String[]> allData = csvReader.readAll();
		
		// Iterating Over Rows and Adding Temp Object of Game to Array of Game
		// Where each row represents a Specific Game
		for (String[] row : allData) {
			Player tempObj = new Player(row);
			PlayersArray.add(tempObj);	
			PlayersHashMap.put(tempObj.FideID, tempObj);
//			tempObj.print();
		}
	}
	public static void CreateGamesList(String file) throws IOException, CsvException
	{
		GamesArray = new ArrayList<Game>();
		GamesHashMap = new HashMap<>();
		// Create an object of filereader class
		// with CSV file as a parameter.
		FileReader filereader = new FileReader(file);
		// create csvReader object
		// and skip first Line
		CSVReader csvReader = new CSVReaderBuilder(filereader)
								.withSkipLines(1)
								.build();
		List<String[]> allData = csvReader.readAll();
		// Iterating Over Rows and Adding Temp Object of Game to Array of Game
		// Where each row represents a Specific Game
		int count = 0;
		for (String[] row : allData) {
		
			Game tempObj = new Game(row);
			GamesArray.add(tempObj);
			GamesHashMap.put(tempObj.game_id, tempObj);
//			for(String col:row) {
//				System.out.print(col + "\t");
//			}
//			System.out.println(count++);
			
//			tempObj.print();

		}
	}
	
public static void CreateContinentsList (String file) throws IOException, CsvException{
		
		continentsArray = new ArrayList<>();
		CountryCodeContinentHashMap = new HashMap<>();
		// Create an object of filereader class
		// with CSV file as a parameter.
		
		FileReader filereader = new FileReader(file);
		// create csvReader object
		// and skip first Line
		
		CSVReader csvReader = new CSVReaderBuilder(filereader)
								.withSkipLines(1)
								.build();
		List<String[]> allData = csvReader.readAll();
		
		// Iterating Over Rows and Adding Temp Object of Game to Array of Game
		// Where each row represents a Specific Game
		for (String[] row : allData) {
			Continent tempObj = new Continent(row);
			continentsArray.add(tempObj);	
			CountryCodeContinentHashMap.put(tempObj.Alpha3Code, tempObj.Continent);
//			tempObj.print();
		}
	}
}
