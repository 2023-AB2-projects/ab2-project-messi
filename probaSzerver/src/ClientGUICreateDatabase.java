import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientGUICreateDatabase extends JPanel implements ActionListener {
    ClientInterface clientInterface;
    JLabel jlabel;
    JTextField textField;
    JButton submitButton;
    JButton backButton;
    String message;
    String query;

    public ClientGUICreateDatabase(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;
        setLayout(new GridLayout(2, 2));

        jlabel = new JLabel("Database name: ");
        textField = new JTextField();
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        add(jlabel);
        add(textField);
        add(backButton);
        add(submitButton);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            query = "CREATE DATABASE " + textField.getText() + ";\n";
            message = "1" + "/" + textField.getText();
            JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
            clientInterface.writeIntoSocket(query);
        } else if (e.getSource() == backButton) {
            textField.setText("");
            clientInterface.showMenu();
        }
    }

    public JTextField getTextField() {
        return textField;
    }

    public void setTextField(JTextField textField) {
        this.textField = textField;
    }

    public String getDatabaseName() {
        return textField.getText();
    }
}
