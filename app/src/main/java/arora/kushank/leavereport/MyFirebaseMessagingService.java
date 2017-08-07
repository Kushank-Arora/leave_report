package arora.kushank.leavereport;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Kushank on 30-07-2017.
 */
public class MyFirebaseMessagingService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Log.d("Token Refresh" ,  FirebaseInstanceId.getInstance().getToken());
    }
}
