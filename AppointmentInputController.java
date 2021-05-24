import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * The Controller Class for the Appointment Input form. Display input text fields to modify/add an appointment
 */
public class AppointmentInputController {

    private boolean isModify;
    private TimeZone currentTimezone = TimeZone.getTimeZone(ZoneId.systemDefault());
    private ObservableList<Contact> contactList = FXCollections.observableArrayList();

    @FXML ComboBox<Contact> contactIdCombo;

    @FXML DatePicker startDate;
    @FXML ComboBox<Integer> startHourCombo;
    @FXML ComboBox<Integer> startMinuteCombo;

    @FXML ComboBox<Integer> endHourCombo;
    @FXML ComboBox<Integer> endMinuteCombo;

    @FXML TextField apptIdField;
    @FXML TextField customerIdField;
    @FXML TextField userIdField;

    @FXML TextField titleField;
    @FXML TextField locationField;
    @FXML TextArea descriptionField;
    @FXML TextField typeField;

    @FXML Button saveButton;
    @FXML Button closeButton;

    @FXML Label timezoneLabel;
    @FXML Label pageHeader;

    /**
     * FXML initialize runs automatically when the Appointment input form is called
     */
    @FXML
    private void initialize(){
        isModify = false;

        // Set device's local Timezone information
        timezoneLabel.setText("<"+currentTimezone.getDisplayName()+">");

        // Populates the Hour and Minute combo boxes
        ObservableList<Integer> hourList = FXCollections.observableArrayList();
        ObservableList<Integer> minuteList = FXCollections.observableArrayList();

        for (int i=0;i<24;i++){
            hourList.add(i);
        }
        for (int i=0;i<60;i++){
            minuteList.add(i);
        }

        startHourCombo.setItems(hourList);
        startMinuteCombo.setItems(minuteList);

        endHourCombo.setItems(hourList);
        endMinuteCombo.setItems(minuteList);

        // Populates the contact combo box
        try {
            contactList = Database.getAllContact();
        } catch (SQLException e){
            System.out.println("SQL Error on retrieving contact");
        }

        contactIdCombo.setItems(contactList);
    }

    /**
     * This method is called when an appointment is to be modified instead of added.
     * @param someAppointment the Appointment object to be modified
     */
    public void initModify(Appointment someAppointment){
        isModify = true;
        // Fills in the input text fields based on the passed Appointment object
        pageHeader.setText("Modify Appointment");
        apptIdField.setText(String.valueOf(someAppointment.getAppointmentId()));
        for (int i=0;i<contactList.size();i++) {
            Contact currentContract = contactList.get(i);
            if (currentContract.getContactId() == someAppointment.getContactId()) {
                contactIdCombo.setValue(currentContract);
                break;
            }
        }

        customerIdField.setText(String.valueOf(someAppointment.getCustomerId()));
        userIdField.setText(String.valueOf(someAppointment.getUserId()));

        titleField.setText(someAppointment.getTitle());
        descriptionField.setText(someAppointment.getDescription());
        locationField.setText(someAppointment.getLocation());
        typeField.setText(someAppointment.getType());

        Calendar startCal = someAppointment.getStart();
        Calendar endCal = someAppointment.getEnd();

        startCal.setTimeZone(currentTimezone);
        endCal.setTimeZone(currentTimezone);

        LocalDate startLocalDate = LocalDateTime.ofInstant(startCal.toInstant(), startCal.getTimeZone().toZoneId()).toLocalDate();

        startDate.setValue(startLocalDate);

        startHourCombo.setValue(startCal.get(Calendar.HOUR_OF_DAY));
        startMinuteCombo.setValue(startCal.get(Calendar.MINUTE));
        endHourCombo.setValue(endCal.get(Calendar.HOUR_OF_DAY));
        endMinuteCombo.setValue(endCal.get(Calendar.MINUTE));
    }

