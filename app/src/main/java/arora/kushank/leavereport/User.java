package arora.kushank.leavereport;

import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kushank on 19-Apr-17.
 */
public class User {
    private String imageURL;
    private String name;
    private String email_id;
    private String user_id;
    private String designation;
    private boolean isFemale;
    public Map<String,Integer> holidaysLeft;
    public Map<String,Integer> holidaysPending;
    private User reportingTo;

    public User(){}

    public User(User u){
        imageURL = u.imageURL;
        name = u.name;
        email_id = u.email_id;
        user_id = u.user_id;
        designation= u.designation;
    }

    public User(String imageURL, String name, String email_id, String user_id, String designation, User reportingTo){
        this.imageURL=imageURL;
        this.name=name;
        this.email_id=email_id;
        this.user_id=user_id;
        this.designation=designation;
        this.reportingTo = reportingTo;
    }

    public Map<String, Integer> getHolidaysPending() {
        return holidaysPending;
    }

    public void setHolidaysPending(Map<String, Integer> holidaysPending) {
        this.holidaysPending = holidaysPending;
    }

    public String getEmail_id() {
        return email_id;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getName() {
        return name;
    }

    public String getUser_id() {
        return user_id;
    }

    public User getReportingTo() {
        return reportingTo;
    }

    public void setReportingTo(User reportingTo) {
        this.reportingTo = reportingTo;
    }

    public Map<String, Integer> getHolidaysLeft() {
        return holidaysLeft;
    }

    public void setFemale(boolean female) {
        isFemale = female;
    }

    public boolean isFemale() {
        return isFemale;
    }

    public void setHolidays(){
        holidaysLeft = new HashMap<>(util.typeOfHolidays.size());
        for(String holiday: util.typeOfHolidays)
            holidaysLeft.put(holiday, util.holidaysAlloted.get(new Pair<>(holiday,this.isFemale)));

        holidaysPending = new HashMap<>(util.typeOfHolidays.size());
        for(String holiday: util.typeOfHolidays)
            holidaysPending.put(holiday, 0);
    }

    public int getHolidayPending(String holiday){ return holidaysPending.get(holiday);}

    public void setHolidayPendingFor(String holiday, int val) { holidaysPending.put(holiday,val); }

    public int getHolidaysLeft(String holiday){
        return holidaysLeft.get(holiday);
    }

    public void setHolidayfor(String holiday,int val){
        holidaysLeft.put(holiday,val);
    }

    public void setHolidaysLeft(Map<String, Integer> holidaysLeft) {
        this.holidaysLeft = holidaysLeft;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDesignation() {
        return designation;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
