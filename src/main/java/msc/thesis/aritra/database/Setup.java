package msc.thesis.aritra.database;

import java.io.IOException;
import java.sql.SQLException;

public class Setup {

    private SQLDatabase sqlDatabase;

    private SQLFactory m_sqlFactory;

    public Setup() throws SQLException, IOException {
        sqlDatabase = SQLDatabase.instance();
        m_sqlFactory = new SQLFactory();
    }

    /**
     * sets up the database schema that is required for the terminology acquisition.
     *
     * @return true if setup was successful, false otherwise.
     */
    public boolean setupSchema() {
        boolean result;
        String classesQuery = this.m_sqlFactory.createClassesTable();
        result = this.sqlDatabase.execute(classesQuery);
        String individualsQuery = this.m_sqlFactory.createIndividualsTable();
        result = this.sqlDatabase.execute(individualsQuery) && result;
        String propertiesQuery = this.m_sqlFactory.createPropertiesTable();
        result = this.sqlDatabase.execute(propertiesQuery) && result;
        String classesExPropertyTopQuery = this.m_sqlFactory.createClassesExPropertyTopTable();
        result = this.sqlDatabase.execute(classesExPropertyTopQuery) && result;
        String classesExPropertyQuery = this.m_sqlFactory.createClassesExPropertyTable();
        result = this.sqlDatabase.execute(classesExPropertyQuery) && result;
        return result;
    }


}
