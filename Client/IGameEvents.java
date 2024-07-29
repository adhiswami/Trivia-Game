package Project.Client;

import Project.Common.Phase;


public interface IGameEvents extends IClientEvents {
    /**
     * Triggered when a player marks themselves ready
     * 
     * @param clientId Use -1 to reset the list
     */
    void onReceiveReady(long clientId, boolean isReady);

    /**
     * Triggered when client receives phase update from server
     * 
     * @param phase
     */
    void onReceivePhase(Phase phase);

    //as4555 4/12/24
     /**
     * Triggered when client receives category and question from server
     * 
     * @param categoryQuestion
     */
    void onReceiveCategoryQuestion(String categoryQuestion);

     /**
     * Triggered when client receives answer choices from server
     * 
     * @param categoryQuestion
     */
    void onReceiveAnswers(String answerChoices);

    /**
     * Triggered when a message is received from Room (especially Gameroom)
     * 
     * @param message
     */
    void onRoomMessageReceive(String message);

    /**
     * Triggered when a message is received from Room (especially Gameroom)
     * 
     * @param roundNum
     */
    void onReceiveRoundNum(String roundNum);

    /**
     * Triggered when time left in a turn is sent from Room
     * 
     * @param timeLeft
     */
    void onReceiveTimeLeft(String timeLeft);

    /**
     * Triggered when time left in a turn is sent from Room
     * 
     * @param clientId
     * @param score
     */
    void onReceiveScore(long clientId, String score);

    /**
     * Triggered when user list can be sorted by score order after a round is over
     * 
     * @param timeLeft
     */
    void onReceiveSortUser();

    /**
     * Triggered when gameroom sends a message about winner(s)
     * 
     * @param timeLeft
     */
    void onReceiveWinnerMessage(String winner);

    //as4555 4/24/24
    /**
     * Triggered when a player marks themself away/back
     * 
     * @param timeLeft
     */
    void onReceiveAwayStatus(long clientId, boolean away);

    //as4555 4/27/24
    /**
     * Triggered when a player clicks add new question button
     * 
     * @param timeLeft
     */
    void onReceiveAddQuestion();

    //as4555 4/28/24
    /**
     * Triggered when a player clicks choose categories button
     * 
     * @param timeLeft
     */
    void onReceiveCatPress(long clientId);
}