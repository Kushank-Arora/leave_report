package arora.kushank.leavereport;

/**
 * Created by Kushank on 21-07-2017.
 */
public class Status {
    public static String getStatusMsg(int id){
        switch(id){
            case 0:
                return "Sent to Chairman";
            case 1:
                return "Read by Chairman";
            case 2:
                return "Forwarded by Chairman";
            case 3:
                return "Rejected by Chairman";
            case 4:
                return "Approved by Chairman";
            case 5:
                return "Read by VC";
            case 6:
                return "Approved by VC";
            case 7:
                return "Rejected by VC";
            default:
                return "Error Status Message Code";
        }
    }
}
