package app.csb.yrkqw2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.csb.yrkqw2.db.AppDatabase;
import app.csb.yrkqw2.db.entity.ListEntity;

public class BoardActivity extends AppCompatActivity implements ListAdapter.OnListClickListener {
    private RecyclerView listsRecyclerView;
    private ListAdapter listAdapter;
    private List<ListEntity> listItems;
    private Button addListButton;
    private TextView boardTitleTextView;
    private TextView offlineIndicator;
    private RequestQueue requestQueue;
    private AppDatabase db;
    private int boardId;
    private String boardTitle;
    private static final String API_URL_BASE = "https://yrkqw2-5000.csb.app/api/lists/board/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        listsRecyclerView = findViewById(R.id.listsRecyclerView);
        addListButton = findViewById(R.id.addListButton);
        boardTitleTextView = findViewById(R.id.boardTitleTextView);
        offlineIndicator = findViewById(R.id.offlineIndicator);

        Intent intent = getIntent();
        boardId = intent.getIntExtra("boardId", -1);
        boardTitle = intent.getStringExtra("boardTitle");

        if (boardId == -1) {
            Toast.makeText(this, "Invalid board ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        boardTitleTextView.setText(boardTitle != null ? boardTitle : "Board");

        listItems = new ArrayList<>();
        listAdapter = new ListAdapter(listItems, this);
        listsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        listsRecyclerView.setAdapter(listAdapter);

        requestQueue = Volley.newRequestQueue(this);
        db = AppDatabase.getDatabase(this);

        checkNetworkStatus();
        loadLists();

        addListButton.setOnClickListener(v -> showAddListDialog());
    }

    private void checkNetworkStatus() {
        boolean isOnline = NetworkUtils.isNetworkAvailable(this);
        offlineIndicator.setVisibility(isOnline ? TextView.GONE : TextView.VISIBLE);
    }

    private void loadLists() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            fetchListsFromApi();
        } else {
            loadListsFromLocal();
        }
    }

    private void loadListsFromLocal() {
        new Thread(() -> {
            List<ListEntity> localLists = db.listDao().getListsByBoardId(boardId);
            runOnUiThread(() -> {
                listItems.clear();
                listItems.addAll(localLists);
                listAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void fetchListsFromApi() {
        String url = API_URL_BASE + boardId + "/lists";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    new Thread(() -> {
                        db.listDao().deleteListsByBoardId(boardId);
                        List<ListEntity> newLists = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject listJson = response.getJSONObject(i);
                                ListEntity list = new ListEntity();
                                list.id = listJson.getInt("id");
                                list.boardId = listJson.getInt("boardId");
                                list.title = listJson.getString("title");
                                list.createdAt = listJson.getString("createdAt");
                                newLists.add(list);
                                db.listDao().insertList(list);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(() -> {
                            listItems.clear();
                            listItems.addAll(newLists);
                            listAdapter.notifyDataSetChanged();
                        });
                    }).start();
                },
                error -> {
                    Toast.makeText(BoardActivity.this, "Failed to fetch lists", Toast.LENGTH_SHORT).show();
                    loadListsFromLocal();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = AuthManager.getInstance(BoardActivity.this).getToken();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(jsonArrayRequest);
    }

    private void showAddListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New List");
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter list title");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                addList(title);
            } else {
                Toast.makeText(BoardActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addList(String title) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            String token = AuthManager.getInstance(BoardActivity.this).getToken();
            if (token == null || token.isEmpty()) {
                Toast.makeText(BoardActivity.this, "Token missing or expired. Please login again.", Toast.LENGTH_LONG).show();
                startActivity(new android.content.Intent(BoardActivity.this, MainActivity.class));
                finish();
                return;
            }

            // Add list locally immediately with temporary negative ID
            ListEntity tempList = new ListEntity();
            tempList.id = -System.currentTimeMillis() > Integer.MAX_VALUE ? (int)(-System.currentTimeMillis() % Integer.MAX_VALUE) : (int)-System.currentTimeMillis();
            tempList.boardId = boardId;
            tempList.title = title;
            tempList.createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date());
            listItems.add(tempList);
            listAdapter.notifyDataSetChanged();

            JSONObject listData = new JSONObject();
            try {
                listData.put("title", title);
                Toast.makeText(BoardActivity.this, "Sending request: " + listData.toString(), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = API_URL_BASE + boardId + "/lists";

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, listData,
                    response -> {
                        try {
                            ListEntity list = new ListEntity();
                            list.id = response.getInt("id");
                            list.boardId = response.getInt("boardId");
                            list.title = response.getString("title");
                            list.createdAt = response.getString("createdAt");
                            new Thread(() -> {
                                db.listDao().insertList(list);
                                runOnUiThread(() -> {
                                    listItems.remove(tempList);
                                    listItems.add(list);
                                    listAdapter.notifyDataSetChanged();
                                    Toast.makeText(BoardActivity.this, "List added", Toast.LENGTH_SHORT).show();
                                });
                            }).start();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(BoardActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(BoardActivity.this, "Failed to add list", Toast.LENGTH_SHORT).show();
                        // Keep temp list visible on error
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            requestQueue.add(jsonObjectRequest);
        } else {
            ListEntity list = new ListEntity();
            list.boardId = boardId;
            list.title = title;
            list.createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date());
            new Thread(() -> {
                long localId = db.listDao().insertList(list);
                list.id = (int) localId;
                SyncManager.getInstance(this).queueOperation("add_list", list.id, title, String.valueOf(boardId));
                runOnUiThread(() -> {
                    listItems.add(list);
                    listAdapter.notifyDataSetChanged();
                    Toast.makeText(BoardActivity.this, "List added offline", Toast.LENGTH_SHORT).show();
                });
            }).start();
        }
    }

    @Override
    public void onListClick(ListEntity list) {
        Intent intent = new Intent(this, CardActivity.class);
        intent.putExtra("listId", list.id);
        intent.putExtra("listTitle", list.title);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetworkStatus();
        loadLists();
    }
}
