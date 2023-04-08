package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class ClientGUIDropDatabase extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private JLabel jlabel;
    private JTextField textField;
    private JButton submitButton;
    private JButton backButton;
    private String query;
    private JComboBox<String> jComboBox;

    public ClientGUIDropDatabase(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        this.setLayout(new GridLayout(2, 2));

        jlabel = new JLabel("Database Name: ");
        jComboBox = new JComboBox<>();
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        this.add(jlabel);
        this.add(jComboBox);
        this.add(backButton);
        this.add(submitButton);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            query = "DROP DATABASE " + jComboBox.getSelectedItem() + ";\n";
            JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
            clientInterface.writeIntoSocket(query);
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
        }
    }

    public void updateDatabaseComboBox() {
        jComboBox.removeAllItems();
        String[] elements = clientInterface.getDatabasesNames().split(" ");
        Arrays.sort(elements);
        for (String element : elements) {
            jComboBox.addItem(element);
        }
    }
}
