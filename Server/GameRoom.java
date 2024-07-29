package Project.Server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import Project.Common.Constants;
import Project.Common.Phase;
import Project.Common.TextFX;
import Project.Common.TimedEvent;
import Project.Common.TextFX.Color;

public class GameRoom extends Room {

    private ConcurrentHashMap<Long, ServerPlayer> players = new ConcurrentHashMap<Long, ServerPlayer>();

    private TimedEvent readyCheckTimer = null;
    private TimedEvent turnTimer = null;
    private Phase currentPhase = Phase.READY;
    private long numActivePlayers = 0;
    private boolean canEndSession = false;

    //as4555 3/26/24
    private ConcurrentHashMap<ServerPlayer, Float> playersResponseTime = new ConcurrentHashMap<ServerPlayer, Float>();
    //as4555 3/25/24
    private String questionText;
    private String correctAnswer;
    private String category = "";
    protected ArrayList<String> answers = new ArrayList<String>();
    private ArrayList<String> textFiles = new ArrayList<>(Arrays.asList("History.txt", "Literature.txt", "Popculture.txt", "Science.txt", "Sports.txt"));
    Random rand = new Random();
    private int roundNum = 1;

    public int getRoundNum() {
        return roundNum;
    }

    public void setRoundNum(int roundNum) {
        this.roundNum = roundNum;
    }

    public void updateRoundNum()
    {
        this.roundNum++;
    }

    public GameRoom(String name) {
        super(name);
    }

    @Override
    protected synchronized void addClient(ServerThread client) {
        super.addClient(client);
        if (!players.containsKey(client.getClientId())) {
            ServerPlayer sp = new ServerPlayer(client);
            players.put(client.getClientId(), sp);
            System.out.println(TextFX.colorize(client.getClientName() + " join GameRoom " + getName(), Color.WHITE));

            // sync game state

            // sync phase
            sp.sendPhase(currentPhase);
            // sync ready state
            players.values().forEach(p -> {
                sp.sendReadyState(p.getClientId(), p.isReady());
            });
        }
    }

    public boolean isCanEndSession() {
        return canEndSession;
    }

    @Override
    protected synchronized void removeClient(ServerThread client) {
        super.removeClient(client);
        // Note: base Room can close (if empty) before GameRoom cleans up (possibly)
        if (players.containsKey(client.getClientId())) {
            players.remove(client.getClientId());
            System.out.println(TextFX.colorize(client.getClientName() + " left GameRoom " + getName(), Color.WHITE));
        }
    }

    //serverthread interactions
    //as4555 4/28/24
    public synchronized void configureCategories(ServerThread client, ArrayList<String> categories) {
        textFiles.clear();
        for (String category : categories) {
            textFiles.add(category + ".txt");
        }
        client.sendMessage(Constants.DEFAULT_CLIENT_ID, "Your choices were added to this game and this game only");
        sendMessage(Constants.FROM_ROOM, "The following categories were chosen for this game: " + formatArrayString(categories));
        syncCurrentPhase();
    }

    public synchronized void categoriesButtonProcess(ServerThread client) {
        long playerId = client.getClientId();
        Map.Entry<Long, ServerPlayer> firstInRoom = players.entrySet().stream().findFirst().orElse(null);
        if (firstInRoom != null &&  playerId == firstInRoom.getKey()) {
            sendShowCatPanel(playerId);
        }
        else {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You are not the creator of the room, so therefore you cannot configure the categories");
        }
    }
    //as4555 4/27/24
    public synchronized void setAddQuestionPhase(ServerThread client) {
        long playerId = client.getClientId();
        if (players.containsKey(playerId)) {
            players.get(playerId).setReady(false);
            syncReadyState(players.get(playerId));
        }
    }
    //as4555 4/24/24
    public synchronized void setAwayStatus(ServerThread client, boolean away) {
        if (players.containsKey(client.getClientId())) {
            ServerPlayer player = players.get(client.getClientId());
            player.setAway(away);
            sendMessage(Constants.FROM_ROOM, String.format("%s is %s", player.getClientName(), away ? "Away" : "Back"));
            sendAwayStatus(player.getClientId(), away);
            if (away) {
                doTurn(client, "Z");
            }
        }
    }

