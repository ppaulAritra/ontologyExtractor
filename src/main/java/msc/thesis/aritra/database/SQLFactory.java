package msc.thesis.aritra.database;

public class SQLFactory {
    public String createClassesTable() {
        return "CREATE TABLE classes (" +
                "id bigInt(20) primary key, " +
                "uri varchar(1200), " +
                "name varchar(1000), " +
                "size bigInt(20)" +
                ");";
    }


    public String createIndividualsTable() {
        return "CREATE TABLE individuals (" +
                "id bigint(20) PRIMARY KEY, " +
                "uri varchar(1200) NOT NULL, " +
                "name varchar(1000) NOT NULL" +
                ");";
    }

    public String createPropertiesTable() {
        return "CREATE TABLE properties (" +
                "id bigint(20) PRIMARY KEY, " +
                "disjointID bigint(20), " +
                "symmetryID bigint(20), " +
                "uri varchar(1200) NOT NULL, " +
                "name varchar(1000) NOT NULL" +
                ");";
    }
    public String createClassesExPropertyTopTable() {
        return "CREATE TABLE classes_ex_property_top (" +
                "id varchar(20) PRIMARY KEY, " +
                "inverse varchar(255) NOT NULL, " +
                "uri varchar(1200) NOT NULL, " +
                "name varchar(1000) NOT NULL" +
                ");";
    }

    public String createClassesExPropertyTable() {
        return "CREATE TABLE classes_ex_property (" +
                "id bigint(20) PRIMARY KEY, " +
                "prop_uri varchar(1200) NOT NULL, " +
                "class_uri varchar(1200) NOT NULL, " +
                "prop_name varchar(1000) NOT NULL, " +
                "class_name varchar(1000) NOT NULL" +
                ");";
    }
    public String insertClassQuery(int iID, String sURI, String sName, int iSize) {
        return "INSERT INTO classes (id, uri, name, size) VALUES (" + iID + ", '" + sURI + "', '" + sName + "', '" +iSize+ "')";
    }
    public String insertIndividualQuery(int iID, String sURI, String sName) {
        return "INSERT INTO individuals VALUES (" + iID + ", '" + sURI + "', '" + sName + "')";
    }
    public String insertPropertyTopQuery(String sID, int iInv, String sPropURI, String sPropName) {
        return "INSERT INTO classes_ex_property_top VALUES ('" + sID + "', " + iInv + ", '" + sPropURI + "', " +
                "'" + sPropName + "')";
    }
    public String insertPropertyQuery(int iID, String sURI, String sName) {
        return "INSERT INTO properties VALUES (" + iID + ", " + (iID + 1) + ", " + (iID + 2) + ", '" + sURI + "', " +
                "'" + sName + "')";
    }
    public String insertClassExistsPropertyQuery(int iID, String sPropURI, String sClassURI, String sPropName,
                                                 String sClassName) {
        return "INSERT INTO classes_ex_property VALUES (" + iID + ", '" + sPropURI + "', '" + sClassURI + "', " +
                "'" + sPropName + "', '" + sClassName + "')";
    }
    public String selectClassesQuery() {
        return "SELECT * FROM classes";
    }


    public String selectIndividualsQuery() {
        return "SELECT * FROM individuals";
    }
    public String selectPropertiesQuery() {
        return "SELECT * FROM properties";
    }
    public String selectPropertyRestrictionsQuery(int iInverse) {
        return "SELECT * FROM classes_ex_property_top WHERE inverse=" + iInverse;
    }
    public String selectClassNameQuery() {
        return "SELECT id, name FROM classes";
    }
    public String selectClassUriQuery() {
        return "SELECT id, uri FROM classes";
    }
    public String selectPropertyNameQuery() {
        return "SELECT id, name FROM classes_ex_property_top";
    }
    public String selectIndividualsQuery(int iStart, int iEnd) {
        return "SELECT * FROM individuals WHERE id >= " + iStart + " AND id < " + iEnd;
    }
    public String selectExistsPropertyIDQuery(String sPropURI, String sClassURI) {
        return "SELECT * FROM classes_ex_property WHERE prop_uri='" + sPropURI + "' AND class_uri='" + sClassURI + "'";
    }
    public String selectExistsPropertyIDQuery() {
        return "SELECT * FROM classes_ex_property WHERE prop_uri = ? AND class_uri = ?";
    }
    public String selectExistsPropertyById(String iExistPropId) {
        return "SELECT * FROM classes_ex_property_top WHERE id in ("+iExistPropId+ ")";
    }

    public String selectClassesById(String iExistClassId) {
        return "SELECT * FROM classes WHERE id in ("+iExistClassId+ ")";
    }
    public String selectPropertyClassName () {
        return "SELECT id, prop_name, class_name FROM classes_ex_property";
    }

}
