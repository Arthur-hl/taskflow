package app.csb.yrkqw2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
import app.csb.yrkqw2.db.entity.BoardEntity;

public class DashboardActivity extends AppCompatActivity implements BoardAdapter.OnBoardClickListener {
    private RecyclerView boardsRecyclerView;
    private BoardAdapter boardAdapter;
    private List<BoardEntity> boardList;
    private Button addBoardButton;
    private TextView offlineIndicator;
    private RequestQueue requestQueue;
    private AppDatabase db;
    private static final String API_URL = "https://yrkqw2-5000.csb.app/api/boards";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        boardsRecyclerView = findViewById(R.id.boardsRecyclerView);
        addBoardButton = findViewById(R.id.addBoardButton);
        offlineIndicator = findViewById(R.id.offlineIndicator);

        boardList = new ArrayList<>();
        boardAdapter = new BoardAdapter(boardList, this);
        boardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        boardsRecyclerView.setAdapter(boardAdapter);

        requestQueue = Volley.newRequestQueue(this);
        db = AppDatabase.getDatabase(this);

        checkNetworkStatus();
        loadBoards();

        addBoardButton.setOnClickListener(v -> showAddBoardDialog());
    }

    private void checkNetworkStatus() {
        boolean isOnline = NetworkUtils.isNetworkAvailable(this);
        offlineIndicator.setVisibility(isOnline ? TextView.GONE : TextView.VISIBLE);
    }

    private void loadBoards() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            fetchBoardsFromApi();
        } else {
            loadBoardsFromLocal();
        }
    }

    private void loadBoardsFromLocal() {
        new Thread(() -> {
            List<BoardEntity> localBoards = db.boardDao().getAllBoards();
            runOnUiThread(() -> {
                boardList.clear();
                boardList.addAll(localBoards);
                boardAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void fetchBoardsFromApi() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, API_URL, null,
                response -> {
                    new Thread(() -> {
                        db.boardDao().deleteAllBoards();
                        List<BoardEntity> newBoards = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject boardJson = response.getJSONObject(i);
                                BoardEntity board = new BoardEntity();
                                board.id = boardJson.getInt("id");
                                board.title = boardJson.getString("title");
                                board.createdAt = boardJson.getString("createdAt");
                                newBoards.add(board);
                                db.boardDao().insertBoard(board);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(() -> {
                            boardList.clear();
                            boardList.addAll(newBoards);
                            boardAdapter.notifyDataSetChanged();
                        });
                    }).start();
                },
                error -> {
                    String message = "Failed to fetch boards: Unknown error";
                    if (error.networkResponse != null) {
                        message = "Failed to fetch boards: HTTP Status " + error.networkResponse.statusCode;
                        if (error.networkResponse.data != null) {
                            try {
                                JSONObject errorJson = new JSONObject(new String(error.networkResponse.data));
                                message += " - " + errorJson.optString("message", "No detailed message");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                message += " - Error parsing error response";
                            }
                        }
                    } else if (error.getMessage() != null) {
                        message = "Failed to fetch boards: " + error.getMessage();
                    }
                    Toast.makeText(DashboardActivity.this, message, Toast.LENGTH_LONG).show();
                    loadBoardsFromLocal();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = AuthManager.getInstance(DashboardActivity.this).getToken();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(jsonArrayRequest);
    }

    private void showAddBoardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Board");
        final EditText input = new EditText(this);
        input.setHint("Enter board title");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (!title.isEmpty()) {
                addBoard(title);
            } else {
                Toast.makeText(DashboardActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addBoard(String title) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            String token = AuthManager.getInstance(DashboardActivity.this).getToken();
            if (token == null || token.isEmpty()) {
                Toast.makeText(DashboardActivity.this, "Token missing or expired. Please login again.", Toast.LENGTH_LONG).show();
                // Redirect to login
                startActivity(new android.content.Intent(DashboardActivity.this, MainActivity.class));
                finish();
                return;
            }
            Toast.makeText(DashboardActivity.this, "Using token: " + token, Toast.LENGTH_LONG).show();

            // Add board locally immediately with temporary negative ID
            BoardEntity tempBoard = new BoardEntity();
            tempBoard.id = -System.currentTimeMillis() > Integer.MAX_VALUE ? (int)(-System.currentTimeMillis() % Integer.MAX_VALUE) : (int)-System.currentTimeMillis();
            tempBoard.title = title;
            tempBoard.createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date());
            boardList.add(tempBoard);
            boardAdapter.notifyDataSetChanged();

            JSONObject boardData = new JSONObject();
            try {
                boardData.put("title", title);
                Toast.makeText(DashboardActivity.this, "Sending request: " + boardData.toString(), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_URL, boardData,
                    response -> {
                        try {
                            BoardEntity board = new BoardEntity();
                            board.id = response.getInt("id");
                            board.title = response.getString("title");
                            board.createdAt = response.getString("createdAt");
                            new Thread(() -> {
                                db.boardDao().insertBoard(board);
                                // Remove temp board and add updated board
                                runOnUiThread(() -> {
                                    boardList.remove(tempBoard);
                                    boardList.add(board);
                                    boardAdapter.notifyDataSetChanged();
                                    Toast.makeText(DashboardActivity.this, "Board added", Toast.LENGTH_SHORT).show();
                                });
                            }).start();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(DashboardActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        String message = "Failed to add board: Unknown error";
                        if (error.networkResponse != null) {
                            message = "Failed to add board: HTTP Status " + error.networkResponse.statusCode;
                            if (error.networkResponse.data != null) {
                                try {
                                    JSONObject errorJson = new JSONObject(new String(error.networkResponse.data));
                                    message += " - " + errorJson.optString("message", "No detailed message");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    message += " - Error parsing error response";
                                }
                            }
                            if (error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403) {
                                Toast.makeText(DashboardActivity.this, "Authentication error. Please login again.", Toast.LENGTH_LONG).show();
                                startActivity(new android.content.Intent(DashboardActivity.this, MainActivity.class));
                                finish();
                                return;
                            }
                        } else if (error.getMessage() != null) {
                            message = "Failed to add board: " + error.getMessage();
                        }
                        Toast.makeText(DashboardActivity.this, message, Toast.LENGTH_LONG).show();
                        // Keep temp board visible on error
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
            BoardEntity board = new BoardEntity();
            board.title = title;
            board.createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date());
            new Thread(() -> {
                long localId = db.boardDao().insertBoard(board);
                board.id = (int) localId;
                SyncManager.getInstance(this).queueOperation("add_board", board.id, title);
                runOnUiThread(() -> {
                    boardList.add(board);
                    boardAdapter.notifyDataSetChanged();
                    Toast.makeText(DashboardActivity.this, "Board added offline", Toast.LENGTH_SHORT).show();
                });
            }).start();
        }
    }

    @Override
    public void onBoardClick(BoardEntity board) {
        Intent intent = new Intent(this, BoardActivity.class);
        intent.putExtra("boardId", board.id);
        intent.putExtra("boardTitle", board.title);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetworkStatus();
        loadBoards();
    }
}
