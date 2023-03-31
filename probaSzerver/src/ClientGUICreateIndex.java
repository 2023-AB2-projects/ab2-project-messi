import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientGUICreateIndex extends JPanel implements ActionListener {
    ClientInterface clientInterface;
    private JLabel indexNameLabel;
    private JLabel indexOnLabel;
    private JTextField indexName;
    private JTextField indexOn;
    private JButton submitButton;
    private JButton backButton;
    private String message;
    private String query;

    public ClientGUICreateIndex(ClientInterface clientInterface) {
        this.clientInterface = clientInterface;

        this.setLayout(new BorderLayout());

        indexNameLabel = new JLabel("Index name: ");
        indexName = new JTextField();
        indexOnLabel = new JLabel("ON: ");
        indexOn = new JTextField();
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(indexNameLabel);
        inputPanel.add(indexName);
        inputPanel.add(indexOnLabel);
        inputPanel.add(indexOn);

        submitButton.addActionListener(this);
        backButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);

        this.add(inputPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            query = "CREATE INDEX " + indexName.getText() + "\nON " + indexOn.getText() + ";\n";
            message = "5" + "/" + indexName.getText();
            JOptionPane.showMessageDialog(this, "SQL query:\n" + query);
            clientInterface.writeIntoSocket(query);
        } else if (e.getSource() == backButton) {
            clientInterface.showMenu();
        }
    }
}
