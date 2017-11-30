/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package model;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 *
 * @author bastinl
 */
public class Users {

    private ResultSet rs = null;
    private PreparedStatement pstmt = null;
    private DataSource ds = null;
    private Connection connection = null;

    public Users() {

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

    }

    /**
     * Checks if the username is already taken
     * @param username - newUsername
     * @return - String XML with information of username found or not
     */
    public String findUsername(String username) {

        try {
            connection = ds.getConnection();
            System.out.println(connection);
            if (connection != null) {
                // Query the database, check if username is already taken.
                pstmt = connection.prepareStatement("SELECT username from clients where username=?");
                pstmt.setString(1, username);
                rs = pstmt.executeQuery();
                //check if the username is found
                //and insert true
                if (rs.next() && !rs.getString("username").equals("")) {
                    return "<?xml version=\"1.0\"?><checkUsername>"
                            + "<found>true</found>"
                            + "</checkUsername>";

                }
            }
        } catch (SQLException e) {
            System.out.println("Exception is : " + e + ": message is " + e.getMessage());
            return "<?xml version=\"1.0\"?><checkUsername>"
                    + "<found>cannotEstablishConnection</found>"
                    + "</checkUsername>";
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    System.out.println("Exception is : " + ex + ": message is " + ex.getMessage());
                }
            }
        }
        //insert false if username is not found
        return "<?xml version=\"1.0\"?><checkUsername>"
                + "<found>false</found>"
                + "</checkUsername>";
    }

    /**
     * Logins the user if provided username and password are correct
     * @param name - username
     * @param pwd - password
     * @return - return cliendID if successful login. return -1 if username or password is not correct. 
     * return -2 if cannot establish connection
     */
    public int isValid(String name, String pwd) {

        try {
            connection = ds.getConnection();

            if (connection != null) {
                // If the username and password are correct, it should return the 'clientID' value from the database.
                pstmt = connection.prepareStatement("SELECT clientid from clients where username=? and password=?");
                pstmt.setString(1, name);
                pstmt.setString(2, pwd);
                rs = pstmt.executeQuery();
                String clientID = "";
                //if user is found get the clientID
                if (rs.next()) {
                    clientID = rs.getString("clientid");
                }
                //if clientID is emplty return -1
                if (clientID.equals("")) {
                    return -1;
                } else {
                    //else return the clientID
                    return Integer.parseInt(clientID);
                }

            } else {
                //if connection null return -2
                return -2;
            }
        } catch (SQLException e) {

            System.out.println("Exception is: " + e + ": message is " + e.getMessage());
            return -2;
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
     * Registers the user
     * @param name - username.
     * @param pwd - password
     * @return - true on successful registration and false on unsuccessful
     */
    public boolean addUser(String name, String pwd) {

        try {
            // get the connection
            connection = ds.getConnection();

            if (connection != null) {
                // prepare the statement using the username and password
                pstmt = connection.prepareStatement("INSERT INTO clients ( username, password) VALUES (?,?)");
                pstmt.setString(1, name);
                pstmt.setString(2, pwd);

                int row = pstmt.executeUpdate();

                // check if successful and return true
                if (row > 0) {
                    return true;
                }
            }

        } catch (SQLException e) {
            System.out.println("Exception is ;" + e + ": message is " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    System.out.println("Exception is : " + ex + ": message is " + ex.getMessage());
                }
            }
        }
        // return false if unsuccessful
        return false;
    }
}
