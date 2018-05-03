package model;

import org.junit.*;
import sql.SQLHelper;

import java.sql.SQLException;
import java.util.Calendar;

import static org.junit.Assert.*;

public class TaskManagerTest {
    private static final int TIMEOUT = 20;
    private static TaskManager manager;
    private static final String URL = "jdbc:sqlite:projects.db";

    @BeforeClass
    static public void managerInit() {
        manager = TaskManager.getInstance();
        manager.setUrl(URL);
        manager.initDatabase();

        try {
            createDatabaseTables();
            fillProjectsTable();
            fillTasksTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @AfterClass
    static public void clearDatabase() {
        manager.dropTables();
    }

    @Test(timeout = TIMEOUT)
    public void getNotDoneProjectsTest() {
        String expected = "Полить цветы\nЗакупить оборудование\nСоставить отчёт за месяц\n";
        String result = manager.getNotDoneProjects();
        assertEquals(expected, result);
    }

    @Test(timeout = TIMEOUT)
    public void numberOfNotDoneTasksTest() {
        int result = manager.numberOfNotDoneTasks("Полить цветы");
        assertEquals(1, result);
    }

    @Test(timeout = TIMEOUT)
    public void notDoneTasksOfResponsible() {
        String expected = "Составить отчёт за 3-4 неделю\n";
        String result = manager.notDoneTasksOfResponsible("Мария");
        assertEquals(expected, result);
    }

    @Test(timeout = TIMEOUT)
    public void dailyTasksTest() {
        String expected = "Закупить шестерёнки, ответственный: Виталий\n";
        //February 3, 2018
        String result = manager.dailyTasks(3, Calendar.FEBRUARY, 2018);
        assertEquals(expected, result);
    }

    @Test(timeout = TIMEOUT)
    public void overdueTasksResponsibleTest() {
        String expected = "Егор, телефон: 75632\n" +
                "Виталий, телефон: 34567\n" +
                "Елена, телефон: 75867\n" +
                "Мария, телефон: 45632\n";
        String result = manager.overdueTasksResponsible();
        assertEquals(expected, result);
    }

    /**
     * Create tables (Projects, Tasks) of database
     */
    private static void createDatabaseTables() throws SQLException {
        SQLHelper helper = new SQLHelper(URL);
        helper.doRequest()
                .tableName("Projects")
                .addColumnWithType(new String[]{"Title TEXT"})
                .createTable();

        helper.doRequest()
                .tableName("Tasks")
                .addColumnWithType(new String[]{
                        "Title TEXT", "Responsible TEXT", "Phone TEXT", "StartDate INTEGER",
                        "Lasting INTEGER", "IsDone INTEGER", "ProjectId INTEGER"})
                .createTable();
    }

    /**
     * fill Projects table
     */
    private static void fillProjectsTable() throws SQLException {
        String[] titleValues = {"Полить цветы", "Закупить оборудование",
                "Отправить продукт заказчику", "Составить отчёт за месяц"};

        SQLHelper helper = new SQLHelper(URL);
        for (String titleValue : titleValues) {
            helper.doRequest()
                    .tableName("Projects")
                    .addColumns(new String[]{"Title"})
                    .addValues(new String[]{titleValue})
                    .insertToTable();
        }
    }

    /**
     * Tasks table
     *
     * @throws SQLException
     */
    private static void fillTasksTable() throws SQLException {
        String[] columnsName = {"Title", "Responsible", "Phone", "StartDate", "Lasting", "IsDone", "ProjectId"};
        String[][] columnsValues = {
                {"Полить цветы в комнате 234", "Мария", "45632", getMillisecondDateAsString(2018, Calendar.JANUARY, 26), "2", "1", "1"},
                {"Полить цветы в окмнате 143", "Егор", "75632", getMillisecondDateAsString(2018, Calendar.JANUARY, 26), "2", "0", "1"},
                {"Закупить шестерёнки", "Виталий", "34567", getMillisecondDateAsString(2018, Calendar.FEBRUARY, 1), "4", "0", "2"},
                {"Закупить болты", "Виталий", "34567", getMillisecondDateAsString(2018, Calendar.FEBRUARY, 4), "3", "0", "2"},
                {"Закупить гайки", "Елена", "75867", getMillisecondDateAsString(2018, Calendar.JANUARY, 29), "2", "1", "2"},
                {"Отправить насос", "Михаил", "59426", getMillisecondDateAsString(2018, Calendar.JANUARY, 15), "2", "1", "3"},
                {"Отправить вентилятор", "Сергей", "75867", getMillisecondDateAsString(2018, Calendar.JANUARY, 17), "4", "1", "3"},
                {"Составить отчёт за 1-2 неделю", "Елена", "75867", getMillisecondDateAsString(2018, Calendar.FEBRUARY, 16), "1", "0", "4"},
                {"Составить отчёт за 3-4 неделю", "Мария", "45632", getMillisecondDateAsString(2018, Calendar.FEBRUARY, 27), "1", "0", "4"}
        };

        SQLHelper helper = new SQLHelper(URL);

        for (String[] columnsValue : columnsValues) {
            helper.doRequest()
                    .tableName("Tasks")
                    .addColumns(columnsName)
                    .addValues(columnsValue)
                    .insertToTable();
        }
    }

    /**
     * present date as milliseconds
     *
     * @param year  ...
     * @param month ...
     * @param day   ...
     * @return milliseconds of this date
     */
    private static String getMillisecondDateAsString(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return String.valueOf(calendar.getTimeInMillis());
    }
}