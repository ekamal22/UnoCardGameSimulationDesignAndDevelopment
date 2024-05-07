package main.java.Gui;

import javax.swing.*;
import main.java.Utils.RegistrationHandler;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrationPage extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> sexComboBox;
    private JTextField ageField;

    public RegistrationPage() {
        setTitle("Registration Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel emailLabel = new JLabel("Email:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(emailLabel, gbc);
        emailField = new JTextField(15);
        gbc.gridx = 1;
        add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        add(passwordField, gbc);

        JLabel sexLabel = new JLabel("Sex:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(sexLabel, gbc);
        String[] sexes = {"Male", "Female"};
        sexComboBox = new JComboBox<>(sexes);
        gbc.gridx = 1;
        add(sexComboBox, gbc);

        JLabel ageLabel = new JLabel("Age:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(ageLabel, gbc);
        ageField = new JTextField(15);
        gbc.gridx = 1;
        add(ageField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegistrationHandler.handleRegistration(emailField.getText(), new String(passwordField.getPassword()), (String) sexComboBox.getSelectedItem(), ageField.getText());
            }
        });
        buttonPanel.add(registerButton);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            new LoginPage();
            dispose(); // Close the registration page
        });
        buttonPanel.add(loginButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        setSize(400, 250);
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        new RegistrationPage();
    }
}
