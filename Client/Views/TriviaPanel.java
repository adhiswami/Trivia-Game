package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Project.Client.Client;
import Project.Client.IGameEvents;
import Project.Common.Phase;

//as4555 4/12/24
public class TriviaPanel extends JPanel implements IGameEvents {
    private JLabel questionText = new JLabel();
    private JButton choiceA = new JButton();
    private JButton choiceB = new JButton();
    private JButton choiceC = new JButton();
    private JButton choiceD = new JButton();
    private JLabel timer = new JLabel();
    //as4555 4/24/24
    private JButton awayButton = new JButton("Away");

    private String myCategoryQuestion;
    private String myRoundNumber;
    private String timeLeft;
    private boolean isReady;
    //as4555 4/24/24
    private boolean isAway = false;

    public boolean isAway() {
        return isAway;
    }
    public void setAway(boolean isAway) {
        this.isAway = isAway;
    }

    public boolean isReady() {
        return isReady;
    }
    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    //as4555 4/14/24
    public TriviaPanel() {
        super(new BorderLayout(10,10));
        JPanel wrapper = new JPanel(new BorderLayout());
        Client.INSTANCE.addCallback(this); 
        wrapper.add(questionText, BorderLayout.NORTH);
        //answer choices
        JPanel answerChoices = new JPanel();
        answerChoices.setLayout(new GridLayout(2,2));
        answerChoices.add(choiceA);
        choiceA.addActionListener(l -> {
            try {
                choiceB.setEnabled(false);
                choiceC.setEnabled(false);
                choiceD.setEnabled(false);
                choiceA.setBackground(Color.RED);
                Client.INSTANCE.sendTakeTurn("A");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        answerChoices.add(choiceB);
        choiceB.addActionListener(l -> {
            try {
                choiceA.setEnabled(false);
                choiceC.setEnabled(false);
                choiceD.setEnabled(false);
                choiceB.setBackground(Color.RED);
                Client.INSTANCE.sendTakeTurn("B");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        answerChoices.add(choiceC);
        choiceC.addActionListener(l -> {
            try {
                choiceA.setEnabled(false);
                choiceB.setEnabled(false);
                choiceD.setEnabled(false);
                choiceC.setBackground(Color.RED);
                Client.INSTANCE.sendTakeTurn("C");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        answerChoices.add(choiceD);
        choiceD.addActionListener(l -> {
            try {
                choiceA.setEnabled(false);
                choiceB.setEnabled(false);
                choiceC.setEnabled(false);
                choiceD.setBackground(Color.RED);
                Client.INSTANCE.sendTakeTurn("D");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        wrapper.add(answerChoices, BorderLayout.CENTER);
        //round timer + away button
        //as4555 4/24/24
        JPanel timerAndAway = new JPanel(new BorderLayout());
        awayButton.addActionListener(l -> {
            if (awayButton.getText().equalsIgnoreCase("away")) {
                awayButton.setText("Back");
                try {
                    Client.INSTANCE.sendAwayStatus(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                awayButton.setText("Away");
                try {
                    Client.INSTANCE.sendAwayStatus(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        timerAndAway.add(timer, BorderLayout.CENTER);
        timerAndAway.add(awayButton, BorderLayout.EAST);
        wrapper.add(timerAndAway, BorderLayout.SOUTH);

        this.add(wrapper, BorderLayout.CENTER);
    }
    //as4555 4/24/24
    public void resetView() {
        if (isAway) {
            choiceA.setEnabled(false);
            choiceA.setBackground(null);

            choiceB.setEnabled(false);
            choiceB.setBackground(null);

            choiceC.setEnabled(false);
            choiceC.setBackground(null);

            choiceD.setEnabled(false);
            choiceD.setBackground(null);
        } else {
            choiceA.setEnabled(true);
            choiceA.setBackground(null);

            choiceB.setEnabled(true);
            choiceB.setBackground(null);

            choiceC.setEnabled(true);
            choiceC.setBackground(null);

            choiceD.setEnabled(true);
            choiceD.setBackground(null);
        }
    }

    public void setQuestionText(String roundNum, String question){
        questionText.setText(roundNum + " " + question);
        questionText.revalidate();
        questionText.repaint();
    }

    public void setTimerText(String timeLeft) {
        timer.setText("Time left: " + timeLeft);
        timer.revalidate();
        timer.repaint();
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
    //as4555 4/13/24
    public void onReceiveReady(long clientId, boolean isReady) {
        setReady(isReady);
        if (!isReady) {
            choiceA.setEnabled(false);
            choiceB.setEnabled(false);
            choiceC.setEnabled(false);
            choiceD.setEnabled(false);
            awayButton.setEnabled(false);
        }
    }

    @Override
    public void onReceivePhase(Phase phase) {
        if (phase == Phase.TURN && isReady) {
            resetView();
        }
    }
    
    @Override
    public void onReceiveCategoryQuestion(String categoryQuestion) {
        myCategoryQuestion = categoryQuestion;
        setQuestionText(myRoundNumber, myCategoryQuestion);
    }
    @Override
    public void onReceiveAnswers(String answerChoices) {
        choiceA.setText(answerChoices.split("\n")[0]);
        choiceA.revalidate();
        choiceA.repaint();

        choiceB.setText(answerChoices.split("\n")[1]);
        choiceB.revalidate();
        choiceB.repaint();

        choiceC.setText(answerChoices.split("\n")[2]);
        choiceC.revalidate();
        choiceC.repaint();

        choiceD.setText(answerChoices.split("\n")[3]);
        choiceD.revalidate();
        choiceD.repaint();
        //as4555 4/25/24
        if (isAway) {
            try {
                Client.INSTANCE.sendTakeTurn("Z");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onRoomMessageReceive(String message) {
    }

    @Override
    public void onReceiveRoundNum(String roundNum) {
       myRoundNumber = roundNum;
    }

    @Override
    public void onReceiveTimeLeft(String timeLeft) {
       this.timeLeft = timeLeft;
       setTimerText(timeLeft);
    }

    public String getTimeLeft() {
        return timeLeft;
    }

    @Override
    public void onReceiveScore(long clientId, String score) {
    }

    @Override
    public void onReceiveSortUser() {
    }

    @Override
    public void onReceiveWinnerMessage(String winner) {
    }

    @Override
    public void onReceiveAwayStatus(long clientId, boolean away) {
        if (Client.INSTANCE.getMyClientId() == clientId) {
            setAway(away);
            if (away) {
                choiceA.setEnabled(false);
                choiceB.setEnabled(false);
                choiceC.setEnabled(false);
                choiceD.setEnabled(false);
            }
        }
    }
    @Override
    public void onReceiveAddQuestion() {
    }
    @Override
    public void onReceiveCatPress(long clientId) {
    }
}
