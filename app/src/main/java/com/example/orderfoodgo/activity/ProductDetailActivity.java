package com.example.orderfoodgo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.orderfoodgo.R;
import com.example.orderfoodgo.model.CartItem;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {
    ImageView ivProduct;
    ImageButton ibBack, btnIncrease, btnDecrease;
    TextView tvCategory, tvRating, tvProductDescribe, tvProductName, tvProductPrice, tvQuantity;
    Button btnAdd;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        init();

        Intent intent = getIntent();
        Glide.with(ProductDetailActivity.this)
                .load(intent.getStringExtra("image"))
                .into(ivProduct);
        tvCategory.setText(intent.getStringExtra("category"));
        tvRating.setText(String.valueOf(intent.getDoubleExtra("rating", 1)));
        tvProductDescribe.setText(intent.getStringExtra("describe"));
        tvProductName.setText(intent.getStringExtra("name"));
        tvProductPrice.setText(String.format("%,d VNĐ", intent.getIntExtra("price", 1)));
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity = Integer.parseInt(tvQuantity.getText().toString());
                if (currentQuantity > 1) {
                    currentQuantity--;
                    tvQuantity.setText(String.valueOf(currentQuantity));
                }
            }
        });

        btnDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity = Integer.parseInt(tvQuantity.getText().toString());
                currentQuantity++;
                tvQuantity.setText(String.valueOf(currentQuantity));
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lấy dữ liệu từ Intent
                Intent intent = getIntent();
                String idProduct = intent.getStringExtra("id"); // ID sản phẩm
                int quantityToAdd = Integer.parseInt(tvQuantity.getText().toString()); // Số lượng đặt thêm

                // Lấy userId từ SharedPreferences
                SharedPreferencesUtil util = new SharedPreferencesUtil();
                String userId = util.getUserIdFromSharedPreferences(ProductDetailActivity.this);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Truy vấn xem sản phẩm đã có trong giỏ hàng chưa
                db.collection("users")
                        .document(userId)
                        .collection("carts")
                        .whereEqualTo("idProduct", idProduct)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                if (!task.getResult().isEmpty()) {
                                    // Nếu sản phẩm đã có trong giỏ: cập nhật số lượng
                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0); // Lấy sản phẩm đầu tiên
                                    String cartItemId = documentSnapshot.getId();
                                    long currentQuantity = documentSnapshot.getLong("quantity"); // Số lượng hiện có

                                    // Cập nhật quantity: quantity hiện tại + quantity thêm mới
                                    db.collection("users")
                                            .document(userId)
                                            .collection("carts")
                                            .document(cartItemId)
                                            .update("quantity", currentQuantity + quantityToAdd)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(ProductDetailActivity.this, "Đã cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(ProductDetailActivity.this, "Cập nhật giỏ thất bại", Toast.LENGTH_SHORT).show();
                                            });

                                } else {
                                    // Nếu chưa có sản phẩm đó trong giỏ: thêm mới
                                    String cartItemId = db.collection("users").document(userId).collection("carts").document().getId(); // Tạo ID tự động

                                    CartItem cartItem = new CartItem(cartItemId, userId, idProduct, quantityToAdd);

                                    db.collection("users")
                                            .document(userId)
                                            .collection("carts")
                                            .document(cartItemId)
                                            .set(cartItem)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(ProductDetailActivity.this, "Thêm vào giỏ thất bại", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                Toast.makeText(ProductDetailActivity.this, "Lỗi khi kiểm tra giỏ hàng", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void init() {
        recyclerView = findViewById(R.id.recyclerView);
        ivProduct = findViewById(R.id.ivProduct);
        tvCategory = findViewById(R.id.tvCategory);
        tvRating = findViewById(R.id.tvRating);
        tvProductDescribe = findViewById(R.id.tvProductDescribe);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        ibBack = findViewById(R.id.ibBack);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnAdd = findViewById(R.id.btnAdd);
    }
}
