package arora.kushank.leavereport;

import java.util.ArrayList;

/**
 * Created by Kushank on 21-07-2017.
 */
public class Attachment {
    private ArrayList<String> URL;
    public Attachment(){}
    public Attachment(ArrayList<String> URL){
        this.URL=URL;
    }

    public ArrayList<String> getURL() {
        return URL;
    }

    public void setURL(ArrayList<String> URL) {
        this.URL = URL;
    }

    public void addURL(String url) {
        if(this.URL==null)
            this.URL = new ArrayList<>();
        this.URL.add(url);
    }

    public String getURL(int i){
        return this.URL.get(i);
    }

}
