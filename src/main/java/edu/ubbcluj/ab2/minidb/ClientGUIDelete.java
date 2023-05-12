package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUIDelete extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private MyComboBox databaseComboBox;
    private MyComboBox tableComboBox;
    private JTextField keyValueField;
    private JButton submitButton;
    private JButton backButton;

    public ClientGUIDelete(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        JLabel databaseNameLabel = new JLabel("Database Name:");
        JLabel tableNameLabel = new JLabel("Table Name:");
        JLabel keyValueLabel = new JLabel("Key value:");

        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setSelectedIndex(0);
        tableComboBox = new MyComboBox((clientInterface.getTableNames((String) databaseComboBox.getSelectedItem())));
        tableComboBox.setSelectedIndex(0);
        keyValueField = new JTextField(20);
        keyValueField.setText("");

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.add(databaseNameLabel);
        inputPanel.add(databaseComboBox);
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableComboBox);
        inputPanel.add(keyValueLabel);
        inputPanel.add(keyValueField);

        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        databaseComboBox.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);

        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == databaseComboBox) {
            tableComboBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
        }
        if (e.getSource() == submitButton) {
            if (keyValueField.getText().equals("")) {
                JOptionPane.showMessageDialog(this, "Fill in the Key value field first!");
            } else {
                String databaseName = (String) databaseComboBox.getSelectedItem();
                String tableName = (String) tableComboBox.getSelectedItem();
                String query = "DELETE FROM " + databaseName + "." + tableName + "\nWHERE key = " + keyValueField.getText() + ";";
                JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
                clientInterface.writeIntoSocket(query);
            }
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
        }
    }

    public MyComboBox getDatabaseComboBox() {
        return this.databaseComboBox;
    }
}
