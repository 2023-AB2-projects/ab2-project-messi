package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUICreateIndex extends JPanel implements ActionListener {
    String sortOrderString;
    private ClientInterface clientInterface;
    private JTextField indexName;
    private MyComboBox fieldNameBox;
    private JButton ascButton;
    private JButton descButton;
    private JCheckBox uniqueCheckbox;
    private MyComboBox databaseComboBox;
    private MyComboBox tableComboBox;
    private JButton submitButton;
    private JButton backButton;
    private JButton addButton;

    private JTextArea queryAreaMessage;

    public ClientGUICreateIndex(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;
        this.setLayout(new BorderLayout());


        JLabel indexNameLabel = new JLabel("Index name: ");
        indexName = new JTextField();
        // TODO: index name to dynamically change on fields/tables change, ex: tableName_field1_field2...
        JLabel indexOnLabel = new JLabel("ON");
        JLabel databaseNameLabel = new JLabel("Database name: ");
        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setSelectedIndex(0);
        JLabel tableNameLabel = new JLabel("Table name: ");
        tableComboBox = new MyComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
        tableComboBox.setSelectedIndex(0);
        JLabel fieldNameLabel = new JLabel("Field name(s): ");
        fieldNameBox = new MyComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()));
        fieldNameBox.setSelectedIndex(0);
        JLabel sortOrderLabel = new JLabel("Sort order:");
        ascButton = new JButton("ASC");
        ascButton.setEnabled(false);
        sortOrderString = ascButton.getText();
        descButton = new JButton("DESC");
        JLabel uniqueLabel = new JLabel("Unique:");
        uniqueCheckbox = new JCheckBox();

        JPanel orderPanel = new JPanel(new GridLayout(1, 2));
        orderPanel.add(ascButton);
        orderPanel.add(descButton);

        submitButton = new JButton("Submit");
        addButton = new JButton("Add");
        backButton = new JButton("Back");

        queryAreaMessage = new JTextArea(10, 40);
        queryAreaMessage.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(queryAreaMessage);

        JPanel inputPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        inputPanel.add(indexNameLabel);
        inputPanel.add(indexName);
        inputPanel.add(indexOnLabel);
        inputPanel.add(new JLabel());
        inputPanel.add(databaseNameLabel);
        inputPanel.add(databaseComboBox);
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableComboBox);
        inputPanel.add(fieldNameLabel);
        inputPanel.add(fieldNameBox);
        inputPanel.add(sortOrderLabel);
        inputPanel.add(orderPanel);
        inputPanel.add(uniqueLabel);
        inputPanel.add(uniqueCheckbox);

        databaseComboBox.addActionListener(this);
        tableComboBox.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);
        addButton.addActionListener(this);
        ascButton.addActionListener(this);
        descButton.addActionListener(this);

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
        if (e.getSource() == databaseComboBox) {
            if (databaseComboBox.getSelectedItem() != null) {
                tableComboBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
            }
        } else if (e.getSource() == tableComboBox) {
            if (tableComboBox.getSelectedItem() != null) {
                fieldNameBox.updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()));
            }
        } else if (e.getSource() == ascButton) {
            descButton.setEnabled(true);
            ascButton.setEnabled(false);
            sortOrderString = ascButton.getText();
        } else if (e.getSource() == descButton) {
            descButton.setEnabled(false);
            ascButton.setEnabled(true);
            sortOrderString = descButton.getText();
        } else if (e.getSource() == addButton) {
            databaseComboBox.setEnabled(false);
            tableComboBox.setEnabled(false);
            uniqueCheckbox.setEnabled(false);
            queryAreaMessage.append(fieldNameBox.getSelectedItem() + " " + sortOrderString + "\n");
            fieldNameBox.removeItem(fieldNameBox.getSelectedItem());
            addButton.setEnabled(fieldNameBox.getSelectedItem() != null);
        } else if (e.getSource() == submitButton) {
            if (indexName.getText().equals("")) {
                JOptionPane.showMessageDialog(this, "Specify the name of the index!");
            } else {
                String fields = queryAreaMessage.getText().replace("\n", ", ");
                fields = fields.substring(0, fields.lastIndexOf(", "));
                String unique = uniqueCheckbox.isSelected() ? " UNIQUE" : "";
                String query = "CREATE" + unique + " INDEX " + indexName.getText() + "\nON " + databaseComboBox.getSelectedItem() + "." + tableComboBox.getSelectedItem() + "(" + fields + ");\n";
                JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
                clientInterface.writeIntoSocket(query);
                indexName.setText("");
                queryAreaMessage.setText("");
                databaseComboBox.setEnabled(true);
                tableComboBox.setEnabled(true);
            }
        } else if (e.getSource() == backButton) {
            indexName.setText("");
            queryAreaMessage.setText("");
            databaseComboBox.setEnabled(true);
            tableComboBox.setEnabled(true);
            uniqueCheckbox.setEnabled(true);
            clientInterface.showMenu();
        }

    }

    public MyComboBox getDatabaseComboBox() {
        return this.databaseComboBox;
    }
}