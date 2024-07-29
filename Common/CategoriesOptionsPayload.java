package Project.Common;

import java.util.ArrayList;

public class CategoriesOptionsPayload extends Payload {
    public CategoriesOptionsPayload() {
        setPayloadType(PayloadType.CAT_OPTIONS);
    }

    private ArrayList<String> categories = new ArrayList<String>();

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }
    
}
