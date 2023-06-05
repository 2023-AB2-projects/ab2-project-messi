package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ClientGUISelect extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private JPanel inputPanel;
    private MyComboBox databaseComboBox;
    private MyComboBox tableComboBox;
    private JTextField alias;
    private JList<String> fieldsList;
    private JComboBox<String> conditionField;
    private JComboBox<String> operators;
    private JoinPanel joinPanel;
    private JTextField condition;
    private JButton addConditionButton;
    private JButton addJoinButton;
    private JButton submitButton;
    private JButton backButton;
    private JTextArea selectQuery;
    private JTable resultsTable;


    public ClientGUISelect(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;
        initializeComponents();
        addListeners();
        this.setVisible(true);
    }

    private void initializeComponents() {
        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setPreferredSize(new Dimension(300, 20));
        databaseComboBox.setSelectedIndex(0);

        tableComboBox = new MyComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
        tableComboBox.setPreferredSize(new Dimension(140, 20));
        tableComboBox.setSelectedIndex(0);

        fieldsList = new JList<>(getFields(true));
        fieldsList.setPreferredSize(new Dimension(290, 120));
        fieldsList.setSelectedIndex(0);
        JScrollPane scrollPane = new JScrollPane(fieldsList);

        inputPanel = new JPanel(new GridLayout(0, 2, 10, 1));
        inputPanel.setPreferredSize(new Dimension(690, 430));
        addInputLabel("SELECT:");
        inputPanel.add(scrollPane);
        addInputLabel("FROM");
        addInputLabel("");
        addInputLabel("Database Name:");
        inputPanel.add(databaseComboBox);

        JPanel tablePanel = new JPanel(new GridLayout(1, 3, 10, 0));
        tablePanel.setMaximumSize(new Dimension(340, 20));
        tablePanel.add(new JLabel("Table Name:"));
        tablePanel.add(tableComboBox);
        tablePanel.add(new JLabel("AS"));
        inputPanel.add(tablePanel);
        alias = new JTextField();
        alias.setEditable(true);
        inputPanel.add(alias);

        addInputLabel("WHERE");
        addInputLabel("");

        // creating condition panel
        JPanel conditionPane = new JPanel(new GridLayout(1, 2, 5, 0));
        conditionPane.setPreferredSize(new Dimension(340, 30));

        conditionField = new JComboBox<>(getFields(false));
        conditionField.addActionListener(this);
        operators = new JComboBox<>();
        operators.setPreferredSize(new Dimension(90, 20));
        condition = new JTextField("");
        condition.setEditable(true);
        condition.setPreferredSize(new Dimension(340, 20));
        updateOperators();
        operators.setSelectedIndex(0);
        conditionField.setSelectedIndex(0);
        conditionField.setPreferredSize(new Dimension(240, 20));

        conditionPane.add(conditionField);
        conditionPane.add(operators);

        inputPanel.add(conditionPane);
        inputPanel.add(condition);

        // creating the join panel
        createJoinPanel();

        // creating the select query panel
        selectQuery = new JTextArea("", 30, 50);
        selectQuery.setPreferredSize(new Dimension(690, 210));
        selectQuery.setEditable(false);
        scrollPane = new JScrollPane(selectQuery);
        scrollPane.setPreferredSize(new Dimension(690, 200));
        updateQuery();

        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(new JLabel("Select statement:"), BorderLayout.NORTH);
        queryPanel.add(scrollPane, BorderLayout.CENTER);


        addConditionButton = new JButton("Add condition");
        addJoinButton = new JButton("Add table join");
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        // creating a button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setPreferredSize(new Dimension(690, 30));
        buttonPanel.add(addConditionButton);
        buttonPanel.add(addJoinButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        // creating the results table
        resultsTable = new JTable();
        JScrollPane scrollResults = new JScrollPane(resultsTable);
        scrollResults.setPreferredSize(new Dimension(490, 730));

        // creating results panel
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        resultsPanel.setPreferredSize(new Dimension(510, 800));
        // adding elements to the results panel
        resultsPanel.add(new JLabel("Results"), BorderLayout.NORTH);
        resultsPanel.add(scrollResults, BorderLayout.SOUTH);

        JPanel tempPanel = new JPanel(new BorderLayout());
        tempPanel.add(inputPanel, BorderLayout.NORTH);
        tempPanel.add(joinPanel, BorderLayout.SOUTH);

        // creating the select panel
        JPanel selectPanel = new JPanel(new BorderLayout());
        selectPanel.setPreferredSize(new Dimension(700, 800));
        selectPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 2));
        selectPanel.add(tempPanel, BorderLayout.NORTH);
        selectPanel.add(buttonPanel, BorderLayout.CENTER);
        selectPanel.add(queryPanel, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(selectPanel, BorderLayout.WEST);
        this.add(resultsPanel, BorderLayout.CENTER);

        this.setVisible(true);
    }

    private void addInputLabel(String text) {
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(320, 20));
        inputPanel.add(label);
    }

    private void createJoinPanel() {
        String mainTableName = (String) tableComboBox.getSelectedItem();
        String mainTableAlias = alias.getText();
        String listOfTables = clientInterface.getTableNames((String) databaseComboBox.getSelectedItem());
        String joinedAttributes = "";
        String mainAttributes = clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem());
        joinPanel = new JoinPanel(mainTableName, mainTableAlias, listOfTables, joinedAttributes, mainAttributes);
        addActionListenersToJoinPane();
        joinPanel.setVisible(false);
    }

    private void addActionListenersToJoinPane() {
        joinPanel.getSelectedJoinOn().addActionListener(this);
        joinPanel.getAlias().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!alias.getText().isBlank()) {
                    joinPanel.getAttrOfJoinedTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) joinPanel.getSelectedJoinOn().getSelectedItem()), "", joinPanel.getAlias().getText());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (!alias.getText().isBlank()) {
                    joinPanel.getAttrOfJoinedTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) joinPanel.getSelectedJoinOn().getSelectedItem()), "", joinPanel.getAlias().getText());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!alias.getText().isBlank()) {
                    joinPanel.getAttrOfJoinedTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) joinPanel.getSelectedJoinOn().getSelectedItem()), "", joinPanel.getAlias().getText());
                }
            }
        });
    }

    private void addListeners() {
        databaseComboBox.addActionListener(this);
        tableComboBox.addActionListener(this);
        addConditionButton.addActionListener(this);
        addJoinButton.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);
        fieldsList.addListSelectionListener(e -> updateQuery());
        alias.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!alias.getText().isBlank()) {
                    joinPanel.getAttrOfMainTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()), "", alias.getText());
                    updateQuery();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (!alias.getText().isBlank()) {
                    joinPanel.getAttrOfMainTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()), "", alias.getText());
                    updateQuery();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!alias.getText().isBlank()) {
                    joinPanel.getAttrOfMainTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()), "", alias.getText());
                    updateQuery();
                }
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == databaseComboBox) {
            tableComboBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
            tableComboBox.setSelectedIndex(0);
            updateWhere();
            if (joinPanel.isVisible()) {
                joinPanel.getSelectedJoinOn().updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
                joinPanel.getAttrOfMainTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()), "", alias.getText());
            }
        } else if (e.getSource() == tableComboBox) {
            updateWhere();
            if (joinPanel.isVisible()) {
                joinPanel.getSelectedJoinOn().updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
                joinPanel.getAttrOfMainTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()), "", alias.getText());
            }
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
        } else if (e.getSource() == addJoinButton) {
            if (alias.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Fill in the alias first!");
                return;
            }

            if (!joinPanel.isVisible()) {
                joinPanel.setVisible(true);
                this.revalidate();
                this.repaint();
                return;
            }

            if (joinPanel.getAlias().getText().isBlank() || Objects.equals(joinPanel.getAttrOfJoinedTable().getSelectedItem(), "")) {
                JOptionPane.showMessageDialog(this, "Fill in the join panel!");
                return;
            }

            if(Objects.equals(joinPanel.getSelectedJoinOn().getSelectedItem(), tableComboBox.getSelectedItem()) && joinPanel.getAlias().getText().equals(alias.getText())){
                JOptionPane.showMessageDialog(this, "Change tha alias to prevent ambiguous column names!");
                return;
            }

            // updateQuery();
            if (selectQuery.getText().contains("WHERE")) {
                String[] split = selectQuery.getText().split("\nWHERE ");
                selectQuery.setText(split[0]);
                selectQuery.append("\n     INNER JOIN " + joinPanel.getSelectedJoinOn().getSelectedItem() + " " + joinPanel.getAlias().getText() + " ON " + joinPanel.getAttrOfJoinedTable().getSelectedItem() + " = " + joinPanel.getAttrOfMainTable().getSelectedItem());
                selectQuery.append("\nWHERE " + split[1]);
            } else {
                selectQuery.append("\n     INNER JOIN " + joinPanel.getSelectedJoinOn().getSelectedItem() + " " + joinPanel.getAlias().getText() + " ON " + joinPanel.getAttrOfJoinedTable().getSelectedItem() + " = " + joinPanel.getAttrOfMainTable().getSelectedItem());
            }
        } else if (e.getSource() == joinPanel.getSelectedJoinOn()) {
            if (!joinPanel.getAlias().getText().isBlank()) {
                joinPanel.updateJoinedAttributes(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) joinPanel.getSelectedJoinOn().getSelectedItem()));
            } else {
                joinPanel.updateJoinedAttributes("");
            }
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
            if (!alias.getText().isBlank()) {
                update.append(" ").append(alias.getText());
            }
            selectQuery.setText(update.toString());
        }
        // if there are not selected fields, by default, select everything (*)
        else {
            fieldsList.setSelectedIndex(0);
            selectQuery.setText("SELECT " + fieldsList.getSelectedValue() + "\nFROM " + databaseComboBox.getSelectedItem() + "." + tableComboBox.getSelectedItem());
            if (!alias.getText().isBlank()) {
                selectQuery.append(" " + alias.getText());
            }
        }
    }

    public void updateOperators() {
        System.out.println((String) tableComboBox.getSelectedItem());
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