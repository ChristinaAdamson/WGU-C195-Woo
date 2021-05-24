import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;


/**
 * The Controller Class for the Login Form
 */
public class LoginController {

    @FXML Pane mainPane;

    @FXML Label subTitleLabel;
    @FXML Label usernameLabel;
    @FXML Label passwordLabel;
    @FXML Label locationLabel;

    @FXML TextField usernameField;
    @FXML TextField passwordField;

    @FXML Button loginButton;
    @FXML Button closeButton;

    /**
     * Class constructor does nothing
     */
    public LoginController(){
    }

    /**
     * FXML initialize method runs automatically when Login Form scene is created.
     */
    @FXML
    private void initialize(){
        try {
            Database.initializeDatabase();
        } catch (Exception e){
            System.out.println("Initializing Database Failed");
        }
        // If System Language is french, change labels texts to french translations
        if (System.getProperty("user.language").equals("fr")){
            subTitleLabel.setText("Connexion au gestionnaire\n de rendez-vous");
            usernameLabel.setText("Nom d'utilisateur");
            passwordLabel.setText("mot de passe");
            loginButton.setText("entrer");
            closeButton.setText("proche");
        }
        // Get System ZoneID (Timezone/Region) and fill location label
        ZoneId zoneId = ZoneId.systemDefault();
        if (zoneId != null) {
            locationLabel.setText(zoneId.toString());
        }
        // Center Location Label in Login Menu Pane
        locationLabel.layoutXProperty().bind(
                mainPane.widthProperty().subtract(locationLabel.widthProperty()).divide(2));
    }

    /**
     * Takes in a calender object and turns it into a readable String of local time
     * @param someCalendar A Calendar object of any timezone
     * @return A String of local time based on the someCalendar's set time
     */
    private String getLocalTimeString(Calendar someCalendar){
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        TimeZone currentTimezone = TimeZone.getTimeZone(ZoneId.systemDefault());
        someCalendar.setTimeZone(currentTimezone);
        return sdf.format(someCalendar.getTime());
    }

    /**
     * This method creates an informational pop up that tells whether or not there is an appointment that starts in the
     * next 15 minutes
     */
    private void appointmentIn15Popup(){
        try {

            int apptId = Database.hasAppointmentIn15min();
            Alert infoBox = new Alert(Alert.AlertType.INFORMATION);

            // If appId == -1, it means that there is no upcoming appointments.
            // appId == -1 is not a valid appointment ID so it will never over lap with valid appointment IDs
            if (apptId == -1){

                infoBox.setTitle("Upcoming Appointments");
                infoBox.setContentText("No appointments coming up in the next 15 minutes.");
                infoBox.show();

            } else{
                Appointment comingAppointment = Database.getAppointment(apptId);
                infoBox.setTitle("Upcoming Appointments");
                infoBox.setContentText("Appointment coming up in the next 15 minutes.\n"+
                        "Appointment ID: "+comingAppointment.getAppointmentId()+"\n"+
                        "Start Time: "+getLocalTimeString(comingAppointment.getStart()));
                infoBox.show();
            }
        } catch (SQLException e){
            Alert confirmBox = new Alert(Alert.AlertType.ERROR);
            confirmBox.setTitle("SQL Error");
            confirmBox.setContentText("An Error has occurred in the SQL Query");
        }
    }

    /**
     * Checks whether the username and the password given is a valid combination in database.users
     * @param username the String representing a username
     * @param password the String representing a password
     * @return returns a boolean for whether or not the combination is valid
     */
    private boolean isValidCredentials(String username, String password){
        try {
            // Checks whether the given username matches the password in the database
            String rightPassword = Database.getUserPassword(username);
            if (password.equals(rightPassword)){
                return true;
            }
            else{
                return false;
            }
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e){
            // Does this when the username is not a valid username in the database
            return false;
        }
    }

    /**
     * Logs the log in attempt to login_activity.log file whether or not the attempt was successful
     * @param username the String representing username
     * @param isSuccess the boolean value for whether or not the log in attempt was successful
     */
    private void logLoginAttempt(String username, boolean isSuccess){

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy::HH:mm.ss");
        sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        String timestamp = sdf.format(new Timestamp(System.currentTimeMillis()));
        String logPath = new File(System.getProperty("user.dir")).getAbsolutePath();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(logPath+"\\login_activity.txt", true));
            String appendingLog = timestamp+" "+username+" has attempted login. Success="+isSuccess+"\n";
            writer.write(appendingLog);
            writer.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * FXML action method for when the login button is pressed.
     * Initiates the login authentication, logging, and UI scene change
     */
    @FXML
    private void loginButtonPressed(){

        // if username and password text input box is empty
        if (usernameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty()){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            // If system language is french, use translated french error message
            if (System.getProperty("user.language").equals("fr")) {
                errorBox.setTitle("Erreur d'entrée de connexion");
                errorBox.setContentText("Veuillez saisir un nom d'utilisateur et un mot de passe");
            }
            else {
                errorBox.setTitle("Login Input Error");
                errorBox.setContentText("Please Enter a Username and Password");
            }
            errorBox.showAndWait();
        }
        else{
            // Get String values for texts in input fields
            String usernameInput = usernameField.getText();
            String passwordInput = passwordField.getText();

            // If username and password matches credentials
            if (isValidCredentials(usernameInput, passwordInput)) {
                this.logLoginAttempt(usernameInput, true);

                try {
                    // Change login screen to appointment management system home screen
                    Stage stage = (Stage) loginButton.getScene().getWindow();

                    Database.setDatabaseUser(usernameInput);
                    Database.prepopulateLocations();
                    Parent root = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
                    stage.setScene(new Scene(root));
                    appointmentIn15Popup();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            // If username and password does not match credentials
            else{
                this.logLoginAttempt(usernameInput, false);

                // Create error box
                Alert errorBox = new Alert(Alert.AlertType.ERROR);
                // If system language is french, use translated french error message
                if (System.getProperty("user.language").equals("fr")) {
                    errorBox.setTitle("Erreur des informations de connexion");
                    errorBox.setContentText("Les informations de connexion ne sont pas valides");
                }
                else {
                    errorBox.setTitle("Login Credentials Error");
                    errorBox.setContentText("Login Credentials are Invalid");
                }
                errorBox.showAndWait();
            }
        }
    }

    /**
     * FXML Action method to close the login form screen. Ends the software.
     */
    @FXML
    private void closeButtonPressed(){
        // Get close button's stage and close the selected stage
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
