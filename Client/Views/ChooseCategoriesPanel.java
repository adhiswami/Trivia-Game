package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Project.Client.Client;

//as4555 4/29/24
public class ChooseCategoriesPanel extends JPanel{
    private ArrayList<String> categories = new ArrayList<String>();
    public ChooseCategoriesPanel() {
        super(new BorderLayout());
        JLabel instructions = new JLabel("By default, all categories are selected. Deselect the ones you do not want for this game and leave the ones you want selcted.");
        this.add(instructions, BorderLayout.NORTH);
      

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridLayout(6,1));

        JCheckBox historyCat = new JCheckBox("History", true);
        JCheckBox literatureCat = new JCheckBox("Literature", true);
        JCheckBox popCat = new JCheckBox("Popculture", true);
        JCheckBox scienceCat = new JCheckBox("Science", true);
        JCheckBox sportsCat = new JCheckBox("Sports", true);

        JButton submitButton = new JButton();
        submitButton.setText("Submit Categories");
        submitButton.addActionListener(l -> {
            Component[] cs = wrapper.getComponents(); 
            for (Component c : cs) {
                JCheckBox  box = (JCheckBox) c;
                if (box.isSelected()) {
                    categories.add(box.getText());
                }
            }
            try {
                if (categories.size() != 0) {
                    Client.INSTANCE.sendCatOptions(categories);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // default the  checkboxes to selected when we're done
            historyCat.setSelected(true);
            literatureCat.setSelected(true);
            popCat.setSelected(true);
            scienceCat.setSelected(true);
            sportsCat.setSelected(true);
        });

        wrapper.add(historyCat);
        wrapper.add(literatureCat);
        wrapper.add(popCat);
        wrapper.add(scienceCat);
        wrapper.add(sportsCat);

         this.add(submitButton, BorderLayout.SOUTH);
        this.add(new JPanel(), BorderLayout.EAST);
        this.add(new JPanel(), BorderLayout.WEST);
        this.add(wrapper, BorderLayout.CENTER);
    }
}
