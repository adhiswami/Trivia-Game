package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import Project.Client.CardView;
import Project.Client.Client;
import Project.Client.ICardControls;
import Project.Client.IGameEvents;
import Project.Common.Constants;
import Project.Common.Phase;

public class GamePanel extends JPanel implements IGameEvents {
    private TriviaPanel triviaPanel;
    private ShowResultsPanel srp;
    //as4555 4/26/24
    private AddQuestionPanel aqp;
    private ChooseCategoriesPanel ccp;
    private CardLayout cardLayout;
    private final static String READY_PANEL = "READY";
    private final static String TRIVIA_PANEL = "TRIVIA";
    private final static String SHOWRESULTS_PANEL = "RESULTS";
    private final static String ADD_QUESTION_PANEL = "ADDQUES";
    private final static String CHOOSE_CAT_PANEL = "CHOOSECAT";
    private ICardControls controls;

    public GamePanel(ICardControls controls) throws IOException {
        super(new BorderLayout());
        this.controls = controls;
        JPanel gameContainer = new JPanel();
        gameContainer.setLayout(new CardLayout());
        cardLayout = (CardLayout) gameContainer.getLayout();
        this.setName(CardView.GAME_SCREEN.name());
        Client.INSTANCE.addCallback(this);
        //ready panel
        ReadyPanel rp = new ReadyPanel();
        rp.setName(READY_PANEL);
        gameContainer.add(READY_PANEL, rp);
        //trivia
        triviaPanel = new TriviaPanel();
        triviaPanel.setName(TRIVIA_PANEL);
        gameContainer.add(TRIVIA_PANEL, triviaPanel);
        // game events
        GameEventsPanel gep = new GameEventsPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gameContainer, gep);
        splitPane.setResizeWeight(.7);
        //show results
        srp = new ShowResultsPanel();
        srp.setName(SHOWRESULTS_PANEL);
        gameContainer.add(SHOWRESULTS_PANEL, srp);
        //as4555 4/27/24
        //add question
        aqp = new AddQuestionPanel();
        aqp.setName(ADD_QUESTION_PANEL);
        gameContainer.add(ADD_QUESTION_PANEL, aqp);

        //as4555 4/28/24
        ccp = new ChooseCategoriesPanel();
        ccp.setName(CHOOSE_CAT_PANEL);
        gameContainer.add(CHOOSE_CAT_PANEL, ccp);

        triviaPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // Recalculate the divider location when the left panel becomes visible
                splitPane.setDividerLocation(0.7);
            }
        });
        this.add(splitPane, BorderLayout.CENTER);
        setVisible(false);
        // don't need to add this to ClientUI as this isn't a primary panel(it's nested
        // in ChatGamePanel)
        // controls.addPanel(Card.GAME_SCREEN.name(), this);

        srp.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // Recalculate the divider location when the left panel becomes visible
                splitPane.setDividerLocation(0.7);
            }
        });
        this.add(splitPane, BorderLayout.CENTER);
        setVisible(false);
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
    public void onRoomJoin(String roomName) {
        if (Constants.LOBBY.equals(roomName)) {
            setVisible(false);
            this.revalidate();
            this.repaint();
        }
    }

    @Override
    public void onReceivePhase(Phase phase) {
        // I'll temporarily do next(), but there may be scenarios where the screen can
        // be inaccurate
        System.out.println("Received phase: " + phase.name());
        if (!isVisible()) {
            setVisible(true);
            this.getParent().revalidate();
            this.getParent().repaint();
            System.out.println("GamePanel visible");
        } 
        if (phase == Phase.READY) {
            cardLayout.show(triviaPanel.getParent(), READY_PANEL);
        } else if (phase == Phase.TURN) {
            cardLayout.show(triviaPanel.getParent(), TRIVIA_PANEL);
        } else if (phase == Phase.SHOW_RESULTS) {
            cardLayout.show(triviaPanel.getParent(), SHOWRESULTS_PANEL);
        }
    }

    @Override
    public void onReceiveReady(long clientId, boolean isReady) {
    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {
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
        controls.updateClientPoints(clientId, score);
    }

    @Override
    public void onReceiveSortUser() {
        controls.sortUserList();
    }

    @Override
    public void onReceiveWinnerMessage(String winner) {
    }

    //as4555 4/24/24
    @Override
    public void onReceiveAwayStatus(long clientId, boolean away) {
        controls.updateUserAwayStatus(clientId, away);
    }

    //as4555 4/27/24
    @Override
    public void onReceiveAddQuestion() {
        cardLayout.show(triviaPanel.getParent(), ADD_QUESTION_PANEL);
    }

    //as4555 4/28/24
    @Override
    public void onReceiveCatPress(long clientId) {
        if (Client.INSTANCE.getMyClientId() == clientId) {
            cardLayout.show(triviaPanel.getParent(), CHOOSE_CAT_PANEL);
        }
    }
}