import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static JLabel labelEmployeeDetails = new JLabel ("Employee Details");
    private static JLabel labelFirstName = new JLabel ("First Name");
    private static JLabel labelLastName = new JLabel ("Last Name");
    private static JLabel labelSSN = new JLabel ("SSN");
    private static JLabel labelGender = new JLabel ("Gender");
    private static JLabel labelDOB = new JLabel ("Date of Birth");
    private static JLabel labelSalary = new JLabel ("Salary");
    private static JLabel labelError = new JLabel();
    private static JTextField tfFirstName = new JTextField();
    private static JTextField tfLastName = new JTextField();
    private static JTextField tfSSN = new JTextField();
    private static JTextField tfDOB = new JTextField();
    private static JTextField tfSalary = new JTextField();
    private static String genders[] = {"Male", "Female", "Other"};
    private static JComboBox cbGender = new JComboBox(genders);
    private static JButton btPrevious = new JButton("Previous");
    private static JButton btNext = new JButton("Next");
    private static JButton btClear = new JButton("Clear");
    private static JButton btAdd = new JButton("Add");
    private static JButton btDelete = new JButton("Delete");
    private static JButton btUpdate = new JButton("Update");
    private static JButton btSearch = new JButton("Search");
    private static Connection con;
    private static ResultSet allData;

    public static void main(String[] args){
        Main main = new Main();
        main.generateGui();
        main.connectToMySql();
        con = main.connectToMySql();
        allData = main.resultSet();
    }

    //Method to connect to the mySQL running with WAMP, using the credentials username:"root", password:""
    public Connection connectToMySql() {

        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            con = DriverManager.getConnection("jdbc:mysql://localhost/test?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT", "root", "");
            if (!con.isClosed())
                System.out.println("Connected to mysql server!");
        } catch (Exception e) {
            System.err.println("Exception " + e.getMessage());
        }
        return con;
    }

    //Method that takes in text from the GUI to create a user with validation which then calls the method to add it to the SQL database
    public void createUser(){
        String ssn = tfSSN.getText();
        String dob = tfDOB.getText();
        String firstName = tfFirstName.getText();
        String lastName = tfLastName.getText();
        String salary = tfSalary.getText();
        String gender = cbGender.getSelectedItem().toString();

        //checks if any of the data is left blank
        if (ssn.equals("")){
            labelError.setText("Please Enter an SSN");
        }
        else if (dob.equals("")) {
            labelError.setText("Please Enter a Date");
        }
        else if (firstName.equals("")) {
            labelError.setText("Please Enter a First Name");
        }
        else if (lastName.equals("")){
            labelError.setText("Please Enter a Last Name");
        }
        else if (salary.equals("")) {
            labelError.setText("Please Enter a Salary");
        }

        //validation for the ssn as well as the first and last names
        else if (firstName.length() > 20 || firstName.length() < 2){
            labelError.setText("Please Enter a valid First Name");
        }
        else if (lastName.length() > 20 || lastName.length() < 2){
            labelError.setText("Please Enter a valid Last Name");
        }
        else if (ssn.length() > 10){
            labelError.setText("Please Enter a valid SSN");
        }
        else {
            //casting the salary as an int as that is what the sql expects
            int SalaryToInt = 0;
            try {
                SalaryToInt = Integer.parseInt(salary);
            }
            catch (NumberFormatException e)
            {
                labelError.setText("Invalid salary");
            }
            //converts the string into a valid date
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date dateOfBirth = null;
            try {

                dateOfBirth = formatter.parse(dob);

            } catch (ParseException e) {
                labelError.setText("Invalid DOB");
            }
            try{
                //if none of the validation fails, the user with the specified data is added to the sql DB
                this.sqlAddUser(ssn, gender, SalaryToInt, firstName, lastName, dateOfBirth);
            } catch (Exception e) {
                labelError.setText("Error Adding User");
            }
        }
    }

    //Method to add the user from the create user method up to the sql DB
    public void sqlAddUser(String ssn, String gender, int SalaryToInt, String firstName, String lastName, Date dateOfBirth){
        try{
            //Prepared statement that runs an INSERT SQL statement that takes in the data from the createUser method and adds it to the DB
            PreparedStatement psmnt = con.prepareStatement("INSERT  INTO employee VALUES (?,?,?,?,?,?)");
            psmnt.setString(1,firstName);
            psmnt.setString(2, lastName);
            psmnt.setString(3, ssn);
            psmnt.setDate(4, new java.sql.Date(dateOfBirth.getTime()));
            psmnt.setInt(5, SalaryToInt);
            psmnt.setString(6, gender);
            psmnt.executeUpdate();
            labelError.setText("User Added");
            resultSet();
        }catch (Exception e){
            labelError.setText("Error Adding User");
        }
    }

    //Prepared statement that runs an DELETE SQL statement the searches for a specified parameter (ssn as its the primary key) and then deletes it from the DB
    public void sqlDeleteUser(){
        String ssn = tfSSN.getText();
        try{
            PreparedStatement psmnt = con.prepareStatement("DELETE FROM employee WHERE ssn = ?");
            psmnt.setString(1,ssn);
            psmnt.executeUpdate();
            labelError.setText("User Deleted");
            resultSet();
        }catch(Exception e){
            e.printStackTrace();
            labelError.setText("Error Deleting User");
        }
    }

    //Prepared statement that runs an UPDATE SQL statement that searches for a user via ssn (primary key) and updates the info of the user based on the data currently in the GUI text fields
    public void sqlUpdateUser(){
        try{
            PreparedStatement psmnt = con.prepareStatement("UPDATE employee SET first_name = ?, last_name = ?, gender = ?, date_of_birth = ?, salary = ? WHERE ssn = ?");
            psmnt.setString(1, tfFirstName.getText());
            psmnt.setString(6, tfSSN.getText());
            psmnt.setString(3, cbGender.getSelectedItem().toString());
            String dob = tfDOB.getText();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date dateOfBirth = null;
            try {
                //casts the date to a valid date format
                dateOfBirth = formatter.parse(dob);

            } catch (ParseException e) {
                labelError.setText("Invalid DOB");
            }
            psmnt.setDate(4, new java.sql.Date(dateOfBirth.getTime()));
            psmnt.setInt(5, Integer.parseInt(tfSalary.getText()));
            psmnt.setString(2, tfLastName.getText());
            psmnt.executeUpdate();
            labelError.setText("Update Success");
            resultSet();
        }catch (Exception e){
            labelError.setText("Update Failed");
        }
    }


    //Method that uses a preparedStatement to find a user based on the last_name
    public void sqlSearch(){
        ResultSet rs = null;
        String lastName = tfLastName.getText();
        try{
            PreparedStatement psmnt = con.prepareStatement("SELECT * FROM employee WHERE last_name = ?");
            psmnt.setString(1,lastName);
            rs = psmnt.executeQuery();

            if (rs != null){
                allData = rs;
                allData.first();
                displayUserData();
            }
            else {
                labelError.setText("No Users");
            }

        }catch (Exception e){
        }
    }

    //Method that generates the result set of the database
    //NOTE: Global variable "allData" was set up to be equal to the result of this method (rs) in main()
    public ResultSet resultSet(){
        ResultSet rs = null;
        try{
            Statement s = con.createStatement();
            s.executeQuery("SELECT * FROM employee");
            rs = s.getResultSet();
        }catch(Exception e){
        }
        return rs;
    }

    //Method that displays the user on the GUI from the result set.
    public void displayUserData(){
        try{
            tfSSN.setText(allData.getString("ssn"));
            tfFirstName.setText(allData.getString("first_name"));
            tfLastName.setText(allData.getString("last_name"));
            tfSalary.setText(allData.getString("salary"));
            tfDOB.setText(allData.getString("date_of_birth"));
            if (allData.getString("gender").equals("M")){
                cbGender.setSelectedIndex(0);
            }else if (allData.getString("gender").equals("F")){
                cbGender.setSelectedIndex(1);
            }else{
                cbGender.setSelectedIndex(2);
            }
        }catch (Exception e){
            labelError.setText("");
        }
    }

    //Method that iterates through global resultSet
    public void iterateRS(String option){
        Boolean result = true;
        if (option.equals("next")){
            try {
                allData.next();
            }catch (Exception e){
                result = false;
            }
        }else if (option.equals("previous")){
            try{
                allData.previous();
            }catch (Exception e){
                result = false;
            }
        }else {
            try {
                allData.first();
            }catch (Exception e){
                System.out.println("");
            }
        }
        //if there are no more users to display after the user hits next, its will then revert back to the previous displayed user
        //and vice versa for when previous is used with no more users to display
        try {
            if (!result){
                if (option.equals("next")){
                    option.equals("previous");
                }
                else{
                    option.equals("next");
                }
            }
        }catch (Exception e){
        }
        //Displays the user after an attempted iteration
        displayUserData();
    }

    //Method that clears the GUI by emptying the text fields
    public void clearGui(){
        tfDOB.setText("");
        tfSSN.setText("");
        tfLastName.setText("");
        tfFirstName.setText("");
        tfSalary.setText("");
        cbGender.setSelectedItem(null);
        labelError.setText("");
    }

    //Method that creates the Java Swing GUI
    private void generateGui(){
        JFrame frame = new JFrame("Employee Details");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Setting boundaries
        labelEmployeeDetails.setBounds(140,0,160,40);
        labelSSN.setBounds(10, 30, 80, 40);
        tfSSN.setBounds(90, 42, 160, 20);
        labelDOB.setBounds(10, 60, 80, 40);
        tfDOB.setBounds(90, 72, 160, 20);
        labelFirstName.setBounds(10, 92, 80, 40);
        tfFirstName.setBounds(90, 102, 160, 20);
        labelLastName.setBounds(10, 122, 80, 40);
        tfLastName.setBounds(90, 132, 160, 20);
        labelSalary.setBounds(10, 152, 80, 40);
        tfSalary.setBounds(90,162, 160, 20);
        labelGender.setBounds(10,182,80,40);
        cbGender.setBounds(90,192,160,20 );
        btPrevious.setBounds(275,42,85,20);
        btNext.setBounds(275, 72, 85,20);
        btClear.setBounds(275,192, 85,20 );
        btAdd.setBounds(50,255, 85,20);
        btDelete.setBounds(150,255, 85, 20);
        btUpdate.setBounds(250, 255,85,20);
        btSearch.setBounds(150,285,85,20);
        labelError.setBounds(50,310,400,20);
        //Adding components
        frame.add(labelEmployeeDetails);
        frame.add(labelFirstName);
        frame.add(tfFirstName);
        frame.add(labelLastName);
        frame.add(tfLastName);
        frame.add(labelSSN);
        frame.add(tfSSN);
        frame.add(labelDOB);
        frame.add(tfDOB);
        frame.add(labelSalary);
        frame.add(tfSalary);
        frame.add(labelGender);
        frame.add(cbGender);
        frame.add(btPrevious);
        //Adding buttons as well as their event handlers
        btPrevious.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iterateRS("previous");
            }
        });
        frame.add(btNext);
        btNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iterateRS("next");
            }
        });
        frame.add(btClear);
        btClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearGui();
            }
        });
        frame.add(btAdd);
        btAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createUser();
            }
        });
        frame.add(btDelete);
        btDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sqlDeleteUser();
            }
        });
        frame.add(btUpdate);
        btUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sqlUpdateUser();
            }
        });
        frame.add(btSearch);
        btSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sqlSearch();
            }
        });
        frame.add(labelError);
        frame.getContentPane().setLayout(null);
        frame.pack();
        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}