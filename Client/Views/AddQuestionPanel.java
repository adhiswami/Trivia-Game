package Project.Client.Views;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Project.Client.Client;

//as4555 4/27/24
public class AddQuestionPanel extends JPanel {
    public AddQuestionPanel() throws IOException {
        super (new BorderLayout());
        JLabel instructions = new JLabel("Enter your category, question, and answer choices in this format: Category;Question?;A) choice;B) choice;C) choice;D) choice;A (or whatever the correct letter is)");
        this.add(instructions, BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        JLabel exampleQues = new JLabel("For example: History;What year came after 1999?;A) 2001;B) 2000;C) 2002;D) 1998;B");
        wrapper.add(exampleQues, BorderLayout.NORTH);

        JTextField input = new JTextField();
        wrapper.add(input, BorderLayout.CENTER);

        JButton submitButton = new JButton();
        submitButton.setText("Submit");
        submitButton.addActionListener(l -> {
            try {
                String text = input.getText().trim();
                String[] textArray = text.split(";");
                String category = text.split(";")[0];
                String[] removeCat = new String[6];
                System.arraycopy(textArray, 1, removeCat, 0, 6);
                String newQuestion = String.join(";", removeCat);

                Client.INSTANCE.sendNewQuestion(category, newQuestion);
                input.setText("");// clear the original text

            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        });
        wrapper.add(submitButton, BorderLayout.SOUTH);

        this.add(new JPanel(), BorderLayout.WEST);
        this.add(new JPanel(), BorderLayout.EAST);
        this.add(new JPanel(), BorderLayout.SOUTH);
        this.add(wrapper, BorderLayout.CENTER);
    }    
}
