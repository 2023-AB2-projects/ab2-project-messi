package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientGUISelect extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private MyComboBox databaseComboBox;
    private MyComboBox tableComboBox;
    private JList<String> fieldsList;
    private JButton submitButton;
    private JButton addConditionButton;
    private JComboBox<String> conditionField;
    private JComboBox<String> operators;
    private JTextField condition;
    private JButton backButton;
    private JTextArea selectQuery;
    private JTable resultsTable;


    public ClientGUISelect(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        JLabel selectLabel = new JLabel("SELECT:");
        JLabel databaseNameLabel = new JLabel("Database Name:");
        JLabel tableNameLabel = new JLabel("Table Name:");

        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setSelectedIndex(0);
        tableComboBox = new MyComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
        tableComboBox.setSelectedIndex(0);
        fieldsList = new JList<>(getFields(true));
        fieldsList.setSelectedIndex(0);

        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.add(selectLabel);
        inputPanel.add(fieldsList);
        inputPanel.add(new JLabel("FROM:"));
        inputPanel.add(new JLabel());
        inputPanel.add(databaseNameLabel);
        inputPanel.add(databaseComboBox);
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableComboBox);
        inputPanel.add(new JLabel("WHERE:"));
        inputPanel.add(new JLabel());

        JPanel conditionPane = new JPanel(new GridLayout(1, 2, 5, 10));
        conditionField = new JComboBox<>(getFields(false));
        conditionField.setSelectedIndex(0);
        conditionField.addActionListener(this);
        operators = new JComboBox<>();
        condition = new JTextField("");
        condition.setSize(80, 30);
        condition.setEditable(true);
        updateOperators();
        operators.setSelectedIndex(0);
        conditionPane.add(conditionField);
        conditionPane.add(operators);
        inputPanel.add(conditionPane);
        inputPanel.add(condition);

        selectQuery = new JTextArea("", 10, 40);
        selectQuery.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(selectQuery);
        updateQuery();

        submitButton = new JButton("Submit");
        backButton = new JButton("Back");
        addConditionButton = new JButton("Add condition");
        databaseComboBox.addActionListener(this);
        tableComboBox.addActionListener(this);
        addConditionButton.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);
        fieldsList.addListSelectionListener(e -> updateQuery());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addConditionButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        JPanel selectPanel = new JPanel(new BorderLayout());
        selectPanel.add(new JLabel("Select statement:"), BorderLayout.NORTH);
        selectPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel resultsPanel = new JPanel(new BorderLayout());
        //resultsPanel.setPreferredSize(new Dimension(500, 800));
        resultsPanel.add(new JLabel("\n RESULTS"), BorderLayout.NORTH);

        resultsTable = new JTable();
        JScrollPane scrollResults = new JScrollPane(resultsTable);
        scrollResults.setPreferredSize(new Dimension(500, 740));
        resultsPanel.add(scrollResults, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setSize(500, 800);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(selectPanel, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(panel, BorderLayout.WEST);
        this.add(resultsPanel, BorderLayout.EAST);

        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == databaseComboBox) {
            tableComboBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
            tableComboBox.setSelectedIndex(0);
            updateWhere();
        } else if (e.getSource() == tableComboBox) {
            updateWhere();
        } else if (e.getSource() == conditionField) {
            updateOperators();
        } else if (e.getSource() == addConditionButton) {
            if (condition.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Fill in the condition first!");
            } else {
                String cond = conditionField.getSelectedItem() + " " + operators.getSelectedItem();
                // if there was only 1 operator in the combo box, then it means teh field is a string/date/datetime, and has to be surrounded with ''
                if (operators.getItemCount() == 1) {
                    cond += " '" + condition.getText() + "'";
                } else {
                    cond += " " + condition.getText();
                }
                if (selectQuery.getText().contains("WHERE")) {
                    selectQuery.append(" AND " + cond);
                } else {
                    selectQuery.append("\nWHERE " + cond);
                }
            }
            condition.setText("");
        } else if (e.getSource() == submitButton) {
            JOptionPane.showMessageDialog(this, "SQL query:\n" + selectQuery.getText());
            clientInterface.writeIntoSocket(selectQuery.getText());
            updateQuery();
            showResults();
        } else if (e.getSource() == backButton) {
            resultsTable.setModel(new DefaultTableModel(0, 0));
            clientInterface.setSize(500, 800);
            clientInterface.showMenu();
            selectQuery.setText("");
        }
    }

    public String[] getFields(boolean all) {
        ArrayList<String> fieldsList = new ArrayList<>();
        if (all) {
            fieldsList.add("*");
        }

        // join eseten: fieldsList.add(tableComboBox.getSelectedItem() + "." + attrName);
        fieldsList.addAll(Arrays.asList(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()).split(" ")));

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
                    for (int selectedIndex : selectedIndices) {
                        if (selectedIndex != indexOfStar) {
                            fieldsList.setSelectedIndex(selectedIndex);
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

    public void updateOperators() {
        String type = clientInterface.getAttributeType((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem(), (String) conditionField.getSelectedItem());
        if (!type.equals("NUMERIC")) {
            operators.removeAllItems();
            operators.addItem("=");
        } else {
            operators.removeAllItems();
            for (String item : new String[]{"=", ">", ">=", "<", "<="}) {
                operators.addItem(item);
            }
        }
        condition.setText("");
    }

    public void updateWhere() {
        fieldsList.removeAll();
        fieldsList.setListData(getFields(true));
        fieldsList.setSelectedIndex(0);
        conditionField.removeAll();
        for (String field : getFields(false)) {
            conditionField.addItem(field);
        }
        conditionField.setSelectedIndex(0);
        updateQuery();
        updateOperators();
    }

    public int getIndexOfSelectedItem(String value) {
        for (int i : fieldsList.getSelectedIndices()) {
            if (fieldsList.getModel().getElementAt(i).equals(value))
                return i;
        }
        return -1;
    }

    public void showResults() {
        Object[] columnNames = clientInterface.readFromSocket().split("#");
        String values = clientInterface.readFromSocket();

        resultsTable.setModel(new DefaultTableModel(columnNames, 0));
        DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();

        // if there are no results
        if (values.split("#")[0].equals("")) {
            return;
        }
        for (String row : values.split("#")) {
            model.addRow(row.split(" "));
        }
        model.fireTableDataChanged();
    }

    public MyComboBox getDatabaseComboBox() {
        return this.databaseComboBox;
    }
}