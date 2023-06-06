package edu.ubbcluj.ab2.minidb;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientGUICreateTable extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private MyComboBox databaseComboBox;
    private JTextField tableNameField;
    private JTextField columnNameField;
    private JComboBox<String> columnTypeBox;
    private MyComboBox fkToTableBox;
    private MyComboBox fkToColumnBox;
    private JCheckBox pkCheckBox;
    private JCheckBox fkCheckBox;
    private JCheckBox uniqueCheckBox;
    private JPanel fkPanel;
    private JButton addColumnButton;
    private JButton backButton;
    private JButton createTableButton;
    private JButton clearAllButton;
    private JTextArea queryAreaMessage;
    private String primaryKey = "";
    private String foreignKey = "";
    private String unique = "";


    public ClientGUICreateTable(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        JLabel databaseNameLabel = new JLabel("Database Name:");
        JLabel tableNameLabel = new JLabel("Table Name:");
        JLabel columnNameLabel = new JLabel("Column Name:");
        JLabel columnTypeLabel = new JLabel("Column Type:");
        JLabel fkToTableLabel = new JLabel("Table Name:");
        JLabel fkToColumnLabel = new JLabel("Column Name:");

        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setSelectedIndex(0);
        tableNameField = new JTextField(20);
        columnNameField = new JTextField(20);
        columnTypeBox = new JComboBox<>(new String[]{"INT", "FLOAT", "BIT", "DATE", "DATETIME", "VARCHAR"});
        columnTypeBox.setSelectedIndex(0);
        fkToTableBox = new MyComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
        fkToTableBox.setSelectedIndex(0);
        fkToColumnBox = new MyComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) fkToTableBox.getSelectedItem()));
        fkToColumnBox.setSelectedIndex(0);

        addColumnButton = new JButton("Add Column");
        backButton = new JButton("Back");
        createTableButton = new JButton("Create Table");
        clearAllButton = new JButton("Clear all");

        queryAreaMessage = new JTextArea(10, 40);
        queryAreaMessage.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(queryAreaMessage);

        JLabel pkLabel = new JLabel("Primary key");
        JLabel fkLabel = new JLabel("Foreign key");
        JLabel uniqueLabel = new JLabel("Unique");

        pkCheckBox = new JCheckBox();
        fkCheckBox = new JCheckBox();
        uniqueCheckBox = new JCheckBox();

        addColumnButton.addActionListener(this);
        backButton.addActionListener(this);
        createTableButton.addActionListener(this);
        clearAllButton.addActionListener(this);
        fkCheckBox.addActionListener(this);
        pkCheckBox.addActionListener(this);
        uniqueCheckBox.addActionListener(this);
        databaseComboBox.addActionListener(this);
        fkToTableBox.addActionListener(this);


        JPanel inputPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        inputPanel.add(databaseNameLabel);
        inputPanel.add(databaseComboBox);
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableNameField);
        inputPanel.add(columnNameLabel);
        inputPanel.add(columnNameField);
        inputPanel.add(columnTypeLabel);
        inputPanel.add(columnTypeBox);
        inputPanel.add(pkLabel);
        inputPanel.add(pkCheckBox);
        inputPanel.add(fkLabel);
        inputPanel.add(fkCheckBox);
        inputPanel.add(uniqueLabel);
        inputPanel.add(uniqueCheckBox);

        fkPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        fkPanel.add(fkToTableLabel);
        fkPanel.add(fkToTableBox);
        fkPanel.add(fkToColumnLabel);
        fkPanel.add(fkToColumnBox);
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
        queryPanel.add(new JLabel("Columns:"), BorderLayout.NORTH);
        queryPanel.add(scrollPane, BorderLayout.CENTER);


        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(inputDataPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(queryPanel, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addColumnButton) {
            String tableName = tableNameField.getText();
            String columnName = columnNameField.getText();
            String columnType = (String) columnTypeBox.getSelectedItem();
            if (tableName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a table name.");
            } else if (columnName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a column name.");
            } else {
                assert columnType != null;
                if (columnType.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Enter a column type.");
                } else {
                    // if there were columns added before, a "," needs to be added to the end of the line
                    if (!queryAreaMessage.getText().equals("")) {
                        queryAreaMessage.append(",\n");
                    }

                    if (pkCheckBox.isSelected()) {
                        if (primaryKey.equals("")) {
                            primaryKey += columnName;
                        } else {
                            primaryKey += ", " + columnName;
                        }
                    }

                    if (uniqueCheckBox.isSelected()) {
                        if (unique.equals("")) {
                            unique += "CONSTRAINT UK_" + tableName + "_" + columnName + " UNIQUE(" + columnName + "),\n";

                        } else {
                            unique += ",\nCONSTRAINT UK_" + tableName + "_" + columnName + " UNIQUE(" + columnName + "),\n";
                        }
                    }

                    if (fkCheckBox.isSelected()) {
                        if (foreignKey.equals("")) {
                            foreignKey += "CONSTRAINT FK_" + tableName + "_" + columnName + " FOREIGN KEY(" + columnName + ")\nREFERENCES " + fkToTableBox.getSelectedItem() + "(" + fkToColumnBox.getSelectedItem() + "),\n";

                        } else {
                            foreignKey += ",\nCONSTRAINT FK_" + tableName + "_" + columnName + " FOREIGN KEY(" + columnName + ")\nREFERENCES " + fkToTableBox.getSelectedItem() + "(" + fkToColumnBox.getSelectedItem() + "),\n";
                        }
                    }

                    queryAreaMessage.append(columnName + " " + columnType + "");
                    pkCheckBox.setSelected(false);
                    pkCheckBox.setEnabled(true);
                    fkCheckBox.setSelected(false);
                    uniqueCheckBox.setSelected(false);
                    uniqueCheckBox.setEnabled(true);
                    fkToTableBox.setSelectedIndex(0);
                    fkToColumnBox.setSelectedIndex(0);
                    fkPanel.setVisible(false);
                    columnNameField.setText("");
                    columnTypeBox.setSelectedIndex(0);
                }
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
                if (primaryKey.equals("")) {
                    JOptionPane.showMessageDialog(this, "Declare a primary key to the " + tableName + " table.");
                } else {
                    String query;
                    if (foreignKey.equals("")) {
                        if (unique.equals("")) {
                            query = "CREATE TABLE " + databaseComboBox.getSelectedItem() + "." + tableName + " (\n" + queryAreaMessage.getText() + ",\nCONSTRAINT PK_" + tableName + " PRIMARY KEY(" + primaryKey + ")\n);";
                        } else {
                            query = "CREATE TABLE " + databaseComboBox.getSelectedItem() + "." + tableName + " (\n" + queryAreaMessage.getText() + ",\nCONSTRAINT PK_" + tableName + " PRIMARY KEY(" + primaryKey + "),\n" + unique + "\n);";
                        }
                    } else {
                        if (unique.equals("")) {
                            query = "CREATE TABLE " + databaseComboBox.getSelectedItem() + "." + tableName + " (\n" + queryAreaMessage.getText() + ",\nCONSTRAINT PK_" + tableName + " PRIMARY KEY(" + primaryKey + "),\n" + foreignKey + "\n);";
                        } else {
                            query = "CREATE TABLE " + databaseComboBox.getSelectedItem() + "." + tableName + " (\n" + queryAreaMessage.getText() + ",\nCONSTRAINT PK_" + tableName + " PRIMARY KEY(" + primaryKey + "),\n" + unique + foreignKey + "\n);";
                        }
                    }
                    JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
                    clientInterface.writeIntoSocket(query);

                    tableNameField.setText("");
                    columnNameField.setText("");
                    columnTypeBox.setSelectedIndex(0);
                    pkCheckBox.setSelected(false);
                    pkCheckBox.setEnabled(true);
                    primaryKey = "";
                    foreignKey = "";
                    unique = "";
                    queryAreaMessage.setText("");
                    fkPanel.setVisible(false);
                    fkCheckBox.setSelected(false);
                    uniqueCheckBox.setSelected(false);
                    uniqueCheckBox.setEnabled(true);
                }
            }
        } else if (e.getSource() == fkCheckBox) {
            fkToTableBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
            fkToTableBox.setSelectedIndex(0);
            fkToColumnBox.updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) fkToTableBox.getSelectedItem()));
            fkToColumnBox.setSelectedIndex(0);
            fkPanel.setVisible(fkCheckBox.isSelected());
        } else if (e.getSource() == uniqueCheckBox) {
            pkCheckBox.setEnabled(!pkCheckBox.isEnabled());
        } else if (e.getSource() == pkCheckBox) {
            uniqueCheckBox.setEnabled(!uniqueCheckBox.isEnabled());
        } else if (e.getSource() == clearAllButton) {
            tableNameField.setText("");
            columnNameField.setText("");
            columnTypeBox.setSelectedIndex(0);
            pkCheckBox.setSelected(false);
            pkCheckBox.setEnabled(true);
            primaryKey = "";
            foreignKey = "";
            unique = "";
            queryAreaMessage.setText("");
            fkPanel.setVisible(false);
            fkCheckBox.setSelected(false);
            uniqueCheckBox.setSelected(false);
            uniqueCheckBox.setEnabled(true);

        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
        } else if (e.getSource() == databaseComboBox) {
            fkToTableBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
            fkToTableBox.setSelectedIndex(0);
            fkToColumnBox.updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) fkToTableBox.getSelectedItem()));
            fkToColumnBox.setSelectedIndex(0);
        } else if (e.getSource() == fkToTableBox) {
            fkToColumnBox.updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) fkToTableBox.getSelectedItem()));
            fkToColumnBox.setSelectedIndex(0);
        }
    }

    public MyComboBox getDatabaseComboBox() {
        return this.databaseComboBox;
    }
}