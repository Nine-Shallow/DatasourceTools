package cn.i0v0p.datasource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DatabaseSchemaComparator {
    private static final Logger log = LogManager.getLogger(DatabaseSchemaComparator.class);

    public static void main(String[] args) {
        log.info("开始比较数据库结构");
        try {
            DatabaseConfig config = DatabaseConfig.initConfig();
            log.info("开始获取oracle表结构");
            Map<String, List<String>> oracleSchema = DatasourceTools.getSchema(config.getOracleUrl(), config.getOracleUser(), config.getOraclePassword(), config.getOracleSchemaName());
            log.info("开始获取dameng表结构");
            Map<String, List<String>> damengSchema = DatasourceTools.getSchema(config.getDamengUrl(), config.getDamengUser(), config.getDamengPassword(), config.getDamengSchemaName());
            Map<String, Map<String, String>> differences = Collections.emptyMap();
            log.info("开始两端对比");
            if("oracle".equals(config.getPrimaryDatabaseSource())){
                differences= DatasourceTools.compareSchemas(oracleSchema, damengSchema);
            }else if ("dm".equals(config.getPrimaryDatabaseSource())){
                differences = DatasourceTools.compareSchemas(damengSchema, oracleSchema);
            }
            log.info("差异结果汇总中...");
            String reportName = DatasourceTools.generateExcelReport2(differences, config);
            log.info("比较完成 - {}",reportName);
        } catch (Exception e) {
            log.error("差异对比失败！");
            e.printStackTrace();
        }
    }
}
