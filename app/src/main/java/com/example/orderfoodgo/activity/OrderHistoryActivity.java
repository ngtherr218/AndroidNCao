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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private ListenerRegistration orderListener;
    private String userId; // thêm userId cố định để dùng nhiều lần

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(OrderHistoryActivity.this, orderList);
        recyclerView.setAdapter(orderAdapter);

        // Lấy userId
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        userId = util.getUserIdFromSharedPreferences(OrderHistoryActivity.this);

        // Lấy danh sách đơn hàng ban đầu
        getOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenToOrderChanges(); // Lắng nghe thay đổi dữ liệu
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (orderListener != null) {
            orderListener.remove(); // Bỏ lắng nghe khi pause
        }
    }

    private void getOrders() {
        db.collection("users").document(userId)
                .collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear(); // clear danh sách cũ
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Order order = document.toObject(Order.class);
                            orderList.add(order);
                        }
                        orderAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(OrderHistoryActivity.this, "Không có đơn hàng nào", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi lấy danh sách đơn hàng", e);
                    Toast.makeText(OrderHistoryActivity.this, "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private void listenToOrderChanges() {
        if (orderListener != null) {
            orderListener.remove();
        }

        orderListener = db.collection("users")
                .document(userId)
                .collection("orders")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("FirestoreListen", "Listen failed.", error);
                            return;
                        }

                        if (value != null) {
                            orderList.clear(); // clear lại danh sách
                            for (QueryDocumentSnapshot document : value) {
                                Order order = document.toObject(Order.class);
                                orderList.add(order);
                            }
                            orderAdapter.notifyDataSetChanged(); // thông báo cập nhật giao diện
                        }
                    }
                });
    }
}
