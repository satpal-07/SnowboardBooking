/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Lesson;
import model.LessonSelection;
import model.LessonTimetable;
import model.Users;

/**
 *
 * @author bastinl
 */
public class Controller extends HttpServlet {

    private Users users;
    private LessonTimetable availableLessons;

    public void init() {
        users = new Users();
        availableLessons = new LessonTimetable();

    }

    public void destroy() {

    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        RequestDispatcher dispatcher = null;
        if (action.equals("/login")) {
            // get the username and password parameters
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            //verify username and password are valid
            int clientID = users.isValid(username, password);
            // check if clientID returned is valid
            if (clientID > -1) {
                HttpSession session = request.getSession();
                session.setAttribute("clientId", clientID);
                //System.out.println(username);
                session.setAttribute("username", username);
                // create lessonSelection bean here as the constructor is parameterised hence cannot be created within jsp page
                LessonSelection lessonsSelected = new LessonSelection(clientID);
                session.setAttribute("lessonsSelected", lessonsSelected);
                dispatcher = this.getServletContext().getRequestDispatcher("/LessonTimetableView.jspx");

            } else if (clientID == -1) {
                // return value is -1 if username is invalid
                // if username and password are wrong issue a valid message
                request.setAttribute("message", "Invalid username and password!");
                dispatcher = this.getServletContext().getRequestDispatcher("/login.jsp");
            } else {
                // this else is for when the connection is not established with the database
                // generate appropriate error message
                request.setAttribute("message", "Unable to connect to database...!");
                dispatcher = this.getServletContext().getRequestDispatcher("/login.jsp");
            }
            dispatcher.forward(request, response);
        } else if (action.equals("/checkAvailability")) {
            //check the username if available for new registration
            //set the content type to XML
            response.setContentType("application/xml;charset=UTF-8");
            PrintWriter out = response.getWriter();
            //get the username parameter from the request
            String username = request.getParameter("newUsername");

            try {
                //find the username and pass the result back to response
                out.println(users.findUsername(username));

            } finally {
                out.close();
            }

        } else if (action.equals("/addUser")) {
            // adding user to the database
            // get the username and password from the parameters
            String username = request.getParameter("newUsername");
            String password = request.getParameter("newPassword");
            // add the user to the database and check the success
            if (users.addUser(username, password)) {
                //if successfully registered display a valid message
                request.setAttribute("message", "User registration completed!");
            } else {
                //else display error message
                request.setAttribute("message", "Unable to register new user!");
            }
            dispatcher = this.getServletContext().getRequestDispatcher("/login.jsp");

            dispatcher.forward(request, response);
        } else {
            // if another action requested, first check for a session.
            // check username attribute in the session to check whether session has username attribute
            // if the username attribute is null redirect the user back to login page (in this case user try to bypass login)
            HttpSession session = request.getSession();
            if (session.getAttribute("username") == null) {
                dispatcher = this.getServletContext().getRequestDispatcher("/login.jsp");
                dispatcher.forward(request, response);
            } else {
                //if username attribute exists then address the request accordingly
                if (action.equals("/chooseLesson")) {

                    //get the lessonID bean from the parameter
                    String lessonId = request.getParameter("lessonId");
                    //get the lessonSelected bean from session
                    LessonSelection lessonsSelected = (LessonSelection) session.getAttribute("lessonsSelected");

                    // check whether user has already selected the lesson
                    Lesson lesson = lessonsSelected.getLesson(lessonId);
                    if (lesson == null) {
                        // if the lesson has not been selected by the user add the lesson to the lessonSelection bean
                        lessonsSelected.addLesson(availableLessons.getLesson(lessonId));
                        // success message for user
                        request.setAttribute("success", "Lessson added to your selection. Please click finalise booking to save your selection.");
                    }

                    dispatcher = this.getServletContext().getRequestDispatcher("/LessonSelectionView.jspx");
                    dispatcher.forward(request, response);
                } else if (action.equals("/finaliseBooking")) {

                    //get the lessonSelection bean
                    LessonSelection lessonsSelected = (LessonSelection) session.getAttribute("lessonsSelected");
                    //update the database and issue a message for the user to notify the success
                    lessonsSelected.updateBooking();
                    // success message for user
                    request.setAttribute("success", "Saved your lessons selection!");
                    dispatcher = this.getServletContext().getRequestDispatcher("/LessonSelectionView.jspx");
                    dispatcher.forward(request, response);
                } else if (action.equals("/logout")) {

                    // delete the session and takes the user to the login page
                    session.invalidate();
                    dispatcher = this.getServletContext().getRequestDispatcher("/login.jsp");
                    dispatcher.forward(request, response);
                } else if (action.equals("/viewTimetable")) {
                    // take the user to LessonTimetableView page
                    dispatcher = this.getServletContext().getRequestDispatcher("/LessonTimetableView.jspx");
                    dispatcher.forward(request, response);
                } else if (action.equals("/viewSelection")) {
                    // take the user to LessonSelectionView page
                    dispatcher = this.getServletContext().getRequestDispatcher("/LessonSelectionView.jspx");
                    dispatcher.forward(request, response);
                } else if (action.equals("/cancelBooking")) {

                    //get the lessonId from the parameter
                    String lessonId = request.getParameter("lessonId");
                    //get the lessonSelection bean from the session
                    LessonSelection lessonsSelected = (LessonSelection) session.getAttribute("lessonsSelected");
                    //remove the lesson from the lessonSelection object
                    lessonsSelected.remove(lessonId);
                    // success message for user
                    request.setAttribute("success", "Lessson removed from your selection. Please click finalise booking to save.");
                    dispatcher = this.getServletContext().getRequestDispatcher("/LessonSelectionView.jspx");

                    dispatcher.forward(request, response);
                } else if (action.equals("/viewChart")) {
                    // take the user to viewchart page
                    dispatcher = this.getServletContext().getRequestDispatcher("/viewChart.jsp");
                    dispatcher.forward(request, response);
                } else if (action.equals("/showLessonsOnChart")) {
                    // action to get all the lessons selected and their counts
                    // set the response type to JSON
                    response.setContentType("application/json;charset=UTF-8");
                    PrintWriter out = response.getWriter();
                    LessonSelection lessonsSelected = (LessonSelection) session.getAttribute("lessonsSelected");
                    try {
                        //get the all the selected lessons by all the users and their count and parse it as JSON string
                        out.println(lessonsSelected.getLessonsAndCount());
                    } finally {
                        out.close();
                    }
                }
            }

        }
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
