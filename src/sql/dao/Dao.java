package sql.dao;

import java.sql.SQLException;

public interface Dao {
    /**
     * create new table into DB
     *
     * @throws SQLException...
     */
    void createTable() throws SQLException;

    /**
     * insert data to DB table
     *
     * @throws SQLException...
     */
    void insertToTable() throws SQLException;

    /**
     * delete table from DB
     *
     * @throws SQLException...
     */
    void deleteTable() throws SQLException;
}
