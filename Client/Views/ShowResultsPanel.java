package Project.Client.Views;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

import Project.Client.Client;
import Project.Client.IGameEvents;
import Project.Common.Phase;

//as4555 4/14/24
public class ShowResultsPanel extends JPanel implements IGameEvents{
    private String winnerMessage;
    JEditorPane textContainer = new JEditorPane("text/plain", "");

    public ShowResultsPanel() {
        super(new BorderLayout());
        Client.INSTANCE.addCallback(this);
        JPanel wrapper = new JPanel(new BorderLayout());
        textContainer.setEditable(false);
        textContainer.setName("ResultsText");
        wrapper.add(textContainer, BorderLayout.CENTER);
        this.add(new JPanel(), BorderLayout.WEST);
        this.add(new JPanel(), BorderLayout.EAST);
        this.add(new JPanel(), BorderLayout.NORTH);
        this.add(new JPanel(), BorderLayout.SOUTH);
        this.add(wrapper, BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void onClientConnect(long id, String clientName, String message) {

    }

    @Override
    public void onClientDisconnect(long id, String clientName, String message) {

    }

    @Override
    public void onMessageReceive(long id, String message) {
         
    }

    @Override
    public void onReceiveClientId(long id) {
        
    }

    @Override
    public void onSyncClient(long id, String clientName) {
        
    }

    @Override
    public void onResetUserList() {
        
    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {
        
    }

    @Override
    public void onRoomJoin(String roomName) {
        
    }

    @Override
    public void onReceiveReady(long clientId, boolean isReady) {
        
    }

    @Override
    public void onReceivePhase(Phase phase) {
        
    }

    @Override
    public void onReceiveCategoryQuestion(String categoryQuestion) {
        
    }

    @Override
    public void onReceiveAnswers(String answerChoices) {
        
    }

    @Override
    public void onRoomMessageReceive(String message) {
        
    }

    @Override
    public void onReceiveRoundNum(String roundNum) {
        
    }

    @Override
    public void onReceiveTimeLeft(String timeLeft) {
       
    }

    @Override
    public void onReceiveScore(long clientId, String score) {
    }

    @Override
    public void onReceiveSortUser() {
    }

    @Override
    public void onReceiveWinnerMessage(String winner) {
        setWinnerMessage(winner);
    }

    public String getWinnerMessage() {
        return winnerMessage;
    }

    public void setWinnerMessage(String winnerMessage) {
        this.winnerMessage = winnerMessage;
        textContainer.setText(winnerMessage);
        textContainer.repaint();
        textContainer.revalidate();
    }

    @Override
    public void onReceiveAwayStatus(long clientId, boolean away) {
    }

    @Override
    public void onReceiveAddQuestion() {
    }

    @Override
    public void onReceiveCatPress(long clientId) {
    }
}
