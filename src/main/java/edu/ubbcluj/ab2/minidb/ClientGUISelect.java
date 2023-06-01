package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ClientGUISelect extends JPanel implements ActionListener {
    JPanel inputPanel;
    private ClientInterface clientInterface;
    private MyComboBox databaseComboBox;
    private MyComboBox tableComboBox;

    private JButton submitButton;
    private JButton backButton;
    private JList<String> fieldsList;
    private JTextArea selectQuery;


    public ClientGUISelect(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        JLabel selectLabel = new JLabel("SELECT:");
        JLabel databaseNameLabel = new JLabel("Database Name:");
        JLabel tableNameLabel = new JLabel("Table Name:");

        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setSelectedIndex(0);
        tableComboBox = new MyComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
        tableComboBox.setSelectedIndex(0);
        fieldsList = new JList<>(getFields());
        fieldsList.setSelectedIndex(0);

        inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.add(selectLabel);
        inputPanel.add(fieldsList);
        inputPanel.add(new JLabel("FROM:"));
        inputPanel.add(new JLabel());
        inputPanel.add(databaseNameLabel);
        inputPanel.add(databaseComboBox);
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableComboBox);

        selectQuery = new JTextArea("", 10, 40);
        selectQuery.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(selectQuery);
        updateQuery();

        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        databaseComboBox.addActionListener(this);
        tableComboBox.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        fieldsList.addListSelectionListener(e -> {
            updateQuery();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        JPanel selectPanel = new JPanel(new BorderLayout());
        selectPanel.add(new JLabel("Select statement:"), BorderLayout.NORTH);
        selectPanel.add(scrollPane, BorderLayout.CENTER);

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(selectPanel, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == databaseComboBox) {
            tableComboBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
            tableComboBox.setSelectedIndex(0);
            fieldsList.removeAll();
            fieldsList.setListData(getFields());
            fieldsList.setSelectedIndex(0);
            updateQuery();
            inputPanel.revalidate();
            inputPanel.repaint();
            // selectQuery.setText("SELECT " + fieldsList.getSelectedValue() + "\nFROM " + databaseComboBox.getSelectedItem() + "." + tableComboBox.getSelectedItem());
        } else if (e.getSource() == tableComboBox) {
            fieldsList.removeAll();
            fieldsList.setListData(getFields());
            fieldsList.setSelectedIndex(0);
            updateQuery();
            inputPanel.revalidate();
            inputPanel.repaint();
            // TODO: jelenitse meg hogy az adott tablaban milyen attr-ok vannal
            // updateTextFields();
        } else if (e.getSource() == submitButton) {
            JOptionPane.showMessageDialog(this, "SQL query:\n" + selectQuery.getText());
            clientInterface.writeIntoSocket(selectQuery.getText());
            selectQuery.setText("");
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
            selectQuery.setText("");
        }
    }

    public String[] getFields() {
        ArrayList<String> fieldsList = new ArrayList<>();
        fieldsList.add("*");

        for (String attrName : clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()).split(" ")) {
            fieldsList.add(tableComboBox.getSelectedItem() + "." + attrName);
        }

        String[] fields = new String[fieldsList.size()];
        for (int i = 0; i < fieldsList.size(); i++) {
            fields[i] = fieldsList.get(i);
        }

        return fields;
    }

    public void updateQuery() {
        // if there are selected fields
        StringBuilder update = new StringBuilder("SELECT ");
        if (!fieldsList.isSelectionEmpty()) {
            int[] selectedIndices = fieldsList.getSelectedIndices();
            int indexOfStar = getIndexOfSelectedItem("*");

            // if every field of the table is selected, rewrite it as SELECT *
            if (selectedIndices.length == fieldsList.getModel().getSize() - 1 && indexOfStar == -1) {
                fieldsList.clearSelection();
                fieldsList.setSelectedIndex(0);
            } else {
                if (selectedIndices.length > 1 && indexOfStar != -1) {
                    fieldsList.clearSelection();
                    for (int i = 0; i < selectedIndices.length; i++) {
                        if (selectedIndices[i] != indexOfStar) {
                            fieldsList.setSelectedIndex(selectedIndices[i]);
                        }
                    }
                    System.out.println();
                    selectedIndices = fieldsList.getSelectedIndices();
                }
                for (int i = 0; i < selectedIndices.length - 1; i++) {
                    update.append(fieldsList.getModel().getElementAt(selectedIndices[i])).append(", ");
                }
            }
            update.append(fieldsList.getModel().getElementAt(selectedIndices[selectedIndices.length - 1]));
            update.append("\nFROM ").append(databaseComboBox.getSelectedItem()).append(".").append(tableComboBox.getSelectedItem());
            selectQuery.setText(update.toString());
        }
        // if there are not selected fields, by default, select everything (*)
        else {
            fieldsList.setSelectedIndex(0);
            selectQuery.setText("SELECT " + fieldsList.getSelectedValue() + "\nFROM " + databaseComboBox.getSelectedItem() + "." + tableComboBox.getSelectedItem());
        }
    }

    public int getIndexOfSelectedItem(String value) {
        for (int i : fieldsList.getSelectedIndices()) {
            if (fieldsList.getModel().getElementAt(i).equals(value))
                return i;
        }
        return -1;
    }

    public MyComboBox getDatabaseComboBox() {
        return this.databaseComboBox;
    }
}