import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Controller class for the Main Menu Form. Contains the appointment list table and directory buttons.
 */
public class MainMenuController {

    private ObservableList<Appointment> filteredAppointmentList = FXCollections.observableArrayList();

    @FXML ComboBox<Integer> contactIdCombo;
    @FXML Pane menuPane;
    @FXML Label timezoneLabel;
    @FXML Button customerButton;
    @FXML Button closeButton;

    @FXML RadioButton weeklyToggle;
    @FXML RadioButton monthlyToggle;

    @FXML ToggleGroup calenderToggle;

    java.text.SimpleDateFormat sdf =
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
    final TimeZone currentTimezone = TimeZone.getTimeZone(ZoneId.systemDefault());

    @FXML TableView<Appointment> appointmentTableView;
    @FXML TableColumn<Appointment, Calendar> startDateColumn;
    @FXML TableColumn<Appointment, Calendar> endDateColumn;
    @FXML TableColumn<Appointment, Integer> appointmentIdColumn;
    @FXML TableColumn<Appointment, String> titleColumn;
    @FXML TableColumn<Appointment, String> descriptionColumn;
    @FXML TableColumn<Appointment, String> locationColumn;
    @FXML TableColumn<Appointment, String> contactColumn;
    @FXML TableColumn<Appointment, String> typeColumn;
    @FXML TableColumn<Appointment, Integer> customerIdColumn;

