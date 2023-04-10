package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Objects;

public class ClientGUIDropTable extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private JLabel databaseLabel;
    private JLabel tabelLabel;
    private MyComboBox jComboBoxTable;
    private MyComboBox jComboBoxDatabase;
    private JButton submitButton;
    private JButton backButton;
    private String query;

    public ClientGUIDropTable(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        this.setLayout(new GridLayout(3, 2));

        databaseLabel = new JLabel("Database Name: ");
        jComboBoxDatabase = new MyComboBox(clientInterface.getDatabasesNames());
        jComboBoxDatabase.setSelectedIndex(0);
        tabelLabel = new JLabel("Table Name: ");
        jComboBoxTable = new MyComboBox(clientInterface.getTablesNames((String) jComboBoxDatabase.getSelectedItem()));
        jComboBoxTable.setSelectedIndex(0);
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");


        jComboBoxDatabase.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        this.add(databaseLabel);
        this.add(jComboBoxDatabase);
        this.add(tabelLabel);
        this.add(jComboBoxTable);
        this.add(backButton);
        this.add(submitButton);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jComboBoxDatabase) {
            jComboBoxTable.updateComboBox(clientInterface.getTablesNames((String) jComboBoxDatabase.getSelectedItem()));
        }

        if (e.getSource() == submitButton) {
            query = "DROP TABLE " + jComboBoxDatabase.getSelectedItem() + "." + jComboBoxTable.getSelectedItem() + ";\n";
            JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
            clientInterface.writeIntoSocket(query);
            jComboBoxTable.updateComboBox(clientInterface.getTablesNames((String) jComboBoxDatabase.getSelectedItem()));
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
        }
    }

    public void updateDatabaseComboBox() {
        jComboBoxDatabase.removeAllItems();
        String[] elements = clientInterface.getDatabasesNames().split(" ");
        Arrays.sort(elements);
        for (String element : elements) {
            jComboBoxDatabase.addItem(element);
        }
    }

//    public void updateTableComboBox() {
//        jComboBoxTable.removeAllItems();
//        String[] elements = clientInterface.getTablesNames().split(" ");
//        Arrays.sort(elements);
//        for (String element : elements) {
//            jComboBoxTable.addItem(element);
//        }
//    }
}
