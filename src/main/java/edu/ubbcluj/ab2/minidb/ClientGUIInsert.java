package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class ClientGUIInsert extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private JPanel inputPanel;
    private JLabel databaseNameLabel;
    private JLabel tableNameLabel;
    private JLabel valuesLabel;
    private MyComboBox databaseComboBox;
    private MyComboBox tableComboBox;
    private JTextField valuesField;
    private JButton addValuesButton;
    private JButton submitButton;
    private JButton backButton;
    private JTextArea valuesTextArea;
    private JScrollPane scrollPane;
    private String values = "";
    private String query;

    public ClientGUIInsert(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        databaseNameLabel = new JLabel("Database Name:");
        tableNameLabel = new JLabel("Table Name:");
        valuesLabel = new JLabel("Values:");

        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setSelectedIndex(0);
        tableComboBox = new MyComboBox((clientInterface.getTablesNames((String) databaseComboBox.getSelectedItem())));
        tableComboBox.setSelectedIndex(0);
        valuesField = new JTextField(20);
        valuesField.setText("");


        inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.add(databaseNameLabel);
        inputPanel.add(databaseComboBox);
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableComboBox);
        inputPanel.add(valuesLabel);
        inputPanel.add(valuesField);

        valuesTextArea = new JTextArea(10, 40);
        valuesTextArea.setEditable(false);
        valuesTextArea.setText("");
        scrollPane = new JScrollPane(valuesTextArea);


        addValuesButton = new JButton("Add Values");
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");


        addValuesButton.addActionListener(this);
        databaseComboBox.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addValuesButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        JPanel valuesPanel = new JPanel(new BorderLayout());
        valuesPanel.add(new JLabel("Values:"), BorderLayout.NORTH);
        valuesPanel.add(scrollPane, BorderLayout.CENTER);

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(valuesPanel, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == databaseComboBox) {
            tableComboBox.updateComboBox(clientInterface.getTablesNames((String) databaseComboBox.getSelectedItem()));
        }
        if (e.getSource() == addValuesButton) {
            if (valuesField.getText().equals("")) {
                JOptionPane.showMessageDialog(this, "Fill in the Values field first!");
            } else {
                if (valuesTextArea.getText().equals("")) {
                    valuesTextArea.setText("(" + valuesField.getText() + ")");
                    values += "(" + valuesField.getText() + ")";
                } else {
                    valuesTextArea.append("\n(" + valuesField.getText() + ")");
                    values += "\n                (" + valuesField.getText() + ")";
                }
                valuesField.setText("");
            }

        }
        if (e.getSource() == submitButton) {
            if (valuesTextArea.getText().equals("")) {
                JOptionPane.showMessageDialog(this, "Unable to perform insertion, if values are not given.");
            } else {
                String databaseName = (String) databaseComboBox.getSelectedItem();
                String tableName = (String) tableComboBox.getSelectedItem();
                query = "INSERT INTO " + databaseName + "." + tableName + "\nVALUES " + values + ";";
                JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
                clientInterface.writeIntoSocket(query);
                valuesTextArea.setText("");
            }
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
            valuesTextArea.setText("");
        }
    }

    public void updateDatabaseComboBox() {
        databaseComboBox.removeAllItems();
        String[] elements = clientInterface.getDatabasesNames().split(" ");
        Arrays.sort(elements);
        for (String element : elements) {
            databaseComboBox.addItem(element);
        }
    }
}
