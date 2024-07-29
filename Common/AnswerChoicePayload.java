package Project.Common;

import java.util.ArrayList;

//as4555 3/26/24
public class AnswerChoicePayload extends QuestionPayload {
    protected ArrayList<String> answersList = new ArrayList<String>();
    protected String answers = "";
    public AnswerChoicePayload()
    {
        setPayloadType(PayloadType.ANSWER_CHOICE);
    }
    public void setAnswerChoicesList(ArrayList<String> choices){
        this.answersList=choices;
    }
    public void setAnswerChoices(String choices){
        this.answers=choices;
    }
    public ArrayList<String> getAnswerChoicesList() {
        return answersList;
    }
    public String getAnswerChoices() {
        return answers;
    }
}
