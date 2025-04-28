package com.example.orderfoodgo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class SignInActivity extends AppCompatActivity {
    TextView tvSignUp;
    EditText etName, etPass;
    Button btnSignIn;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferencesUtil util = new SharedPreferencesUtil();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        init();

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = etName.getText().toString();
                String pass = etPass.getText().toString();
                loginUser(userName,pass);
            }
        });
    }

    private void init(){
        tvSignUp = findViewById(R.id.tvSignUp);
        etName = findViewById(R.id.etName);
        etPass = findViewById(R.id.etPass);
        btnSignIn = findViewById(R.id.btnSignIn);
    }

    public void loginUser(String username, String password){
        db.collection("users")
                .whereEqualTo("name", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                            // Lấy userId từ key của document
                            String userId = document.getId();  // Lấy userId từ document

                            // Kiểm tra mật khẩu
                            if (password.equals(document.getString("password"))) {
                                // Lưu userId vào SharedPreferences
                                SharedPreferencesUtil.saveUserIdToSharedPreferences(SignInActivity.this, userId);

                                // Đăng nhập thành công
                                Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finishAffinity();
                            } else {
                                // Mật khẩu không đúng
                                Toast.makeText(SignInActivity.this, "Mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Không tìm thấy người dùng
                            Toast.makeText(SignInActivity.this, "Người dùng chưa được đăng ký", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
