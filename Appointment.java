import java.util.Calendar;

/**
 * The Appointment Class to translate Contact information from the Database
 */

public class Appointment {

    private int appointmentId;
    private String title;
    private String description;
    private String location;
    private String type;
    private Calendar start;
    private Calendar end;
    private int customerId;
    private int userId;
    private int contactId;

    public Appointment(int appointmentId,
                       String title,
                       String description,
                       String location,
                       int contactId,
                       String type,
                       Calendar start,
                       Calendar end,
                       int customerId,
                       int userId) {
        this.setAppointmentId(appointmentId);
        this.setTitle(title);
        this.setDescription(description);
        this.setLocation(location);
        this.setContactId(contactId);
        this.setType(type);
        this.setStart(start);
        this.setEnd(end);
        this.setCustomerId(customerId);
        this.setUserId(userId);
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Calendar getStart() {
        return start;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public Calendar getEnd() {
        return end;
    }

    public void setEnd(Calendar end) {
        this.end = end;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        if (customerId < 0){
            throw new IllegalArgumentException("Customer ID argument cannot be less than 0");
        }
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
