package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUIDropDatabase extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private JButton submitButton;
    private JButton backButton;
    private MyComboBox databaseComboBox;

    public ClientGUIDropDatabase(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        this.setLayout(new GridLayout(2, 2));

        JLabel jlabel = new JLabel("Database Name: ");
        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setSelectedIndex(0);
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        this.add(jlabel);
        this.add(databaseComboBox);
        this.add(backButton);
        this.add(submitButton);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            String query = "DROP DATABASE " + databaseComboBox.getSelectedItem() + ";\n";
            JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
            clientInterface.writeIntoSocket(query);
            databaseComboBox.updateComboBox(clientInterface.getDatabasesNames());
            databaseComboBox.setSelectedIndex(0);
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
        }
    }

    public MyComboBox getDatabaseComboBox(){
        return this.databaseComboBox;
    }
}