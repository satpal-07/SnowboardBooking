/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 *
 * @author bastinl
 */
public class LessonTimetable {

    private Connection connection = null;

    private ResultSet rs = null;
    private PreparedStatement pstmt = null;
    private Map lessons = null;

    private DataSource ds = null;

    public LessonTimetable() {

        // You don't need to make any changes to the try/catch code below
        try {
            // Obtain our environment naming context
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            // Look up our data source
            ds = (DataSource) envCtx.lookup("jdbc/LessonDatabase");
        } catch (Exception e) {
            System.out.println("Exception message is " + e.getMessage());
        }

        try {
            // Connect to the database - you can use this connection to 
            connection = ds.getConnection();

            if (connection != null) {
                
                lessons = new HashMap<String, Lesson>();
                // create and prepare statements
                pstmt = connection.prepareStatement("SELECT * FROM lessons");
                rs = pstmt.executeQuery();
                // create lesson objects by selecting the relevant information from the database then populate the 'lessons' HashMap 
                while (rs.next()) {
                    String desc = rs.getString("description");
                    String startTime = rs.getString("startDateTime");
                    Timestamp st = Timestamp.valueOf(startTime);
                    String endTime = rs.getString("endDateTime");
                    Timestamp et = Timestamp.valueOf(endTime);
                    String level = rs.getString("level");
                    String lessonID = rs.getString("lessonid");
                    Lesson lesson = new Lesson(desc, st, et, Integer.parseInt(level), lessonID);
                    lessons.put(lessonID, lesson);
                }

               
            }

        } catch (SQLException e) {

            System.out.println("Exception is ;" + e + ": message is " + e.getMessage());

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    System.out.println("Exception is : " + ex + ": message is " + ex.getMessage());
                }
            }
        }

    }

    /**
     * @return the items
     */
    public Lesson getLesson(String itemID) {

        return (Lesson) this.lessons.get(itemID);
    }

    /**
     * @return the lessons Map
     */
    public Map getLessons() {

        return this.lessons;

    }

}
