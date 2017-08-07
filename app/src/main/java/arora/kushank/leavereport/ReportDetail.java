package arora.kushank.leavereport;

import arora.kushank.leavereport.Attachment;

/**
 * Created by Kushank-Arora on 21-Apr-17.
 */
public class ReportDetail {
    private String type;
    private long durationStart;
    private long durationEnd;
    private String address;
    private Attachment attachment;

    public ReportDetail(){}

    public ReportDetail(String type, long durationStart, long durationEnd, String address,Attachment attachment){
        this.type=type;
        this.durationStart=durationStart;
        this.durationEnd=durationEnd;
        this.address=address;
        this.attachment=attachment;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public long getDurationEnd() {
        return durationEnd;
    }

    public long getDurationStart() {
        return durationStart;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDurationEnd(long durationEnd) {
        this.durationEnd = durationEnd;
    }

    public void setDurationStart(long durationStart) {
        this.durationStart = durationStart;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }
}
