import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * The Controller class for the customer home form. Customer information can be viewed by location.
 */
public class CustomerHomeController{

    private ObservableList<Customer> filteredCustomerList = FXCollections.observableArrayList();

    @FXML private TableView<Customer> tableViewCustomer;
    @FXML private TableColumn<Customer, Integer> tableColumnId;
    @FXML private TableColumn<Customer, String> tableColumnName;

    @FXML private ComboBox<Country> countryCombo;
    @FXML private ComboBox<Division> divisionCombo;

    @FXML private Button addButton;
    @FXML private Button modifyButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;

    @FXML private TextField idTextField;
    @FXML private TextField nameTextField;
    @FXML private TextField addressTextField;
    @FXML private TextField postalTextField;
    @FXML private TextField phoneTextField;

    /**
     * FXML initialize method is ran automatically when the Customer Home form is called
     */
    @FXML
    private void initialize(){

        // Sets Customer Table's column values
        tableColumnId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Sets the country combo box
        countryCombo.setItems(Database.getAllCountries());
        tableViewCustomer.setItems(filteredCustomerList);

        /* LAMBDA FUNCTION. Create a listener for the country combo box. Sets new values for the First Division combo box based on the
         Selected Country.
         */
        countryCombo.getSelectionModel().selectedItemProperty().addListener(( c -> {
            try {
                ObservableList<Division> divisions = Database.getDivision(countryCombo.getSelectionModel().getSelectedItem().getCountryId());
                divisionCombo.setItems(divisions);
                divisionCombo.getSelectionModel().selectFirst();
            }
            catch (NullPointerException e){
                System.out.println(e.getMessage());
            }
        }));

        /* LAMBDA FUNCTION. Create a listener for the first division combo box. Updates the Customer Table based
         on the country and the first division values.
         */
        divisionCombo.getSelectionModel().selectedItemProperty().addListener(( c -> {
            updateFilteredData();
        }));

        /*
        LAMBDA FUNCTION. Updates the customer detailed informational boxes on the right of the box based on the
        selected row in the Customer table.
         */
        tableViewCustomer.getSelectionModel().selectedItemProperty().addListener(( c -> {
            Customer selectedCustomer = tableViewCustomer.getSelectionModel().getSelectedItem();
            showCustomerData(selectedCustomer);
        }));
    }

    /**
     * Populates the customer detailed information text boxes on the right side of the form
     * @param selectedCustomer A Customer object to display information
     */
    private void showCustomerData(Customer selectedCustomer){
        if (selectedCustomer != null) {
            idTextField.setText(String.valueOf(selectedCustomer.getCustomerId()));
            nameTextField.setText(selectedCustomer.getName());
            addressTextField.setText(selectedCustomer.getAddress());
            postalTextField.setText(selectedCustomer.getPostalCode());
            phoneTextField.setText(selectedCustomer.getPhoneNumber());
        }
    }

    /**
     * Updates the Customer Table based on the Country and the First Division Data. Re-queries Customer Database
     */
    private void updateFilteredData() {
        try {
            filteredCustomerList = Database.getCustomerList(divisionCombo.getSelectionModel().getSelectedItem().getDivisionId());
            tableViewCustomer.setItems(filteredCustomerList);
            reapplyTableSortOrder();
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            ;
        }
    }

    /**
     * Sorts the Customer Table
     */
    private void reapplyTableSortOrder() {
        ArrayList<TableColumn<Customer, ?>> sortOrder = new ArrayList<>(tableViewCustomer.getSortOrder());
        tableViewCustomer.getSortOrder().clear();
        tableViewCustomer.getSortOrder().addAll(sortOrder);
        tableViewCustomer.getSelectionModel().selectFirst();
    }

    /**
     * FXML Action method that opens the customer input form as add
     */
    @FXML
    private void addCustomer(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CustomerInput.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root1));
            stage.showAndWait();
            updateFilteredData();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * FXML Action method that opens the customer input form as modify. Passes the selected Customer row to modify customer.
     */
    @FXML
    private void modifyCustomer(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CustomerInput.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root1));
            CustomerInputController controller = fxmlLoader.getController();
            // Passes the selected Customer row into customer input form
            controller.setCustomer(tableViewCustomer.getSelectionModel().getSelectedItem(),
                    countryCombo.getSelectionModel().getSelectedItem(),
                    divisionCombo.getSelectionModel().getSelectedItem());
            stage.showAndWait();
            updateFilteredData();
        } catch (NullPointerException e){
            ;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * FXML Action method to deleted the selected customer row. Does not delete if customer is associated with an
     *  appointment. A confirmation popup box asks to confirm the delete action.
     */
    @FXML
    private void deleteButton(){
        try{
            Customer selectedCustomer = tableViewCustomer.getSelectionModel().getSelectedItem();
            if (Database.customerHasAppointment(selectedCustomer.getCustomerId())){
                Alert errorBox = new Alert(Alert.AlertType.ERROR);
                errorBox.setTitle("Error Box");
                errorBox.setContentText("Customer is associated with an appointment");
                errorBox.showAndWait();
            }
            else {
                Alert confirmBox = new Alert(Alert.AlertType.CONFIRMATION);
                confirmBox.setTitle("Confirm Box");
                confirmBox.setContentText("Would you like to delete this customer record?");
                Optional<ButtonType> confirmResult = confirmBox.showAndWait();

                if (confirmResult.get() == ButtonType.OK) {
                    Database.deleteCustomer(selectedCustomer.getCustomerId());
                }
            }
        } catch (NullPointerException e){
            ;
        } catch (SQLException e){
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
        updateFilteredData();
    }

    /**
     * Changes scene back to the Main Menu Form.
     */
    @FXML
    private void backButton(){
        try {
            // Change login screen to appointment management system home screen
            Stage stage = (Stage) backButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
            stage.setScene(new Scene(root));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
