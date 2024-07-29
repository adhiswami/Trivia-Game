package Project.Client.Views;

import java.awt.GridLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

import Project.Client.ClientUtils;

public class UserListItem extends JPanel {
    private JEditorPane usernameContainer;

    private JEditorPane pointsField;
    private float points = -1;

    //as4555 4/26/24
    private boolean isAway;
    public boolean isAway() {
        return isAway;
    }

    public void setAway(boolean isAway) {
        this.isAway = isAway;
    }

    private JEditorPane awayStatusField;
    
    public UserListItem(long clientId, String clientName) {
        // Set a layout that will organize the children vertically
        this.setLayout(new GridLayout(1, 3));
        this.setName(Long.toString(clientId));
        JEditorPane textContainer = new JEditorPane("text/plain", clientName);
        this.usernameContainer = textContainer;
        textContainer.setEditable(false);
        textContainer.setName(Long.toString(clientId));

        ClientUtils.clearBackground(textContainer);
        this.add(textContainer);

         //as4555 4/24/24
        awayStatusField = new JEditorPane();
        awayStatusField.setText("[AWAY]");
        awayStatusField.setEditable(false);
        awayStatusField.setVisible(false);

        ClientUtils.clearBackground(awayStatusField);
        this.add(awayStatusField);
        
        pointsField = new JEditorPane();
        pointsField.setText(Float.toString(points));
        pointsField.setEditable(false);
        pointsField.setVisible(false);

        ClientUtils.clearBackground(pointsField);
        this.add(pointsField);       
    }

    //as4555 4/24/24
    public void setAwayStatus(boolean isAway) {
        setAway(isAway);
        if (isAway) {
            awayStatusField.setVisible(true);
            this.revalidate();
            this.repaint();
        } else {
            awayStatusField.setVisible(false);
            this.revalidate();
            this.repaint();
        }
    }

    public void setPoints(float points) {
        this.points = points;
        pointsField.setText(Float.toString(points));
        pointsField.setVisible(points >= 0);
        this.revalidate();
        this.repaint();
    }

    // Can be used for sorting purposes later
    public float getPoints() {
        return points;
    }

    public JEditorPane getUsernameContainer() {
        return usernameContainer;
    }

    public void setUsernameContainer(JEditorPane usernameContainer) {
        this.usernameContainer = usernameContainer;
    }

    public JEditorPane getPointsField() {
        return pointsField;
    }

    public void setPointsField(JEditorPane pointsField) {
        this.pointsField = pointsField;
    }

    public JEditorPane getAwayStatusField() {
        return awayStatusField;
    }

    public void setAwayStatusField(JEditorPane awayStatusField) {
        this.awayStatusField = awayStatusField;
    }
}