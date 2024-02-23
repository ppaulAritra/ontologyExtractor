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
    private HashMap<Integer, String> classNameCache;
    private HashMap<Integer, String> classUriCache;
    private HashMap<Integer, String> propertyNameCache;

    public CacheTable(SQLDatabase d){
        m_sqlFactory = new SQLFactory();
        sqlDatabase = d;
        classNameCache = new HashMap<Integer, String>();
        propertyNameCache = new HashMap<Integer, String>();
        classUriCache = new HashMap<Integer, String>();
    }
    public HashMap<Integer, String> getClassNames( ) throws SQLException {
        Connection conn = sqlDatabase.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery(m_sqlFactory.selectClassNameQuery());
        while (res.next()) {
            classNameCache.put(res.getInt(1), res.getString(2));
        }
        res.close();
        stmt.close();
        return classNameCache;
    }
    public HashMap<Integer, String> getClassUrl( ) throws SQLException {
        Connection conn = sqlDatabase.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery(m_sqlFactory.selectClassUriQuery());
        while (res.next()) {
            classUriCache.put(res.getInt(1), res.getString(2));
        }
        res.close();
        stmt.close();
        return classUriCache;
    }
    public HashMap<Integer, String> getPropertyNames( ) throws SQLException {
        Connection conn = sqlDatabase.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery(m_sqlFactory.selectPropertyNameQuery());
        while (res.next()) {
            propertyNameCache.put(res.getInt(1), res.getString(2));
        }
        res.close();
        stmt.close();
        return propertyNameCache;
    }
    public List<HashMap<Integer, String>> getClassAndPropertyNameForExPropClass( ) throws SQLException {
        List<HashMap<Integer, String>> result = new ArrayList<HashMap<Integer, String>>();
         HashMap<Integer, String> classNameCacheForExPropClass = new HashMap<Integer, String>();;
         HashMap<Integer, String> propertyNameCacheForExPropClass= new HashMap<Integer, String>();;
        Connection conn = sqlDatabase.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery(m_sqlFactory.selectPropertyClassName());
        while (res.next()) {
            propertyNameCacheForExPropClass.put(res.getInt(1), res.getString(2));
            classNameCacheForExPropClass.put(res.getInt(1), res.getString(3));
        }
        res.close();
        stmt.close();
        result.add(propertyNameCacheForExPropClass);
        result.add(classNameCacheForExPropClass);
        return result;
    }
}
