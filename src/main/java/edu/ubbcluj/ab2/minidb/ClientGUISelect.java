package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ClientGUISelect extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private JPanel inputPanel;
    private MyComboBox databaseComboBox;
    private MyComboBox tableComboBox;
    private JTextField alias;
    private boolean wasAliasAltered = false;
    private JList<String> fieldsList;
    private JPanel conditionPane;
    private JComboBox<String> conditionField;
    private JComboBox<String> operators;
    private JoinPanel joinPanel;
    private JTextField condition;
    private JButton addConditionButton;
    private JButton addJoinButton;
    private JButton submitButton;
    private JButton backButton;
    private JButton clearAll;
    private JTextArea selectQuery;
    private JTable resultsTable;
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
        databaseComboBox.setPreferredSize(new Dimension(300, 20));
        databaseComboBox.setSelectedIndex(0);

        tableComboBox = new MyComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
        tableComboBox.setPreferredSize(new Dimension(140, 20));
        tableComboBox.setSelectedIndex(0);

        fieldsList = new JList<>(getFields(true));
        fieldsList.setPreferredSize(new Dimension(290, 300));
        fieldsList.setSelectedIndex(0);
        JScrollPane scrollPane = new JScrollPane(fieldsList);

        inputPanel = new JPanel(new GridLayout(0, 2, 10, 1));
        inputPanel.setPreferredSize(new Dimension(690, 370));
        addInputLabel("SELECT:");
        inputPanel.add(scrollPane);
        addInputLabel("FROM");
        addInputLabel("");
        addInputLabel("Database Name:");
        databaseComboBox.setPreferredSize(new Dimension(300, 30));
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
        conditionPane = new JPanel(new GridLayout(2, 3, 5, 5));
        conditionPane.setPreferredSize(new Dimension(340, 50));
        JLabel label = new JLabel("WHERE");
        label.setPreferredSize(new Dimension(200, 20));
        conditionPane.add(label);
        label = new JLabel("");
        label.setPreferredSize(new Dimension(200, 20));
        conditionPane.add(label);
        label = new JLabel("");
        label.setPreferredSize(new Dimension(200, 20));
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
        clearAll = new JButton("Clear");

        // creating a button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setPreferredSize(new Dimension(690, 30));
        buttonPanel.add(addConditionButton);
        buttonPanel.add(addJoinButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);
        buttonPanel.add(clearAll);

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
        tempPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tempPanel.add(inputPanel, BorderLayout.NORTH);
        tempPanel.add(joinPanel, BorderLayout.CENTER);
        tempPanel.add(conditionPane, BorderLayout.SOUTH);

        // creating the select panel
        JPanel selectPanel = new JPanel(new BorderLayout());
        selectPanel.setPreferredSize(new Dimension(680, 750));
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
        joinPanel.getAlias().addActionListener(e -> {
            if (!joinPanel.getAlias().isEditable()) {
                updateFieldsList(true);
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
        if (!wasAliasAltered) {
            joinPanel.getAttrOfMainTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()), "", alias.getText());
            updateFieldsList(true);
            updateWhere();
            wasAliasAltered = true;
//            updateQuery();
        } else if (!alias.getText().isBlank()) {
            joinPanel.getAttrOfMainTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()), "", alias.getText());
            updateFieldsList(true);
            updateWhere();
            //updateQuery();
        } else {
            updateFieldsList(true);
            updateWhere();
            // updateQuery();
        }
    }

    private void addListeners() {
        databaseComboBox.addActionListener(this);
        tableComboBox.addActionListener(this);
        addConditionButton.addActionListener(this);
        addJoinButton.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);
        clearAll.addActionListener(this);
        fieldsList.addListSelectionListener(e -> updateQuery());
        alias.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    alias.setEditable(false);
                    aliasDict.put(alias.getText().strip(), (String) tableComboBox.getSelectedItem());
                } else {
                    updateAlias();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    alias.setEditable(false);
                    aliasDict.put(alias.getText().strip(), (String) tableComboBox.getSelectedItem());
                } else {
                    updateAlias();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    alias.setEditable(false);
                    aliasDict.put(alias.getText().strip(), (String) tableComboBox.getSelectedItem());
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
            updateFieldsList(true);
            updateWhere();
            if (joinPanel.isVisible()) {
                joinPanel.getSelectedJoinOn().updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
                joinPanel.getAttrOfMainTable().updateComboBox(clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()), "", alias.getText());
            }
        } else if (e.getSource() == tableComboBox) {
            updateFieldsList(true);
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

//            updateFieldsList(true);
//            updateWhere();

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
            } else {
                selectQuery.append("\n     INNER JOIN " + joinPanel.getSelectedJoinOn().getSelectedItem() + " " + joinPanel.getAlias().getText() + " ON " + joinPanel.getAttrOfJoinedTable().getSelectedItem() + " = " + joinPanel.getAttrOfMainTable().getSelectedItem());
            }

            if (joinPanel.isVisible()) {
                joinPanel.setVisible(false);
                joinPanel.getAlias().setText("");
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
        } else if (e.getSource() == clearAll) {
            selectQuery.setText("");
            databaseComboBox.updateComboBox(clientInterface.getDatabasesNames());
            databaseComboBox.setSelectedIndex(0);
            tableComboBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
            updateQuery();
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

    public void updateQuery() {
        String[] selectJoin = selectQuery.getText().split("\n     INNER JOIN ");
        String[] selectWhere = selectQuery.getText().split("\nWHERE ");
        StringBuilder update = new StringBuilder("SELECT ");
        // if there are selected fields
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
        }
        // if there are not selected fields, by default, select everything (*)
        else {
            fieldsList.setSelectedIndex(0);
            update.append(fieldsList.getSelectedValue()).append("\nFROM ").append(databaseComboBox.getSelectedItem()).append(".").append(tableComboBox.getSelectedItem());
            if (!alias.getText().isBlank()) {
                update.append(" ").append(alias.getText());
            }
        }

        if (selectJoin.length >= 2) {
            for (int i = 1; i < selectJoin.length; i++) {
                if (selectJoin[i].contains("\nWHERE")) {
                    update.append("\n     INNER JOIN ").append(selectJoin[i].split("\nWHERE")[0]);
                } else {
                    update.append("\n     INNER JOIN ").append(selectJoin[i]);
                }
            }
        }

        if (selectWhere.length >= 2) {
            update.append("\nWHERE ").append(selectWhere[1]);
        }

        selectQuery.setText(update.toString());
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
        fieldsList.setListData(getFields(b));
        fieldsList.setSelectedIndex(0);
        inputPanel.revalidate();
        inputPanel.repaint();
        this.revalidate();
        this.repaint();
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
        //updateQuery();
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