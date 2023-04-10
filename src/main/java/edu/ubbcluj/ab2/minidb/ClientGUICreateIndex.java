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
    private JTextField indexOn;
    private JLabel databaseNameLabel;
    private MyComboBox databaseBox;
    private JLabel tableNameLabel;
    private MyComboBox tableBox;
    private JButton submitButton;
    private JButton backButton;
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

        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.add(indexNameLabel);
        inputPanel.add(indexName);
        inputPanel.add(indexOnLabel);
        inputPanel.add(new JLabel());
        inputPanel.add(databaseNameLabel);
        inputPanel.add(databaseBox);
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableBox);

        databaseBox.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == databaseBox) {
            tableBox.updateComboBox(clientInterface.getTablesNames((String) databaseBox.getSelectedItem()));
        }

        if (e.getSource() == submitButton) {
            query = "CREATE INDEX " + indexName.getText() + "\nON " + databaseBox.getSelectedItem() + "." + tableBox.getSelectedItem() + ";\n";
            JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
            clientInterface.writeIntoSocket(query);
        } else if (e.getSource() == backButton) {
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