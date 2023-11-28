package msc.thesis.aritra.main;

import msc.thesis.aritra.util.Settings;

import java.io.File;

/**
 * Names to use for transaction table file names.
 */
public enum TransactionTable {
    CLASS_MEMBERS("classmembers"),
    PROPERTY_RESTRICTIONS1("propertyrestrictions1");



    private String tableName;

    private TransactionTable(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return tableName;
    }

    /**
     * Returns the name for this table's transaction table file name.
     * @return the name for this table's transaction table file name
     */
    public String getFileName() {
        return tableName + ".txt";
    }

    /**
     * Returns the absolute path for this table's transaction table file name.
     * @return the absolute path for this table's transaction table file name
     */
    public String getAbsoluteFileName() {
        return Settings.getString("transaction_tables")  + getFileName();
    }


}
