package app.csb.yrkqw2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginLink;
    private TextView offlineIndicator;
    private RequestQueue requestQueue;
    private static final String API_URL = "https://yrkqw2-5000.csb.app/api/auth/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.regUsernameEditText);
        emailEditText = findViewById(R.id.regEmailEditText);
        passwordEditText = findViewById(R.id.regPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.regConfirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        offlineIndicator = findViewById(R.id.offlineIndicator);

        requestQueue = Volley.newRequestQueue(this);

        checkNetworkStatus();

        registerButton.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }

    private void checkNetworkStatus() {
        boolean isOnline = NetworkUtils.isNetworkAvailable(this);
        offlineIndicator.setVisibility(isOnline ? TextView.GONE : TextView.VISIBLE);
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Registration requires internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject userData = new JSONObject();
        try {
            userData.put("username", username);
            userData.put("email", email);
            userData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_URL, userData,
                response -> {
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                },
                error -> {
                    String message = "Registration failed";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            JSONObject errorJson = new JSONObject(new String(error.networkResponse.data));
                            message = errorJson.optString("message", "Registration failed");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                });

        requestQueue.add(jsonObjectRequest);
    }
}
