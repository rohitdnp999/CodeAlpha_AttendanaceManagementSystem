import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AttendanceManagementSystem {
    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.display();
    }
}

// Model
class Attendance {
    private int id;
    private String studentName;
    private String date;
    private String time;
    private String status;

    public Attendance() {}

    public Attendance(String studentName, String date,String time, String status) {
        this.studentName = studentName;
        this.date = date;
        this.time=time;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

// DAO (Data Access Object)
class AttendanceDao {
    private String jdbcURL = "jdbc:mysql://localhost/attendance_db";
    private String jdbcUsername = "root";
    private String jdbcPassword = "";

    private static final String INSERT_ATTENDANCE_SQL = "INSERT INTO attendance (student_name, date,time, status) VALUES (?, ?, ?,?);";
    private static final String SELECT_ALL_ATTENDANCE = "SELECT * FROM attendance";

    protected Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void insertAttendance(Attendance attendance) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_ATTENDANCE_SQL)) {
            preparedStatement.setString(1, attendance.getStudentName());
            preparedStatement.setString(2, attendance.getDate());
            preparedStatement.setString(3, attendance.getTime());
            preparedStatement.setString(4, attendance.getStatus());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            printSQLException(e);
        }
    }

    public List<Attendance> selectAllAttendance() {
        List<Attendance> attendanceList = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_ATTENDANCE)) {
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String studentName = rs.getString("student_name");
                String date = rs.getString("date");
                String time = rs.getString("time");
                String status = rs.getString("status");
                Attendance attendance = new Attendance(studentName, date,time,status);
                attendance.setId(id);
                attendanceList.add(attendance);
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return attendanceList;
    }

    private void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
}

// Service
class AttendanceService {
    private AttendanceDao attendanceDao = new AttendanceDao();

    public void addAttendance(Attendance attendance) {
        try {
            attendanceDao.insertAttendance(attendance);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Attendance> getAllAttendance() {
        return attendanceDao.selectAllAttendance();
    }
}

// GUI
class GUI {
    private JFrame frame;
    private JLabel title;
    private JPanel p1;
    private JTextField studentNameField;
    private JTextField statusField;
    private JTable table;
    private AttendanceService attendanceService;
    private JDatePickerImpl datePicker;
    private JSpinner timeSpinner;

    public GUI() {
        attendanceService = new AttendanceService();
        initialize();
    }

    private void initialize() {
        p1 = new JPanel();
        Cursor cr = new Cursor(Cursor.HAND_CURSOR);
        BevelBorder edge = new BevelBorder(BevelBorder.RAISED);
        EtchedBorder edge1 = new EtchedBorder(EtchedBorder.RAISED);
        frame = new JFrame();
        frame.setBounds(450, 150, 700, 500);
        frame.setTitle("Attendance Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        title = new JLabel("ATTENDANCE MANAGEMENT SYSTEM                  ");
        title.setBounds(0, 0, 120, 50);
        title.setFont(new Font("Times New Roman", Font.BOLD, 35));
        title.setForeground(Color.YELLOW);

        p1.add(title);
        p1.setBackground(new Color(0, 0, 255));
        p1.setBounds(0, 0, 850, 50);
        frame.getContentPane().add(p1);

        JLabel lblStudentName = new JLabel("Student Name:");
        lblStudentName.setBounds(100, 60, 100, 25);
        frame.getContentPane().add(lblStudentName);
        lblStudentName.setBorder(edge);

        studentNameField = new JTextField();
        studentNameField.setBounds(220, 60, 200, 25);
        studentNameField.setFont(new Font("Times New Roman", 1, 16));
        studentNameField.setForeground(Color.red);
        frame.getContentPane().add(studentNameField);
        studentNameField.setColumns(10);
        studentNameField.setBorder(edge1);

        JLabel lblDate = new JLabel("Date:");
        lblDate.setBounds(100, 90, 100, 25);
        frame.getContentPane().add(lblDate);
        lblDate.setBorder(edge);

        // Create the date picker
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        datePicker.setBounds(220, 90, 200, 25);
        frame.getContentPane().add(datePicker);

        JLabel lblTime = new JLabel("Time:");
        lblTime.setBounds(100, 120, 100, 25);
        frame.getContentPane().add(lblTime);
        lblTime.setBorder(edge);

        // Create the time spinner
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setBounds(220, 120, 200, 25);
        frame.getContentPane().add(timeSpinner);

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setBounds(100, 150, 100, 25);
        frame.getContentPane().add(lblStatus);
        lblStatus.setBorder(edge);

        statusField = new JTextField();
        statusField.setBounds(220, 150, 200, 25);
        statusField.setFont(new Font("Times New Roman", 1, 16));
        statusField.setForeground(Color.red);
        frame.getContentPane().add(statusField);
        statusField.setColumns(10);
        statusField.setBorder(edge1);

        JButton btnAddAttendance = new JButton("Add Attendance");
        btnAddAttendance.setCursor(cr);
        btnAddAttendance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String studentName = studentNameField.getText();
                java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
                java.util.Date selectedTime = (java.util.Date) timeSpinner.getModel().getValue();
                String date = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
                String time = new SimpleDateFormat("HH:mm:ss").format(selectedTime);
                String dateTime = date + " " + time;
                String status = statusField.getText();
                Attendance attendance = new Attendance(studentName, dateTime,time, status);
                attendanceService.addAttendance(attendance);
                populateTable();

                studentNameField.setText("");
                statusField.setText("");

            }
        });
        btnAddAttendance.setBounds(200, 190, 150, 23);
        frame.getContentPane().add(btnAddAttendance);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 230, 670, 220);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.getContentPane().add(scrollPane);

        table = new JTable();
        table.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        table.setForeground(Color.blue);
        scrollPane.setViewportView(table);
        table.setModel(new DefaultTableModel(
            new Object[][] {},
            new String[] {"ID", "Student Name", "Date","Time", "Status"}
        ));
        populateTable();
    }

    private void populateTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        List<Attendance> list = attendanceService.getAllAttendance();
        for (Attendance attendance : list) {
            model.addRow(new Object[]{attendance.getId(), attendance.getStudentName(), attendance.getDate(),attendance.getTime() ,attendance.getStatus()});
        }
    }

    public void display() {
        frame.setVisible(true);
    }

    // Custom formatter for date picker
    public class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private String datePattern = "yyyy-MM-dd";
        private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                java.util.Calendar cal = (java.util.Calendar) value;
                return dateFormatter.format(cal.getTime());
            }
            return "";
        }
    }
}
