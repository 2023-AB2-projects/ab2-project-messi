package edu.ubbcluj.ab2.minidb;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

public class ClientInterface extends JFrame implements ActionListener {

    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Socket socket;
    private ClientGUICreateDatabase createDatabasePanel;
    private ClientGUIDropDatabase dropDatabasePanel;
    private ClientGUICreateTable createTablePanel;
    private ClientGUIDropTable dropTablePanel;
    private ClientGUICreateIndex createIndexPanel;
    private JButton createDatabaseButton;
    private JButton dropDatabaseButton;
    private JButton createTableButton;
    private JButton dropTableButton;
    private JButton createIndexButton;
    private static JPanel cardPanel;
    private static CardLayout layout;

    public ClientInterface() {
        startConnection();
        this.setTitle("Messi's database");
        this.setSize(500, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        layout = new CardLayout();
        cardPanel = new JPanel();
        cardPanel.setLayout(layout);

        //creating the buttons
        createDatabaseButton = new JButton("Create Database");
        dropDatabaseButton = new JButton("Drop Database");
        createTableButton = new JButton("Create Table");
        dropTableButton = new JButton("Drop Table");
        createIndexButton = new JButton("Create Index");


        //adding listeners to the buttons
        createDatabaseButton.addActionListener(this);
        dropDatabaseButton.addActionListener(this);
        createTableButton.addActionListener(this);
        dropTableButton.addActionListener(this);
        createIndexButton.addActionListener(this);


        JPanel menu = new JPanel(new GridLayout(5, 1));
        menu.add(createDatabaseButton);
        menu.add(dropDatabaseButton);
        menu.add(createTableButton);
        menu.add(dropTableButton);
        menu.add(createIndexButton);


        //panels:
        createDatabasePanel = new ClientGUICreateDatabase(this);
        dropDatabasePanel = new ClientGUIDropDatabase(this);
        createTablePanel = new ClientGUICreateTable(this);
        dropTablePanel = new ClientGUIDropTable(this);
        createIndexPanel = new ClientGUICreateIndex(this);


        //add panels to cardPanel
        cardPanel.add(menu, "menu");
        cardPanel.add(createDatabasePanel, "createDatabase");
        cardPanel.add(dropDatabasePanel, "dropDatabase");
        cardPanel.add(createTablePanel, "createTable");
        cardPanel.add(dropTablePanel, "dropTable");
        cardPanel.add(createIndexPanel, "createIndex");


        layout.show(cardPanel, "menu");
        this.add(cardPanel);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createDatabaseButton) {
            layout.show(cardPanel, "createDatabase");
        } else if (e.getSource() == dropDatabaseButton) {
            dropDatabasePanel.updateDatabaseComboBox();
            layout.show(cardPanel, "dropDatabase");
        } else if (e.getSource() == createTableButton) {
            createTablePanel.updateDatabaseComboBox();
            layout.show(cardPanel, "createTable");
        } else if (e.getSource() == dropTableButton) {
            dropTablePanel.updateDatabaseComboBox();
            layout.show(cardPanel, "dropTable");
        } else if (e.getSource() == createIndexButton) {
            layout.show(cardPanel, "createIndex");
        }
    }

    private void startConnection() {
        try {
            socket = new Socket("localhost", 1111);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("ERROR at initializing connection!");
        }
    }

    private void endConnection() {
        try {
            System.out.println("ENDING");
            // close socket
            socket.close();
            // close streams
            objectInputStream.close();
            objectOutputStream.close();
        } catch (IOException e) {
            System.out.println("ERROR at ending connection!");
        }
    }

    public void writeIntoSocket(String message) {
        try {
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            System.out.println("ERROR at writing object to socket!");
            endConnection();
        }
    }

    public String readFromSocket() {
        try {
            return (String) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("ERROR at reading object from socket!");
            endConnection();
            return "";
        }
    }

    public String getDatabasesNames() {
        writeIntoSocket("GETDATABASES");
        return readFromSocket();
    }

    public String getTablesNames() {
        System.out.println("get selected db: " + dropTablePanel.getSelectedDatabase());
        writeIntoSocket("GETTABLES " + dropTablePanel.getSelectedDatabase().toString());
        return readFromSocket();
    }

    public void showMenu() {
        layout.show(cardPanel, "menu");
    }

    public static void main(String[] args) {
        new ClientInterface();
    }
}

