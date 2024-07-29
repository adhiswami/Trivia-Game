package Project.Client;

import javax.swing.JPanel;

public interface ICardControls {
    void next();

    void previous();

    void show(String cardName);

    void addPanel(String name, JPanel panel);

    void connect();

    void updateClientPoints(long clientId, String score);

    //as4555 4/15/24
    void sortUserList();

    //as4555 4/24/24
    void updateUserAwayStatus(long clientId, boolean away);
}