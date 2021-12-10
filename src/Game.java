import java.util.ArrayList;

import org.apache.jena.riot.thrift.wire.RDF_ANY._Fields;

public class Game {
	public String game_id = "";
	public String start_time = "";
	public String start_Date = "";
	public String end_Date = "";
	public String end_time = "";
	public String time_class = "";
	public String rules = "";
	public String white_usernameID = "";
	public String white_result = "";
	public String black_usernameID = "";
	public String black_result = "";
	public ArrayList<String> moves;
	public String TMoves = "";
	public String BlackMoves = "";
	public String WhiteMoves = "";
	public String BlackMovesNum = "";
	public String WhiteMovesNum = "";
	public Game() {}
	
	public Game(String[] row) {
		game_id = row[1];
		String[] startDateTimeArray = getDateTime(row[2]);
//		start_Date = startDateTimeArray[0];
//		start_time = startDateTimeArray[1];
//		String[] endDateTimeArray = getDateTime(row[3]);
//		end_Date = endDateTimeArray[0];
//		end_time = endDateTimeArray[1];
		start_time = row[2];
		end_time = row[3];
		time_class = row[4];
		rules = row[5];
		white_usernameID = row[6];
		white_result = row[7];
		black_usernameID = row[8];
		black_result = row[9];
		moves = getMoves(row[10]);
		TMoves = row[11];
		BlackMoves = row[12];
		WhiteMoves = row[13];
		BlackMovesNum = row[14];
		WhiteMovesNum = row[15];
//		System.out.print("Moves length = ");
//		System.out.println(moves.size());
//		System.out.print("T Moves length = ");
//		System.out.println(TMoves);	
	}
	
	public void print() {
		System.out.println("Game ID = "+ game_id);
		System.out.println("Start Date = "+ start_Date);
		System.out.println("Start Time = "+ start_time);
		System.out.println("End Date = "+ end_Date);
		System.out.println("End Time = "+ end_time);
		System.out.println("Time Class = "+ time_class);
		System.out.println("Rules = "+ rules);
		System.out.println("Black User ID = "+ black_usernameID);
		System.out.println("Black User Result = "+ black_result);
		System.out.println("White User ID = "+ white_usernameID);
		System.out.println("White User Result = "+ white_result);
		System.out.println("Total Number of Moves = "+ TMoves);
		Integer itr = 0;
		System.out.print("[");
		while(itr<moves.size()) {
			Integer nmove = itr+1;
//			System.out.print("Move#"+nmove+" = "+moves.get(itr) );
			System.out.print(moves.get(itr) + ", " );
			itr++;
		}
		System.out.println("]");
		
	}
	
	private String[] getDateTime(String date) {
		String[] DateTimeArray = date.split(" ", 5);
		return DateTimeArray;
	}
	
	private ArrayList<String> getMoves(String stringofMoves){
		ArrayList<String> to_ret = new ArrayList<String>();
		stringofMoves = removeFirstandLast(stringofMoves);
		String[] splittedMoves = stringofMoves.split(", ", 200);
		for(String move:splittedMoves ) {
			move = removeFirstandLast(move);
			to_ret.add(move);
		}
		return to_ret;	
	}
	
    private String removeFirstandLast(String str)
    {
        // Removing first and last character
        // of a string using substring() method
        str = str.substring(1, str.length() - 1);
        // Return the modified string
        return str;
    }

}
