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
                "id bigint(20) PRIMARY KEY, " +
                "inverse varchar(255) NOT NULL, " +
                "uri varchar(1200) NOT NULL, " +
                "name varchar(1000) NOT NULL" +
                ");";
    }
    public String insertClassQuery(int iID, String sURI, String sName, int iSize) {
        return "INSERT INTO classes (id, uri, name, size) VALUES (" + iID + ", '" + sURI + "', '" + sName + "', '" +iSize+ "')";
    }
    public String insertIndividualQuery(int iID, String sURI, String sName) {
        return "INSERT INTO individuals VALUES (" + iID + ", '" + sURI + "', '" + sName + "')";
    }
    public String insertPropertyTopQuery(int iID, int iInv, String sPropURI, String sPropName) {
        return "INSERT INTO classes_ex_property_top VALUES (" + iID + ", " + iInv + ", '" + sPropURI + "', " +
                "'" + sPropName + "')";
    }
    public String insertPropertyQuery(int iID, String sURI, String sName) {
        return "INSERT INTO properties VALUES (" + iID + ", " + (iID + 1) + ", " + (iID + 2) + ", '" + sURI + "', " +
                "'" + sName + "')";
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
}
