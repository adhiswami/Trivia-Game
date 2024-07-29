package Project.Server;

import Project.Common.Constants;
import Project.Common.Phase;
import Project.Common.Player;
import Project.Common.TextFX;
import Project.Common.TextFX.Color;

public class ServerPlayer extends Player {
    private ServerThread client;

    public ServerPlayer(ServerThread t) {
        client = t;
        System.out.println(TextFX.colorize("Wrapped ServerThread " + t.getClientName(), Color.CYAN));
    }

    public long getClientId() {
        if (client == null) {
            return Constants.DEFAULT_CLIENT_ID;
        }
        return client.getClientId();
    }

    public String getClientName() {
        if (client == null) {
            return "";
        }
        return client.getClientName();
    }

    public void sendPhase(Phase phase) {
        if (client == null) {
            return;
        }
        client.sendPhase(phase.name());
    }

    public void sendReadyState(long clientId, boolean isReady) {
        if (client == null) {
            return;
        }
        client.sendReadyState(clientId, isReady);
    }
    public void sendPlayerTurnStatus(long clientId, boolean didTakeTurn)
    {
        if (client == null) {
            return;
        }
        client.sendPlayerTurnStatus(clientId, didTakeTurn);
    }
    public void sendResetLocalTurns()
    {
        if (client == null) {
            return;
        }
        client.sendResetLocalTurns();
    }
    public void sendResetLocalReadyState()
    {
        if (client == null) {
            return;
        }
        client.sendResetLocalReadyState();
    }
    //as4555 3/26/24
    public void sendQuestion(String category, String question)
    {
        if (client == null) {
            return;
        }
        client.sendQuestion(category, question);
    }
    public void sendAnswerChoices(String answerChoices)
    {
        if (client == null) {
            return;
        }
        client.sendAnswerChoices(answerChoices);
    }
    public void sendResetCorrectness()
    {
        if (client == null) {
            return;
        }
        client.sendResetCorrectness();
    }
    public void sendRoundNum(int roundNumber)
    {
        if (client == null) {
            return;
        }
        client.sendRoundNum(roundNumber);
    }
    public void sendResetRoundNum()
    {
        if (client == null) {
            return;
        }
        client.sendResetRoundNum();
    }
    public void sendMessage(long clientId, String message)
    {
        if (client == null) {
            return;
        }
        client.sendMessage(clientId, message);
    }
    //as4555 4/14/24
    public void sendStartTimer(int timeLeft)
    {
        if (client == null) {
            return;
        }
        client.sendStartTimer(timeLeft);
    }

    public void sendScore(long clientId, float score)
    {
        if (client == null) {
            return;
        }
        client.sendScore(clientId, score);
    }

    public void sendSortUsers()
    {
        if (client == null) {
            return;
        }
        client.sendSortUsers();
    }

    public void sendWinnerMessage(String winner)
    {
        if (client == null) {
            return;
        }
        client.sendWinnerMessage(winner);
    }
    //as4555 4/24/24
    public void sendAwayStatus(long clientId, boolean away) {
        if (client == null) {
            return;
        }
        client.sendAwayStatus(clientId, away);
    }
    //as4555 4/28/24
    public void sendShowCatPanel(long clientId) {
        if (client == null) {
            return;
        }
        client.sendShowCatPanel(clientId);
    }
}