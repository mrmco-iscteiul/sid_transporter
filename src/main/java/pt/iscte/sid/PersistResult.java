package pt.iscte.sid;

import com.mongodb.async.SingleResultCallback;

public class PersistResult implements SingleResultCallback<Void> {
    @Override
    public void onResult(Void aVoid, Throwable throwable) {
        if (throwable == null) {
            System.out.println("SUCCESS");
        } else {
            System.out.println("There was a problem persisting the data!!!");
        }
    }
}
