package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.util.Arrays;

public class MyComboBox extends JComboBox<String> {

    public MyComboBox(String listOfNames) {
        String[] elements = listOfNames.split(" ");
        Arrays.sort(elements);

        this.setEnabled(true);
        Arrays.sort(elements);
        for (String element : elements) {
            this.addItem(element);
        }
    }

    // exclude name from the list
    public MyComboBox(String listOfNames, String name) {
        String[] elements = listOfNames.split(" ");
        Arrays.sort(elements);

        this.setEnabled(true);
        Arrays.sort(elements);
        for (String element : elements) {
            if (!name.equals(element)) {
                this.addItem(element);
            }
        }
    }

    // exclude name from the list, put alias in front of attributes
    public MyComboBox(String listOfNames, String name, String alias) {
        String[] elements = listOfNames.split(" ");
        Arrays.sort(elements);

        this.setEnabled(true);
        Arrays.sort(elements);
        for (String element : elements) {
            if (!name.equals(element)) {
                this.addItem(alias + "." + element);
            }
        }
    }

    public void updateComboBox(String listOfNames) {
        this.removeAllItems();
        String[] elements = listOfNames.split(" ");

        this.setEnabled(true);
        Arrays.sort(elements);
        for (String element : elements) {
            this.addItem(element);
        }
    }

    // update, but exclude name from the list
    public void updateComboBox(String listOfNames, String name) {
        this.removeAllItems();
        String[] elements = listOfNames.split(" ");

        this.setEnabled(true);
        Arrays.sort(elements);
        for (String element : elements) {
            if (!name.equals(element)) {
                this.addItem(element);
            }
        }
    }

    // update, but exclude name from the list, put alias in front of attributes
    public void updateComboBox(String listOfNames, String name, String alias) {
        this.removeAllItems();
        String[] elements = listOfNames.split(" ");

        this.setEnabled(true);
        Arrays.sort(elements);
        for (String element : elements) {
            if (!name.equals(element)) {
                this.addItem(alias + "." + element);
            }
        }
    }
}
