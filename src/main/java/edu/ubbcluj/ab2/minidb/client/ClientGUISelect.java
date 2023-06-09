package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ClientGUISelect extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private JPanel inputPanel;
    private JComboBox<String> aggFunctions;
    private MyComboBox databaseComboBox;
    private MyComboBox tableComboBox;
    private JTextField alias;
    private JComboBox<String> fieldsList;
    private JPanel conditionPane;
    private JComboBox<String> conditionField;
    private JComboBox<String> operators;
    private JoinPanel joinPanel;
    private JPanel groupByPanel;
    private JComboBox<String> groupByList;
    private JTextField condition;
    private JButton selectButton;
    private JButton addConditionButton;
    private JButton addJoinButton;
    private JButton addGroupByButton;
    private JButton submitButton;
    private JButton backButton;
    private JButton clearAll;
    private JTextArea selectQuery;
    private JTable resultsTable;
    private boolean containsAggregation = false;
    private Dictionary<String, String> aliasDict;


    public ClientGUISelect(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;
        initializeComponents();
        addListeners();
        aliasDict = new Hashtable<>();
        this.setVisible(true);
    }

    private void initializeComponents() {
        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setPreferredSize(new Dimension(300, 30));
        databaseComboBox.setSelectedIndex(0);

        tableComboBox = new MyComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
        tableComboBox.setPreferredSize(new Dimension(150, 20));
        tableComboBox.setSelectedIndex(0);

        fieldsList = new JComboBox<>(getFields(true));
        fieldsList.setPreferredSize(new Dimension(290, 300));
        fieldsList.setSelectedIndex(0);

        inputPanel = new JPanel(new GridLayout(0, 2, 6, 1));
        inputPanel.setPreferredSize(new Dimension(680, 340));
        JPanel temp = new JPanel(new GridLayout(1, 2, 6, 5));
        temp.setPreferredSize(new Dimension(300, 20));
        selectButton = new JButton("SELECT");
        selectButton.setPreferredSize(new Dimension(40, 20));
        aggFunctions = new JComboBox<>(new String[]{"NONE", "MIN", "MAX", "AVG", "COUNT"});
        aggFunctions.setPreferredSize(new Dimension(150, 20));
        aggFunctions.setSelectedIndex(0);
        temp.add(selectButton);
        temp.add(aggFunctions);

        inputPanel.add(temp);
        inputPanel.add(fieldsList);

        addInputLabel("FROM");
        addInputLabel("");
        addInputLabel("Database Name:");
        inputPanel.add(databaseComboBox);

        JPanel tablePanel = new JPanel(new GridLayout(1, 3, 10, 0));
        tablePanel.setPreferredSize(new Dimension(340, 20));
        tablePanel.setMaximumSize(new Dimension(340, 20));
        tablePanel.add(new JLabel("Table Name:"));
        tablePanel.add(tableComboBox);
        tablePanel.add(new JLabel("AS"));
        inputPanel.add(tablePanel);
        alias = new JTextField();
        alias.setEditable(true);
        inputPanel.add(alias);

        // creating condition panel
        conditionPane = new JPanel(new GridLayout(2, 3, 5, 3));
        conditionPane.setPreferredSize(new Dimension(600, 50));
        JLabel label = new JLabel("WHERE");
        label.setPreferredSize(new Dimension(200, 20));
        conditionPane.add(label);
        label = new JLabel("");
        label.setPreferredSize(new Dimension(200, 20));
        conditionPane.add(label);
        label = new JLabel("");
        label.setPreferredSize(new Dimension(190, 20));
        conditionPane.add(label);

        conditionField = new JComboBox<>(getFields(false));
        conditionField.addActionListener(this);
        operators = new JComboBox<>();
        operators.setPreferredSize(new Dimension(90, 40));
        condition = new JTextField("");
        condition.setEditable(true);
        condition.setPreferredSize(new Dimension(340, 40));
        updateOperators();
        operators.setSelectedIndex(0);
        conditionField.setSelectedIndex(0);
        conditionField.setPreferredSize(new Dimension(240, 30));

        conditionPane.add(conditionField);
        conditionPane.add(operators);
        conditionPane.add(condition);

        // creating the join panel
        createJoinPanel();

        // creating the select query panel
        selectQuery = new JTextArea("SELECT *\nFROM " + databaseComboBox.getSelectedItem() + "." + tableComboBox.getSelectedItem(), 30, 50);
        selectQuery.setPreferredSize(new Dimension(680, 190));
        selectQuery.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(selectQuery);
        scrollPane.setPreferredSize(new Dimension(690, 180));
        //updateQuery();

        JPanel queryPanel = new JPanel(new BorderLayout());
        queryPanel.add(new JLabel("Select statement:"), BorderLayout.NORTH);
        queryPanel.add(scrollPane, BorderLayout.CENTER);


        addConditionButton = new JButton("Add condition");
        addConditionButton.setSize(new Dimension(120, 25));
        addJoinButton = new JButton("Add table join");
        addJoinButton.setSize(new Dimension(115, 25));
        addGroupByButton = new JButton("Add group by");
        addGroupByButton.setSize(new Dimension(110, 25));
        submitButton = new JButton("Submit");
        submitButton.setSize(new Dimension(90, 25));
        backButton = new JButton("Back");
        backButton.setSize(new Dimension(70, 25));
        clearAll = new JButton("Clear");
        clearAll.setSize(new Dimension(70, 25));

        // creating a button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setPreferredSize(new Dimension(680, 25));
        buttonPanel.setMaximumSize(new Dimension(680, 25));
        buttonPanel.setMinimumSize(new Dimension(680, 25));
        buttonPanel.add(addConditionButton);
        buttonPanel.add(addJoinButton);
        buttonPanel.add(addGroupByButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);
        buttonPanel.add(clearAll);

        // creating the results table
        resultsTable = new JTable();
        JScrollPane scrollResults = new JScrollPane(resultsTable);
        scrollResults.setPreferredSize(new Dimension(700, 730));

        // creating results panel
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        resultsPanel.setPreferredSize(new Dimension(510, 800));
        // adding elements to the results panel
        resultsPanel.add(new JLabel("Results"), BorderLayout.NORTH);
        resultsPanel.add(scrollResults, BorderLayout.SOUTH);

        // creating group by panel
        groupByPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        groupByPanel.setPreferredSize(new Dimension(0, 0));
        label = new JLabel("GROUP BY");
        label.setPreferredSize(new Dimension(150, 20));
        groupByPanel.add(label);

        groupByList = new JComboBox<>(getFields(false));
        groupByList.setPreferredSize(new Dimension(290, 0));
        groupByList.setSelectedIndex(0);
        groupByPanel.add(groupByList);
        groupByPanel.setVisible(false);

        temp = new JPanel(new BorderLayout());
        temp.setBorder(BorderFactory.createEmptyBorder(8, 0, 5, 5));
        temp.setPreferredSize(new Dimension(350, 140));
        temp.add(conditionPane, BorderLayout.NORTH);
        temp.add(groupByPanel, BorderLayout.CENTER);


        JPanel tempPanel = new JPanel(new BorderLayout());
        tempPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tempPanel.setPreferredSize(new Dimension(690, 550));
        tempPanel.add(inputPanel, BorderLayout.NORTH);
        tempPanel.add(joinPanel, BorderLayout.CENTER);
        tempPanel.add(temp, BorderLayout.SOUTH);

        // creating the select panel
        JPanel selectPanel = new JPanel(new BorderLayout());
        selectPanel.setPreferredSize(new Dimension(700, 820));
        selectPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 2));
        selectPanel.add(tempPanel, BorderLayout.NORTH);
        selectPanel.add(buttonPanel, BorderLayout.CENTER);
        selectPanel.add(queryPanel, BorderLayout.SOUTH);

        JScrollPane selectScrollPane = new JScrollPane();
        selectScrollPane.setPreferredSize(new Dimension(710, 800));
        selectScrollPane.setViewportView(selectPanel);

        this.setLayout(new BorderLayout());
        this.add(selectScrollPane, BorderLayout.WEST);
        this.add(resultsPanel, BorderLayout.CENTER);

        this.setVisible(true);
    }

    private void addInputLabel(String text) {
        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(320, 15));
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
        joinPanel.getAlias().addActionListener(e -> {
            if (!joinPanel.getAlias().isEditable()) {
                updateFieldsList(true);
                updateGroupByFields();
                updateWhere();
            }
        });
        joinPanel.getAlias().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!joinPanel.getAlias().getText().isBlank()) {
                    if (e.getKeyChar() == '\n') {
                        joinPanel.getAttrOfJoinedTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) joinPanel.getSelectedJoinOn().getSelectedItem()), "", joinPanel.getAlias().getText().strip());
                        joinPanel.getAlias().setEditable(false);
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (!joinPanel.getAlias().getText().isBlank()) {
                    if (e.getKeyChar() == '\n') {
                        joinPanel.getAttrOfJoinedTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) joinPanel.getSelectedJoinOn().getSelectedItem()), "", joinPanel.getAlias().getText().strip());
                        joinPanel.getAlias().setEditable(false);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!joinPanel.getAlias().getText().isBlank()) {
                    if (e.getKeyChar() == '\n') {
                        joinPanel.getAttrOfJoinedTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) joinPanel.getSelectedJoinOn().getSelectedItem()), "", joinPanel.getAlias().getText().strip());
                        joinPanel.getAlias().setEditable(false);
                    }
                }
            }
        });
    }

    public void updateAlias() {
        if (!alias.getText().isBlank()) {
            joinPanel.getAttrOfMainTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()), "", alias.getText());
            updateFieldsList(true);
            updateGroupByFields();
            updateWhere();
        }
    }

    private void addListeners() {
        databaseComboBox.addActionListener(this);
        tableComboBox.addActionListener(this);
        addConditionButton.addActionListener(this);
        addJoinButton.addActionListener(this);
        addGroupByButton.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);
        clearAll.addActionListener(this);
        groupByList.addActionListener(this);
        selectButton.addActionListener(this);
        aggFunctions.addActionListener(this);
        alias.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    alias.setEditable(false);
                    aliasDict.put(alias.getText().strip(), (String) tableComboBox.getSelectedItem());
                    String[] query = selectQuery.getText().split("\n");
                    StringBuilder update = new StringBuilder(query[0] + "\n" + query[1] + " " + alias.getText());
                    for (int i = 2; i < query.length; i++) {
                        update.append("\n").append(query[i]);
                    }
                    selectQuery.setText(update.toString());
                } else {
                    updateAlias();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    alias.setEditable(false);
                    aliasDict.put(alias.getText().strip(), (String) tableComboBox.getSelectedItem());
//                    String[] query = selectQuery.getText().split("\n");
//                    StringBuilder update = new StringBuilder(query[0] + "\n" + query[1] + " " + alias.getText());
//                    for (int i = 2; i < query.length; i++) {
//                        update.append("\n").append(query[i]);
//                    }
//                    selectQuery.setText(update.toString());
                } else {
                    updateAlias();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    alias.setEditable(false);
                    aliasDict.put(alias.getText().strip(), (String) tableComboBox.getSelectedItem());
//                    String[] query = selectQuery.getText().split("\n");
//                    StringBuilder update = new StringBuilder(query[0] + "\n" + query[1] + " " + alias.getText());
//                    for (int i = 2; i < query.length; i++) {
//                        update.append("\n").append(query[i]);
//                    }
//                    selectQuery.setText(update.toString());
                } else {
                    updateAlias();
                }
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == databaseComboBox) {
            tableComboBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
            tableComboBox.setSelectedIndex(0);
            updateSelectionListQuery();
            updateFieldsList(true);
            updateWhere();
            updateGroupByFields();
            updateFrom();
            if (joinPanel.isVisible()) {
                joinPanel.getSelectedJoinOn().updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
                joinPanel.getAttrOfMainTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()), "", alias.getText());
            }
        } else if (e.getSource() == tableComboBox) {
            updateSelectionListQuery();
            updateFieldsList(true);
            updateWhere();
            updateGroupByFields();
            updateFrom();
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

                String[] query = selectQuery.getText().split("\nGROUP BY ");
                String update = query[0];
                if (selectQuery.getText().contains("WHERE")) {
                    update += " AND " + cond;
                } else {
                    update += "\nWHERE " + cond;
                }

                if (query.length != 1) {
                    update += "\nGROUP BY " + query[1];
                }

                selectQuery.setText(update);
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

            if (Objects.equals(joinPanel.getSelectedJoinOn().getSelectedItem(), tableComboBox.getSelectedItem()) && joinPanel.getAlias().getText().equals(alias.getText())) {
                JOptionPane.showMessageDialog(this, "Change the alias to prevent ambiguous column names!");
                return;
            }

            aliasDict.put(joinPanel.getAlias().getText(), ((String) joinPanel.getSelectedJoinOn().getSelectedItem()));
            if (selectQuery.getText().contains("WHERE")) {
                String[] split = selectQuery.getText().split("\nWHERE ");
                selectQuery.setText(split[0]);
                selectQuery.append("\n     INNER JOIN " + joinPanel.getSelectedJoinOn().getSelectedItem() + " " + joinPanel.getAlias().getText() + " ON " + joinPanel.getAttrOfJoinedTable().getSelectedItem() + " = " + joinPanel.getAttrOfMainTable().getSelectedItem());
                selectQuery.append("\nWHERE " + split[1]);
            } else if (selectQuery.getText().contains("GROUP BY")) {
                String[] split = selectQuery.getText().split("\nGROUP BY ");
                selectQuery.setText(split[0]);
                selectQuery.append("\n     INNER JOIN " + joinPanel.getSelectedJoinOn().getSelectedItem() + " " + joinPanel.getAlias().getText() + " ON " + joinPanel.getAttrOfJoinedTable().getSelectedItem() + " = " + joinPanel.getAttrOfMainTable().getSelectedItem());
                selectQuery.append("\nGROUP BY " + split[1]);
            } else {
                selectQuery.append("\n     INNER JOIN " + joinPanel.getSelectedJoinOn().getSelectedItem() + " " + joinPanel.getAlias().getText() + " ON " + joinPanel.getAttrOfJoinedTable().getSelectedItem() + " = " + joinPanel.getAttrOfMainTable().getSelectedItem());
            }

            if (joinPanel.isVisible()) {
                joinPanel.setVisible(false);
                joinPanel.getAlias().setText("");
                joinPanel.getAttrOfJoinedTable().updateComboBox("");
                joinPanel.getAlias().setEditable(true);
                this.revalidate();
                this.repaint();
            }
        } else if (e.getSource() == joinPanel.getSelectedJoinOn()) {
            if (!joinPanel.getAlias().getText().isBlank()) {
                joinPanel.updateJoinedAttributes(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) joinPanel.getSelectedJoinOn().getSelectedItem()));
            } else {
                joinPanel.updateJoinedAttributes("");
            }
        } else if (e.getSource() == aggFunctions) {
            if (!Objects.equals(aggFunctions.getSelectedItem(), "COUNT") && !Objects.equals(aggFunctions.getSelectedItem(), "NONE")) {
                if (fieldsList.getItemAt(0).equals("*")) {
                    fieldsList.remove(0);
                }
            } else {
                String[] elements;
                if (!fieldsList.getModel().getElementAt(0).equals("*")) {
                    elements = new String[fieldsList.getModel().getSize() + 1];
                    elements[0] = "*";
                    for (int i = 0; i < fieldsList.getModel().getSize(); i++) {
                        elements[i + 1] = fieldsList.getItemAt(i);
                    }
                } else {
                    elements = new String[fieldsList.getModel().getSize()];
                    for (int i = 0; i < fieldsList.getModel().getSize(); i++) {
                        elements[i] = fieldsList.getItemAt(i);
                    }
                }
                fieldsList.removeAllItems();
                for (String s : elements) {
                    fieldsList.addItem(s);
                }
            }
        } else if (e.getSource() == selectButton) {
            updateSelectionListQuery();
        } else if (e.getSource() == addGroupByButton) {
            if (!groupByPanel.isVisible()) {
                groupByPanel.setPreferredSize(new Dimension(690, 60));
                groupByList.setPreferredSize(new Dimension(210, 40));
                groupByPanel.setVisible(true);
            } else {
                updateGroupByQuery();
            }
        } else if (e.getSource() == submitButton) {
            JOptionPane.showMessageDialog(this, "SQL query:\n" + selectQuery.getText());
            clientInterface.writeIntoSocket(selectQuery.getText());
            showResults();
        } else if (e.getSource() == backButton) {
            resultsTable.setModel(new DefaultTableModel(0, 0));
            clientInterface.setSize(500, 800);
            clientInterface.showMenu();
            selectQuery.setText("");
        } else if (e.getSource() == clearAll) {
            containsAggregation = false;
            alias.setText("");
            alias.setEditable(true);
            updateFrom();
            String[] query = selectQuery.getText().split("\n");
            selectQuery.setText(query[0] + "\n" + query[1]);
        }
    }

    public String[] getFields(boolean all) {
        ArrayList<String> list = new ArrayList<>();

        if (joinPanel != null && joinPanel.isVisible()) {
            // if this is the first join
            if (!selectQuery.getText().contains("JOIN")) {
                if (all) {
                    list.add("*");
                }

                for (int i = 0; i < joinPanel.getAttrOfMainTable().getItemCount(); i++) {
                    list.add(joinPanel.getAttrOfMainTable().getItemAt(i));
                }

                if (!joinPanel.getAlias().getText().isBlank()) {
                    for (int i = 0; i < joinPanel.getAttrOfJoinedTable().getItemCount(); i++) {
                        list.add(joinPanel.getAttrOfJoinedTable().getItemAt(i));
                    }
                }
            } else {
                for (int i = 0; i < fieldsList.getModel().getSize(); i++) {
                    list.add(fieldsList.getModel().getElementAt(i));
                }

                if (!joinPanel.getAlias().getText().isBlank()) {
                    for (int i = 0; i < joinPanel.getAttrOfJoinedTable().getItemCount(); i++) {
                        list.add(joinPanel.getAttrOfJoinedTable().getItemAt(i));
                    }
                }
            }
        } else {
            if (all) {
                list.add("*");
            }

            list.addAll(Arrays.asList(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()).split(" ")));
            if (alias != null && !alias.getText().isBlank()) {
                for (int i = 1; i < list.size(); i++) {
                    list.set(i, alias.getText() + "." + list.get(i));
                }
            }
        }

        String[] fields = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            fields[i] = list.get(i);
        }

        return fields;
    }

    public void updateSelectionListQuery() {
        String[] selection = selectQuery.getText().split("\n");
        if (selection.length < 2) {
            return;
        }
        StringBuilder update;
        if (fieldsList.getSelectedIndex() != -1) {
            containsAggregation = true;
            String agg = (String) aggFunctions.getSelectedItem();
            assert agg != null;
            if (!agg.equals("NONE") && !agg.equals("COUNT") && Objects.equals(fieldsList.getSelectedItem(), "*")) {
                JOptionPane.showMessageDialog(this, "Incorrect syntax near *");
                return;
            }

            if (agg.equals("NONE")) {
                agg = (String) fieldsList.getSelectedItem();
            } else {
                agg += "(" + fieldsList.getSelectedItem() + ")";
            }

            update = new StringBuilder(selection[0]);
            if (selectQuery.getText().contains(" *")) {
                update = new StringBuilder("SELECT " + agg);
            } else {
                if (selection[0].split(" ").length < 2) {
                    update.append(agg);
                } else {
                    update.append(", ").append(agg);
                }
            }

            for (int i = 1; i < selection.length; i++) {
                update.append("\n").append(selection[i]);
            }
            selectQuery.setText(String.valueOf(update));
        }
    }

    private void updateGroupByQuery() {
        if (groupByPanel != null && groupByPanel.isVisible()) {
            String[] query = selectQuery.getText().split("\nGROUP BY ");
            if (groupByList != null) {
                StringBuilder update = new StringBuilder();
                if (groupByList.getSelectedIndex() != -1) {
                    // if there is already group by
                    if (query.length >= 2) {
                        if (!query[1].contains((CharSequence) Objects.requireNonNull(groupByList.getSelectedItem())))
                            update.append("\nGROUP BY ").append(query[1]).append(", ").append(groupByList.getSelectedItem());
                    } else {
                        update.append("\nGROUP BY ").append(groupByList.getSelectedItem());
                    }
                    selectQuery.setText(query[0] + update);
                }
            }
        }
    }

    public void updateOperators() {
        String type;
        if (selectQuery != null && conditionField.getSelectedItem() != null && (selectQuery.getText().contains("JOIN") || !alias.getText().isBlank())) {
            String[] selected = ((String) conditionField.getSelectedItem()).split("\\.");
            type = clientInterface.getAttributeType((String) databaseComboBox.getSelectedItem(), aliasDict.get(selected[0]), selected[1]);
        } else {
            type = clientInterface.getAttributeType((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem(), (String) conditionField.getSelectedItem());
        }

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

    public void updateFieldsList(boolean b) {
        String[] fields = getFields(b);
        fieldsList.removeAllItems();
        for (String s : fields) {
            fieldsList.addItem(s);
        }

        fieldsList.setSelectedIndex(0);
        inputPanel.revalidate();
        inputPanel.repaint();
        this.revalidate();
        this.repaint();
    }

    private void updateFrom() {
        String[] query = selectQuery.getText().split("\n");
        StringBuilder update = new StringBuilder(query[0]);

        update.append("\nFROM ").append(databaseComboBox.getSelectedItem()).append(".").append(tableComboBox.getSelectedItem());

        if (!alias.getText().isBlank())
            update.append(" ").append(alias.getText());

        for (int i = 2; i < query.length; i++) {
            update.append("\n").append(query[i]);
        }

        selectQuery.setText(update.toString());
    }

    public void updateGroupByFields() {
        if (groupByList != null) {
            groupByList.removeAllItems();
            for (int i = 0; i < fieldsList.getModel().getSize(); i++) {
                if (!fieldsList.getModel().getElementAt(i).equals("*"))
                    groupByList.addItem(String.valueOf(fieldsList.getModel().getElementAt(i)));
            }
            groupByList.setSelectedIndex(0);
        }
    }

    public void updateWhere() {
        conditionField.removeAllItems();
        for (int i = 0; i < fieldsList.getModel().getSize(); i++) {
            if (!fieldsList.getModel().getElementAt(i).equals("*"))
                conditionField.addItem(String.valueOf(fieldsList.getModel().getElementAt(i)));
        }

        conditionField.setSelectedIndex(0);
        conditionPane.revalidate();
        conditionPane.repaint();
        inputPanel.revalidate();
        inputPanel.repaint();
        this.revalidate();
        this.repaint();
        updateOperators();
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