package msc.thesis.aritra.main;

import msc.thesis.aritra.util.Settings;

import java.io.File;

/**
 * Names to use for transaction table file names.
 */
public enum TransactionTable {
    CLASS_MEMBERS("classmembers"),
    PROPERTY_RESTRICTIONS1("propertyrestrictions1"),
    PROPERTY_RESTRICTIONS2("propertyrestrictions2"),
    EXISTS_PROPERTY_MEMBERS("existspropertymembers"),

    ALL_MEMBER("allmembers");


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

    /**
     * Returns the absolute path of the association rule file generated from this table.
     * @return the absolute path of the association rule file generated from this table
     */
    public String getAbsoluteAssociationRuleFileName() {
        return Settings.getString("association_rules") + File.separator + getAssociationRuleFileName();
    }

    /**
     * Returns the name of the association rule file generated from this table.
     * @return the name of the association rule file generated from this table
     */
    public String getAssociationRuleFileName() {
        return tableName + "AR.txt";
    }

    public String getAbsoluteFileName(String key) {
        return Settings.getString(key)  + getFileName();
    }
}
