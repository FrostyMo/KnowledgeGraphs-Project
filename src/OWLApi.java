import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.ProfileRegistry;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;

public class OWLApi {

	public static void main(String[] args) {
		
		// No inference
		OntModel mominsgalaxyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		mominsgalaxyModel.read("mominsgalaxy.owl", "RDF/XML");
		
//		mominsgalaxyModel.write(System.out);
		
		HashMap<String, String> prefixesHashMap = (HashMap<String, String>) mominsgalaxyModel.getNsPrefixMap();
		
		System.out.println(prefixesHashMap.get("mominsgalaxy"));
		String NS = prefixesHashMap.get("mominsgalaxy");
		
		// Create a dummy satellite
		OntClass artificialSatelliteClass = mominsgalaxyModel.getOntClass(NS+"ArtificialSatellite");
		Individual a1 = mominsgalaxyModel.createIndividual(NS+"myASatellite", artificialSatelliteClass);
		
		for (Iterator<Resource> iterator = a1.listRDFTypes(false); iterator.hasNext();) {
			System.out.println(a1.getURI() + " is asserted in class " + iterator.next());
		}
		
		// Inferencing Model
		OntModel mominsgalaxyInfModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, mominsgalaxyModel);
		
		a1 = mominsgalaxyInfModel.getIndividual(NS+"myASatellite");
		for (Iterator<Resource> iterator = a1.listRDFTypes(false); iterator.hasNext();) {
			System.out.println(a1.getURI() + " is inferred to be a(n) " + iterator.next());
		}

	}

}
