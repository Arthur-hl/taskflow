package app.csb.yrkqw2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView registerLink;
    private TextView offlineIndicator;
    private RequestQueue requestQueue;
    private static final String API_URL = "https://yrkqw2-5000.csb.app/api/auth/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);
        offlineIndicator = findViewById(R.id.offlineIndicator);

        requestQueue = Volley.newRequestQueue(this);

        checkNetworkStatus();
        checkLoginStatus();

        loginButton.setOnClickListener(v -> loginUser());
        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }

    private void checkNetworkStatus() {
        boolean isOnline = NetworkUtils.isNetworkAvailable(this);
        offlineIndicator.setVisibility(isOnline ? TextView.GONE : TextView.VISIBLE);
    }

    private void checkLoginStatus() {
        if (AuthManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
        }
    }

    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Login requires internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject loginData = new JSONObject();
        try {
            loginData.put("username", username);
            loginData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_URL, loginData,
                response -> {
                    try {
                        String token = null;
                        // Check for token under different possible keys
                        if (response.has("token")) {
                            token = response.getString("token");
                        } else if (response.has("access_token")) {
                            token = response.getString("access_token");
                        } else if (response.has("jwt")) {
                            token = response.getString("jwt");
                        }

                        if (token != null && !token.isEmpty()) {
                            AuthManager.getInstance(MainActivity.this).saveToken(token);
                            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            String responseStr = response.toString();
                            Toast.makeText(MainActivity.this, "Login failed: No token found. Response: " + responseStr, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String message = "Login failed";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            JSONObject errorJson = new JSONObject(new String(error.networkResponse.data));
                            message = errorJson.optString("message", "Login failed");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                });

        requestQueue.add(jsonObjectRequest);
    }
}
