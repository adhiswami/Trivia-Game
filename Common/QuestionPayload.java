package Project.Common;

//as4555 3/24/24
public class QuestionPayload extends Payload
{
    private String questionText;
    private String category = "";


    public QuestionPayload()
    {
        setPayloadType(PayloadType.CATEGORY_QUESTION);
    }
    public void setQuestionText(String question)
    {
        this.questionText = question;
    }    
    public String getQuestionText() {
        return questionText;
    }
    public void setCategory(String cateogry)
    {
        this.category = cateogry;
    }
    public String getCategory() {
        return category;
    }

 }

