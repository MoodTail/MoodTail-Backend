package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V1__align_auth_schema extends BaseJavaMigration {

    private static final String USERS = "users";
    private static final String SOCIAL_ACCOUNTS = "social_accounts";

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        if (!tableExists(connection, USERS)) {
            createUsers(connection);
        }

        alignUsers(connection);
        if (!tableExists(connection, SOCIAL_ACCOUNTS)) {
            createSocialAccounts(connection);
        }
        alignSocialAccounts(connection);
    }

    private void createUsers(Connection connection) throws SQLException {
        execute(connection, "CREATE TABLE users ("
                + "id BIGINT NOT NULL AUTO_INCREMENT, "
                + "guest_uuid VARCHAR(36) NULL, "
                + "nickname VARCHAR(50) NULL, "
                + "representative_mood_type_id BIGINT NULL, "
                + "role VARCHAR(20) NOT NULL, "
                + "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', "
                + "last_accessed_at DATETIME NOT NULL, "
                + "created_at DATETIME NULL, "
                + "updated_at DATETIME NULL, "
                + "deleted_at DATETIME NULL, "
                + "PRIMARY KEY (id), "
                + "CONSTRAINT uk_users_guest_uuid UNIQUE (guest_uuid), "
                + "CONSTRAINT ck_users_guest_identity CHECK "
                + "((role = 'GUEST' AND guest_uuid IS NOT NULL) OR "
                + "(role IN ('USER', 'ADMIN') AND guest_uuid IS NULL))"
                + ")");
    }

    private void createSocialAccounts(Connection connection) throws SQLException {
        execute(connection, "CREATE TABLE social_accounts ("
                + "id BIGINT NOT NULL AUTO_INCREMENT, "
                + "user_id BIGINT NOT NULL, "
                + "provider VARCHAR(20) NOT NULL, "
                + "provider_user_id VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, "
                + "email VARCHAR(320) NULL, "
                + "PRIMARY KEY (id), "
                + "CONSTRAINT uk_social_provider_user UNIQUE (provider, provider_user_id), "
                + "CONSTRAINT uk_social_user_provider UNIQUE (user_id, provider), "
                + "CONSTRAINT fk_social_account_user FOREIGN KEY (user_id) REFERENCES users (id)"
                + ")");
    }

    private void alignUsers(Connection connection) throws SQLException {
        if (columnExists(connection, USERS, "user_id") && !columnExists(connection, USERS, "id")) {
            execute(connection, "ALTER TABLE users RENAME COLUMN user_id TO id");
        }
        if (!columnExists(connection, USERS, "guest_uuid")) {
            execute(connection, "ALTER TABLE users ADD COLUMN guest_uuid VARCHAR(36) NULL");
        }
        if (!columnExists(connection, USERS, "status")) {
            execute(connection,
                    "ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'");
        }
        if (!columnExists(connection, USERS, "last_accessed_at")) {
            execute(connection, "ALTER TABLE users ADD COLUMN last_accessed_at DATETIME NULL");
        }

        execute(connection, "UPDATE users SET role = 'GUEST' WHERE role = 'ROLE_GUEST'");
        execute(connection, "UPDATE users SET role = 'USER' WHERE role = 'ROLE_USER'");
        execute(connection, "UPDATE users SET role = 'ADMIN' WHERE role = 'ROLE_ADMIN'");
        execute(connection,
                "UPDATE users SET guest_uuid = NULL WHERE role IN ('USER', 'ADMIN')");

        String accessFallback = "CURRENT_TIMESTAMP";
        if (columnExists(connection, USERS, "updated_at")) {
            accessFallback = "COALESCE(updated_at, CURRENT_TIMESTAMP)";
        } else if (columnExists(connection, USERS, "created_at")) {
            accessFallback = "COALESCE(created_at, CURRENT_TIMESTAMP)";
        }
        execute(connection,
                "UPDATE users SET last_accessed_at = " + accessFallback + " WHERE last_accessed_at IS NULL");

        assertNoInvalidGuestRows(connection);
        execute(connection, "ALTER TABLE users MODIFY COLUMN nickname VARCHAR(50) NULL");
        execute(connection, "ALTER TABLE users MODIFY COLUMN role VARCHAR(20) NOT NULL");
        execute(connection, "ALTER TABLE users MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'");
        execute(connection, "ALTER TABLE users MODIFY COLUMN last_accessed_at DATETIME NOT NULL");

        if (columnExists(connection, USERS, "email")) {
            execute(connection, "ALTER TABLE users MODIFY COLUMN email VARCHAR(100) NULL");
        }
        if (columnExists(connection, USERS, "password")) {
            execute(connection, "ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NULL");
        }
        if (!uniqueIndexExists(connection, USERS, "guest_uuid")) {
            execute(connection,
                    "ALTER TABLE users ADD CONSTRAINT uk_users_guest_uuid UNIQUE (guest_uuid)");
        }
        if (!constraintExists(connection, USERS, "ck_users_guest_identity")) {
            execute(connection,
                    "ALTER TABLE users ADD CONSTRAINT ck_users_guest_identity CHECK "
                            + "((role = 'GUEST' AND guest_uuid IS NOT NULL) OR "
                            + "(role IN ('USER', 'ADMIN') AND guest_uuid IS NULL))");
        }
    }

    private void alignSocialAccounts(Connection connection) throws SQLException {
        if (columnExists(connection, SOCIAL_ACCOUNTS, "provider_user_id")) {
            execute(connection,
                    "ALTER TABLE social_accounts MODIFY COLUMN provider_user_id "
                            + "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL");
        }
        if (columnExists(connection, SOCIAL_ACCOUNTS, "email")) {
            execute(connection,
                    "ALTER TABLE social_accounts MODIFY COLUMN email VARCHAR(320) NULL");
        }
        if (!uniqueIndexExists(connection, SOCIAL_ACCOUNTS, "provider", "provider_user_id")) {
            assertNoDuplicateRows(
                    connection,
                    "SELECT 1 FROM social_accounts GROUP BY provider, provider_user_id "
                            + "HAVING COUNT(*) > 1 LIMIT 1",
                    "Duplicate social provider identities must be resolved before migration"
            );
            execute(connection, "ALTER TABLE social_accounts ADD CONSTRAINT "
                    + "uk_social_provider_user UNIQUE (provider, provider_user_id)");
        }
        if (!uniqueIndexExists(connection, SOCIAL_ACCOUNTS, "user_id", "provider")) {
            assertNoDuplicateRows(
                    connection,
                    "SELECT 1 FROM social_accounts GROUP BY user_id, provider HAVING COUNT(*) > 1 LIMIT 1",
                    "Duplicate user-provider links must be resolved before migration"
            );
            execute(connection, "ALTER TABLE social_accounts ADD CONSTRAINT "
                    + "uk_social_user_provider UNIQUE (user_id, provider)");
        }
    }

    private void assertNoInvalidGuestRows(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT COUNT(*) FROM users WHERE role = 'GUEST' AND guest_uuid IS NULL")) {
            resultSet.next();
            if (resultSet.getLong(1) > 0) {
                throw new SQLException(
                        "Cannot enforce guest identity constraint: GUEST rows without guest_uuid exist"
                );
            }
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet tables = metadata.getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            return tables.next();
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            return columns.next();
        }
    }

    private boolean uniqueIndexExists(Connection connection, String tableName, String... columnNames)
            throws SQLException {
        String expectedColumns = String.join(",", columnNames).toLowerCase();
        String sql = "SELECT LOWER(GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ',')) "
                + "FROM information_schema.statistics "
                + "WHERE table_schema = DATABASE() AND table_name = '" + tableName + "' AND non_unique = 0 "
                + "GROUP BY index_name";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                if (expectedColumns.equals(resultSet.getString(1))) {
                    return true;
                }
            }
            return false;
        }
    }

    private void assertNoDuplicateRows(Connection connection, String sql, String message) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                throw new SQLException(message);
            }
        }
    }

    private boolean constraintExists(Connection connection, String tableName, String constraintName)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.table_constraints "
                + "WHERE table_schema = DATABASE() AND table_name = '" + tableName + "' "
                + "AND constraint_name = '" + constraintName + "'";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1) > 0;
        }
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
