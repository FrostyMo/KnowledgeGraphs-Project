
public class Continent {
	String Continent;
	String CountryName;
	String Alpha3Code;
	
	public Continent() {}
	public Continent(String row[]) {
		Continent = row[0];
		CountryName = row[1];
		Alpha3Code = row[2];
	}
	
	public void displayContinents() {
		System.out.println("Continent = "+ Continent);
		System.out.println("CountryName = "+ CountryName);
		System.out.println("Alpha3Code = "+ Alpha3Code);
	}
}
