package Project.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

import Project.Common.AddQuestionPayload;
import Project.Common.AnswerChoicePayload;
import Project.Common.CategoriesOptionsPayload;
import Project.Common.ConnectionPayload;
import Project.Common.Constants;
import Project.Common.Payload;
import Project.Common.PayloadType;
import Project.Common.QuestionPayload;
import Project.Common.ReadyPayload;
import Project.Common.RoomResultsPayload;
import Project.Common.TextFX;
import Project.Common.TurnStatusPayload;
import Project.Common.TextFX.Color;

/**
 * A server-side representation of a single client
 */
public class ServerThread extends Thread {
    private Socket client;
    private String clientName;
    private boolean isRunning = false;
    private long clientId = Constants.DEFAULT_CLIENT_ID;
    private ObjectOutputStream out;// exposed here for send()
    // private Server server;// ref to our server so we can call methods on it
    // more easily
    private Room currentRoom;
    private Logger logger = Logger.getLogger(ServerThread.class.getName());

    private void info(String message) {
        logger.info(String.format("Thread[%s]: %s", getClientName(), message));
    }

    public ServerThread(Socket myClient/* , Room room */) {
        info("Thread created");
        // get communication channels to single client
        this.client = myClient;
        // this.currentRoom = room;

    }

    protected void setClientId(long id) {
        clientId = id;
        if (id == Constants.DEFAULT_CLIENT_ID) {
            logger.info(TextFX.colorize("Client id reset", Color.WHITE));
        }
        sendClientId(id);
    }

    protected boolean isRunning() {
        return isRunning;
    }
    protected void setClientName(String name) {
        if (name == null || name.isBlank()) {
            logger.severe("Invalid client name being set");
            return;
        }
        clientName = name;
    }

    protected String getClientName() {
        return clientName;
    }

    protected synchronized Room getCurrentRoom() {
        return currentRoom;
    }

    protected synchronized void setCurrentRoom(Room room) {
        if (room != null) {
            currentRoom = room;
        } else {
            info("Passed in room was null, this shouldn't happen");
        }
    }

    public void disconnect() {
        info("Thread being disconnected by server");
        isRunning = false;
        cleanup();
    }

