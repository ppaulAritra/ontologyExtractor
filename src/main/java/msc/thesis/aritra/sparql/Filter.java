package msc.thesis.aritra.sparql;

import msc.thesis.aritra.util.Parameter;
import msc.thesis.aritra.util.Settings;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Contains the different types of filters.
 *
 *
 */
public class Filter {
	
	/** filter that is used for the creation of class tables. */
	private final String classesFilter;
	
	/** filter that is used for the creation of the individuals tables. */
	private final String individualsFilter;
	
	/**
	 * the parameterless constructor takes the values for the filters
	 * from the properties file.
	 * 
	 * @throws FileNotFoundException if the prop. file wasn't found.
	 * @throws IOException if an problem occurs while reading the prop. file.
	 */
	public Filter() throws FileNotFoundException, IOException {
		if(!Settings.loaded()) {
			Settings.load();
		}
		classesFilter = Settings.getString(Parameter.CLASSES_FILTER);
		individualsFilter = Settings.getString(Parameter.INDIVIDUALS_FILTER);
	}

	
	/**
	 * getter for the classes filter.
	 * 
	 * @return classes filter string.
	 */
	public String getClassesFilter() {
		return this.classesFilter;
	}
	
	/**
	 * getter for the individuals filter.
	 * 
	 * @return individuals filter string.
	 */
	public String getIndividualsFilter() {
		return this.individualsFilter;
	}

}
