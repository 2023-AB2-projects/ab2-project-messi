package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientGUISelect extends JPanel implements ActionListener {
    private ClientInterface clientInterface;
    private JTextField tableNameTextField;
    private JTextField columnsTextField;
    private JTextField whereTextField;
    private JTextField groupByTextField;
    private JTextField havingTextField;
    private JTextField orderByTextField;
    private JTextArea selectStatementTextArea;


    public ClientGUISelect(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        setSize(500, 500);
        setVisible(true);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(6, 2));
        JLabel tableNameLabel = new JLabel("Table name:");
        tableNameTextField = new JTextField();
        JLabel columnsLabel = new JLabel("Columns (separated by commas):");
        columnsTextField = new JTextField();
        JLabel whereLabel = new JLabel("WHERE clause:");
        whereTextField = new JTextField();
        JLabel groupByLabel = new JLabel("GROUP BY clause:");
        groupByTextField = new JTextField();
        JLabel havingLabel = new JLabel("HAVING clause:");
        havingTextField = new JTextField();
        JLabel orderByLabel = new JLabel("ORDER BY clause:");
        orderByTextField = new JTextField();
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableNameTextField);
        inputPanel.add(columnsLabel);
        inputPanel.add(columnsTextField);
        inputPanel.add(whereLabel);
        inputPanel.add(whereTextField);
        inputPanel.add(groupByLabel);
        inputPanel.add(groupByTextField);
        inputPanel.add(havingLabel);
        inputPanel.add(havingTextField);
        inputPanel.add(orderByLabel);
        inputPanel.add(orderByTextField);

        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());
        JLabel selectStatementLabel = new JLabel("Select Statement:");
        selectStatementTextArea = new JTextArea();
        selectStatementTextArea.setEditable(false);
        outputPanel.add(selectStatementLabel, BorderLayout.NORTH);
        outputPanel.add(selectStatementTextArea, BorderLayout.CENTER);

        JButton generateButton = new JButton("Generate Select Statement");
        generateButton.addActionListener(this);

        this.add(inputPanel, BorderLayout.NORTH);
        this.add(outputPanel, BorderLayout.CENTER);
        this.add(generateButton, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Generate Select Statement")) {
            String tableName = tableNameTextField.getText();
            String columns = columnsTextField.getText();
            String where = whereTextField.getText();
            String groupBy = groupByTextField.getText();
            String having = havingTextField.getText();
            String orderBy = havingTextField.getText();

            String selectStatement = "SELECT " + columns + " FROM " + tableName;

            if (!where.isEmpty()) {
                selectStatement += " WHERE " + where;
            }

            if (!groupBy.isEmpty()) {
                selectStatement += " GROUP BY " + groupBy;
            }

            if (!having.isEmpty()) {
                selectStatement += " HAVING " + having;
            }

            if (!orderBy.isEmpty()) {
                selectStatement += " ORDER BY " + orderBy;
            }

            selectStatementTextArea.setText(selectStatement);
        }
    }
}