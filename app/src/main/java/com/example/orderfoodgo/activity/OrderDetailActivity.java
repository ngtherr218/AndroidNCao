package com.example.orderfoodgo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.adapter.OrderDetailAdapter;
import com.example.orderfoodgo.model.OrderDetail;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {
    ImageView goback;
    private RecyclerView recyclerView;
    private OrderDetailAdapter adapter;
    private List<OrderDetail> orderDetailList;
    private FirebaseFirestore db;
    private String idOrder;
    TextView tvHuy;
    String userId;
    TextView tvStatus, tvTotalMoney, tvDeliveryFee, tvDate, tvName, tvAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        init();

        goback.setOnClickListener(v -> finish());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderDetailList = new ArrayList<>();
        adapter = new OrderDetailAdapter(this, orderDetailList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        idOrder = intent.getStringExtra("idOrder");
        String nameReceiver = intent.getStringExtra("nameReceiver");
        String formattedDate = intent.getStringExtra("formattedDate");
        int deliveryFee = intent.getIntExtra("deliveryFee", 0);
        int totalMoney = intent.getIntExtra("totalMoney", 0);
        String status = intent.getStringExtra("status");
        String deliveryId = intent.getStringExtra("deliveryId");

        // Kiểm tra xem idOrder có phải là null hay không
        if (idOrder == null) {
            Toast.makeText(this, "ID đơn hàng không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Đặt giá trị cho các TextView
        tvName.setText(nameReceiver);
        tvDate.setText(formattedDate);
        tvDeliveryFee.setText(String.format("%,d VNĐ", deliveryFee)); // Sửa lại format nếu cần
        tvTotalMoney.setText(String.format("%,d VNĐ", totalMoney));
        tvStatus.setText(status);

        if (deliveryId != null) {
            // Lấy thông tin người nhận từ Firestore
            SharedPreferencesUtil util = new SharedPreferencesUtil();
            userId = util.getUserIdFromSharedPreferences(this);
            if (userId != null) {
                DocumentReference deliveryRef = db.collection("users")
                        .document(userId)
                        .collection("deliveryinfos")
                        .document(deliveryId);

                // Truy vấn tài liệu và lấy thông tin
                deliveryRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String address = document.getString("address");
                            tvAddress.setText(address != null ? address : "Không có địa chỉ");
                        } else {
                            tvAddress.setText("Thông tin người nhận không có");
                        }
                    } else {
                        tvAddress.setText("Lỗi khi lấy thông tin");
                    }
                });
            }
        }

        loadOrderDetails(idOrder);
        updateTimeline(status);


        if (status.equals("Chờ xác nhận")) {
            tvHuy.setVisibility(View.VISIBLE);
        }

        tvHuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new androidx.appcompat.app.AlertDialog.Builder(OrderDetailActivity.this)
                        .setTitle("Xác nhận huỷ đơn")
                        .setMessage("Bạn có chắc chắn muốn huỷ đơn hàng này không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            cancelOrder(userId, idOrder);
                            finish();
                        })
                        .setNegativeButton("Không", (dialog, which) -> {
                            dialog.dismiss(); // Đóng dialog nếu chọn Không
                        })
                        .show();
            }
        });


    }

    private void init() {
        goback = findViewById(R.id.goback);
        recyclerView = findViewById(R.id.recyclerView);
        tvName = findViewById(R.id.tvName);
        tvDate = findViewById(R.id.tvDate);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTotalMoney = findViewById(R.id.tvTotalMoney);
        tvStatus = findViewById(R.id.tvStatus);
        tvAddress = findViewById(R.id.tvAddress);
        tvHuy = findViewById(R.id.tvHuy);
    }

    // Hàm hủy đơn hàng
    private void cancelOrder(String userId, String orderId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference orderRef = db.collection("users").document(userId).collection("orders").document(orderId);

        // Xóa toàn bộ orderDetails trước
        orderRef.collection("orderDetails")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete(); // Xóa từng chi tiết đơn hàng
                    }

                    // Sau khi xóa hết orderDetails, xóa luôn đơn hàng
                    orderRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(OrderDetailActivity.this, "Đơn hàng đã được hủy thành công!", Toast.LENGTH_SHORT).show();
                                Log.d("CancelOrder", "Order canceled successfully");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(OrderDetailActivity.this, "Không thể xóa đơn hàng", Toast.LENGTH_SHORT).show();
                                Log.e("CancelOrderError", "Error deleting order", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OrderDetailActivity.this, "Không thể xóa chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                    Log.e("OrderDetailDeleteError", "Error deleting order details", e);
                });
    }


    private void loadOrderDetails(String idOrder) {
        if (idOrder == null) {
            Toast.makeText(this, "Không có ID đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferencesUtil util = new SharedPreferencesUtil();
        String userId = util.getUserIdFromSharedPreferences(this);

        if (userId != null) {
            db.collection("users").document(userId)
                    .collection("orders").document(idOrder)
                    .collection("orderDetails") // Collection của bạn
                    .whereEqualTo("idOrder", idOrder) // Lọc theo idOrder
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        orderDetailList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            OrderDetail orderDetail = document.toObject(OrderDetail.class);
                            orderDetailList.add(orderDetail);
                        }
                        adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                        Log.e("OrderDetailActivity", "Error: " + e.getMessage());
                    });
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateTimeline(String status) {
        ImageView statusPending = findViewById(R.id.statusPending);
        ImageView statusConfirmed = findViewById(R.id.statusConfirmed);
        ImageView statusInProgress = findViewById(R.id.statusInProgress);
        ImageView statusDelivered = findViewById(R.id.statusDelivered);

        TextView statusTextPending = findViewById(R.id.statusTextPending);
        TextView statusTextConfirmed = findViewById(R.id.statusTextConfirmed);
        TextView statusTextInProgress = findViewById(R.id.statusTextInProgress);
        TextView statusTextDelivered = findViewById(R.id.statusTextDelivered);

        // Reset all statuses to default
        statusPending.setImageResource(R.drawable.ic_pending); // Chờ xác nhận
        statusConfirmed.setImageResource(R.drawable.ic_confirmed); // Đã xác nhận
        statusInProgress.setImageResource(R.drawable.ic_in_progress); // Đang giao
        statusDelivered.setImageResource(R.drawable.ic_delivered); // Đã giao

        // Set default text
        statusTextPending.setText("Chờ xác nhận");
        statusTextConfirmed.setText("Đã xác nhận");
        statusTextInProgress.setText("Đang giao");
        statusTextDelivered.setText("Đã giao");

        // Update based on status
        switch (status) {
            case "Chờ xác nhận":
                statusPending.setImageResource(R.drawable.ic_pending_active);
                break;
            case "Đã xác nhận":
                statusPending.setImageResource(R.drawable.ic_pending_active);
                statusConfirmed.setImageResource(R.drawable.ic_confirmed_active);
                break;
            case "Đang giao":
                statusPending.setImageResource(R.drawable.ic_pending_active);
                statusConfirmed.setImageResource(R.drawable.ic_confirmed_active);
                statusInProgress.setImageResource(R.drawable.ic_in_progress_active);
                break;
            case "Đã giao":
                statusPending.setImageResource(R.drawable.ic_pending_active);
                statusConfirmed.setImageResource(R.drawable.ic_confirmed_active);
                statusInProgress.setImageResource(R.drawable.ic_in_progress_active);
                statusDelivered.setImageResource(R.drawable.ic_delivered_active);
                break;
        }
    }
}
