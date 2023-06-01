package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ClientGUIInsert extends JPanel implements ActionListener {
    private JPanel inputPanel;
    private ArrayList<JTextField> textFields;
    private ArrayList<JLabel> labels;
    private ClientInterface clientInterface;
    private MyComboBox databaseComboBox;
    private MyComboBox tableComboBox;
    private JButton addValuesButton;
    private JButton submitButton;
    private JButton backButton;
    private JTextArea valuesTextArea;
    private String values = "";

    public ClientGUIInsert(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        JLabel databaseNameLabel = new JLabel("Database Name:");
        JLabel tableNameLabel = new JLabel("Table Name:");

        databaseComboBox = new MyComboBox(clientInterface.getDatabasesNames());
        databaseComboBox.setSelectedIndex(0);
        tableComboBox = new MyComboBox((clientInterface.getTableNames((String) databaseComboBox.getSelectedItem())));
        tableComboBox.setSelectedIndex(0);

        inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.add(databaseNameLabel);
        inputPanel.add(databaseComboBox);
        inputPanel.add(tableNameLabel);
        inputPanel.add(tableComboBox);

        textFields = new ArrayList<>();
        labels = new ArrayList<>();
        for (String attrName : clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()).split(" ")) {
            JLabel attrLabel = new JLabel(attrName);
            JTextField attrTextArea = new JTextField("");

            textFields.add(attrTextArea);
            labels.add(attrLabel);

            inputPanel.add(attrLabel);
            inputPanel.add(attrTextArea);
        }

        valuesTextArea = new JTextArea("", 10, 40);
        valuesTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(valuesTextArea);

        addValuesButton = new JButton("Add Values");
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        addValuesButton.addActionListener(this);
        databaseComboBox.addActionListener(this);
        tableComboBox.addActionListener(this);
        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addValuesButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        JPanel valuesPanel = new JPanel(new BorderLayout());
        valuesPanel.add(new JLabel("Values:"), BorderLayout.NORTH);
        valuesPanel.add(scrollPane, BorderLayout.CENTER);

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(valuesPanel, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == databaseComboBox) {
            tableComboBox.updateComboBox(clientInterface.getTableNames((String) databaseComboBox.getSelectedItem()));
            updateTextFields();
        } else if (e.getSource() == tableComboBox) {
            updateTextFields();
        } else if (e.getSource() == addValuesButton) {
            if (valuesTextArea.getText().equals("")) {
                valuesTextArea.setText("(");
                values += "(";
            } else {
                valuesTextArea.append("\n(");
                values += "\n                (";
            }
            int lastIndex = textFields.size() - 1;
            for (int i = 0; i < textFields.size(); i++) {
                JTextField jTextField = textFields.get(i);
                boolean isLast = (i == lastIndex);

                if (jTextField.getText().equals("")) {
                    JOptionPane.showMessageDialog(this, "Fill in the Values field first!");
                } else {
                    if (!isLast) {
                        valuesTextArea.append(jTextField.getText() + ", ");
                        values += jTextField.getText() + ", ";
                    } else {
                        valuesTextArea.append(jTextField.getText());
                        values += jTextField.getText();
                    }
                    jTextField.setText("");
                }
            }
            values += ")";
            valuesTextArea.append(")");
        } else if (e.getSource() == submitButton) {
            if (valuesTextArea.getText().equals("")) {
                JOptionPane.showMessageDialog(this, "Unable to perform insertion, if values are not given.");
            } else {
                String databaseName = (String) databaseComboBox.getSelectedItem();
                String tableName = (String) tableComboBox.getSelectedItem();
                String query = "INSERT INTO " + databaseName + "." + tableName + "\nVALUES " + values + ";";
                JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
                clientInterface.writeIntoSocket(query);
                valuesTextArea.setText("");
                values = "";
            }
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
            valuesTextArea.setText("");
            values = "";
        }
    }

    public void updateTextFields() {
        for (JTextField textField : textFields) {
            inputPanel.remove(textField);
        }
        textFields.clear();

        for (JLabel label : labels) {
            inputPanel.remove(label);
        }
        labels.clear();

        for (String attrName : clientInterface.getFieldNames((String) databaseComboBox.getSelectedItem(), (String) tableComboBox.getSelectedItem()).split(" ")) {
            JLabel attrLabel = new JLabel(attrName);
            JTextField attrTextArea = new JTextField("");

            textFields.add(attrTextArea);
            labels.add(attrLabel);

            inputPanel.add(attrLabel);
            inputPanel.add(attrTextArea);
        }

        inputPanel.revalidate();
        inputPanel.repaint();
    }

    public MyComboBox getDatabaseComboBox() {
        return this.databaseComboBox;
    }
}
