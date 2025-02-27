package Project.Client.Views;

import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;

import Project.Client.Client;

public class ReadyPanel extends JPanel {

    public ReadyPanel() {        
        JButton readyButton = new JButton();
        readyButton.setText("Ready");
        readyButton.addActionListener(l -> {
            try {
                Client.INSTANCE.sendReadyCheck();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        this.add(readyButton);
        //as4555 4/26/24
        JButton chooseCategoriesButton = new JButton();
        chooseCategoriesButton.setText("Choose Categories");
        chooseCategoriesButton.addActionListener(l -> {
            try {
                Client.INSTANCE.sendCatButtonPress();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.add(chooseCategoriesButton);

        JButton addQuestionButton = new JButton();
        addQuestionButton.setText("Add New Question");
        addQuestionButton.addActionListener(l -> {
           try {
                Client.INSTANCE.sendAddQuestion();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.add(addQuestionButton);
    }
}
