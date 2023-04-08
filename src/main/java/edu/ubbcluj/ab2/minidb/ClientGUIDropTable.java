package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Objects;

public class ClientGUIDropTable extends JPanel implements ActionListener {
    ClientInterface clientInterface;
    JLabel databaseLabel;
    JLabel tabelLabel;
    JComboBox<String> jComboBoxTable;
    JComboBox<String> jComboBoxDatabase;
    JButton submitButton;
    JButton backButton;
    String query;

    public ClientGUIDropTable(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        this.setLayout(new GridLayout(3, 2));

        databaseLabel = new JLabel("Database Name: ");
        jComboBoxDatabase = new JComboBox<>();
        tabelLabel = new JLabel("Table Name: ");
        jComboBoxTable = new JComboBox<>();
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");


        jComboBoxDatabase.addActionListener(this);
        jComboBoxTable.addActionListener(this);
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
        if(e.getSource() == jComboBoxDatabase) {
            updateTableComboBox();
        }
        if (e.getSource() == submitButton) {
            query = "DROP TABLE " + jComboBoxDatabase.getSelectedItem() + "." + jComboBoxTable.getSelectedItem() + ";\n";
            JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
            clientInterface.writeIntoSocket(query);
            updateTableComboBox();
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

    public void updateTableComboBox() {
        jComboBoxTable.removeAllItems();
        String[] elements = clientInterface.getTablesNames().split(" ");
        Arrays.sort(elements);
        for (String element : elements) {
            jComboBoxTable.addItem(element);
        }
    }

    public Object getSelectedDatabase() {
        return jComboBoxDatabase.getSelectedItem();
    }

    public void setjComboBoxDatabase(JComboBox<String> jComboBoxDatabase) {
        this.jComboBoxDatabase = jComboBoxDatabase;
    }
}
