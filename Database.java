import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;

import java.sql.*;
import java.sql.Date;
import java.util.TimeZone;

/**
 * The class that operates on the MySQL database to update and query information to and from the MySQL database
 */
public class Database {
    private static Connection connect = null;
    private static Statement statement = null;
    private static final String databaseName = "WJ08dDP";
    private static String username;

    private static ObservableList<Country> allCountries;
    private static ObservableList<Division> allDivisions;

    public static void initializeDatabase() throws Exception {
        try {
            username = "temp";
            // This will load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            String dbUsername = "U08dDP";
            String dbPassword = "53689259048";

            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://wgudb.ucertify.com/" + databaseName, dbUsername, dbPassword);
            statement = connect.createStatement();

            // Initialize the country and division observable arraylists
            allCountries = FXCollections.observableArrayList();
            allDivisions = FXCollections.observableArrayList();
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
        }
    }

    /**
     * Sets the database username to reflect the user of the Appointment Manager Program
     * @param dbUser The String form of the user's username
     */
    public static void setDatabaseUser(String dbUser){
        username = dbUser;
    }

    /**
     * Filters the Country list to include countries that are eligible for appointment operation.
     */
    private static void filterKnownCountries(){
        ObservableList<Country> newCountries = FXCollections.observableArrayList();
        for (int i=0;i<allCountries.size();i++){
            String countryName = allCountries.get(i).getCountryName();
            if (countryName.equals("United States") ||
                    countryName.equals("United Kingdom") ||
                    countryName.equals("Canada"))
            newCountries.add(allCountries.get(i));
        }
        allCountries = newCountries;
    }

    /**
     * Pre-populates all the Countries and First-Division data from the database.
     * @throws SQLException Throws SQL Exception for SQL Errors
     */
    public static void prepopulateLocations() throws SQLException{
        // Pre-populate all Countries
        ResultSet resultSet = statement.executeQuery("SELECT Country_ID, Country FROM "+databaseName+".countries;");
        while (resultSet.next()){
            allCountries.add(new Country(
                    resultSet.getInt("Country_ID"),
                    resultSet.getString("Country")
            ));
        }
        filterKnownCountries();

        // Pre-populate all Divisions
        resultSet = statement.executeQuery("SELECT Division_ID, Division, Country_ID FROM "+databaseName+".first_level_divisions;");
        while (resultSet.next()){
            allDivisions.add(new Division(
                    resultSet.getInt("Division_ID"),
                    resultSet.getString("Division"),
                    resultSet.getInt("Country_ID")
            ));
        }
        resultSet.close();
    }

    /**
     * Gets all the pre-populated Countries list
     * @return An ObservableList of all Country Object Types
     */
    public static ObservableList<Country> getAllCountries(){
        return allCountries;
    }

    /**
     * Gets all the pre-populated First-Division list
     * @return An ObservableList of all Division Object Types
     */
    public static ObservableList<Division> getAllDivisions(){
        return allDivisions;
    }

    /**
     * Gets all division for a given Country ID
     * @param country_id the Unique ID of the selecting Country
     * @return An ObservableList of all Division Object Types
     */
    public static ObservableList<Division> getDivision(int country_id){
        ObservableList<Division> divisionList = FXCollections.observableArrayList();
        for (int i=0; i<allDivisions.size();i++){
            if (allDivisions.get(i).getCountryId() == country_id){
                divisionList.add(allDivisions.get(i));
            }
        }
        return divisionList;
    }

