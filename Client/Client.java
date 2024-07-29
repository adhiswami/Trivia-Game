package Project.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import Project.Common.AddQuestionPayload;
import Project.Common.AnswerChoicePayload;
import Project.Common.CategoriesOptionsPayload;
import Project.Common.ConnectionPayload;
import Project.Common.Constants;
import Project.Common.Payload;
import Project.Common.PayloadType;
import Project.Common.Phase;
import Project.Common.QuestionPayload;
import Project.Common.ReadyPayload;
import Project.Common.RoomResultsPayload;
import Project.Common.TextFX;
import Project.Common.TurnStatusPayload;
import Project.Common.TextFX.Color;

public enum Client {
    INSTANCE;

    private Socket server = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    final String ipAddressPattern = "/connect\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{3,5})";
    final String localhostPattern = "/connect\\s+(localhost:\\d{3,5})";
    boolean isRunning = false;
    private Thread inputThread;
    private Thread fromServerThread;
    private String clientName = "";


    private static final String CREATE_ROOM = "/createroom";
    private static final String JOIN_ROOM = "/joinroom";
    private static final String LIST_ROOMS = "/listrooms";
    private static final String LIST_USERS = "/users";
    private static final String DISCONNECT = "/disconnect";
    private static final String READY_CHECK = "/ready";
    private static final String SIMULATE_TURN = "/turn";

    // client id, is the key, client name is the value
    // private ConcurrentHashMap<Long, String> clientsInRoom = new
    // ConcurrentHashMap<Long, String>();
    private ConcurrentHashMap<Long, ClientPlayer> clientsInRoom = new ConcurrentHashMap<Long, ClientPlayer>();
    private long myClientId = Constants.DEFAULT_CLIENT_ID;

    //as4555 4/24/24
    public long getMyClientId() {
        return myClientId;
    }

    private Logger logger = Logger.getLogger(Client.class.getName());
    private Phase currentPhase = Phase.READY;

    //callback that updates the UI
    private static List<IClientEvents> events = new ArrayList<IClientEvents>();

    public void addCallback(IClientEvents e) {
        events.add(e);
    }

    public boolean isConnected() {
        if (server == null) {
            return false;
        }
        // https://stackoverflow.com/a/10241044
        // Note: these check the client's end of the socket connect; therefore they
        // don't really help determine
        // if the server had a problem
        return server.isConnected() && !server.isClosed() && !server.isInputShutdown() && !server.isOutputShutdown();

    }

    private boolean isQuit(String text) {
        return text.equalsIgnoreCase("/quit");
    }

