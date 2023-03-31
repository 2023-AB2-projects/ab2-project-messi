import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.*;

public class ClientGUICreateTable extends JPanel implements ActionListener {
    ClientInterface clientInterface;
    private JLabel tableNameLabel;
    private JLabel columnNameLabel;
    private JLabel columnTypeLabel;
    private JLabel pkLabel;
    private JTextField tableNameField;
    private JTextField columnNameField;
    private JTextField columnTypeField;
    private JCheckBox pkCheckBox;
    private JButton addColumnButton;
    private JButton backButton;
    private JButton createTableButton;
    private JTextArea queryAreaMessage;
    private JScrollPane scrollPane;
    private String query;
    private String message;


    public ClientGUICreateTable(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;
//        this.startConnection();

        message = "";

        tableNameLabel = new JLabel("Table Name:");
        columnNameLabel = new JLabel("Column Name:");
        columnTypeLabel = new JLabel("Column Type:");

        tableNameField = new JTextField(20);
        columnNameField = new JTextField(20);
        columnTypeField = new JTextField(20);
        addColumnButton = new JButton("Add Column");
        backButton = new JButton("Back");
        createTableButton = new JButton("Create Table");
        queryAreaMessage = new JTextArea(10, 40);
        queryAreaMessage.setEditable(false);
        scrollPane = new JScrollPane(queryAreaMessage);
        pkLabel = new JLabel("Primary key?");
        pkCheckBox = new JCheckBox();

        addColumnButton.addActionListener(this);
        backButton.addActionListener(this);
        createTableButton.addActionListener(this);
        pkCheckBox.addActionListener(this);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableNameField);
        inputPanel.add(columnNameLabel);
        inputPanel.add(columnNameField);
        inputPanel.add(columnTypeLabel);
        inputPanel.add(columnTypeField);
        inputPanel.add(pkLabel);
        inputPanel.add(pkCheckBox);


        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addColumnButton);
        buttonPanel.add(createTableButton);
        buttonPanel.add(backButton);


        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(new JLabel("SQL Query:"), BorderLayout.NORTH);
        queryPanel.add(scrollPane, BorderLayout.CENTER);


        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(queryPanel, BorderLayout.SOUTH);

        this.setVisible(true);
//        setContentPane(mainPanel);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addColumnButton) {
            String columnName = columnNameField.getText();
            String columnType = columnTypeField.getText().toUpperCase();
            String isPrimaryKey = String.valueOf(pkCheckBox.isSelected());
            if (columnName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a column name.");
            } else if (columnType.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a column type.");
            } else {
                if (queryAreaMessage.getText().equals("")) {
                    queryAreaMessage.append(isPrimaryKey + " " + columnName + " " + columnType);
                } else {
                    queryAreaMessage.append(",\n" + isPrimaryKey + " " + columnName + " " + columnType);
                }
                message += columnName + " " + columnType + "/";
                columnNameField.setText("");
                columnTypeField.setText("");
                pkCheckBox.setSelected(false);
            }
        } else if (e.getSource() == createTableButton) {
            String tableName = tableNameField.getText();
            query = "CREATE TABLE " + tableName + " (\n" + queryAreaMessage.getText() + "\n);";
            message = "3" + "/" + tableName + "/" + message;
            JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
            clientInterface.writeIntoSocket(query);
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
        }
    }
}