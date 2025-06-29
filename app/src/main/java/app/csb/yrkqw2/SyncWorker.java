package app.csb.yrkqw2;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        SyncManager syncManager = SyncManager.getInstance(getApplicationContext());
        syncManager.syncData();
        return Result.success();
    }
}
