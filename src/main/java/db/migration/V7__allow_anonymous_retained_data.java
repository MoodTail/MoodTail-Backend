package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Set;

public class V7__allow_anonymous_retained_data extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        configureAnonymousUserReference(
                context.getConnection(),
                "mood_test_results",
                "fk_mood_test_result_user"
        );
        configureAnonymousUserReference(
                context.getConnection(),
                "inquiries",
                "fk_inquiry_user"
        );
    }

    private void configureAnonymousUserReference(
            Connection connection,
            String tableName,
            String constraintName
    ) throws SQLException {
        if (!tableExists(connection, tableName) || !columnExists(connection, tableName, "user_id")) {
            return;
        }

        StringBuilder statement = new StringBuilder("alter table `")
                .append(tableName)
                .append("` ");
        for (String foreignKey : userForeignKeys(connection, tableName)) {
            statement.append("drop foreign key `")
                    .append(foreignKey.replace("`", "``"))
                    .append("`, ");
        }
        statement.append("modify column `user_id` bigint null, ")
                .append("add constraint `")
                .append(constraintName)
                .append("` foreign key (`user_id`) references `users` (`id`) on delete set null");
        execute(connection, statement.toString());
    }

    private Set<String> userForeignKeys(Connection connection, String tableName) throws SQLException {
        Set<String> foreignKeys = new LinkedHashSet<>();
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet keys = metadata.getImportedKeys(connection.getCatalog(), null, tableName)) {
            while (keys.next()) {
                if ("user_id".equalsIgnoreCase(keys.getString("FKCOLUMN_NAME"))
                        && "users".equalsIgnoreCase(keys.getString("PKTABLE_NAME"))) {
                    String name = keys.getString("FK_NAME");
                    if (name != null && !name.isBlank()) {
                        foreignKeys.add(name);
                    }
                }
            }
        }
        return foreignKeys;
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (ResultSet tables = connection.getMetaData().getTables(
                connection.getCatalog(),
                null,
                tableName,
                new String[]{"TABLE"}
        )) {
            return tables.next();
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        try (ResultSet columns = connection.getMetaData().getColumns(
                connection.getCatalog(),
                null,
                tableName,
                columnName
        )) {
            return columns.next();
        }
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
