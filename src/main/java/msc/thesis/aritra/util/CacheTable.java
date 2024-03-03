package msc.thesis.aritra.util;

import msc.thesis.aritra.database.SQLDatabase;
import msc.thesis.aritra.database.SQLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CacheTable {
    private final static Logger log = LoggerFactory.getLogger(CacheTable.class);
    private SQLDatabase sqlDatabase;
    private SQLFactory m_sqlFactory;
    private HashMap<String, String> classNameCache;
    private HashMap<String, String> classUriCache;
    private HashMap<String, String> propertyNameCache;

    public CacheTable(SQLDatabase d){
        m_sqlFactory = new SQLFactory();
        sqlDatabase = d;
        classNameCache = new HashMap<String, String>();
        propertyNameCache = new HashMap<String, String>();
        classUriCache = new HashMap<String, String>();
    }
    public HashMap<String, String> getClassNames( ) throws SQLException {
        Connection conn = sqlDatabase.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery(m_sqlFactory.selectClassNameQuery());
        while (res.next()) {
            classNameCache.put(res.getString(1), res.getString(2));
        }
        res.close();
        stmt.close();
        return classNameCache;
    }
    public HashMap<String, String> getClassUrl( ) throws SQLException {
        Connection conn = sqlDatabase.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery(m_sqlFactory.selectClassUriQuery());
        while (res.next()) {
            classUriCache.put(res.getString(1), res.getString(2));
        }
        res.close();
        stmt.close();
        return classUriCache;
    }
    public HashMap<String, String> getPropertyNames( ) throws SQLException {
        Connection conn = sqlDatabase.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery(m_sqlFactory.selectPropertyNameQuery());
        while (res.next()) {
            propertyNameCache.put(res.getString(1), res.getString(2));
        }
        res.close();
        stmt.close();
        return propertyNameCache;
    }
    public List<HashMap<String, String>> getClassAndPropertyNameForExPropClass( ) throws SQLException {
        List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
         HashMap<String, String> classNameCacheForExPropClass = new HashMap<String, String>();;
         HashMap<String, String> propertyNameCacheForExPropClass= new HashMap<String, String>();;
        Connection conn = sqlDatabase.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery(m_sqlFactory.selectPropertyClassName());
        while (res.next()) {
            propertyNameCacheForExPropClass.put(res.getString(1), res.getString(2));
            classNameCacheForExPropClass.put(res.getString(1), res.getString(3));
        }
        res.close();
        stmt.close();
        result.add(propertyNameCacheForExPropClass);
        result.add(classNameCacheForExPropClass);
        return result;
    }
}