    public synchronized void setReady(ServerThread client) {
        if (currentPhase != Phase.READY) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "Can't initiate ready check at this time");
            return;
        }
        long playerId = client.getClientId();
        if (players.containsKey(playerId)) {
            // players.get(playerId).setReady(!players.get(playerId).isReady()); //<--
            // toggles ready state
            players.get(playerId).setReady(true);// <-- simply sets the ready state to true
            syncReadyState(players.get(playerId));
            System.out.println(TextFX.colorize(players.get(playerId).getClientName() + " marked themselves as ready ",
                    Color.YELLOW));
            readyCheck();
        } else {
            System.err.println(TextFX.colorize("Player doesn't exist: " + client.getClientName(), Color.RED));
        }
    }

    public synchronized void doTurn(ServerThread client, String chosenAnswer)
    {
        if (currentPhase != Phase.TURN)
        {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You can't do turns just yet");
            return;
        }

        long clientId = client.getClientId();
        if(players.containsKey(clientId))
        {
            ServerPlayer sp = players.get(clientId);
            //they can only participate if they're ready
            if (!sp.isReady())
            {
                client.sendMessage(Constants.DEFAULT_CLIENT_ID, "Sorry, you aren't ready in time and can't participate");
                return;
            }
            //player can only update their turn "action" once
            if(!sp.didTakeTurn())
            {
                sp.setTakenTurn(true);
                sp.setAnswerChoice(chosenAnswer);
                sendMessage(Constants.FROM_ROOM, String.format("%s completed their turn", sp.getClientName()));
                //as4555 3/26/24
                playersResponseTime.put(sp, (float) turnTimer.getRemainingTime());
                checkAnswerAndScore(playersResponseTime);
                syncUserTookTurn(sp);
            }
            else
            {
                client.sendMessage(Constants.DEFAULT_CLIENT_ID, "You already completed your turn, please wait");
            }
            checkEarlyEndTurn(turnTimer.getRemainingTime());
        }
    }
    // end serverthread interactions
    //as4555 3/26/24
    public void newQuestion()
    {
        int numOfLines = 0;
        answers.clear(); //clear any answer choices from previous round     
        //as4555 4/29/24
        int fileIndex = rand.nextInt(textFiles.size());
        try (BufferedReader reader = new BufferedReader(new FileReader(textFiles.get(fileIndex)));) 
        {
            //as4555 4/28/24
            while (reader.readLine() != null) {
                numOfLines++;
            }
            reader.close();

            BufferedReader readFile = new BufferedReader(new FileReader(textFiles.get(fileIndex)));
            category = textFiles.get(fileIndex).replace(".txt", "").trim();
            int lineNumber = rand.nextInt(numOfLines) + 1;         

            for (int i = 1; i < lineNumber; i++) {
                readFile.readLine();
            }
            String line = readFile.readLine();
            questionText = line.split(";")[0];
            correctAnswer = (line.split(";")[5]);
            for (int i = 1; i <= 4; i++) {
                answers.add(line.split(";")[i]);
            }
            readFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //as4555 4/27/24
    public void addNewQuestion(ServerThread client, String category, String question) {
        boolean containsCat = false;
        for (String fileName : textFiles) {
            if (fileName.contains(category)) {
                containsCat = true;
                try (FileWriter writer = new FileWriter(fileName, true)) {
                    writer.write(System.lineSeparator());
                    writer.write(question);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!containsCat) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, category + " is not a  valid category.");
        } else {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, "Your question has been added to the list");
        }
        syncCurrentPhase();
    }
    public String getFormattedAnswers()
    {
        return answers.get(0) + "\n" + answers.get(1) + "\n" + answers.get(2) + "\n" + answers.get(3) + "\n";
    }
    public synchronized void checkAnswerAndScore(ConcurrentHashMap<ServerPlayer, Float> playersResponseTime)
    {
        playersResponseTime.forEach((ServerPlayer, timeLeft) -> {
        if (ServerPlayer.getAnswerChoice().equalsIgnoreCase(correctAnswer)) {
            ServerPlayer.calculateScore(timeLeft);
            ServerPlayer.setCorrect(true);
        }
        else {
            ServerPlayer.setScore(0);
            ServerPlayer.setCorrect(false);
        }
        playersResponseTime.remove(ServerPlayer);
        });
    }
    private synchronized void readyCheck() {
        if (readyCheckTimer == null) {
            readyCheckTimer = new TimedEvent(30, () -> {
                long numReady = players.values().stream().filter(p -> {
                    return p.isReady();
                }).count();
                // condition 1: start if we have the minimum ready
                boolean meetsMinimum = numReady >= Constants.MINIMUM_REQUIRED_TO_START;
                // condition 2: start if everyone is ready
                int totalPlayers = players.size();
                boolean everyoneIsReady = numReady >= totalPlayers;
                if (meetsMinimum || (everyoneIsReady && meetsMinimum)) {
                    start();
                } else {
                    sendMessage(Constants.FROM_ROOM, "Minimum players not met during ready check, please try again");
                    // added after recording as I forgot to reset the ready check
                    players.values().forEach(p -> {
                        p.setReady(false);
                        syncReadyState(p);
                    });
                }
                readyCheckTimer.cancel();
                readyCheckTimer = null;
            });
            readyCheckTimer.setTickCallback((time) -> System.out.println("Ready Countdown: " + time));
        }
        // handle immediate start if all players are ready (doesn't allow people to
        // easily join the room after ready check has begun)
        long numReady = players.values().stream().filter(p -> {
            return p.isReady();
        }).count();
        // condition 1: start if we have the minimum ready
        boolean meetsMinimum = numReady >= Constants.MINIMUM_REQUIRED_TO_START;
        // condition 2: start if everyone is ready
        int totalPlayers = players.size();
        boolean everyoneIsReady = numReady >= totalPlayers;
        if ((everyoneIsReady && meetsMinimum)) {
            if (readyCheckTimer != null) {
                readyCheckTimer.cancel();
                readyCheckTimer = null;
            }
            start();
        }
    }
    private void changePhase(Phase incomingChange) {
        if (currentPhase != incomingChange) {
            currentPhase = incomingChange;
            syncCurrentPhase();
        }
    }

    private void start() {
        if (currentPhase != Phase.READY) {
            System.err.println("Invalid phase called during start()");
            return;
        }
        canEndSession = false;
        changePhase(Phase.TURN);
        numActivePlayers = players.values().stream().filter(ServerPlayer::isReady).count();
        startTurnTimer();
        //as4555 3/24/24
        newQuestion();
        sendRoundNum(roundNum);
        sendQuestion(category, questionText);
        sendAnswerChoices(getFormattedAnswers());       
    }
    
    private void startTurnTimer()
    {
        if (turnTimer != null)
        {
            turnTimer.cancel();
        }
        if (turnTimer == null)
        {
            turnTimer = new TimedEvent(60, this::handleEndOfTurn);
            turnTimer.setTickCallback(this::checkEarlyEndTurn);
            //as4555 3/26/24
            
            sendMessage(Constants.FROM_ROOM, "Pick your answer choice by typing '/turn' and the corresponding letter");
        }
    }
    private void checkEarlyEndTurn(int timeRemaining)
    {  
        //as4555 4/14/24
        sendStartTimer(turnTimer.getRemainingTime());
        long numEnded = players.values().stream().filter(ServerPlayer::didTakeTurn).count();
        if (numEnded >= numActivePlayers)
        {
            //end turn early
            handleEndOfTurn();
        }
    }
    private void handleEndOfTurn()
    {
        if(turnTimer != null)
        {
            turnTimer.cancel();
            turnTimer = null;
        }
        System.out.println("Handling end of turn");
        List<ServerPlayer> playersToProcess = players.values().stream().filter(ServerPlayer::didTakeTurn).toList();
        playersToProcess.forEach(p -> {
            sendMessage(Constants.FROM_ROOM, String.format("%s chose an answer for the question", p.getClientName()));
        });
        //as4555 3/26/24
        playersResponseTime.clear();
        for (ServerPlayer player : players.values())
            {
                //as4555 4/26/24
                sendAwayStatus(player.getClientId(), player.isAway());
                player.sendMessage(Constants.DEFAULT_CLIENT_ID, answerFeedback(player)) ;//send feedback to each player about their response
                sendMessage(Constants.FROM_ROOM, String.format("%s : %f", player.getClientName(), player.getScore()));
                sendScore(player.getClientId(), player.getScore());
            }
        sendSortUsers();
        // if game ends after 10 rounds
        if(getRoundNum() >= 10)
        {
            //as4555 4/14/24
            changePhase(Phase.SHOW_RESULTS);
            float maxScore = Integer.MIN_VALUE;
            ArrayList<ServerPlayer> playerWithMaxScore = new ArrayList<ServerPlayer>();
            for (ServerPlayer player : players.values())
            {
                if  (player.getScore() > maxScore)
                {
                    maxScore = player.getScore();
                    playerWithMaxScore.clear();
                    playerWithMaxScore.add(player);
                }
                else if (player.getScore() == maxScore)
                {
                    playerWithMaxScore.add(player);
                }
            }
            if (playerWithMaxScore.size() == 1)
            {
                sendMessage(Constants.FROM_ROOM, playerWithMaxScore.get(0).getClientName() + " won the game with " + playerWithMaxScore.get(0).getScore() + " points!");
                sendWinnerMessage(playerWithMaxScore.get(0).getClientName() + " won the game with " + playerWithMaxScore.get(0).getScore() + " points!");
            }
            else{
                ArrayList<String> nameList = new ArrayList<String>();
                sendMessage(Constants.FROM_ROOM, "There was a tie with " + playerWithMaxScore.get(0).getScore() + " points between: ");
                for (ServerPlayer p : playerWithMaxScore)
                {
                    nameList.add(p.getClientName());
                    sendMessage(Constants.FROM_ROOM, p.getClientName());
                }
                sendWinnerMessage("There was a tie with " + playerWithMaxScore.get(0).getScore() + " points between: " + formatArrayString(nameList));
            }
            canEndSession = true;
            //as4555 4/16/24
            TimedEvent waitForEnd = new TimedEvent(10, this::end);
            waitForEnd.setTickCallback(null);
            //end();
            return;
        }
        // if game doesn't end
        changePhase(Phase.READY);
        updateRoundNum();
        resetTurns();
        resetCorrectness();
        start();
    }

    private String formatArrayString(ArrayList<String> nameList) {
        String names = "";
        for (String name : nameList) {
            names += name + "\n";
        }
        return names;
    }

    private String answerFeedback(ServerPlayer player)
    {
        if(player.getIsCorrect() == false) {
            return "Sorry, your answer was incorrect. The correct answer was " + correctAnswer;
        }
        else {
            return "Congrats, your answer was correct!";
        }
    }

    private void resetTurns()
    {
        players.values().stream().forEach(p->p.setTakenTurn(false));
        sendResetLocalTurns();
    }
    private void resetCorrectness(){
        //as4555 3/27/24
        // reset correct boolean for each Player
        players.values().stream().forEach(p -> {
            p.setCorrect(false);
        });
        sendResetCorrectness();
    }

    private void end() {
        //reset categories list to include all categories, not just the selected ones from this round
        //as4555 4/28/24
        textFiles.clear();
        textFiles.add("History.txt");
        textFiles.add("Literature.txt");
        textFiles.add("Popculture.txt");
        textFiles.add("Science.txt");
        textFiles.add("Sports.txt");
        // mark everyone not ready and reset their scores
        players.values().forEach(p -> {
            // fix/optimize, avoid nested loops if/when possible
            p.setReady(false);
            p.setTakenTurn(false);
            p.setCorrect(false);
            //as4555 4/26/24
            p.resetScore();
            p.sendScore(p.getClientId(), p.getScore());
            syncReadyState(p);
        });
        // depending if this is not called yet, we can clear this state here too
        sendResetLocalReadyState();
        sendResetLocalTurns();
        //as4555 3/26/24
        sendResetRoundNum();
        setRoundNum(1);;
        playersResponseTime.clear();
        changePhase(Phase.READY);
        sendMessage(Constants.FROM_ROOM, "Session over!");
    }

    // start send/sync methods
    //as4555 4/28/24
    private void sendShowCatPanel(long clientId) {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendShowCatPanel(clientId);
        }
    }
    //as4555 4/24/24
    private void sendAwayStatus(long clientId, boolean away) {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendAwayStatus(clientId, away);
        }
    }
    //as4555 4/14/24
    private void sendWinnerMessage(String winners) {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendWinnerMessage(winners);
        }
    }
    private void sendSortUsers() {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendSortUsers();
        }
    }
    private void sendScore(long clientId, float score)
    {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendScore(clientId, score);
        }
    }

    private void sendStartTimer(int timeLeft)
    {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendStartTimer(timeLeft);
        }
    }

     //as4555 3/25/24
    private void sendResetRoundNum()
    {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendResetRoundNum();
        }
    }
    private void sendRoundNum(int round)
    {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            //as4555 4/24/24
            sp.sendRoundNum(round);
        }
    }
    private void sendResetCorrectness()
    {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendResetCorrectness();
        }
    }
    private void sendQuestion(String category, String question)
    {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            //as4555 4/24/24
            sp.sendQuestion(category, question);
        }
    }
    private void sendAnswerChoices(String answerChoices)
    {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            //as4555 4/24/24
            sp.sendAnswerChoices(answerChoices);
        }
    }
    private void sendResetLocalReadyState()
    {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendResetLocalReadyState();
        }
    }
    private void sendResetLocalTurns()
    {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendResetLocalTurns();
        }
    }
    private void syncUserTookTurn(ServerPlayer isp)
    {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendPlayerTurnStatus(isp.getClientId(), isp.didTakeTurn());
        }
    }
    private void syncCurrentPhase() {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendPhase(currentPhase);
        }
    }

    private void syncReadyState(ServerPlayer csp) {
        Iterator<ServerPlayer> iter = players.values().iterator();
        while (iter.hasNext()) {
            ServerPlayer sp = iter.next();
            sp.sendReadyState(csp.getClientId(), csp.isReady());
        }
    }
    // end send/sync methods
}