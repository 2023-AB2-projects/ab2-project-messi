import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientGUIDropTable extends JPanel implements ActionListener {
    ClientInterface clientInterface;
    JLabel jlabel;
    JTextField textField;
    JButton submitButton;
    JButton backButton;
    String message;
    String query;

    public ClientGUIDropTable(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        this.setLayout(new GridLayout(2, 2));

        jlabel = new JLabel("Table name: ");
        textField = new JTextField();
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        this.add(jlabel);
        this.add(textField);
        this.add(backButton);
        this.add(submitButton);
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            query = "DROP TABLE " + textField.getText() + ";\n";
            message = "4" + "/" + textField.getText();
            JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
            clientInterface.writeIntoSocket(query);
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
        }
    }
}
