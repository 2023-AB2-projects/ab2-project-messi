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

    public void updateComboBox(String listOfNames) {
        this.removeAllItems();
        String[] elements = listOfNames.split(" ");

        this.setEnabled(true);
        Arrays.sort(elements);
        for (String element : elements) {
            this.addItem(element);
        }
    }
}
