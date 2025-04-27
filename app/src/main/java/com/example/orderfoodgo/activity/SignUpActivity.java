package com.example.orderfoodgo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class SignUpActivity extends AppCompatActivity {
    TextView tvSignUp;
    EditText etName, etPass, etEmail;
    Button btnSignUp;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etName.getText().toString();
                String password = etPass.getText().toString();
                String email = etEmail.getText().toString();
                registerUser(username, password, email);
            }
        });
    }

    private void init() {
        tvSignUp = findViewById(R.id.tvSignIn);
        etName = findViewById(R.id.etName);
        etPass = findViewById(R.id.etPass);
        etEmail = findViewById(R.id.etEmail);
        btnSignUp = findViewById(R.id.btnSignUp);
    }

    public void registerUser(String username, String password, String email) {
        // Kiểm tra xem username đã tồn tại chưa
        db.collection("users")
                .whereEqualTo("name", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            // Nếu không tìm thấy người dùng có username này
                            if (querySnapshot.isEmpty()) {
                                // Tạo đối tượng User mới với ID tự sinh ra
                                User user = new User(null, username, password, email);  // Firestore sẽ tự động tạo ID

                                // Thêm người dùng vào Firestore
                                db.collection("users")
                                        .add(user)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                // Gán userId cho người dùng sau khi tài liệu được thêm thành công
                                                user.setUserId(documentReference.getId());

                                                // Hiển thị thông báo và chuyển sang trang đăng nhập
                                                Toast.makeText(SignUpActivity.this, "Đăng ký thành công! Mời bạn đăng nhập.", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                                startActivity(intent);
                                                finishAffinity();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Register", "Đăng ký thất bại: " + e.getMessage());
                                                Toast.makeText(SignUpActivity.this, "Đăng ký không thành công. Hãy thử lại!", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                            } else {
                                Toast.makeText(SignUpActivity.this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("Register", "Lỗi khi kiểm tra user: " + task.getException());
                        }
                    }
                });
    }

}