/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.json.simple.JSONValue;

/**
 *
 * @author bastinl
 */
public class LessonSelection {

    private HashMap<String, Lesson> chosenLessons;
    private int ownerID;

    private DataSource ds = null;
    private Connection connection = null;
    private ResultSet rs = null;
    private PreparedStatement pstmt = null;
    //use as counting lesson selected
    private int lessonCount;

    public LessonSelection(int owner) {

        chosenLessons = new HashMap<String, Lesson>();
        this.ownerID = owner;
        lessonCount = 0;

        try {
            // Obtain our environment naming context
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            // Look up our data source
            ds = (DataSource) envCtx.lookup("jdbc/LessonDatabase");
        } catch (Exception e) {
            System.out.println("Exception message is " + e.getMessage());
        }

        // Connect to the database - this is a pooled connection
        try {
            connection = ds.getConnection();

            if (connection != null) {
                // Query the database to get the all the lessons stored in the database.

                pstmt = connection.prepareStatement("SELECT lessons.* FROM lessons INNER JOIN lessons_booked ON lessons_booked.lessonid=lessons.lessonid and lessons_booked.clientid=?");
                pstmt.setString(1, Integer.toString(owner));
                rs = pstmt.executeQuery();
                // Add each lesson to the hashmap
                while (rs.next()) {
                    String desc = rs.getString("description");
                    String startTime = rs.getString("startDateTime");
                    Timestamp st = Timestamp.valueOf(startTime);
                    String endTime = rs.getString("endDateTime");
                    Timestamp et = Timestamp.valueOf(endTime);
                    String level = rs.getString("level");
                    String id = rs.getString("lessonid");
                    Lesson lesson = new Lesson(desc, st, et, Integer.parseInt(level), id);
                    addLesson(lesson);
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
    public Set<Entry<String, Lesson>> getItems() {
        return chosenLessons.entrySet();
    }

    public void addLesson(Lesson l) {
        Lesson i = new Lesson(l);
        this.chosenLessons.put(l.getId(), i);
        //increase the count after adding lesson to hashmap
        lessonCount++;
    }

    public Lesson getLesson(String id) {
        return this.chosenLessons.get(id);
    }

    public int getNumChosen() {
        return this.chosenLessons.size();
    }

    public int getOwner() {
        return this.ownerID;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    /**
     * remove lesson from chosenLesson object - HashMap
     * @param key - lessonID 
     */
    public void remove(String key) {
        chosenLessons.remove(key);
        //decrease the count after adding lesson to hashmap
        lessonCount--;
    }

    public HashMap<String, Lesson> getLessonsHashMap() {
        return chosenLessons;
    }
    
    /**
     * Updates the lesson table in the database
     */
    public void updateBooking() {

        // Connect to the database
        try {
            //get a connection to the database as in the method above
            connection = ds.getConnection();

            if (connection != null) {
                

                //In the database, delete any existing lessons booked for this user in the table 'lessons_booked'
                pstmt = connection.prepareStatement("DELETE FROM lessons_booked WHERE clientid=?");
                pstmt.setString(1, Integer.toString(ownerID));
                //perform query to delete the lesson
                pstmt.executeUpdate();
 
                // insert each selected lesson to the database
                Object[] lessonKeys = chosenLessons.keySet().toArray();
                for (int i = 0; i < lessonKeys.length; i++) {
                    pstmt = connection.prepareStatement("INSERT INTO lessons_booked (clientid, lessonid) VALUES (?,?)");
                    pstmt.setString(1, Integer.toString(ownerID));
                    pstmt.setString(2, (String) lessonKeys[i]);
                    pstmt.executeUpdate();
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
     * Gets lessons and their count and returns JSON string
     * @return names and counts of lessons as String JSON
     */
    public String getLessonsAndCount() {
        HashMap lessonNamesAndCount = new HashMap();
        ArrayList lessonNames = new ArrayList();
        ArrayList lessonCounts = new ArrayList();

        try {

            connection = ds.getConnection();

            if (connection != null) {

                //get all the lessons selected by all the users and their count
                pstmt = connection.prepareStatement("SELECT description, lessonid, (SELECT COUNT(*) FROM lessons_booked WHERE lessons.lessonid=lessons_booked.lessonid) as bookCount FROM lessons");
                rs = pstmt.executeQuery();
                // add the result to lessonNames and lessionCounts lists
                while (rs.next()) {
                    String desc = rs.getString("description");
                    String count = rs.getString("bookCount");
                    lessonNames.add(desc);
                    lessonCounts.add(Integer.parseInt(count));
                }
                //store the names and counts lists to hashmap
                lessonNamesAndCount.put("names", lessonNames);
                lessonNamesAndCount.put("counts", lessonCounts);

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
        //parse the names and counts to JSON and return it
        return JSONValue.toJSONString(lessonNamesAndCount);
    }

}