    private boolean isName(String text) {
        if (text.startsWith("/name")) {
            String[] parts = text.split(" ");
            if (parts.length >= 2) {
                clientName = parts[1].trim();
                logger.info("Name set to " + clientName);
            }
            return true;
        }
        return false;
    }
    /**
     * Controller for handling various text commands.
     * <p>
     * Add more here as needed
     * </p>
     * 
     * @param text
     * @return true if a text was a command or triggered a command
     */
    //process client commands
     private boolean processClientCommand(String text) {
        if (isQuit(text)) {
            isRunning = false;
            return true;
        } else if (isName(text)) {
            return true;
        } else if (text.startsWith(CREATE_ROOM)) {

            try {
                String roomName = text.replace(CREATE_ROOM, "").trim();
                sendCreateRoom(roomName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (text.startsWith(JOIN_ROOM)) {

            try {
                String roomName = text.replace(JOIN_ROOM, "").trim();
                sendJoinRoom(roomName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (text.startsWith(LIST_ROOMS)) {

            try {
                String searchQuery = text.replace(LIST_ROOMS, "").trim();
                sendListRooms(searchQuery);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (text.equalsIgnoreCase(LIST_USERS)) {
            System.out.println(TextFX.colorize("Users in Room: ", Color.CYAN));
            clientsInRoom.forEach(((clientId, u) -> {
                System.out.println(TextFX.colorize((
                    String.format("%s - %s [%s] %s", 
                            clientId, 
                            u.getClientName(), 
                            u.isReady(), 
                            u.didTakeTurn() ? "*" : "")),
                            Color.CYAN));
            }));
            return true;
        }
        else if (text.equalsIgnoreCase(DISCONNECT)) {
            try {
                sendDisconnect();
            }
            catch(Exception e){
              e.printStackTrace(); 
            }
            return true;
        }
        else if (text.equalsIgnoreCase(READY_CHECK)) {
            try {
                sendReadyCheck();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        else if (text.startsWith(SIMULATE_TURN))
        {
            try {
                String chosenAnswer = text.replace(SIMULATE_TURN, "").trim();
                sendTakeTurn(chosenAnswer);
            }
            catch(Exception e){
              e.printStackTrace(); 
            }
            return true;
        }
        return false;
    }



    /**
     * Takes an ip address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @param username
     * @param callback (for triggering UI events)
     * @return true if connection was successful
     */
    public boolean connect(String address, int port, String username, IClientEvents callback) {
        clientName = username;
        addCallback(callback);
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            logger.info("Client connected");
            listenForServerPayload();
            sendConnect();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected();
    }
    // Send methods
    //as4555 4/28/24
    public void sendCatOptions(ArrayList<String> categories) throws IOException {
        CategoriesOptionsPayload cop = new CategoriesOptionsPayload();
        cop.setCategories(categories);
        out.writeObject(cop);
    }
    public void sendCatButtonPress() throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CAT_PRESS);
        out.writeObject(p);
    }
    //as4555 4/27/24
    public void sendNewQuestion(String category, String question) throws IOException{
        AddQuestionPayload aqp = new AddQuestionPayload();
        aqp.setCategory(category);
        aqp.setQuestionText(question);
        out.writeObject(aqp);
    }
    public void sendAddQuestion() throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.ADD_QUESTION);
        out.writeObject(p);
    }
    //as4555 4/24/24
    public void sendAwayStatus(boolean away) throws IOException {
        Payload p = new Payload();
        if (away) {
            p.setPayloadType(PayloadType.AWAY);
        } else {
            p.setPayloadType(PayloadType.BACK);
        }
        p.setAway(away);
        out.writeObject(p);
    }
    // as4555 3/26/24
    public void sendTakeTurn(String chosenAnswer) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.TURN);
        p.setMessage(chosenAnswer);
        out.writeObject(p);
    }
    public void sendReadyCheck() throws IOException {
        ReadyPayload rp = new ReadyPayload();
        out.writeObject(rp);
    }
    void sendDisconnect() throws IOException {
        ConnectionPayload cp = new ConnectionPayload();
        cp.setPayloadType(PayloadType.DISCONNECT);
        out.writeObject(cp);
    }
    public void sendCreateRoom(String roomName) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CREATE_ROOM);
        p.setMessage(roomName);
        out.writeObject(p);
    }

    public void sendJoinRoom(String roomName) throws IOException {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(roomName);
        out.writeObject(p);
    }

    public void sendListRooms(String searchString) throws IOException {
        // Updated after video to use RoomResultsPayload so we can (later) use a limit
        // value
        RoomResultsPayload p = new RoomResultsPayload();
        p.setMessage(searchString);
        p.setLimit(10);
        out.writeObject(p);
    }

    private void sendConnect() throws IOException {
        ConnectionPayload p = new ConnectionPayload(true);

        p.setClientName(clientName);
        out.writeObject(p);
    }

    public void sendMessage(String message) throws IOException {
        if (message.startsWith("/") && processClientCommand(message)) {
            return;
        }
        Payload p = new Payload();
        p.setPayloadType(PayloadType.MESSAGE);
        p.setMessage(message);
        // no need to send an identifier, because the server knows who we are
        // p.setClientName(clientName);
        out.writeObject(p);
    }

    // end send methods
    private void listenForServerPayload() {
        fromServerThread = new Thread() {
            @Override
            public void run() {
                try {
                    Payload fromServer;
                    isRunning = true;
                    // while we're connected, listen for strings from server
                    while (isRunning && !server.isClosed() && !server.isInputShutdown()
                            && (fromServer = (Payload) in.readObject()) != null) {

                        logger.info("Debug Info: " + fromServer);
                        processPayload(fromServer);

                    }
                    logger.info("Loop exited");
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!server.isClosed()) {
                        logger.severe("Server closed connection");
                    } else {
                        logger.severe("Connection closed");
                    }
                } finally {
                    close();
                    logger.info("Stopped listening to server input");
                }
            }
        };
        fromServerThread.start();// start the thread
    }

    private void addClientReference(long id, String name) {
        if (!clientsInRoom.containsKey(id)) {
            ClientPlayer cp = new ClientPlayer();
            cp.setClientId(id);
            cp.setClientName(name);
            clientsInRoom.put(id, cp);
        }
    }

    private void removeClientReference(long id) {
        if (clientsInRoom.containsKey(id)) {
            clientsInRoom.remove(id);
        }
    }

    public String getClientNameFromId(long id) {
        if (clientsInRoom.containsKey(id)) {
            return clientsInRoom.get(id).getClientName();
        }
        if (id == Constants.DEFAULT_CLIENT_ID) {
            return "[Room]";
        }
        return "[name not found]";
    }
    /**
     * Used to process payloads from the server-side and handle their data
     * 
     * @param p
     */
    private void processPayload(Payload p) {
        String message;
        switch (p.getPayloadType()) {
            case CLIENT_ID:
                if (myClientId == Constants.DEFAULT_CLIENT_ID) {
                    myClientId = p.getClientId();
                    addClientReference(myClientId, ((ConnectionPayload) p).getClientName());
                    logger.info(TextFX.colorize("My Client Id is " + myClientId, Color.GREEN));
                } else {
                    logger.info(TextFX.colorize("Setting client id to default", Color.RED));
                }
                events.forEach(e -> {
                    e.onReceiveClientId(p.getClientId());
                });
                break;
            case CONNECT:// for now connect,disconnect are all the same
            case DISCONNECT:
                ConnectionPayload cp = (ConnectionPayload) p;
                message = TextFX.colorize(String.format("*%s %s*",
                        cp.getClientName(),
                        cp.getMessage()), Color.YELLOW);
                logger.info(message);
            case SYNC_CLIENT:
                ConnectionPayload cp2 = (ConnectionPayload) p;
                if (cp2.getPayloadType() == PayloadType.CONNECT || cp2.getPayloadType() == PayloadType.SYNC_CLIENT) {
                    addClientReference(cp2.getClientId(), cp2.getClientName());
                } else if (cp2.getPayloadType() == PayloadType.DISCONNECT) {
                    removeClientReference(cp2.getClientId());
                }

                if (cp2.getPayloadType() == PayloadType.CONNECT) {
                    events.forEach(e -> {
                        e.onClientConnect(p.getClientId(), cp2.getClientName(), p.getMessage());
                    });
                } else if (cp2.getPayloadType() == PayloadType.DISCONNECT) {
                    events.forEach(e -> {
                        e.onClientDisconnect(p.getClientId(), cp2.getClientName(), p.getMessage());
                    });
                } else if (cp2.getPayloadType() == PayloadType.SYNC_CLIENT) {
                    events.forEach(e -> {
                        e.onSyncClient(p.getClientId(), cp2.getClientName());
                    });
                }
                break;
            case JOIN_ROOM:
                clientsInRoom.clear();// we changed a room so likely need to clear the list
                events.forEach(e -> {
                    e.onResetUserList();
                });
                events.forEach(e -> {
                    e.onRoomJoin(p.getMessage());
                });
                break;
            case MESSAGE:

                message = TextFX.colorize(String.format("%s: %s",
                        getClientNameFromId(p.getClientId()),
                        p.getMessage()), Color.BLUE);
                System.out.println(message);
                //as4555 4/13/24
                if (p.getClientId() == Constants.DEFAULT_CLIENT_ID) {
                    events.forEach(e -> {
                        if (e instanceof IGameEvents) {
                            ((IGameEvents) e).onRoomMessageReceive(p.getMessage());
                        }
                    });
                } else {
                    events.forEach(e -> {
                    e.onMessageReceive(p.getClientId(), p.getMessage());
                });
                }
                
                break;
            case LIST_ROOMS:
                try {
                    RoomResultsPayload rp = (RoomResultsPayload) p;
                    // if there's a message, print it
                    if (rp.getMessage() != null && !rp.getMessage().isBlank()) {
                        message = TextFX.colorize(rp.getMessage(), Color.RED);
                        logger.info(message);
                    }
                    // print room names found
                    List<String> rooms = rp.getRooms();
                    System.out.println(TextFX.colorize("Room Results", Color.CYAN));
                    for (int i = 0; i < rooms.size(); i++) {
                        String msg = String.format("%s %s", (i + 1), rooms.get(i));
                        System.out.println(TextFX.colorize(msg, Color.CYAN));
                    }
                    events.forEach(e -> {
                        e.onReceiveRoomList(rp.getRooms(), rp.getMessage());
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case READY:
                try {
                    ReadyPayload rp = (ReadyPayload) p;
                    if (clientsInRoom.containsKey(rp.getClientId())) {
                        clientsInRoom.get(rp.getClientId()).setReady(rp.isReady());
                    }
                    if (p.getClientId() == myClientId) {
                        events.forEach(e -> {
                            if (e instanceof IGameEvents) {
                                ((IGameEvents) e).onReceiveReady(p.getClientId(), rp.isReady());
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PHASE:
                try {
                    currentPhase = Enum.valueOf(Phase.class, p.getMessage());
                    events.forEach(e -> {
                        if (e instanceof IGameEvents) {
                            ((IGameEvents) e).onReceivePhase(currentPhase);
                        }
                    });
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case TURN:
                try{
                    TurnStatusPayload tsp = (TurnStatusPayload)p;
                    if (clientsInRoom.containsKey(tsp.getClientId())) {
                        clientsInRoom.get(tsp.getClientId()).setTakenTurn(tsp.isDidTakeTurn());
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;  
            case RESET_TURNS:
                clientsInRoom.values().stream().forEach(c->c.setTakenTurn(false));
                break;
            case RESET_READY:
                clientsInRoom.values().stream().forEach(c->c.setReady(false));
                break;
            case CATEGORY_QUESTION:
                QuestionPayload qp = (QuestionPayload)p;
                System.out.println(TextFX.colorize(qp.getMessage(), Color.RED));
                //as4555 3/27/24
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveCategoryQuestion(qp.getMessage());
                    }
                });
                break;
            case ANSWER_CHOICE:
                AnswerChoicePayload acp = (AnswerChoicePayload)p;
                System.out.println(TextFX.colorize(acp.getMessage(), Color.RED));
                //as4555 4/13/24
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveAnswers(acp.getMessage());
                    }
                });
                break;
            case RESET_CORRECTNESS:
                clientsInRoom.values().stream().forEach(c->c.setCorrect(false));
                break;
            case ROUND_NUM:
                System.out.println(TextFX.colorize(p.getMessage(), Color.RED));
                //as4555 4/14/24
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveRoundNum(p.getMessage());
                    }
                });
                break;
            case RESET_ROUND_NUM:
                message = TextFX.colorize(p.getMessage(), Color.YELLOW);
                logger.info(message);
                break;
            //as4555 4/14/24
            case TIME_LEFT:
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveTimeLeft(p.getMessage());
                    }
                });
                break;
            case SCORE:
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveScore(p.getClientId(), p.getMessage());
                    }
                });
                break;
            case SORT_USERS:
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveSortUser();
                    }
                });
                break;
            case WINNER_MESSAGE:
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveWinnerMessage(p.getMessage());
                    }
                });
                break;
            //as4555 4/24/24
            case AWAY:
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveAwayStatus(p.getClientId(), true);
                    }
                });
                break;
            case BACK:
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveAwayStatus(p.getClientId(), false);
                    }
                });
                break;
            //as4555 4/27/24
            case ADD_QUESTION:
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveAddQuestion();
                    }
                });
                break;
            //as4555 4/28/24
            case CAT_PRESS:
                events.forEach(e -> {
                    if (e instanceof IGameEvents) {
                        ((IGameEvents) e).onReceiveCatPress(p.getClientId());
                    }
                });
                break;
            default:
                break;

        }
    }

    private void close() {
        myClientId = Constants.DEFAULT_CLIENT_ID;
        clientsInRoom.clear();
        try {
            inputThread.interrupt();
        } catch (Exception e) {
            logger.severe("Error interrupting input");
            e.printStackTrace();
        }
        try {
            fromServerThread.interrupt();
        } catch (Exception e) {
            logger.severe("Error interrupting listener");
            e.printStackTrace();
        }
        try {
            logger.info("Closing output stream");
            out.close();
        } catch (NullPointerException ne) {
            logger.severe("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            logger.info("Closing input stream");
            in.close();
        } catch (NullPointerException ne) {
            logger.severe("Server was never opened so this exception is ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            logger.info("Closing connection");
            server.close();
            logger.severe("Closed socket");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ne) {
            logger.warning("Server was never opened so this exception is ok");
        }
    }
}