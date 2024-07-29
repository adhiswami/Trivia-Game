package Project.Common;

//as4555 4/27/24
public class AddQuestionPayload extends Payload {
    public String questionText;

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String category;
    

    public AddQuestionPayload() {
        setPayloadType(PayloadType.NEW_QUES);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
