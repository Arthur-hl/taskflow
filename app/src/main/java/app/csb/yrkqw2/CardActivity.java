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
import app.csb.yrkqw2.db.entity.CardEntity;

public class CardActivity extends AppCompatActivity implements CardAdapter.OnCardClickListener {
    private RecyclerView cardsRecyclerView;
    private CardAdapter cardAdapter;
    private List<CardEntity> cardList;
    private Button addCardButton;
    private TextView listTitleTextView;
    private TextView offlineIndicator;
    private RequestQueue requestQueue;
    private AppDatabase db;
    private int listId;
    private String listTitle;
    private static final String API_URL = "https://yrkqw2-5000.csb.app/api/cards";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        cardsRecyclerView = findViewById(R.id.cardsRecyclerView);
        addCardButton = findViewById(R.id.addCardButton);
        listTitleTextView = findViewById(R.id.listTitleTextView);
        offlineIndicator = findViewById(R.id.offlineIndicator);

        Intent intent = getIntent();
        listId = intent.getIntExtra("listId", -1);
        listTitle = intent.getStringExtra("listTitle");

        if (listId == -1) {
            Toast.makeText(this, "Invalid list ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listTitleTextView.setText(listTitle != null ? listTitle : "List");

        cardList = new ArrayList<>();
        cardAdapter = new CardAdapter(cardList, this);
        cardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardsRecyclerView.setAdapter(cardAdapter);

        requestQueue = Volley.newRequestQueue(this);
        db = AppDatabase.getDatabase(this);

        checkNetworkStatus();
        loadCards();

        addCardButton.setOnClickListener(v -> showAddCardDialog());
    }

    private void checkNetworkStatus() {
        boolean isOnline = NetworkUtils.isNetworkAvailable(this);
        offlineIndicator.setVisibility(isOnline ? TextView.GONE : TextView.VISIBLE);
    }

    private void loadCards() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            fetchCardsFromApi();
        } else {
            loadCardsFromLocal();
        }
    }

    private void loadCardsFromLocal() {
        new Thread(() -> {
            List<CardEntity> localCards = db.cardDao().getCardsByListId(listId);
            runOnUiThread(() -> {
                cardList.clear();
                cardList.addAll(localCards);
                cardAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void fetchCardsFromApi() {
        String url = API_URL + "?listId=" + listId;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    new Thread(() -> {
                        db.cardDao().deleteCardsByListId(listId);
                        List<CardEntity> newCards = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject cardJson = response.getJSONObject(i);
                                CardEntity card = new CardEntity();
                                card.id = cardJson.getInt("id");
                                card.listId = cardJson.getInt("listId");
                                card.title = cardJson.getString("title");
                                if (cardJson.has("description")) {
                                    card.description = cardJson.getString("description");
                                }
                                card.createdAt = cardJson.getString("createdAt");
                                newCards.add(card);
                                db.cardDao().insertCard(card);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(() -> {
                            cardList.clear();
                            cardList.addAll(newCards);
                            cardAdapter.notifyDataSetChanged();
                        });
                    }).start();
                },
                error -> {
                    Toast.makeText(CardActivity.this, "Failed to fetch cards", Toast.LENGTH_SHORT).show();
                    loadCardsFromLocal();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = AuthManager.getInstance(CardActivity.this).getToken();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(jsonArrayRequest);
    }

    private void showAddCardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Card");
        final TextView titleInput = new TextView(this);
        titleInput.setText("");
        final TextView descInput = new TextView(this);
        descInput.setText("");
        builder.setView(titleInput);
        builder.setView(descInput);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String description = descInput.getText().toString().trim();
            if (!title.isEmpty()) {
                addCard(title, description);
            } else {
                Toast.makeText(CardActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addCard(String title, String description) {
        if (NetworkUtils.isNetworkAvailable(this)) {
            JSONObject cardData = new JSONObject();
            try {
                cardData.put("title", title);
                if (!description.isEmpty()) {
                    cardData.put("description", description);
                }
                cardData.put("listId", listId);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_URL, cardData,
                    response -> {
                        try {
                            CardEntity card = new CardEntity();
                            card.id = response.getInt("id");
                            card.listId = response.getInt("listId");
                            card.title = response.getString("title");
                            if (response.has("description")) {
                                card.description = response.getString("description");
                            }
                            card.createdAt = response.getString("createdAt");
                            new Thread(() -> db.cardDao().insertCard(card)).start();
                            cardList.add(card);
                            cardAdapter.notifyDataSetChanged();
                            Toast.makeText(CardActivity.this, "Card added", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(CardActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(CardActivity.this, "Failed to add card", Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String token = AuthManager.getInstance(CardActivity.this).getToken();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            requestQueue.add(jsonObjectRequest);
        } else {
            CardEntity card = new CardEntity();
            card.listId = listId;
            card.title = title;
            card.description = description;
            card.createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new java.util.Date());
            new Thread(() -> {
                long localId = db.cardDao().insertCard(card);
                card.id = (int) localId;
                SyncManager.getInstance(this).queueOperation("add_card", card.id, title, description, String.valueOf(listId));
                runOnUiThread(() -> {
                    cardList.add(card);
                    cardAdapter.notifyDataSetChanged();
                    Toast.makeText(CardActivity.this, "Card added offline", Toast.LENGTH_SHORT).show();
                });
            }).start();
        }
    }

    @Override
    public void onCardClick(CardEntity card) {
        Toast.makeText(this, "Card: " + card.title, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetworkStatus();
        loadCards();
    }
}
