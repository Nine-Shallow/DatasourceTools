package cn.i0v0p.datasource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private String oracleUrl;
    private String oracleUser;
    private String oraclePassword;
    private String oracleSchemaName;
    private String damengUrl;
    private String damengUser;
    private String damengPassword;
    private String damengSchemaName;
    private String primaryDatabaseSource;
    private String secondaryDatabaseSource;
    private static final String propertiesFileName = "datasource.properties";
    private static final Logger log = LogManager.getLogger(DatabaseConfig.class);

    private DatabaseConfig() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFileName)) {
            if (input == null) {
                log.error("Properties file not found: " + propertiesFileName);
                return;
            }
            properties.load(input);
            this.oracleUrl = properties.getProperty("oracle.url");
            this.oracleUser = properties.getProperty("oracle.user");
            this.oraclePassword = properties.getProperty("oracle.password");
            this.oracleSchemaName = properties.getProperty("oracle.schemaName");
            this.damengUrl = properties.getProperty("dameng.url");
            this.damengUser = properties.getProperty("dameng.user");
            this.damengPassword = properties.getProperty("dameng.password");
            this.damengSchemaName = properties.getProperty("dameng.schemaName");
            this.primaryDatabaseSource = properties.getProperty("compared.primaryDatabaseSource");
            this.secondaryDatabaseSource = properties.getProperty("compared.secondaryDatabaseSource");
        } catch (IOException e) {
            log.error("Failed to load properties file: " + e.getMessage());
        }
    }

    public static DatabaseConfig initConfig() {
        return new DatabaseConfig();
    }

    // Getters
    public String getOracleUrl() {
        return oracleUrl;
    }

    public String getOracleUser() {
        return oracleUser;
    }

    public String getOraclePassword() {
        return oraclePassword;
    }

    public String getDamengUrl() {
        return damengUrl;
    }

    public String getDamengUser() {
        return damengUser;
    }

    public String getDamengPassword() {
        return damengPassword;
    }

    public String getOracleSchemaName() {
        return oracleSchemaName;
    }

    public String getDamengSchemaName() {
        return damengSchemaName;
    }

    public String getPrimaryDatabaseSource() {
        return primaryDatabaseSource;
    }

    public String getSecondaryDatabaseSource() {
        return secondaryDatabaseSource;
    }

}

