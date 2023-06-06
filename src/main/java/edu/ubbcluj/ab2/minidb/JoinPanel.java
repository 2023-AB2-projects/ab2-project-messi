package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JoinPanel extends JPanel implements ActionListener {
    private MyComboBox joinOn;
    private JTextField alias;
    private MyComboBox attrOfJoinedTable;
    private MyComboBox attrOfMainTable;
    private String joinedAttributes;

    public JoinPanel(String mainTableName, String mainTableAlias, String listOfTables, String joinedAttributes, String mainAttributes) {
        this.joinedAttributes = joinedAttributes;

        joinOn = new MyComboBox(listOfTables);
        joinOn.setSelectedIndex(0);
        alias = new JTextField("");
        alias.setEditable(true);
        attrOfJoinedTable = new MyComboBox("");
        attrOfMainTable = new MyComboBox(mainAttributes, mainTableName, mainTableAlias);
        attrOfMainTable.setSelectedIndex(0);

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.addComponent(new JLabel("INNER JOIN"), 100, 20);
        this.addComponent(joinOn, 100, 20);
        this.addComponent(new JLabel("AS"), 30, 20);
        this.addComponent(alias, 50, 20);
        this.addComponent(new JLabel("ON"), 30, 20);
        this.addComponent(attrOfJoinedTable, 100, 20);
        this.addComponent(new JLabel("="), 20, 20);
        this.addComponent(attrOfMainTable, 100, 20);

        Dimension d = new Dimension(680, 40);
        this.setMinimumSize(d);
        this.setMaximumSize(d);
        this.setPreferredSize(d);
        this.setVisible(true);
    }

    public void addComponent(Component comp, int width, int height) {
        Dimension d = new Dimension(width, height);
        comp.setPreferredSize(d);
        comp.setMinimumSize(d);
        comp.setMaximumSize(d);
        this.add(comp);
        this.add(Box.createHorizontalGlue());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == joinOn) {
            if (!alias.getText().isBlank())
                attrOfJoinedTable.updateComboBox(joinedAttributes, "", alias.getText());
        } else if (e.getSource() == attrOfJoinedTable) {
            if (alias.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Fill in the alias(AS) first!");
            }
        }
    }

    public MyComboBox getSelectedJoinOn() {
        return joinOn;
    }

    public JTextField getAlias() {
        return alias;
    }

    public MyComboBox getAttrOfJoinedTable() {
        return attrOfJoinedTable;
    }

    public MyComboBox getAttrOfMainTable() {
        return attrOfMainTable;
    }


    public void updateJoinedAttributes(String joinedAttributes) {
        this.joinedAttributes = joinedAttributes;
        attrOfJoinedTable.updateComboBox(joinedAttributes, "", alias.getText());
    }
}