    // send methods
    //as4555 4/27/24
    protected boolean sendShowCatPanel(long clientId) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CAT_PRESS);
        p.setClientId(clientId);
        return send(p);
    }
    protected boolean sendAddQuestion(PayloadType pt) {
        Payload p = new Payload();
        p.setPayloadType(pt);
        return send(p);
    }
    //as4555 4/24/24
    protected boolean sendAwayStatus(long clientId, boolean away) {
        Payload p = new Payload();
        if (away) {
            p.setPayloadType(PayloadType.AWAY);
        } else {
            p.setPayloadType(PayloadType.BACK);
        }
        p.setClientId(clientId);
        return send(p);
    }
    //as4555 4/14/24
    protected boolean sendWinnerMessage(String winner) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.WINNER_MESSAGE);
        p.setMessage(winner);
        return send(p);
    }    
    protected boolean sendSortUsers() {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.SORT_USERS);
        return send(p);
    }
    protected boolean sendScore(long clientId, float score) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.SCORE);
        p.setClientId(clientId);
        p.setMessage(String.valueOf(score));
        return send(p);
    }
    protected boolean sendStartTimer(int timeLeft)
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.TIME_LEFT);
        p.setMessage(String.valueOf(timeLeft));
        return send(p);
    }
    //as4555 3/26/24
    protected boolean sendResetRoundNum()
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RESET_ROUND_NUM);
        return send(p);
    }
    protected boolean sendRoundNum(int roundNumber)
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.ROUND_NUM);
        p.setMessage("Round " + roundNumber + ":");
        return send(p);
    }
    protected boolean sendResetCorrectness()
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RESET_CORRECTNESS);
        return send(p);
    }
    protected boolean sendAnswerChoices(String answerChoices)
    {
        AnswerChoicePayload acp = new AnswerChoicePayload();
        acp.setAnswerChoices(answerChoices);
        acp.setMessage(answerChoices);
        return send(acp);
    }
    protected boolean sendQuestion(String category, String question)
    {
        QuestionPayload qp = new QuestionPayload();
        qp.setCategory(category);
        qp.setQuestionText(question);
        qp.setMessage(category + ": \n" + question);
        return send(qp);
    }
    protected boolean sendResetLocalReadyState()
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RESET_READY);
        return send(p);
    }
    protected boolean sendResetLocalTurns()
    {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RESET_TURNS);
        return send(p);
    }
    protected boolean sendPlayerTurnStatus(long clientId, boolean didTookTurn)
    {
        TurnStatusPayload tsp = new TurnStatusPayload();
        tsp.setClientId(clientId);
        tsp.setDidTakeTurn(didTookTurn);
        return send(tsp);
    }
    protected boolean sendReadyState(long clientId, boolean isReady) {
        ReadyPayload rp = new ReadyPayload();
        rp.setReady(isReady);
        rp.setClientId(clientId);
        return send(rp);
    }

    protected boolean sendPhase(String phase) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.PHASE);
        p.setMessage(phase);
        return send(p);
    }
    protected boolean sendClientMapping(long id, String name) {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.SYNC_CLIENT);
        cp.setClientId(id);
        cp.setClientName(name);
        return send(cp);
    }

    protected boolean sendJoinRoom(String roomName) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(roomName);
        return send(p);
    }

    protected boolean sendClientId(long id) {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setClientId(id);
        cp.setClientName(clientName);
        return send(cp);
    }
    private boolean sendListRooms(List<String> potentialRooms) {
        RoomResultsPayload rp = new RoomResultsPayload();
        rp.setRooms(potentialRooms);
        if (potentialRooms == null) {
            rp.setMessage("Invalid limit, please choose a value between 1-100");
        } else if (potentialRooms.size() == 0) {
            rp.setMessage("No rooms found matching your search criteria");
        }
        return send(rp);
    }

    public boolean sendMessage(long from, String message) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        // p.setClientName(from);
        p.setClientId(from);
        p.setMessage(message);
        return send(p);
    }

    /**
     * Used to associate client names and their ids from the server perspective
     * 
     * @param whoId       id of who is connecting/disconnecting
     * @param whoName     name of who is connecting/disconnecting
     * @param isConnected status of connection (true connecting, false,
     *                    disconnecting)
     * @return
     */
    public boolean sendConnectionStatus(long whoId, String whoName, boolean isConnected) {
        ConnectionPayload p = new ConnectionPayload(isConnected);
        // p.setClientName(who);
        p.setClientId(whoId);
        p.setClientName(whoName);
        p.setMessage(isConnected ? "connected" : "disconnected");
        return send(p);
    }

    private boolean send(Payload payload) {
        // added a boolean so we can see if the send was successful
        try {
            out.writeObject(payload);
            return true;
        } catch (IOException e) {
            info("Error sending message to client (most likely disconnected)");
            // comment this out to inspect the stack trace
            // e.printStackTrace();
            cleanup();
            return false;
        } catch (NullPointerException ne) {
            info("Message was attempted to be sent before outbound stream was opened");
            return true;// true since it's likely pending being opened
        }
    }

    // end send methods
    @Override
    public void run() {
        info("Thread starting");
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());) {
            this.out = out;
            isRunning = true;
            Payload fromClient;
            while (isRunning && // flag to let us easily control the loop
                    (fromClient = (Payload) in.readObject()) != null // reads an object from inputStream (null would
                                                                     // likely mean a disconnect)
            ) {

                info("Received from client: " + fromClient);
                processPayload(fromClient);

            } // close while loop
        } catch (Exception e) {
            // happens when client disconnects
            e.printStackTrace();
            info("Client disconnected");
        } finally {
            isRunning = false;
            info("Exited thread loop. Cleaning up connection");
            cleanup();
        }
    }

    /**
     * Used to process payloads from the client and handle their data
     * 
     * @param p
     */
    private void processPayload(Payload p) {
        switch (p.getPayloadType()) {
            case CONNECT:
                try {
                    ConnectionPayload cp = (ConnectionPayload) p;
                    setClientName(cp.getClientName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case DISCONNECT:
                if (currentRoom != null) {
                    Room.disconnectClient(this, currentRoom);
                }
                break;
            case MESSAGE:
                if (currentRoom != null) {
                    currentRoom.sendMessage(this, p.getMessage());
                } else {
                    Room.joinRoom(Constants.LOBBY, this);
                }
                break;
            case CREATE_ROOM:
                Room.createRoom(p.getMessage(), this);
                break;
            case JOIN_ROOM:
                Room.joinRoom(p.getMessage(), this);
                break;
            case LIST_ROOMS:
                String searchString = p.getMessage() == null ? "" : p.getMessage();
                int limit = 10;
                try {
                    RoomResultsPayload rp = ((RoomResultsPayload) p);
                    limit = rp.getLimit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                List<String> potentialRooms = Room.listRooms(searchString, limit);
                this.sendListRooms(potentialRooms);
                break;
            case READY:
                try {
                    ((GameRoom) currentRoom).setReady(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.sendMessage(Constants.DEFAULT_CLIENT_ID,
                            "You can only use the /ready commmand in a GameRoom and not the Lobby");
                }

                break;
            case TURN:
                try {
                    //as4555 3/26/24
                    ((GameRoom) currentRoom).doTurn(this, p.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    this.sendMessage(Constants.DEFAULT_CLIENT_ID,
                            "You can only use the /turn commmand in a GameRoom and not the Lobby");
                }
                break;
            //as4555 4/24/24
            // Follow the same path, just different boolean values for away
            case AWAY:
            case BACK:
                try {
                    ((GameRoom) currentRoom).setAwayStatus(this, p.isAway());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //as4555 4/27/24
            case ADD_QUESTION:
                sendAddQuestion(p.getPayloadType());
                break;
            case NEW_QUES:
                try {
                    AddQuestionPayload aqp = ((AddQuestionPayload) p);
                    ((GameRoom) currentRoom).addNewQuestion(this, aqp.getCategory(), aqp.getQuestionText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //as4555 4/28/24
            case CAT_PRESS:
                ((GameRoom) currentRoom).categoriesButtonProcess(this);
                break;
            case CAT_OPTIONS:
                CategoriesOptionsPayload cop = ((CategoriesOptionsPayload) p);
                ((GameRoom) currentRoom).configureCategories(this, cop.getCategories());
                break;
            default:
                break;
        }

    }

    private void cleanup() {
        info("Thread cleanup() start");
        try {
            client.close();
        } catch (IOException e) {
            info("Client already closed");
        }
        info("Thread cleanup() complete");
    }

    public long getClientId() {
        return clientId;
    }
}