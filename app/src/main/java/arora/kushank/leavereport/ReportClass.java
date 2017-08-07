package arora.kushank.leavereport;

import com.google.firebase.database.Exclude;

/**
 * Created by Kushank on 19-Apr-17.
 */
public class ReportClass {
    public String subject;
    public long timeOfArrival;
    public User sender;
    public User receiver;
    public ReportDetail detail;
    public boolean readChairman;
    public boolean readVC;
    public boolean readApplicant;
    public String commentChairman;
    public String commentVC;
    public int status;
    public User copyTo;


    @Exclude
    public String ReportKey;

    public ReportClass(){}
    public ReportClass(String subject, long timeOfArrival, User sender, User receiver, ReportDetail detail, boolean readChairman, boolean readVC){
        this.subject=subject;
        this.timeOfArrival=timeOfArrival;
        this.sender=sender;
        this.detail=detail;
        this.receiver=receiver;
        this.readChairman = readChairman;
        this.readVC=readVC;
        this.status=-1;
        this.commentChairman=null;
        this.commentVC=null;
        this.readApplicant=true;
        this.copyTo = null;
    }

    public User getCopyTo() {
        return copyTo;
    }

    public void setCopyTo(User copyTo) {
        this.copyTo = copyTo;
    }

    public boolean isReadApplicant() {
        return readApplicant;
    }

    public void setReadApplicant(boolean readApplicant) {
        this.readApplicant = readApplicant;
    }

    public String getCommentVC() {
        return commentVC;
    }

    public void setCommentVC(String commentVC) {
        this.commentVC = commentVC;
    }

    public String getCommentChairman() {
        return commentChairman;
    }

    public void setCommentChairman(String commentChairman) {
        this.commentChairman = commentChairman;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    @Exclude
    public String getStatusMessage(){
        return Status.getStatusMsg(status);
    }

    public String getReportKey() {
        return ReportKey;
    }

    public void setReportKey(String reportKey) {
        ReportKey = reportKey;
    }

    public void setReadChairman(boolean readChairman) {
        this.readChairman = readChairman;
    }

    public void setReadVC(boolean readVC) {
        this.readVC = readVC;
    }

    public boolean isReadChairman() {
        return readChairman;
    }

    public boolean isReadVC() {
        return readVC;
    }

    public User getReceiver() {
        return receiver;
    }

    public long getTimeOfArrival() {
        return timeOfArrival;
    }

    public ReportDetail getDetail() {
        return detail;
    }

    public String getSubject() {
        return subject;
    }

    public User getSender() {
        return sender;
    }

    public void setDetail(ReportDetail detail) {
        this.detail = detail;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTimeOfArrival(long timeOfArrival) {
        this.timeOfArrival = timeOfArrival;
    }
}
