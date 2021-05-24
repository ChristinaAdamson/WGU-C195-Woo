import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

/**
 * The Controller Class for the Customer Input Form. Displays input boxes to create or modify a customer.
 */
public class CustomerInputController {

    // Sets the modify to 'false' initially. When modifying object is passed in setCustomer(*), modify is set to 'true'
    private boolean modify = false;

    @FXML Button saveButton;
    @FXML Button cancelButton;

    @FXML Label formTypeLabel;

    @FXML TextField idField;
    @FXML TextField nameField;
    @FXML TextField addressField;
    @FXML TextField phoneField;
    @FXML TextField postalField;
    @FXML ComboBox<Country> countryCombo;
    @FXML ComboBox<Division> divisionCombo;

    /**
     * The method to be called to pass the modifying customer object and populates input text boxes accordingly
     * @param customerObj the Customer object to be modified
     * @param countryObj the current Country object in the combo box
     * @param divisionObj the current Division object in the combo box
     */
    public void setCustomer(Customer customerObj, Country countryObj, Division divisionObj){
        idField.setText(String.valueOf(customerObj.getCustomerId()));
        nameField.setText(customerObj.getName());
        addressField.setText(customerObj.getAddress());
        postalField.setText(customerObj.getPostalCode());
        phoneField.setText(customerObj.getPhoneNumber());
        countryCombo.getSelectionModel().select(countryObj);
        divisionCombo.getSelectionModel().select(divisionObj);
        // Sets the global variable 'modify' to true.
        modify = true;
    }

    /**
     * FXML initialize is ran automatically when the Customer Input form is called
     */
    @FXML
    private void initialize(){
        countryCombo.setItems(Database.getAllCountries());

        countryCombo.getSelectionModel().selectedItemProperty().addListener(( c -> {
            divisionCombo.setItems(Database.getDivision(countryCombo.getSelectionModel().getSelectedItem().getCountryId()));
            divisionCombo.getSelectionModel().selectFirst();
        }));
    }

    /**
     * FXML Action method that initializes the save process for the inputted text in the text fields
     */
    @FXML
    private void saveAction(){
        try {
            // Get the values from the input text fields
            String name = nameField.getText();
            String address = addressField.getText();
            String phone = phoneField.getText();
            String postalCode = postalField.getText();
            int divisionId = divisionCombo.getSelectionModel().getSelectedItem().getDivisionId();

            // Generate an error popup if not all text fields are filled out
            if (name.equals("") || address.equals("") || phone.equals("") || postalCode.equals("")){
                Alert errorBox = new Alert(Alert.AlertType.ERROR);
                errorBox.setTitle("Error Box");
                errorBox.setContentText("ERROR: Fill out all the fields");
                errorBox.showAndWait();
            }
            else {
                // Confirmation Box to confirm whether to add/modify a customer with the filled in information
                Alert confirmBox = new Alert(Alert.AlertType.CONFIRMATION);
                confirmBox.setTitle("Confirm Box");
                confirmBox.setContentText("Would you like to save this Customer?");
                Optional<ButtonType> confirmResult = confirmBox.showAndWait();

                // Do this if adding customer
                if ((confirmResult.get() == ButtonType.OK) && !this.modify){
                    Customer someCustomer = new Customer(-1, name, address, postalCode, phone, divisionId);
                    try {
                        boolean resultBool = Database.addCustomer(someCustomer);
                        if (!resultBool){
                            throw new IllegalArgumentException("Unexpected Argument in Modifying Customer Data");
                        }
                    } catch (SQLException e){
                        System.out.println(e.getErrorCode());
                        System.out.println(e.getMessage());
                    }
                    Stage stage = (Stage) saveButton.getScene().getWindow();
                    stage.close();
                }
                // Do this if modifying customer
                else if ((confirmResult.get() == ButtonType.OK)){
                    try {
                        int id = Integer.parseInt(idField.getText());
                        Customer someCustomer = new Customer(id, name, address, postalCode, phone, divisionId);
                        Database.modifyCustomer(someCustomer);
                        Stage stage = (Stage) cancelButton.getScene().getWindow();
                        stage.close();
                    } catch (SQLException e){
                        System.out.println(e.getErrorCode());
                        System.out.println(e.getMessage());
                    }
                }
            }

        } catch (NullPointerException e){
            Alert errorBox = new Alert(Alert.AlertType.ERROR);
            errorBox.setTitle("Error Box");
            errorBox.setContentText("ERROR: Fill out all Fields");
            errorBox.showAndWait();
        }
    }

    /**
     * FXML Action method that closes the customer input form. Does not close the software.
     */
    @FXML
    private void cancelAction(){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
