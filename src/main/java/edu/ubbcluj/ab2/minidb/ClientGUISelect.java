package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class ClientGUISelect extends JPanel {
    JPanel inputPanel;
    private ClientInterface clientInterface;
    private MyComboBox databaseComboBox;
    private MyComboBox tableComboBox;
    private MyComboBox fieldComboBox;


    public ClientGUISelect(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        JLabel selectLabel = new JLabel("SELECT:");
        JLabel fromLabel = new JLabel("FROM:");

        JLabel databaseNameLabel = new JLabel("Database Name:");
        JLabel tableNameLabel = new JLabel("Table Name:");

        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setSelectedIndex(0);
        tableComboBox = new MyComboBox((clientInterface.getTableNames((String) databaseComboBox.getSelectedItem())));
        tableComboBox.setSelectedIndex(0);

        inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.add(selectLabel);
        inputPanel.add(new JLabel());
        inputPanel.add(fromLabel);
        inputPanel.add(new JLabel());
        inputPanel.add(databaseNameLabel);
        inputPanel.add(databaseComboBox);
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableComboBox);

        this.add(inputPanel);
    }
}