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


    public String insertClassQuery(int iID, String sURI, String sName, int iSize) {
        return "INSERT INTO classes (id, uri, name, size) VALUES (" + iID + ", '" + sURI + "', '" + sName + "', '" +iSize+ "')";
    }
    public String insertIndividualQuery(int iID, String sURI, String sName) {
        return "INSERT INTO individuals VALUES (" + iID + ", '" + sURI + "', '" + sName + "')";
    }

    public String selectClassesQuery() {
        return "SELECT * FROM classes";
    }


    public String selectIndividualsQuery() {
        return "SELECT * FROM individuals";
    }


}
