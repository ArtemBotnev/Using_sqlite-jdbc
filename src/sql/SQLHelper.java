package sql;

import sql.dao.Dao;

import java.sql.*;

public class SQLHelper {
    private final String URL;

    public SQLHelper(String url) {
        URL = url;
    }

    public SQLRequest doRequest() {
        return new SQLRequest();
    }

    /**
     * initialization JDBC
     */
    public void jdbcInit(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public class SQLRequest implements Dao {
        private String tableName;
        private String[] columnsName;
        private String[] columnsValue;

        public SQLRequest tableName(String name) {
            tableName = name;
            return this;
        }

        public SQLRequest addColumnWithType(String[] columns) {
            columnsName = columns;
            return this;
        }

        public SQLRequest addColumns(String[] columns) {
            columnsName = columns;
            return this;
        }

        public SQLRequest addValues(String[] values) {
            columnsValue = values;
            return this;
        }

        @Override
        public void createTable() throws SQLException {
            try (Connection connection = DriverManager.getConnection(URL)) {
                connection.setAutoCommit(false);
                adjustTransactionIsolation(connection);
                final Statement statement = connection.createStatement();
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS " +
                                tableName +
                                " (" +
                                "Id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                columnBuild() +
                                ");");
                statement.close();
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Error of create table");
            }
        }

        @Override
        public void insertToTable() throws SQLException {
            try (Connection connection = DriverManager.getConnection(URL)) {
                connection.setAutoCommit(false);
                adjustTransactionIsolation(connection);
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO " +
                                tableName +
                                " (" + columnBuild() + ") " +
                                "VALUES (" +
                                requestValuesBuild() +
                                ");");
                fillStatement(statement);
                statement.execute();
                statement.close();
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Error of insert to table");
            }
        }

        @Override
        public void deleteTable() throws SQLException {
            try (Connection connection = DriverManager.getConnection(URL)) {
                connection.setAutoCommit(false);
                adjustTransactionIsolation(connection);
                final Statement statement = connection.createStatement();
                statement.execute("DROP TABLE IF EXISTS " + tableName);
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Error of table delete");
            }
        }

        private String columnBuild() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < columnsName.length; i++) {
                builder.append(columnsName[i]);

                if (i != columnsName.length - 1) {
                    builder.append(", ");
                }
            }

            return builder.toString();
        }

        private String requestValuesBuild() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < columnsName.length; i++) {
                builder.append(" ?");

                if (i != columnsName.length - 1) {
                    builder.append(", ");
                }
            }

            return builder.toString();
        }

        private void fillStatement(PreparedStatement statement) throws SQLException {
            for (int i = 0; i < columnsValue.length; i++) {
                statement.setString(i + 1, columnsValue[i]);
            }
        }

        private void adjustTransactionIsolation(Connection connection) throws SQLException {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        }
    }
}