package Project.Common;

/**
 * For chatroom projects, you can call this "User"
 */
public class Player {
    private boolean isReady;
    //as4555 3/24/24
    private float score = 0;
    private String answerChoice = "";
    private float POSSIBLE_POINTS = 1000;
    private float TIMER_AMT = 60;

    public String getAnswerChoice() {
        return answerChoice;
    }

    public void setAnswerChoice(String answerChoice) {
        this.answerChoice = answerChoice;
    }

    public float getScore() {
        return score;
    }
    public void setScore(float score) {
        this.score += score;
    }
    //as4555 4/26/24
    public void resetScore() {
        this.score = 0;
    }
    public void calculateScore(float responeTime)
    {
        // Kahoot!'s formula for scores 
        // https://support.kahoot.com/hc/en-us/articles/115002303908-How-points-work#:~:text=Points%20are%20awarded%20based%20on,30%2Dsecond%20question%20timer%20started.
        setScore(Math.round((1 - (((TIMER_AMT - responeTime) / TIMER_AMT) / 2)) * POSSIBLE_POINTS));
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    private boolean takenTurn;

    public boolean didTakeTurn() {
        return takenTurn;
    }

    public void setTakenTurn(boolean takenTurn) {
        this.takenTurn = takenTurn;
    }
    //as4555 3/27/24
    private boolean isCorrect = false;

    public boolean getIsCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    //as4555 4/24/24
    private boolean isAway;

    public boolean isAway() {
        return isAway;
    }

    public void setAway(boolean isAway) {
        this.isAway = isAway;
    }
    
}