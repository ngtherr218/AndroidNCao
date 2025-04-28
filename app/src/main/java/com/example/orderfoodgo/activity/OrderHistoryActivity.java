package com.example.orderfoodgo.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.adapter.OrderAdapter;
import com.example.orderfoodgo.model.Order;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        recyclerView = findViewById(R.id.recyclerView);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();

        // Lấy danh sách đơn hàng từ Firestore
        getOrders();
    }
    private void getOrders() {
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        String userId = util.getUserIdFromSharedPreferences(OrderHistoryActivity.this);
        // Truy vấn danh sách đơn hàng của người dùng
        db.collection("users").document(userId)
                .collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Lấy thông tin đơn hàng và thêm vào danh sách
                            Order order = document.toObject(Order.class);
                            orderList.add(order);
                        }

                        // Gán adapter vào RecyclerView
                        orderAdapter = new OrderAdapter(OrderHistoryActivity.this, orderList);
                        recyclerView.setAdapter(orderAdapter);
                    } else {
                        Toast.makeText(OrderHistoryActivity.this, "Không có đơn hàng nào", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi lấy danh sách đơn hàng", e);
                    Toast.makeText(OrderHistoryActivity.this, "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show();
                });
    }
}
