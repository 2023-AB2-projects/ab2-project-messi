package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class ClientGUICreateIndex extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private JLabel indexNameLabel;
    private JLabel indexOnLabel;
    private JTextField indexName;
    private JLabel fieldNameLabel;
    private MyComboBox fieldNameBox;
    private JLabel databaseNameLabel;
    private MyComboBox databaseBox;
    private JLabel tableNameLabel;
    private MyComboBox tableBox;
    private JButton submitButton;
    private JButton backButton;
    private JButton addButton;

    private JTextArea queryAreaMessage;
    private JScrollPane scrollPane;
    private String query;

    public ClientGUICreateIndex(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;
        this.setLayout(new BorderLayout());


        indexNameLabel = new JLabel("Index name: ");
        indexName = new JTextField();
        indexOnLabel = new JLabel("ON");
        databaseNameLabel = new JLabel("Database name: ");
        databaseBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseBox.setSelectedIndex(0);
        tableNameLabel = new JLabel("Table name: ");
        tableBox = new MyComboBox(clientInterface.getTablesNames((String) databaseBox.getSelectedItem()));
        tableBox.setSelectedIndex(0);
        fieldNameLabel = new JLabel("Field name(s): ");
        fieldNameBox = new MyComboBox(clientInterface.getFieldNames((String) databaseBox.getSelectedItem(), (String) tableBox.getSelectedItem()));
        fieldNameBox.setSelectedIndex(0);

        submitButton = new JButton("Submit");
        addButton = new JButton("Add");
        backButton = new JButton("Back");

        queryAreaMessage = new JTextArea(10, 40);
        queryAreaMessage.setEditable(false);
        scrollPane = new JScrollPane(queryAreaMessage);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.add(indexNameLabel);
        inputPanel.add(indexName);
        inputPanel.add(indexOnLabel);
        inputPanel.add(new JLabel());
        inputPanel.add(databaseNameLabel);
        inputPanel.add(databaseBox);
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableBox);
        inputPanel.add(fieldNameLabel);
        inputPanel.add(fieldNameBox);

        databaseBox.addActionListener(this);
        tableBox.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);
        addButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(new JLabel("Fields:"), BorderLayout.NORTH);
        queryPanel.add(scrollPane, BorderLayout.CENTER);

        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(queryPanel, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == databaseBox) {
            tableBox.updateComboBox(clientInterface.getTablesNames((String) databaseBox.getSelectedItem()));
        }

        if (e.getSource() == tableBox) {
            fieldNameBox.updateComboBox(clientInterface.getFieldNames((String) databaseBox.getSelectedItem(), (String) tableBox.getSelectedItem()));
        }

        if (e.getSource() == addButton) {
            databaseBox.setEnabled(false);
            tableBox.setEnabled(false);
            queryAreaMessage.append(fieldNameBox.getSelectedItem() + "\n");
            fieldNameBox.removeItem(fieldNameBox.getSelectedItem());
        }

        if (e.getSource() == submitButton) {
            if (indexName.getText().equals("")) {
                JOptionPane.showMessageDialog(this, "Specify the name of the index!");
            } else {
                String fields = queryAreaMessage.getText().replace("\n", ", ");
                fields = fields.substring(0, fields.lastIndexOf(", "));
                query = "CREATE INDEX " + indexName.getText() + "\nON " + databaseBox.getSelectedItem() + "." + tableBox.getSelectedItem() + "(" + fields + ");\n";
                JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
                clientInterface.writeIntoSocket(query);
                indexName.setText("");
                queryAreaMessage.setText("");
                databaseBox.setEnabled(true);
                tableBox.setEnabled(true);
            }
        }

        if (e.getSource() == backButton) {
            indexName.setText("");
            queryAreaMessage.setText("");
            databaseBox.setEnabled(true);
            tableBox.setEnabled(true);
            clientInterface.showMenu();
        }
    }

    public void updateDatabaseComboBox() {
        databaseBox.removeAllItems();
        String[] elements = clientInterface.getDatabasesNames().split(" ");
        Arrays.sort(elements);
        for (String element : elements) {
            databaseBox.addItem(element);
        }
    }
}