    /**
     * The FXML initialize method is ran automatically when the Main Menu form is called.
     */
    @FXML
    private void initialize(){

        // Set the Timezone Information using the device's local timezone
        timezoneLabel.setText(currentTimezone.getDisplayName());
        timezoneLabel.layoutXProperty().bind(
                menuPane.widthProperty().subtract(timezoneLabel.widthProperty()).divide(2));

        // Set Appointment table's column cell value types.
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<Appointment, Calendar>("start"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<Appointment, Calendar>("end"));

        // Override the display value for Calendar object types
        startDateColumn.setCellFactory(col -> new TableCell<Appointment, Calendar>() {
            @Override
            public void updateItem(Calendar item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText(null);
                } else {
                    item.setTimeZone(currentTimezone);
                    setText(getLocalTimeString(item));
                }
            }
        });
        endDateColumn.setCellFactory(col -> new TableCell<Appointment, Calendar>() {
            @Override
            public void updateItem(Calendar item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText(null);
                } else {
                    setText(getLocalTimeString(item));
                }
            }
        });
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactId"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        // Initialize Toggle Group and Toggles for the Appointment Table Filter (Weekly/Monthly)
        calenderToggle = new ToggleGroup();

        weeklyToggle.setToggleGroup(calenderToggle);
        monthlyToggle.setToggleGroup(calenderToggle);

        weeklyToggle.setSelected(true);
        calenderToggle.selectedToggleProperty().addListener(( c -> {
            updateCalender();
        }));

        // Initialize Contact ID combo box for the report generator
        ObservableList<Contact> contactList = FXCollections.observableArrayList();
        ObservableList<Integer> contactIntList = FXCollections.observableArrayList();
        try {
            contactList = Database.getAllContact();
        } catch (SQLException e){
            System.out.println("SQL Error on retrieving contact");
        }
        if (contactList.size() != 0){
            for (int i=0;i<contactList.size();i++){
                contactIntList.add(contactList.get(i).getContactId());
            }
        }
        contactIdCombo.setItems(contactIntList);

        // Update the Calendar
        updateCalender();
    }

    /**
     * A method to return a string of the local time based on the given calendar
     * @param someCalender A Calendar object of any timezone
     * @return A String of local time based on the someCalendar's set time
     */
    private String getLocalTimeString(Calendar someCalender){
        someCalender.setTimeZone(currentTimezone);
        return sdf.format(someCalender.getTime());
    }

    /**
     * Updates the appointment table from the appointments database
     */
    private void updateCalender(){
        try {
            Calendar currentTime = Calendar.getInstance();
            RadioButton selectedToggle= (RadioButton)calenderToggle.getSelectedToggle();
            if (selectedToggle.getText().equals("Weekly")) {
                // Get the appointments in the next 168 hours = 7 days
                filteredAppointmentList = Database.getAllTimedAppointment(currentTime, 168); // 168
                appointmentTableView.setItems(filteredAppointmentList);
                reapplyTableSortOrder();
            }
            else if (selectedToggle.getText().equals("Monthly")){
                // Get the appointments in the next 720 hours = 1 month
                filteredAppointmentList = Database.getAllTimedAppointment(currentTime, 720); // 720
                appointmentTableView.setItems(filteredAppointmentList);
                reapplyTableSortOrder();
            }
        }
        catch (SQLException e){
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        catch (NullPointerException e){
            ;
        }
    }

    /**
     * Applies a sort to the appointments table
     */
    private void reapplyTableSortOrder() {
        ArrayList<TableColumn<Appointment, ?>> sortOrder = new ArrayList<>(appointmentTableView.getSortOrder());
        appointmentTableView.getSortOrder().clear();
        appointmentTableView.getSortOrder().addAll(sortOrder);
        appointmentTableView.getSelectionModel().selectFirst();
    }

    /**
     * FXML Action method that changes the current scene from the main menu form to the Customer manager form
     */
    @FXML
    private void changeToCustomerScreen(){
        try {
            // Change login screen to appointment management system home screen
            Stage stage = (Stage) customerButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("CustomerScreen.fxml"));
            stage.setScene(new Scene(root));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Closes the main menu form and exits the program
     */
    @FXML
    private void closeAction(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * FXML Action method that opens the input appointment form in add mode as a popup form
     */
    @FXML
    private void addAppointment(){
        try {
            // Initialize and open the input appointment form
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AppointmentInput.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root1));
            stage.showAndWait();
            updateCalender();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * FXML Action method that opens the input appointment form as modify mode, passing the selected Appointment object,
     * as a popup form
     */
    @FXML
    private void modifyAppointment(){
        try {
            // Initialize and open the input appointment form
            Appointment selectedAppt = appointmentTableView.getSelectionModel().getSelectedItem();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AppointmentInput.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root1));
            AppointmentInputController controller = fxmlLoader.getController();
            // Add the selected Appointment object to the input appointment form
            controller.initModify(selectedAppt);
            stage.showAndWait();
            updateCalender();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * FXML Action method which deletes the selected Appointment in the Appointments table. A popup to confirm the delete
     * action will ask for confimation. If no is selected, the delete process will not occur.
     */
    @FXML
    private void deleteAppointment(){
        try {
            // Get selected Appointment object in the Appointment Table
            Appointment selectedAppt = appointmentTableView.getSelectionModel().getSelectedItem();
            // Create and show the confirm box
            Alert confirmBox = new Alert(Alert.AlertType.CONFIRMATION);
            confirmBox.setTitle("Confirm Box");
            confirmBox.setContentText("Would you like to delete this appointment?\nID: "+selectedAppt.getAppointmentId()+"\nType: "+selectedAppt.getType());
            Optional<ButtonType> confirmResult = confirmBox.showAndWait();
            if (confirmResult.get() == ButtonType.OK) {
                // Deletes the selected Appointment from the database
                Database.deleteAppointment(selectedAppt.getAppointmentId());
                // Update the Appointment Table without the deleted Appointment
                updateCalender();
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            // Does nothing because this happens when no appointment is selected in the Appointment table
            ;
        }
    }

    /**
     * FXML Action method that shows an informational popup giving a report of the "count by month, type: of all
     * appointments in the database
     */
    @FXML
    private void TypeMonthAppointment(){
        try {
            Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
            infoBox.setTitle("Appointment Month-by-Type Report");
            infoBox.setContentText(Database.generateMonthType());
            infoBox.show();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * FXML Action method that shows an informational popup giving a report of the schedule for the selected contact ID.
     * If no contact ID is selected, then nothing will happen.
     */
    @FXML
    private void contactSchedule(){
        try {
            int selectedContactId = contactIdCombo.getSelectionModel().getSelectedItem();
            Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
            infoBox.setTitle("Schedule by Contact ID Report");
            infoBox.setContentText(Database.generateContactSchedule(selectedContactId));
            infoBox.show();

        } catch (NullPointerException e){
            ;
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * FXML Action method that shows an informational popup giving a report of "count by location" of all appointments
     * in the database. This is the custom report functionality for the section A3F.
     */
    @FXML
    private void locationTotal(){
        try {
            Alert infoBox = new Alert(Alert.AlertType.INFORMATION);
            infoBox.setTitle("Total Appointments by Location Report");
            infoBox.setContentText(Database.generateLocationTotal());
            infoBox.show();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
}