    /**
     * Used for testing. Returns the MetaData for a given table in the database
     * @param tableName The String of the table name in the database
     * @return The String of the MetaData information
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static String getTableMetadata(String tableName) throws SQLException{
        ResultSet resultSet = statement.executeQuery("SELECT * FROM "+databaseName+"."+tableName+";");
        return String.valueOf(resultSet.getMetaData());
    }

    /**
     * Used for testing. Gets the data of all rows in the MySQL Table
     * @param tableName The String of the table name in the database
     * @param columns An ArrayList of the columns to retrieve in the database table
     * @return An ArrayList of any Object
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static ArrayList<ArrayList<Object>> getTableData(String tableName, ArrayList<String> columns) throws SQLException{
        ArrayList<ArrayList<Object>> fullData = new ArrayList<>();
        ResultSet dataResult = statement.executeQuery("SELECT * FROM "+databaseName+"."+tableName+";");
        while (dataResult.next()){
            ArrayList<Object> row = new ArrayList<>();
            for (int i=0;i<columns.size();i++){
                row.add(dataResult.getObject(columns.get(i)));
            }
            fullData.add(row);
        }
        return fullData;
    }

    /**
     * Gets a Customer Object for the given Customer ID
     * @param customerId The unique ID of a customer
     * @return A Customer Object
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static Customer getCustomer(int customerId) throws SQLException{
        ResultSet customerResult = statement.executeQuery("SELECT * FROM "+
                databaseName+".customers WHERE Customer_ID = "+customerId+";");
        if (customerResult.next()) {
            return new Customer(
                    customerResult.getInt("Customer_ID"),
                    customerResult.getString("Customer_Name"),
                    customerResult.getString("Address"),
                    customerResult.getString("Postal_Code"),
                    customerResult.getString("Phone"),
                    customerResult.getInt("Division_ID")
            );
        }
        else {
            customerResult.close();
            throw new SQLException("Customer ID doesn't exist in DB");
        }
    }

    /**
     * Gets a list of Customer for the given Division ID
     * @param divisionId A unique division ID
     * @return An ObservableList of Customer Objects
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static ObservableList<Customer> getCustomerList(int divisionId) throws SQLException{
        ObservableList<Customer> customerList = FXCollections.observableArrayList();
        ResultSet customerResult = statement.executeQuery("SELECT * FROM "+
                databaseName+".customers WHERE Division_ID ="+
                divisionId +";");
        while (customerResult.next()) {
            customerList.add(new Customer(
                    customerResult.getInt("Customer_ID"),
                    customerResult.getString("Customer_Name"),
                    customerResult.getString("Address"),
                    customerResult.getString("Postal_Code"),
                    customerResult.getString("Phone"),
                    customerResult.getInt("Division_ID")
            ));
        }
        return customerList;
    }

    /**
     * Get the Contact object for the given Contact ID
     * @param contactId A unique contact ID
     * @return A Contact Object of the given ID
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static Contact getContact(int contactId) throws SQLException{
        ResultSet contactResult = statement.executeQuery("SELECT Contact_ID, Contact_Name, Email FROM "+
                databaseName+".contacts WHERE Contact_ID ="+
                contactId +";");
        if (contactResult.next()){
            return new Contact(
                    contactResult.getInt("Contact_ID"),
                    contactResult.getString("Contact_Name"),
                    contactResult.getString("Email")
            );
        }else{
            throw new SQLException("Contact ID doesn't exist in DB");
        }
    }

    /**
     * Used for adding a Contact to the Database. For some reason, Contacts are not pre-populated in the database
     *  contrary to the information given in the directions sheet.
     * @param newContact A Contact object
     * @return A boolean whether or not the update was successful.
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean addContact(Contact newContact) throws SQLException{

        String query = "INSERT INTO "+databaseName+".contacts "+
                "(Contact_Name, Email) values ('"+
                newContact.getContactName()+"', '"+
                newContact.getEmail()+"');";
        int rowsChanged = statement.executeUpdate(query);
        return rowsChanged != 0 && rowsChanged != -1;
    }

    /**
     * Get all Contacts in the database
     * @return ObservableList of Contact objects
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static ObservableList<Contact> getAllContact() throws SQLException{
        ObservableList<Contact> contactList = FXCollections.observableArrayList();
        ResultSet contactResult = statement.executeQuery("SELECT Contact_ID, Contact_Name, Email FROM "+
                databaseName+".contacts ORDER BY Contact_ID ASC;");
        while (contactResult.next()) {
            contactList.add(new Contact(
                    contactResult.getInt("Contact_ID"),
                    contactResult.getString("Contact_Name"),
                    contactResult.getString("Email"))
            );
        }
        return contactList;
    }

    /**
     * Adds the customer to the database given the passed Customer object
     * @param newCustomer The Customer object to be added to the database
     * @return Whether or not the update was successful
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean addCustomer(Customer newCustomer) throws SQLException{
        String name = newCustomer.getName();
        String address = newCustomer.getAddress();
        String postalCode= newCustomer.getPostalCode();
        String phoneNumber = newCustomer.getPhoneNumber();
        int divisionId = newCustomer.getDivisionId();

        // Get String of UTC Time
        Calendar currentCal = Calendar.getInstance();
        java.sql.Date javaSqlDate = new java.sql.Date(currentCal.getTime().getTime());
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = sdf.format(javaSqlDate);

        String query =
                "INSERT INTO "+
                        databaseName+
                        ".customers (Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By, Division_ID)" +
                        " values ('"+
                        name + "', '"+
                        address + "', '"+
                        postalCode + "', '"+
                        phoneNumber + "', '"+
                        currentTime + "', '"+
                        username + "', '"+
                        currentTime + "', '"+
                        username + "', "+
                        divisionId +
                        ");";
        int rowsChanged = statement.executeUpdate(query);
        return rowsChanged != 0 && rowsChanged != -1;
    }

    /**
     * Modifies the Customer given the passed Customer object.
     * @param modifyingCustomer The customer object to be modified
     * @return Whether the update operation was successful
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean modifyCustomer(Customer modifyingCustomer) throws SQLException{
        int customerId = modifyingCustomer.getCustomerId();
        String name = modifyingCustomer.getName();
        String address = modifyingCustomer.getAddress();
        String postalCode= modifyingCustomer.getPostalCode();
        String phoneNumber = modifyingCustomer.getPhoneNumber();
        int divisionId = modifyingCustomer.getDivisionId();

        // Get String of Current Time
        Calendar currentCal = Calendar.getInstance();
        java.sql.Date javaSqlDate = new java.sql.Date(currentCal.getTime().getTime());
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = sdf.format(javaSqlDate);

        String query =
                " UPDATE " + databaseName+".customers SET "+
                        "Customer_Name = '" + name +
                        "', Address = '" + address +
                        "', Postal_Code = '" + postalCode +
                        "', Phone = '" + phoneNumber +
                        "', Last_Update = '" + currentTime +
                        "', Last_Updated_By = '" + username +
                        "', Division_ID = " + divisionId +
                        " WHERE Customer_ID = "+customerId+";";
        int rowsChanged = statement.executeUpdate(query);
        return rowsChanged != 0;
    }

    /**
     * Deletes the given Custoemr in the database given the unique customer ID
     * @param customerId the Unique customer ID
     * @return whether or not the operation was successful
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean deleteCustomer(int customerId) throws SQLException{
        int rowsChanged = statement.executeUpdate("DELETE FROM "+
                databaseName+".customers WHERE Customer_ID ="+
                customerId +";");
        return rowsChanged != 0 && rowsChanged != -1;
    }

    /**
     * Get all appointments in the time frame given. Current Time frame to number of hours after current time.
     * @param currentTime The Calendar object of current time when operation was called
     * @param hours_ahead The number of hours ahead to grab appointments from.
     * @return An ObservableList of Appointment objects
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static ObservableList<Appointment> getAllTimedAppointment(Calendar currentTime, int hours_ahead) throws SQLException{
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
        ResultSet appointmentResult = statement.executeQuery("SELECT * FROM "+
                databaseName+".appointments ORDER BY start ASC;");
        TimeZone currentTimezone = TimeZone.getTimeZone(ZoneId.systemDefault());

        Timestamp startTime = new java.sql.Timestamp(currentTime.getTimeInMillis());
        currentTime.add(Calendar.HOUR_OF_DAY, hours_ahead);
        Timestamp endTime = new java.sql.Timestamp(currentTime.getTimeInMillis());

        while (appointmentResult.next()){
            Timestamp resultEndTime = appointmentResult.getTimestamp("End");
            Timestamp resultStartTime = appointmentResult.getTimestamp("Start");

            if (resultEndTime.after(startTime) && resultStartTime.before(endTime)) {
                int apptId = appointmentResult.getInt("Appointment_ID");
                String title = appointmentResult.getString("Title");
                String description = appointmentResult.getString("Description");
                String location = appointmentResult.getString("Location");
                String type = appointmentResult.getString("Type");
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();

                start.setTimeInMillis(resultStartTime.getTime());
                end.setTimeInMillis(resultEndTime.getTime());

                start.add(Calendar.HOUR_OF_DAY, (currentTimezone.getRawOffset())/3600000);
                start.add(Calendar.HOUR_OF_DAY, (currentTimezone.getDSTSavings())/3600000);

                end.add(Calendar.HOUR_OF_DAY, (currentTimezone.getRawOffset())/3600000);
                end.add(Calendar.HOUR_OF_DAY, (currentTimezone.getDSTSavings())/3600000);

                int customerId = appointmentResult.getInt("Customer_ID");
                int userId = appointmentResult.getInt("User_ID");
                int contactId = appointmentResult.getInt("Contact_ID");
                appointmentList.add(new Appointment(apptId, title, description, location, contactId, type, start, end, customerId, userId));
            }
        }
        return  appointmentList;
    }

    /**
     * Get an Appointment object from the Database based on the given appointment ID
     * @param appointmentId unique Appointment ID
     * @return An Appointment object according to the appointment ID
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static Appointment getAppointment(int appointmentId) throws SQLException{
        ResultSet appointmentResult = statement.executeQuery("SELECT * FROM "+
                databaseName+".appointments WHERE Appointment_ID="+appointmentId+";");
        TimeZone utcTime = TimeZone.getTimeZone("UTC");

        if (appointmentResult.next()) {
            int apptId = appointmentResult.getInt("Appointment_ID");
            String title = appointmentResult.getString("Title");
            String description = appointmentResult.getString("Description");
            String location = appointmentResult.getString("Location");
            String type = appointmentResult.getString("Type");

            TimeZone currentTimezone = TimeZone.getTimeZone(ZoneId.systemDefault());
            Timestamp resultEndTime = appointmentResult.getTimestamp("End");
            Timestamp resultStartTime = appointmentResult.getTimestamp("Start");
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            start.setTimeInMillis(resultStartTime.getTime() + currentTimezone.getRawOffset());
            end.setTimeInMillis(resultEndTime.getTime() + currentTimezone.getRawOffset());

            int customerId = appointmentResult.getInt("Customer_ID");
            int userId = appointmentResult.getInt("User_ID");
            int contactId = appointmentResult.getInt("Contact_ID");
            return new Appointment(apptId, title, description, location, contactId, type, start, end, customerId, userId);
        }
        else {
            throw new IllegalArgumentException("Appointment ID does not exist");
        }
    }

    /**
     * Deletes an Appointment from the database based on the given Appointment ID
     * @param appointmentId The unique Appointment ID
     * @return Whether or not the operation was successful
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean deleteAppointment(int appointmentId) throws SQLException{
        int rowsChanged = statement.executeUpdate("DELETE FROM "+
                databaseName+".appointments WHERE Appointment_ID ="+
                appointmentId +";");
        return rowsChanged != 0 && rowsChanged != -1;
    }

    /**
     * Modifies the Appointment information in the Database based on the given Appointment Object
     * @param modifiedAppointment The Appointment object to overwrite the information in the Database
     * @return Whether or not the operation was successful
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean modifyAppointment(Appointment modifiedAppointment) throws SQLException{
        int apptId = modifiedAppointment.getAppointmentId();
        String title = modifiedAppointment.getTitle();
        String description = modifiedAppointment.getDescription();
        String location = modifiedAppointment.getLocation();
        String type = modifiedAppointment.getType();
        Date start = new java.sql.Date(modifiedAppointment.getStart().getTime().getTime());
        Date end = new java.sql.Date(modifiedAppointment.getEnd().getTime().getTime());

        int customerId = modifiedAppointment.getCustomerId();
        int contactId = modifiedAppointment.getContactId();
        int userId = modifiedAppointment.getUserId();

        Calendar currentCal = Calendar.getInstance();
        java.sql.Date javaSqlDate = new java.sql.Date(currentCal.getTime().getTime());
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = sdf.format(javaSqlDate);

        String startString = sdf.format(start);
        String endString = sdf.format(end);


        String query =
                " UPDATE " + databaseName+".appointments SET "+
                        " Title = '" + title +
                        "', Description = '" + description +
                        "', Location = '" + location +
                        "', Type = '" + type +
                        "', Start = '" + startString +
                        "', End = '" + endString +
                        "', Customer_ID = " + customerId +
                        ", User_ID = '" + userId +
                        "', Contact_ID = " + contactId +
                        ", Last_Update = '" + currentTime +
                        "', Last_Updated_By = '" + username +
                        "' WHERE Appointment_ID = "+apptId+";";

        int rowsChanged = statement.executeUpdate(query);
        return rowsChanged != 0 && rowsChanged!=-1;
    }

    /**
     * Checks whether or not the given start and end time for an appointment overlaps with another appointment
     * @param startTime The calendar object for the Start time
     * @param endTime The Calendar object for the End time
     * @return A boolean for whether or not the given times overlap with any Appointments that
     * already exist in the Database. Overlaps = true
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean hasAppointmentOverlap(Calendar startTime, Calendar endTime, int appointmentId) throws SQLException{

        java.sql.Date start = new java.sql.Date(startTime.getTime().getTime());
        java.sql.Date end = new java.sql.Date(endTime.getTime().getTime());

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String startString = sdf.format(start);
        String endString = sdf.format(end);

        String query = "SELECT * FROM "+
                databaseName+".appointments WHERE ((Appointment_ID!="+String.valueOf(appointmentId)+") AND (start > '"+
                startString + "') AND (start < '"+
                endString+"')) OR ((end > '"+
                startString+"') AND (end < '"+
                endString+"') AND (Appointment_ID!="+String.valueOf(appointmentId)+")) OR ((Appointment_ID!="+String.valueOf(appointmentId)+
                ") AND (start = '"+startString+"') AND (end = '"+endString+"'));";
        ResultSet results = statement.executeQuery(query);

        return results.next();
    }

    /**
     * Checks if there is an Appointment in the Database that is starting in the next 15 minutes
     * @return The Appointment ID of the starting Appointment. -1 if no Appointments are starting.
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static int hasAppointmentIn15min() throws SQLException{
        Calendar currentTime = Calendar.getInstance();
        java.sql.Date current = new java.sql.Date(currentTime.getTime().getTime());
        currentTime.add(Calendar.MINUTE, 15);
        java.sql.Date after15 = new java.sql.Date(currentTime.getTime().getTime());
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String startString = sdf.format(current);
        String endString = sdf.format(after15);

        String query = "SELECT * FROM "+
                databaseName+".appointments WHERE ((start > '"+
                startString + "') AND (start < '"+
                endString+"')) OR ((end > '"+
                startString+"') AND (end < '"+
                endString+"')) ;";

        ResultSet results = statement.executeQuery(query);
        if (!results.next()){
            return -1;
        } else{
            return results.getInt("Appointment_ID");
        }

    }

    /**
     * Adds an Appointment to the Database based on the Appointment Object passed
     * @param newAppointment The Appointment Object to be added to the database
     * @return Whether or not the operation was successful
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean addAppointment(Appointment newAppointment) throws SQLException{
        String title = newAppointment.getTitle();
        String description = newAppointment.getDescription();
        String location = newAppointment.getLocation();
        String type = newAppointment.getType();
        java.sql.Date start = new java.sql.Date(newAppointment.getStart().getTime().getTime());
        java.sql.Date end = new java.sql.Date(newAppointment.getEnd().getTime().getTime());
        int customerId = newAppointment.getCustomerId();
        int contactId = newAppointment.getContactId();
        int userId = newAppointment.getUserId();

        Calendar currentCal = Calendar.getInstance();
        java.sql.Date javaSqlDate = new java.sql.Date(currentCal.getTime().getTime());
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = sdf.format(javaSqlDate);
        String startString = sdf.format(start);
        String endString = sdf.format(end);

        String query =
                "INSERT INTO "+
                        databaseName+
                        ".appointments (Title, Description, Location, Type, Start, End, Create_Date, Created_By, "+
                        "Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID)" +
                        " values ('"+
                        title + "', '"+
                        description + "', '"+
                        location + "', '"+
                        type + "', '"+
                        startString + "', '"+
                        endString + "', '"+
                        currentTime + "', '"+
                        username + "', '"+
                        currentTime + "', '"+
                        username + "', "+
                        customerId + ", "+
                        userId + ", "+
                        contactId +
                        ");";

        int rowsChanged = statement.executeUpdate(query);
        return rowsChanged != 0 && rowsChanged!=-1;
    }

    /**
     * Checks whether or not the given customer ID has an appointment in the Database. Only check appointments that
     * end after the current time.
     * @param customerId The unique Customer ID
     * @return A boolean on whether or not the customer has a valid appointment. Has an Appointment = true
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean customerHasAppointment(int customerId) throws SQLException{
        Calendar currentTime = Calendar.getInstance();
        java.sql.Date current = new java.sql.Date(currentTime.getTime().getTime());
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentString = sdf.format(current);

        ResultSet customerResult = statement.executeQuery("SELECT Appointment_ID FROM "+
                databaseName+".appointments WHERE Customer_ID ="+
                customerId +" AND end > '"+currentString+"' ;");
        return customerResult.next();
    }

    /**
     * Checks whether or not the Customer ID exist in the Database
     * @param customerId A Unique Customer ID to check the database
     * @return A boolean on whether or not the given Customer ID is in the database. Exist = true
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean customerIdExists(int customerId) throws SQLException{
        ResultSet customerResult = statement.executeQuery("SELECT Customer_ID FROM "+
                databaseName+".customers WHERE Customer_ID ="+
                customerId +";");
        return customerResult.next();
    }

    /**
     * Checks whether or not the User ID exist in the Database
     * @param userId A Unique User ID
     * @return A boolean on whether or not the given User ID is in the Database. Exist = true
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean userIdExists(int userId) throws SQLException{
        ResultSet userResults = statement.executeQuery("SELECT User_ID FROM "+
                databaseName+".users WHERE User_ID ="+
                userId +";");
        return userResults.next();
    }

    /**
     * Adds a User given the username and password.
     * @param user_name A String of the username. Must be unique
     * @param password A String of the password.
     * @return A boolean on whether the operation was successful
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static boolean addUser(String user_name, String password) throws SQLException{

        Calendar currentCal = Calendar.getInstance();
        java.sql.Date javaSqlDate = new java.sql.Date(currentCal.getTime().getTime());
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = sdf.format(javaSqlDate);

        String query = "INSERT INTO "+databaseName+".users "+
                "(User_Name, Password, Create_Date, Created_By, Last_Update, Last_Updated_By)"+
                "values ('"+
                user_name+"', '"+
                password+"', '"+
                currentTime+"', '"+
                username+"', '"+
                currentTime+"', '"+
                username+"');";
        int rowsChanged = statement.executeUpdate(query);
        return rowsChanged != 0 && rowsChanged!=-1;
    }

    /**
     * Get the user password of the given username
     * @param user_name The unique username
     * @return The String of the given username's password
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static String getUserPassword(String user_name) throws SQLException{
        String query = "SELECT Password FROM "+databaseName+".users WHERE User_Name = '"+user_name+"';";
        ResultSet userResults = statement.executeQuery(query);

        if (userResults.next()){
            return userResults.getString("Password");
        }
        else{
            throw new IllegalArgumentException("No User Exists");
        }
    }

    /**
     * Generates a multiline String Report of "count by month, type" for all Appointments in the Database
     * @return A String of the full report
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static String generateMonthType() throws SQLException{
        String someString = "Month, Type, Total\n-----------------------\n\n";
        String query = "SELECT count(Appointment_ID) AS total_count, Month(start) AS month, Type  FROM "+databaseName+".appointments GROUP BY Month(start), Type;";
        ResultSet results = statement.executeQuery(query);
        while (results.next()){
            int total_count  = results.getInt("total_count");
            int month = results.getInt("month");
            String type = results.getString("Type");
            someString += String.valueOf(month)+", "+type+", "+String.valueOf(total_count)+"\n";
        }
        return someString;
    }

    /**
     * Generates a multiline String Report of all appointments for a selected contact ID
     * @param contactId The Unique contact ID
     * @return A String of the full report
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static String generateContactSchedule(int contactId) throws SQLException{
        String someString = "Appointments for Contact ID: "+String.valueOf(contactId)+"\n----------------------------\n";
        String query = "SELECT * FROM "+databaseName+".appointments WHERE Contact_ID = "+String.valueOf(contactId)+" ORDER BY start ASC;";
        ResultSet results = statement.executeQuery(query);

        TimeZone currentTimezone = TimeZone.getTimeZone(ZoneId.systemDefault());
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");

        while (results.next()){
            int apptId = results.getInt("Appointment_ID");
            String title = results.getString("Title");
            String description = results.getString("Description");
            String location = results.getString("Location");
            String type = results.getString("Type");

            Timestamp resultEndTime = results.getTimestamp("End");
            Timestamp resultStartTime = results.getTimestamp("Start");
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            start.setTimeInMillis(resultStartTime.getTime() + currentTimezone.getRawOffset());
            end.setTimeInMillis(resultEndTime.getTime() + currentTimezone.getRawOffset());
            start.setTimeZone(currentTimezone);
            end.setTimeZone(currentTimezone);

            int customerId = results.getInt("Customer_ID");
            int userId = results.getInt("User_ID");

            String addingString =
                    "Start Time: " + sdf.format(start.getTime()) +
                    "\nEnd Time: " + sdf.format(end.getTime()) +
                    "\nAppointment ID: "+ String.valueOf(apptId) +
                    "\nTitle: " + title +
                    "\nDescription: " + description +
                    "\nLocation: " + location +
                    "\nType" + type +
                    "\nCustomer ID: " + String.valueOf(customerId) +
                    "\n\n";
            someString += addingString;
        }
        return someString;
    }

    /**
     * Custom Report. Generates a multiline String Report of the total Appointments by Location
     * @return The full String report
     * @throws SQLException SQLException Throws SQL Exception for SQL Errors
     */
    public static String generateLocationTotal() throws SQLException {
        String someString = "Total Appointments by Location\n----------------------------\n\n";
        String query = "SELECT count(Location) AS total_count, Location FROM "+databaseName+".appointments GROUP BY Location;";
        ResultSet results = statement.executeQuery(query);
        while (results.next()){
            int total = results.getInt("total_count");
            String location = results.getString("Location");

            someString += location + ": " + String.valueOf(total) + " Total\n";
        }
        return someString;
    }
}