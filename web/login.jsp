<%-- 
    Document   : index
    Created on : 15-Mar-2010, 14:47:22
    Author     : bastinl
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>

        <title>Login / signup page</title>
        <script type="text/javascript">
            /* 
             * function to validates username -  checks if the useername lenght is more than 7...
             * then creates AJAX request to check if username is already taken or not.
             */
            function validateUsername() {
                var displayMessage = document.getElementById("displayMessage");
                //check the length of the username in if statement and perform action accordingly
                if (document.getElementById('usernameToRegister').value.length < 8) {
                    //disable the button when username is 7 or less
                    document.getElementById('register_button').disabled = true;
                    //Issue a message for the user
                    displayMessage.innerHTML = "The username provided is not more than 7 characters! Please provide username more than 7 characters.";
                } else {
                    
                    //Issue a valid message when the length is more than 7...
                    displayMessage.innerHTML = "Username provided is more than 7 character but please wait for username availability check...";
                    //Generate AJAX request to check if the username is already taken or not
                    var request = new XMLHttpRequest();
                    request.open("POST", "${pageContext.request.contextPath}/do/checkAvailability", true);
                    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                    if (!request) {
                        alert("Error in initialising request.");
                    }
                    //callback onreadystatechange
                    request.onreadystatechange = processResponse;
                    var newUsername = document.getElementById('usernameToRegister');
                    request.send("newUsername=" + newUsername.value);

                }
                /* 
                 * Callback function - checks if the username is taken or not...
                 * enables the register button if username is not taken.
                 */
                function processResponse() {
                    if (this.status === 200 && this.readyState === 4) {

                        var displayMessage = document.getElementById("displayMessage");
                        //get the XML response
                        var XMLdoc = this.responseXML.documentElement;
                        var checkUsernameXML = XMLdoc.childNodes[0];
                        //check if username is taken and enable the register button accordingly 
                        if (checkUsernameXML.childNodes[0].data === "true") {
                            displayMessage.innerHTML = "Username is already taken, please provide different username.";
                            document.getElementById('register_button').disabled = true;
                        } else if (checkUsernameXML.childNodes[0].data === "false") {
                            displayMessage.innerHTML = "Username is available!";
                            document.getElementById('register_button').disabled = false;
                        } else if (checkUsernameXML.childNodes[0].data === "cannotEstablishConnection"){
                            displayMessage.innerHTML = "Cannot connect to database, please check the configuration!";
                            document.getElementById('register_button').disabled = false;
                            
                        }
                    }
                }
            }
        </script>
    </head>
    <body>
        <h2>Please log in!</h2>
        <form method="POST" action="${pageContext.request.contextPath}/do/login">
            Username:<input type="text" name="username" value="" />----
            Password:<input type="password" name="password" value="" />
            <input type="submit" value="Click to log in" />
        </form>

        <form method="POST" action="${pageContext.request.contextPath}/do/addUser">
            <h2> Don't yet have an account? </h2>
            Username:<input type="text" name="newUsername" id="usernameToRegister" value="" onchange="validateUsername()"/>----
            Password:<input type="password" name="newPassword" value="" />      
            <input type="submit" value="Sign up as a new user" id="register_button" disabled="true"/>
        </form>
        <br/>
        <!-- tag to display message -->
        <div id="displayMessage"> ${requestScope.message}</div>

    </body>
</html>
