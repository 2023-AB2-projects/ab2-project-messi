package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUICreateDatabase extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private JTextField textField;
    private JButton submitButton;
    private JButton backButton;

    public ClientGUICreateDatabase(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;
        this.setLayout(new GridLayout(2, 2));

        JLabel jlabel = new JLabel("Database Name: ");
        textField = new JTextField();
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        this.add(jlabel);
        this.add(textField);
        this.add(backButton);
        this.add(submitButton);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            String databaseName = textField.getText();
            if (databaseName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "To create a database, ENTER a name.");
            } else {
                String query = "CREATE DATABASE " + databaseName + ";\n";
                JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
                textField.setText("");
                clientInterface.writeIntoSocket(query);
            }
        } else if (e.getSource() == backButton) {
            textField.setText("");
            clientInterface.showMenu();
        }
    }
}