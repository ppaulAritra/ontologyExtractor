package msc.thesis.aritra.sparql;

import msc.thesis.aritra.OntologyExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPARQLFactory {
	//(;) allows you to append a predicate-object pair to a triple (reusing the subject), a
	//(,) allows you to append another object to a triple (reusing both subject and predicate).
	//Each triple about a subject is terminated by a period. Multiple predicates about the same subject are separated by semicolons,
	//and multiple objects for the same subject and predicate can be listed separated by commas.
	public static Logger log = LoggerFactory.getLogger(SPARQLFactory.class);
	private String m_sFilter = null;
	
	private final String PREFIX = 
	"PREFIX owl: <http://www.w3.org/2002/07/owl#> "+
	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "+
	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
	"PREFIX dbpedia2: <http://dbpedia.org/property/> "+
	"PREFIX dbpedia1: <http://dbpedia.org/resource/> "+
	"PREFIX dbowl: <http://dbpedia.org/ontology/> "+
	"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "+
	 "PREFIX foaf: <http://xmlns.com/foaf/0.1/> ";
	// "PREFIX dc: <http://purl.org/dc/elements/1.1/> "+
	// "PREFIX yago: <http://dbpedia.org/class/yago/> "+
	// "PREFIX cyc: <http://sw.opencyc.org/2008/06/10/concept/en/> "+
	// "PREFIX fbase: <http://rdf.freebase.com/ns/> ";
	

	// get classes
	public String classesQuery(){
		StringBuilder sb = new StringBuilder();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x" );
		sb.append( " WHERE {" );
		sb.append( " ?y a ?x ." );
		sb.append( " }" );
		return sb.toString();
	}


	// count individuals of this class
	public String classExtensionSizeQuery( String sClass ){
		StringBuilder sb = new StringBuilder();
		sb.append( PREFIX +" " );
		sb.append( "SELECT (count(distinct ?x) AS ?count) WHERE {" );
		sb.append( " ?x a <"+ sClass +"> ." );
		sb.append( " }" );
		return sb.toString();
	}

	
	// get individuals in this class
	public String classExtensionQuery( String sClass ){
		StringBuilder sb = new StringBuilder();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x WHERE {" );
		sb.append( " ?x a <"+ sClass +"> ." );
		sb.append( " }" );
		System.out.println();
		return sb.toString();
	}

	
	// get (atomic) classes for this individual
	public String individualClassesQuery( String sInd ){
		StringBuilder sb = new StringBuilder();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x WHERE {" );
		sb.append( " <"+ sInd +"> a ?x ." );
		sb.append( " }" );
		//log.info("Query to get (atomic) classes for this individual "+sb.toString());
		return sb.toString();
	}
	// get individual pairs in this property
	public String propertyExtensionQuery( String sProp ){
		StringBuilder sb = new StringBuilder();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x ?y WHERE {" );
		sb.append( " ?x <"+ sProp +"> ?y ." );
		// sb.append( " ?y a ?yt ." );
		sb.append( " }" );
		return sb.toString();
	}
	/*
	* get properties
	* ?z a ?zt here a= rdf:type; z is rdf:type of zt
	* It lists all properties used at least once in a triple where the object is also involved in a rdf:type triple as a subject.
	* */

	public String propertiesQuery(){
		StringBuilder sb = new StringBuilder();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x" );
		sb.append( " WHERE {" );
		sb.append( " ?y ?x ?z ." );
		//sb.append( " ?z a ?zt ." );
		/* if( m_sFilter != null ){
		 sb.append( " FILTER regex( ?x, '^"+ m_sFilter +"' ) ." );
		 } */
		sb.append( " }" );
		return sb.toString();
	}

	// get (complex) classes for this individual: exists x.y
	public String individualExistsPropertyQuery( String sInd ){
		StringBuilder sb = new StringBuilder();
		sb.append( PREFIX +" " );
		sb.append( "SELECT distinct ?x ?y WHERE {" );
		sb.append( " <"+ sInd +"> ?x ?iy ." );
		sb.append( " ?iy a ?y ." );
		sb.append( " }" );
		return sb.toString();
	}
	public String checkIfSubClass( String sUriCons, String sUriAnte  ){
		StringBuilder sb = new StringBuilder();
		sb.append( PREFIX +" " );
		sb.append( "ASK {" );
		sb.append( " <"+ sUriAnte +"> rdfs:subClassOf <" + sUriCons+">" );
		sb.append( " }" );
		return sb.toString();
	}
}