    /**
     * FXML Action method to initialize the save process for an appointment based on the inputted text fields.
     */
    @FXML
    private void saveAction(){
        try {
            // Get the values from the text fields and combo boxes
            LocalDate startLocal = startDate.getValue();
            int startHour = startHourCombo.getSelectionModel().getSelectedItem();
            int startMinute = startMinuteCombo.getSelectionModel().getSelectedItem();
            int endHour = endHourCombo.getSelectionModel().getSelectedItem();
            int endMinute = endMinuteCombo.getSelectionModel().getSelectedItem();
            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();

            startCal.setTimeZone(currentTimezone);
            endCal.setTimeZone(currentTimezone);


            startCal.set(startLocal.getYear(), startLocal.getMonthValue() - 1, startLocal.getDayOfMonth(), startHour, startMinute, 0);
            endCal.set(startLocal.getYear(), startLocal.getMonthValue() - 1, startLocal.getDayOfMonth(), endHour, endMinute, 0);


            /*
            Automatically adjusts the date based on the hour and minute. If the time of the end datetime is greater than
            the time of the start datetime, than assume date for end datetime is the next day of start date.
             */
            if (endHour == startHour){
                if (startMinute >= endMinute){
                    endCal.add(Calendar.HOUR_OF_DAY, 24);
                }
            }
            else if (endHour < startHour){
                endCal.add(Calendar.HOUR_OF_DAY, 24);
            }


            int apptId = -1;
            int contactId = contactIdCombo.getSelectionModel().getSelectedItem().getContactId();
            int customerId = Integer.parseInt(customerIdField.getText());
            int userId = Integer.parseInt(userIdField.getText());
            String title = titleField.getText();
            String location = locationField.getText();
            String type = typeField.getText();
            String description = descriptionField.getText();

            // If modifying, set Appointment ID to the modifying Appointment object
            if (isModify){
                apptId = Integer.parseInt(apptIdField.getText());
            }

            // Checks whether given customer ID exists
            if (!Database.customerIdExists(customerId)){
                throw new IllegalArgumentException("Customer ID does not exist in database");
            }
            // Checks whether given user ID exists
            if (!Database.userIdExists(userId)){
                throw new IllegalArgumentException("User ID does not exist in the database");
            }
            // Checks whether the time is valid for the EST office hours
            if (!validateTime(startCal, endCal)){
                throw new IllegalArgumentException("Start or End time is not within Office Hours");
            }

            // Checks whether the given time overlaps with any other appointments
            if (Database.hasAppointmentOverlap(startCal, endCal, apptId)){
                throw new IllegalArgumentException("This time overlaps with another appointment");
            }

            startCal.setTimeZone(currentTimezone);
            endCal.setTimeZone(currentTimezone);

            startCal.set(Calendar.ZONE_OFFSET, currentTimezone.getRawOffset());
            startCal.set(Calendar.DST_OFFSET, currentTimezone.getDSTSavings());
            endCal.set(Calendar.ZONE_OFFSET, currentTimezone.getRawOffset());
            endCal.set(Calendar.DST_OFFSET, currentTimezone.getDSTSavings());

            // Asks for confirmation whether or not to save the Appointment of the given information
            Alert confirmBox = new Alert(Alert.AlertType.CONFIRMATION);
            confirmBox.setTitle("Confirm Box");
            confirmBox.setContentText("Would you like to save this Appointment?");
            Optional<ButtonType> confirmResult = confirmBox.showAndWait();
            if (confirmResult.get() == ButtonType.OK) {
                // Make a new appointment object based on the given information in the text fields
                Appointment someAppointment = new Appointment(apptId, title, description, location, contactId, type, startCal, endCal, customerId, userId);
                // Do this if the Appointment is to be modified
                if (isModify) {
                    Database.modifyAppointment(someAppointment);
                }
                // Do this if the Appointment is to be added
                else {
                    Database.addAppointment(someAppointment);
                }
                // Close the Appointment input form
                closeAction();
            }
        }
        catch (NumberFormatException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Error Box");
            errorBox.setContentText("ERROR: Invalid Text Field Value");
            errorBox.showAndWait();
        }
        catch (NullPointerException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Error Box");
            errorBox.setContentText("ERROR: Fill out all the fields");
            errorBox.showAndWait();
        }
        catch (SQLException e){
            e.printStackTrace();
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        catch (IllegalArgumentException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Error Box");
            errorBox.setContentText("ERROR: "+e.getMessage());
            errorBox.showAndWait();
        }
    }

    /**
     * FXML Action method that closes the Appointment Input form. Does not exit the software.
     */
    @FXML
    private void closeAction(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Method to check whether the given times are within operating business hours. EST 8am - 10pm.
     * @param startTime the Calendar object of when the appointment starts
     * @param endTime the Calendar object of when the appointment ends
     * @return a boolean of whether the given time is valid of not. valid = true
     */
    private boolean validateTime(Calendar startTime, Calendar endTime){

        TimeZone estTimeZone = TimeZone.getTimeZone("America/New_York");

        int offset_hours = (estTimeZone.getRawOffset() - currentTimezone.getRawOffset())/3600000;

        int estOfficeHourStart = 8;
        int estOfficeHourEnd = 22;

        int estInputStartHour = startTime.get(Calendar.HOUR_OF_DAY) + offset_hours;
        int estInputEndHour = endTime.get(Calendar.HOUR_OF_DAY) + offset_hours;

        if (estInputStartHour < estOfficeHourStart || estInputEndHour >= estOfficeHourEnd){
            return false;
        }
        if (estInputStartHour == estInputEndHour){
            if (startTime.get(Calendar.MINUTE) >= endTime.get(Calendar.MINUTE)){
                return false;
            }
        }
        if (estInputStartHour > estInputEndHour) {
            return false;
        }
        return true;
    }
}