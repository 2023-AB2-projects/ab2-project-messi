package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUIDropDatabase extends JPanel implements ActionListener {
    ClientInterface clientInterface;
    JLabel jlabel;
    JTextField textField;
    JButton submitButton;
    JButton backButton;
    String query;

    public ClientGUIDropDatabase(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        this.setLayout(new GridLayout(2, 2));

        jlabel = new JLabel("Database name: ");
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
            query = "DROP DATABASE " + textField.getText() + ";\n";
            JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
            clientInterface.writeIntoSocket(query);
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
        }
    }
}
