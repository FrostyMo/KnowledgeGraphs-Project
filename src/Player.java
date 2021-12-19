
public class Player {
	
	public String FideID;
	public String Federation;
	public String Gender;
	public String Year_of_birth;
	public String Title;
	public String Standard_Rating;
	public String Rapid_rating;
	public String Blitz_rating;
	public String Inactive_flag;
	public String firstName;
	public String lastName;
	
	public Player() {
		
	}
	public Player(String[] row) {
		FideID = row[0];
		Federation = row[1];
		Gender = row[2];
		Year_of_birth = row[3];
		Title = row[4];
		Standard_Rating = row[5];
		Rapid_rating = row[6];
		Blitz_rating = row[7];
		Inactive_flag = row[8];
		firstName = row[9];
		lastName = row[10];
	}
	public void print() {
		System.out.println("Fide ID of Player = "+FideID);
		System.out.println("Federation of Player = "+Federation);
		System.out.println("Gender of Player = "+Gender);
		System.out.println("Year Of Birth of Player = "+Year_of_birth);
		System.out.println("Title of Player = "+Title);
		System.out.println("Standard Rating of Player = "+Standard_Rating );
		System.out.println("Rapid Rating of Player = "+Rapid_rating );
		System.out.println("Blitz Rating of Player = "+Blitz_rating );
		System.out.println("Inactive Flag of Player = "+Inactive_flag );
		System.out.println("FName of Player = "+firstName );
		System.out.println("LName of Player = "+lastName );
	}

}
