package org.learn.binlogconnector.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableDDLMatcherUtil {

    private static final String TEMP_TABLE_PREFIX = "temp_help_";

    private static final String DATABASE = "${DATABASE}_";

    /**
     * DB_TABLE_PATTERN
     * 影响 DB_GROUP 和 TABLE_GROUP
     */
    public static final int DB_GROUP = 4;
    public static final int TABLE_GROUP = 5;
    public static final Pattern DB_TABLE_PATTERN = Pattern.compile("(`*(\\w+)`*\\.)?`*(\\w+)`*", Pattern.CASE_INSENSITIVE);

    private static final String RENAME_TABLE = DB_TABLE_PATTERN + "\\s+TO\\s+" + DB_TABLE_PATTERN;

    private static final Pattern RENAME_TABLE_PATTERN_MULTI = Pattern.compile(".*\\s*RENAME\\s+TABLE\\s+" + RENAME_TABLE + ("(\\s*,\\s+" + RENAME_TABLE + ")?"),
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    //默认情况下.*中的.只能匹配出\n以外的字符，如果遇到要匹配的字符串包含回车换行符（多行），则正则表达式遇到换行符后会停止，导致包含回车换行符的串不能正确匹配，解决的办法是：
    //使用Pattern和Matcher对象 设置Pattern模式为：Pattern.DOTALL
    public static final Pattern OPERATE_TABLE_PATTERN = Pattern.compile(".*\\w+\\s+table\\s+(IF\\s+(NOT\\s+)?EXISTS\\s+)?" + DB_TABLE_PATTERN + ".*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern ALTER_PATTERN = Pattern.compile(".*alter\\s+table\\s+" + DB_TABLE_PATTERN + "\\s+(add|change|modify|drop)\\s+.*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern CREATE_PATTERN = Pattern.compile(".*create\\s+TABLE\\s+(IF\\s+(NOT\\s+)?EXISTS\\s+)?" + DB_TABLE_PATTERN + ".*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern DROP_PATTERN = Pattern.compile(".*DROP\\s+TABLE\\s+(IF\\s+EXISTS\\s+)?" + DB_TABLE_PATTERN + ".*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


    private static final String CREATE_TABLE_PATTERN = "(?i)CREATE\\s+TABLE\\s+(IF\\s+NOT\\s+EXISTS\\s+)?" + DB_TABLE_PATTERN;
    private static final String CREATE_TABLE_REPLACE = "CREATE TABLE $1`" + TEMP_TABLE_PREFIX + DATABASE + "$4`";

    private static final String ALTER_TABLE_PATTERN = "(?i)alter\\s+table\\s+" + DB_TABLE_PATTERN;
    private static final String ALTER_TABLE_REPLACE = "alter table `" + TEMP_TABLE_PREFIX + DATABASE + "$3`";

    private static final String DROP_TABLE_PATTERN = "(?i)DROP\\s+table\\s+(IF\\s+EXISTS\\s+)?" + DB_TABLE_PATTERN;
    private static final String DROP_TABLE_REPLACE = "DROP table $1`" + TEMP_TABLE_PREFIX + DATABASE + "$4`";


    /**
     * 业务库创建的临时表名
     */
    public static String buildTempTable(String database, String tableName) {
        return TEMP_TABLE_PREFIX + database + "_" + tableName;
    }

    public static String matchDatabaseName(String sql) {
        Matcher matcher = OPERATE_TABLE_PATTERN.matcher(sql);
        if (matcher.find()) {
            String tableName = matcher.group(DB_GROUP).trim();
            return tableName;
        }
        return null;
    }


    public static String matchTableName(String sql) {
        Matcher matcher = OPERATE_TABLE_PATTERN.matcher(sql);
        if (matcher.find()) {
            return matcher.group(TABLE_GROUP);
        }
        return null;
    }

    public static String replaceCreateTableName(String database, String createTableSql) {
        String pattern = CREATE_TABLE_REPLACE.replaceAll("\\$\\{DATABASE}", database);
        return createTableSql.replaceAll(CREATE_TABLE_PATTERN, pattern);
    }

    public static String matchCreateTableName(String database, String createTableSql) {
        if (CREATE_PATTERN.matcher(createTableSql).matches()) {
            return replaceCreateTableName(database, createTableSql);
        }
        return null;
    }

    public static String matchAlterTableName(String database, String originalSql) {
        if (ALTER_PATTERN.matcher(originalSql).matches()) {
            String pattern = ALTER_TABLE_REPLACE.replaceAll("\\$\\{DATABASE}", database);
            return originalSql.replaceAll(ALTER_TABLE_PATTERN, pattern);
        }
        return null;
    }

    public static String matchDropTableName(String database, String alterTableSql) {
        if (DROP_PATTERN.matcher(alterTableSql).matches()) {
            String pattern = DROP_TABLE_REPLACE.replaceAll("\\$\\{DATABASE}", database);
            return alterTableSql.replaceAll(DROP_TABLE_PATTERN, pattern);
        }
        return null;
    }

    /**
     * 只匹配是否是rename语句。
     */
    public static String matchRenameTableName(String originalSql) {
        return RENAME_TABLE_PATTERN_MULTI.matcher(originalSql).matches() ? originalSql : null;
    }
}
