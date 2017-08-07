package arora.kushank.leavereport;

import android.content.Context;
import android.util.Pair;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by Kushank-Arora on 21-Apr-17.
 */
public class util {
    public static ArrayList<String> typeOfHolidays;
    public static Map<Pair<String,Boolean>,Integer> holidaysAlloted;
    public static String getTimeString(long timeOfArrival,Context context) {
        int today = Calendar.getInstance().get(Calendar.DATE);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeOfArrival);
        int msgDate = cal.get(Calendar.DATE);

        if (msgDate == today) {
            return DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime());
        } else {
            return DateFormat.getDateInstance(DateFormat.SHORT).format(cal.getTime());
        }
    }
    public static String getDateString(long time){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        String month[]={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        return cal.get(Calendar.DATE)+"-"+month[cal.get(Calendar.MONTH)]+"-"+cal.get(Calendar.YEAR);
    }

    public static long getDaysCount(long mTime1, long mTime2) {
        return (mTime2-mTime1)/(24*60*60*1000)+1;
    }

    public static void sort(ArrayList<ReportClass> reportsList) {
        Collections.sort(reportsList, new Comparator<ReportClass>() {
            @Override
            public int compare(ReportClass lhs, ReportClass rhs) {
                if (lhs.getTimeOfArrival()<rhs.getTimeOfArrival())
                    return 1;
                else if(lhs.getTimeOfArrival()>rhs.getTimeOfArrival())
                    return -1;
                else
                    return 0;
            }
        });
    }

    public static boolean canBeAcceptedByChairman(String leave){
        return true;
    }

    public static String convertToString(ReportClass curReport) {
        String message="Leave Report";
        message += "\nSender: "+curReport.getSender().getName();
        message += "\nReason For Leave: "+curReport.getSubject();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(curReport.getTimeOfArrival());

        message += "\nType of Leave: "+curReport.getDetail().getType();
        message += "\nApplication Date: "+DateFormat.getDateTimeInstance().format(cal.getTime());
        message += "\nFrom: "+util.getDateString(curReport.getDetail().getDurationStart());
        message += "\nTill: "+util.getDateString(curReport.getDetail().getDurationEnd());
        message += "\nAddress on Leave: "+curReport.getDetail().getAddress();
        message += "\nAttachment: " + (curReport.getDetail().getAttachment()==null || curReport.getDetail().getAttachment().getURL()==null ||
                curReport.getDetail().getAttachment().getURL().size()==0 ? "No" : "Yes");

        return message;
    }
}
