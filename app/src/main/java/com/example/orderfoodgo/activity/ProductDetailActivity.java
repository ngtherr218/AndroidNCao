package com.example.orderfoodgo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {
    ImageView ivProduct;
    ImageButton ibBack;
    TextView tvCategory, tvRating, tvProductDescribe,tvProductName,tvProductPrice;
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
        tvRating.setText(String.valueOf(intent.getDoubleExtra("rating",1)));
        tvProductDescribe.setText(intent.getStringExtra("describe"));
        tvProductName.setText(intent.getStringExtra("name"));
        tvProductPrice.setText(String.format("%,d VNĐ", intent.getIntExtra("price", 1)));
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void  init(){
        recyclerView = findViewById(R.id.recyclerView);
        ivProduct = findViewById(R.id.ivProduct);
        tvCategory = findViewById(R.id.tvCategory);
        tvRating = findViewById(R.id.tvRating);
        tvProductDescribe = findViewById(R.id.tvProductDescribe);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        ibBack = findViewById(R.id.ibBack);
    }
}
