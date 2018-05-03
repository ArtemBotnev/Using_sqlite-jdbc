package model;

import sql.SQLHelper;

import java.sql.*;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Manager SQL data (singleton)
 * Create by Artem 12.03.2018
 */
public class TaskManager {
    // day lasting in milliseconds
    private static final long DAY_LASTING_MILLIS = TimeUnit.DAYS.toMillis(1);

    private static TaskManager instance;
    private SQLHelper helper;
    private String url;

    private TaskManager() {
    }

    public static TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }

        return instance;
    }

    public void setUrl(String url) {
        this.url = url;
        helper = new SQLHelper(url);
    }

    /**
     * create and fill database
     */
    public void initDatabase() {
        helper.jdbcInit("org.sqlite.JDBC");
    }

    /**
     * remove tables from database
     */
    public void dropTables() {
        try {
            helper.doRequest()
                    .tableName("Projects")
                    .deleteTable();
            helper.doRequest()
                    .tableName("Tasks")
                    .deleteTable();
        } catch (SQLException e) {
            e.printStackTrace();
            e.getMessage();
        }
    }

    /**
     * parse data from database
     *
     * @return title of projects which aren't done yet
     */
    public String getNotDoneProjects() {
        String response = "Все проекты выполнены"; //case when all project are done

        try (Connection connection = DriverManager.getConnection(url)) {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT DISTINCT p.Title" +
                            " FROM Projects p" +
                            " JOIN Tasks t" +
                            " ON p.Id = t.ProjectId" +
                            " WHERE t.IsDone = 0");
            final ResultSet resultSet = statement.executeQuery();

            String answer = getStringResponseFromResultSet(resultSet);
            if (answer != null) {
                response = answer;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * parse data from database
     *
     * @param projectName project name
     * @return number of tasks of this project which isn't done
     */
    public int numberOfNotDoneTasks(String projectName) {
        int response = 0;

        try (Connection connection = DriverManager.getConnection(url)) {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) " +
                            "FROM Tasks t " +
                            "JOIN Projects p " +
                            "ON p.Id = t.ProjectId " +
                            "WHERE p.Title = ? " +
                            "AND t.IsDone = 0");
            statement.setString(1, projectName);
            final ResultSet resultSet = statement.executeQuery();
            response = resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * parse data from database
     *
     * @param responsibleName responsible name
     * @return title of tasks which aren't done by this responsible yet
     */
    public String notDoneTasksOfResponsible(String responsibleName) {
        String response = "Все задачи выполнены"; //case when all tasks this responsible are done

        try (Connection connection = DriverManager.getConnection(url)) {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT Title " +
                            "FROM Tasks " +
                            "WHERE IsDone = 0 " +
                            "AND Responsible = ?");
            statement.setString(1, responsibleName);
            final ResultSet resultSet = statement.executeQuery();

            String answer = getStringResponseFromResultSet(resultSet);
            if (answer != null) {
                response = answer;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * parse data from database
     *
     * @param day   for which it is requested
     * @param month for which it is requested
     * @param year  for which it is requested
     * @return list of tasks and persons who are responsible
     */
    public String dailyTasks(int day, int month, int year) {
        String response = "Нет задач на сегодня";

        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        long thisDay = calendar.getTimeInMillis(); // start this day (milliseconds)

        try (Connection connection = DriverManager.getConnection(url)) {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT Title, Responsible " +
                            "FROM Tasks " +
                            "WHERE StartDate <= ? " +
                            "AND StartDate+Lasting*" + DAY_LASTING_MILLIS + ">= ?");
            statement.setLong(1, thisDay);
            statement.setLong(2, thisDay);
            final ResultSet resultSet = statement.executeQuery();

            StringBuilder builder = new StringBuilder();
            while (resultSet.next()) {
                builder.append(resultSet.getString(1));
                builder.append(", ответственный: ");
                builder.append(resultSet.getString(2));
                builder.append("\n");
            }

            if (builder.length() != 0) {
                response = builder.toString();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * parse data from database
     *
     * @return list of responsible persons with phone number
     */
    public String overdueTasksResponsible() {
        String response = "Все исполнители выполнили задачи в срок";

        final Calendar calendar = Calendar.getInstance();
        long thisDay = calendar.getTimeInMillis(); // start this day (milliseconds)

        try (Connection connection = DriverManager.getConnection(url)) {
            final PreparedStatement statement = connection.prepareStatement(
                    "SELECT DISTINCT Responsible, Phone " +
                            "FROM Tasks " +
                            "WHERE StartDate+Lasting*" + DAY_LASTING_MILLIS + " < ? " +
                            "AND IsDone = 0");
            statement.setLong(1, thisDay);
            final ResultSet resultSet = statement.executeQuery();

            StringBuilder builder = new StringBuilder();
            while (resultSet.next()) {
                builder.append(resultSet.getString(1));
                builder.append(", телефон: ");
                builder.append(resultSet.getString(2));
                builder.append("\n");
            }

            if (builder.length() != 0) {
                response = builder.toString();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * @param resultSet ...
     * @return generalized string read from ResultSet
     * @throws SQLException
     */
    private String getStringResponseFromResultSet(ResultSet resultSet) throws SQLException {
        StringBuilder builder = new StringBuilder();
        while (resultSet.next()) {
            builder.append(resultSet.getString(1));
            builder.append("\n");
        }

        String response;
        if (builder.length() != 0) {
            response = builder.toString();
        } else {
            response = null;
        }
        return response;
    }
}