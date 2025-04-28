package com.example.orderfoodgo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.fragment.HomeFragment;
import com.example.orderfoodgo.fragment.ProfileFragment;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    TextView tvName, tvPass, tvEmail;
    Button btnSave;
    ImageView btnBack;
    FirebaseFirestore  db = FirebaseFirestore.getInstance();
    SharedPreferencesUtil util = new SharedPreferencesUtil();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();

        String userId = util.getUserIdFromSharedPreferences(this);
        if (userId != null && !userId.isEmpty()) {
            db.collection("users")
                    .document(userId)  // Lấy document của người dùng với userId
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Lấy thông tin từ Firestore
                                String name = document.getString("name");
                                String email = document.getString("email");
                                String password = document.getString("password");

                                // Hiển thị thông tin lên các EditText
                                tvName.setText(name);
                                tvEmail.setText(email);
                                tvPass.setText(password);

                            } else {
                                Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Lỗi khi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Không có ID người dùng", Toast.LENGTH_SHORT).show();
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = tvName.getText().toString();
                String email = tvEmail.getText().toString();
                String password = tvPass.getText().toString();

                if (userId != null && !userId.isEmpty()) {
                    // First, check if the name already exists in Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users")
                            .whereEqualTo("name", name)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        // Name already exists in Firestore, prevent saving
                                        Toast.makeText(ProfileActivity.this, "Tên này đã tồn tại!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Name does not exist, proceed with updating
                                        Map<String, Object> updatedData = new HashMap<>();
                                        updatedData.put("name", name);
                                        updatedData.put("email", email);
                                        updatedData.put("password", password); // Hash password if necessary

                                        // Update Firestore document with new data
                                        db.collection("users")
                                                .document(userId)
                                                .update(updatedData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(ProfileActivity.this, "Không cập nhật được", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                } else {
                                    // Handle any error that occurs during the query
                                    Toast.makeText(ProfileActivity.this, "Lỗi khi kiểm tra tên", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void  init(){
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPass = findViewById(R.id.tvPass);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
    }
}
