package edu.ubbcluj.ab2.minidb;

import edu.ubbcluj.ab2.minidb.client.ClientInterface;
import edu.ubbcluj.ab2.minidb.models.MyComboBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUIDropTable extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private MyComboBox databaseComboBox;
    private MyComboBox tableComboBox;
    private JButton submitButton;
    private JButton backButton;

    public ClientGUIDropTable(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        this.setLayout(new GridLayout(3, 2));

        JLabel databaseLabel = new JLabel("Database Name: ");
        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setSelectedIndex(0);
        JLabel tableLabel = new JLabel("Table Name: ");
        tableComboBox = new MyComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
        tableComboBox.setSelectedIndex(0);
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");


        databaseComboBox.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        this.add(databaseLabel);
        this.add(databaseComboBox);
        this.add(tableLabel);
        this.add(tableComboBox);
        this.add(backButton);
        this.add(submitButton);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == databaseComboBox) {
            tableComboBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
        }

        if (e.getSource() == submitButton) {
            String query = "DROP TABLE " + databaseComboBox.getSelectedItem() + "." + tableComboBox.getSelectedItem() + ";\n";
            JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
            clientInterface.writeIntoSocket(query);
            tableComboBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
        }
    }

    public MyComboBox getDatabaseComboBox() {
        return this.databaseComboBox;
    }
}
