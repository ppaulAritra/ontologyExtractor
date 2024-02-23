package msc.thesis.aritra.database;

import msc.thesis.aritra.util.Parameter;
import msc.thesis.aritra.util.Settings;

import java.sql.*;

public class SQLDatabase {
    private Connection connection;

    private static SQLDatabase instance;


    public static SQLDatabase instance() throws SQLException {
        if( instance == null ){
            instance = new SQLDatabase();
        }
        return instance;
    }

    private SQLDatabase() throws SQLException {
        try {
            String database = Settings.getString(Parameter.DATABASE);
            String user = Settings.getString( Parameter.USER );
            String password = Settings.getString( Parameter.PASSWORD );
            DriverManager.registerDriver( new com.mysql.jdbc.Driver() );
            System.out.println( "connection: "+ user +"@"+ database );
            connection = DriverManager.getConnection( database, user, password );
        }
        catch ( SQLException ex )
        {
            System.out.println( "SQLException: " + ex.getMessage() );
            System.out.println( "SQLState: " + ex.getSQLState() );
            System.out.println( "VendorError: " + ex.getErrorCode() );
            throw ex;
        }
    }


    public void close() throws SQLException  {
        connection.close();
    }

    public void assureConnected() throws SQLException {
        try {
            Statement stmt = connection.createStatement();
            ResultSet results = stmt.executeQuery("select 1");
            results.close();
            stmt.close();
        } catch (SQLException e) {
            String state = e.getSQLState();
            if (state.equals("08S01") || state.equals("08003")) {
                // try to reconnect
                String database = Settings.getString(Parameter.DATABASE);
                String user = Settings.getString( Parameter.USER );
                String password = Settings.getString( Parameter.PASSWORD );
                connection = DriverManager.getConnection( database, user, password );
            }
        }
    }

    public ResultSet query( String query ){
        System.out.println("SQLDatabase.query: " + query);
        Statement stmt = null;
        ResultSet results = null;
        try {
            assureConnected();
            stmt = connection.createStatement();
           // connection.prepareStatement(query);
            stmt.setFetchSize(5000);
            results = stmt.executeQuery( query );
            return results;
        }
        catch( SQLException ex ){
            System.out.println( "SQLException: " + ex.getMessage() );
            System.out.println( "SQLState: " + ex.getSQLState() );
            System.out.println( "VendorError: " + ex.getErrorCode() );
        }
        return null;
    }

    public boolean execute( String update ){
        // System.out.println( "Database.execute: "+ update );
        Statement stmt = null;
        ResultSet results = null;
        try {
            assureConnected();
            stmt = connection.createStatement();
            stmt.executeUpdate(update);
        }
        catch( SQLException ex ){
            System.out.println( "SQLException: " + ex.getMessage() );
            System.out.println( "SQLState: " + ex.getSQLState() );
            System.out.println( "VendorError: " + ex.getErrorCode() );
            return false;
        }
        finally {
            if ( results != null ) {
                try {
                    results.close();
                }
                catch ( SQLException sqlEx ) {
                    sqlEx.printStackTrace();
                }
                results = null;
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                }
                catch ( SQLException sqlEx ) {
                    sqlEx.printStackTrace();
                }
                stmt = null;
            }
        }
        return true;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setAutoCommit(boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        }
        catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void commit() {
        try {
            connection.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
