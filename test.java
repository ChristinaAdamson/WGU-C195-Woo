import javax.xml.crypto.Data;
import java.util.ArrayList;

public class test {
    public static void main(String[] args) {
        try {
            Database.initializeDatabase();

            ArrayList<String> columns = new ArrayList<>();
            columns.add("Appointment_ID");
            columns.add("title");
            columns.add("start");
            columns.add("end");

            ArrayList<ArrayList<Object>> data = Database.getTableData("appointments", columns);

            for (int i=0;i<data.size();i++){
                for (int j=0;j<data.get(i).size();j++){
                    System.out.println(data.get(i).get(j));
                }
                System.out.println("\n");
            }

        }
        catch (Exception e) {
            e.getStackTrace();
        }
    }
}
