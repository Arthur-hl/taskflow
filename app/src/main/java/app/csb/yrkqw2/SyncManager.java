package app.csb.yrkqw2;

import android.content.Context;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.csb.yrkqw2.db.AppDatabase;
import app.csb.yrkqw2.db.entity.SyncQueueEntity;

public class SyncManager {
    private static final String API_BOARDS_URL = "https://yrkqw2-5000.csb.app/api/boards";
    private static final String API_LISTS_URL = "https://yrkqw2-5000.csb.app/api/lists";
    private static final String API_CARDS_URL = "https://yrkqw2-5000.csb.app/api/cards";
    private static SyncManager instance;
    private final AppDatabase db;
    private final RequestQueue requestQueue;
    private final AuthManager authManager;
    private final Context context;

    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = AppDatabase.getDatabase(this.context);
        this.requestQueue = Volley.newRequestQueue(this.context);
        this.authManager = AuthManager.getInstance(this.context);
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context.getApplicationContext());
        }
        return instance;
    }

    public void queueOperation(String operation, int localId, String... params) {
        String entityType = "";
        String endpoint = "";
        String action = "CREATE";
        
        switch (operation) {
            case "add_board":
                entityType = "Board";
                endpoint = API_BOARDS_URL;
                break;
            case "add_list":
                entityType = "List";
                endpoint = API_LISTS_URL;
                break;
            case "add_card":
                entityType = "Card";
                endpoint = API_CARDS_URL;
                break;
            default:
                return;
        }

        StringBuilder payloadBuilder = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            payloadBuilder.append(params[i]);
            if (i < params.length - 1) {
                payloadBuilder.append("||");
            }
        }
        
        SyncQueueEntity syncItem = new SyncQueueEntity(
            entityType,
            localId,
            action,
            payloadBuilder.toString(),
            endpoint,
            System.currentTimeMillis()
        );

        new Thread(() -> db.syncQueueDao().insert(syncItem)).start();

        if (NetworkUtils.isNetworkAvailable(context)) {
            WorkManager.getInstance(context)
                    .enqueue(new OneTimeWorkRequest.Builder(SyncWorker.class).build());
        }
    }

    public void syncData() {
        new Thread(() -> {
            List<SyncQueueEntity> queueItems = db.syncQueueDao().getAll();
            for (SyncQueueEntity item : queueItems) {
                processSyncItem(item);
            }
        }).start();
    }

    private void processSyncItem(SyncQueueEntity item) {
        String[] dataParts = item.payload.split("\\|\\|");
        String url = item.endpoint;
        JSONObject jsonData = new JSONObject();

        try {
            switch (item.entityType) {
                case "Board":
                    jsonData.put("title", dataParts[0]);
                    break;
                case "List":
                    jsonData.put("title", dataParts[0]);
                    jsonData.put("boardId", Integer.parseInt(dataParts[1]));
                    break;
                case "Card":
                    jsonData.put("title", dataParts[0]);
                    if (dataParts.length > 2) {
                        jsonData.put("description", dataParts[1]);
                    }
                    jsonData.put("listId", Integer.parseInt(dataParts[dataParts.length - 1]));
                    break;
                default:
                    return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonData,
                response -> {
                    // Handle response, update local ID with server ID if necessary
                    new Thread(() -> db.syncQueueDao().delete(item)).start();
                },
                error -> {
                    // Log error, keep item in queue for retry
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = authManager.getToken();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }
}
