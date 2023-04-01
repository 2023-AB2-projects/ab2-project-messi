import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.*;

public class ClientGUICreateTable extends JPanel implements ActionListener {
    ClientInterface clientInterface;
    private JPanel inputPanel;
    private JLabel tableNameLabel;
    private JLabel columnNameLabel;
    private JLabel columnTypeLabel;
    private JLabel pkLabel;
    private JLabel fkLable;
    private JLabel fkToTableLabel;
    private JLabel fkToColumnLabel;
    private JTextField tableNameField;
    private JTextField columnNameField;
    private JTextField columnTypeField;
    private JTextField fkToTabelField;
    private JTextField fkToColumnField;
    private JCheckBox pkCheckBox;
    private JCheckBox fkCheckBox;
    private JPanel fkPanel;
    private JButton addColumnButton;
    private JButton backButton;
    private JButton createTableButton;
    private JButton clearAllButton;
    private JTextArea queryAreaMessage;
    private JScrollPane scrollPane;
    private String query;
    private String message;
    private Boolean isPrimaryKey;
    private Boolean isForeignKey;


    public ClientGUICreateTable(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;
//        this.startConnection();

        message = "";

        tableNameLabel = new JLabel("Table Name:");
        columnNameLabel = new JLabel("Column Name:");
        columnTypeLabel = new JLabel("Column Type:");
        fkToTableLabel = new JLabel("Table Name:");
        fkToColumnLabel = new JLabel("Column Name:");

        tableNameField = new JTextField(20);
        columnNameField = new JTextField(20);
        columnTypeField = new JTextField(20);
        fkToTabelField = new JTextField(20);
        fkToColumnField = new JTextField(20);

        addColumnButton = new JButton("Add Column");
        backButton = new JButton("Back");
        createTableButton = new JButton("Create Table");
        clearAllButton = new JButton("Clear all");

        queryAreaMessage = new JTextArea(10, 40);
        queryAreaMessage.setEditable(false);
        scrollPane = new JScrollPane(queryAreaMessage);

        pkLabel = new JLabel("Primary key");
        fkLable = new JLabel("Foreign key");

        pkCheckBox = new JCheckBox();
        fkCheckBox = new JCheckBox();

        isPrimaryKey = false;
        isForeignKey = false;

        addColumnButton.addActionListener(this);
        backButton.addActionListener(this);
        createTableButton.addActionListener(this);
        clearAllButton.addActionListener(this);
        fkCheckBox.addActionListener(this);

        inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableNameField);
        inputPanel.add(columnNameLabel);
        inputPanel.add(columnNameField);
        inputPanel.add(columnTypeLabel);
        inputPanel.add(columnTypeField);
        inputPanel.add(pkLabel);
        inputPanel.add(pkCheckBox);
        inputPanel.add(fkLable);
        inputPanel.add(fkCheckBox);

        fkPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        fkPanel.add(fkToTableLabel);
        fkPanel.add(fkToTabelField);
        fkPanel.add(fkToColumnLabel);
        fkPanel.add(fkToColumnField);
        fkPanel.setVisible(false);

        JPanel inputDataPanel = new JPanel(new GridLayout(2, 2));
        inputDataPanel.add(inputPanel);
        inputDataPanel.add(fkPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addColumnButton);
        buttonPanel.add(createTableButton);
        buttonPanel.add(backButton);
        buttonPanel.add(clearAllButton);


        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(new JLabel("SQL Query:"), BorderLayout.NORTH);
        queryPanel.add(scrollPane, BorderLayout.CENTER);


        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(inputDataPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(queryPanel, BorderLayout.SOUTH);

        this.setVisible(true);
//        setContentPane(mainPanel);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addColumnButton) {
            String columnName = columnNameField.getText();
            String columnType = columnTypeField.getText().toUpperCase();
            String isPrimaryKeyString = String.valueOf(pkCheckBox.isSelected());
            String isForeignKeyString = String.valueOf(fkCheckBox.isSelected());
            String foreignKeyData = "";
            if (fkCheckBox.isSelected()) {
                foreignKeyData = fkToTabelField.getText() + " " + fkToColumnField.getText() + " ";
            }

            if (pkCheckBox.isSelected() && isPrimaryKey) {
                JOptionPane.showMessageDialog(this, "There's already a primary key to this table.");
            } else if (columnName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a column name.");

            } else if (columnType.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a column type.");
            } else {
                if(pkCheckBox.isSelected()) {
                    isPrimaryKey = true;
                }
                if (queryAreaMessage.getText().equals("")) {
                    queryAreaMessage.append(columnName + " " + columnType + " " + isPrimaryKeyString + " " + isForeignKeyString + " " + foreignKeyData);
                } else {
                    queryAreaMessage.append(",\n" + columnName + " " + columnType + " " + isPrimaryKeyString + " " + isForeignKeyString + " " + foreignKeyData);
                }
                message += columnName + " " + columnType + "/";
                columnNameField.setText("");
                columnTypeField.setText("");
                pkCheckBox.setSelected(false);
            }
        } else if (e.getSource() == createTableButton) {
            String tableName = tableNameField.getText();
            if (queryAreaMessage.getText().equals("")) {
                if (tableName.equals("")) {
                    JOptionPane.showMessageDialog(this, "To create a table, insert the required data.");
                } else {
                    JOptionPane.showMessageDialog(this, "Insert columns into " + tableName + " table.");
                }
            } else {
                if (!isPrimaryKey) {
                    JOptionPane.showMessageDialog(this, "Declare a primary key to the " + tableName + " table.");
                } else {
                    query = "CREATE TABLE " + tableName + " (\n" + queryAreaMessage.getText() + "\n);";
                    message = "3" + "/" + tableName + "/" + message;
                    JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
                    clientInterface.writeIntoSocket(query);

                    tableNameField.setText("");
                    columnNameField.setText("");
                    columnTypeField.setText("");
                    pkCheckBox.setSelected(false);
                    isPrimaryKey = false;
                    queryAreaMessage.setText("");
                    fkPanel.setVisible(false);
                    fkCheckBox.setSelected(false);
                }
            }
        } else if (e.getSource() == fkCheckBox) {
            fkPanel.setVisible(fkCheckBox.isSelected());
        } else if (e.getSource() == clearAllButton) {
            tableNameField.setText("");
            columnNameField.setText("");
            columnTypeField.setText("");
            pkCheckBox.setSelected(false);
            isPrimaryKey = false;
            queryAreaMessage.setText("");
            fkPanel.setVisible(false);
            fkCheckBox.setSelected(false);
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
        }

    }
}