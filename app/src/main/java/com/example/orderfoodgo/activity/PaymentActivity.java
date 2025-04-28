package com.example.orderfoodgo.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.adapter.DeliveryInfoAdapter;
import com.example.orderfoodgo.model.CartItem;
import com.example.orderfoodgo.model.DeliveryInfo;
import com.example.orderfoodgo.model.Order;
import com.example.orderfoodgo.model.OrderDetail;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PaymentActivity extends AppCompatActivity {
    ConstraintLayout constraintLayout;
    private FirebaseFirestore db;
    TextView totalValue, totalValue2, totalValue3, deliveryValue;
    private Dialog loadingDialog;
    TextView tvNameReceiver;
    Button goBack;
    TextView tvPhoneNumber;
    TextView tvAddress;
    TextView btnOrder;

    interface Callback {
        void callback(List<CartItem> list);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();
        calculateTotalAmount();
        init();
        // Lấy intent
        Intent intent = getIntent();
        if (intent != null) {
            String receiverName = intent.getStringExtra("receiverName");
            String phoneNumber = intent.getStringExtra("phone");
            String address = intent.getStringExtra("address");

            if (receiverName != null) {
                tvNameReceiver.setText(receiverName);
                tvNameReceiver.setVisibility(View.VISIBLE);
            }
            if (phoneNumber != null) {
                tvPhoneNumber.setText(phoneNumber);
                tvPhoneNumber.setVisibility(View.VISIBLE);
            }
            if (address != null) {
                tvAddress.setText(address);
                tvAddress.setVisibility(View.VISIBLE);
            }
        }

        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaymentActivity.this, DeliveryInfoActivity.class);
                intent.putExtra("key", 1);
                startActivity(intent);
                finish();
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Khởi tạo Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Lấy userId từ SharedPreferences
                SharedPreferencesUtil util = new SharedPreferencesUtil();
                String userId = util.getUserIdFromSharedPreferences(PaymentActivity.this);

                // Lấy thông tin từ bộ sưu tập "users"
                DocumentReference userRef = db.collection("users").document(userId);

                // Lấy phí giao hàng và tổng tiền từ TextView và chuyển thành số
                String numberString = deliveryValue.getText().toString().replace(".", "").replace(" VNĐ", "");
                int deliveryFee = Integer.parseInt(numberString);

                String totalString = totalValue2.getText().toString().replace(".", "").replace(" VNĐ", "");
                int total = Integer.parseInt(totalString);

                // Lấy deliveryId từ intent
                String deliveryId = getIntent().getStringExtra("deliveryId");
                if (deliveryId == null || deliveryId.isEmpty()) {
                    Toast.makeText(PaymentActivity.this, "Bạn chưa chọn thông tin giao hàng", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tạo đối tượng Order
                String orderId = userRef.collection("orders").document().getId();
                Order newOrder = new Order(orderId, userId, deliveryId, new Timestamp(new Date()), deliveryFee, total, "Đã đặt hàng");

                // Lưu đơn hàng vào Firestore
                userRef.collection("orders")
                        .document(orderId)
                        .set(newOrder)
                        .addOnSuccessListener(aVoid -> {

                            // Lưu chi tiết đơn hàng vào subcollection "orderDetails"
                            getCartItems(userId, new Callback() {
                                @Override
                                public void callback(List<CartItem> list) {
                                    Log.d("eh", list.size() + "  ");// Bạn cần viết phương thức để lấy cartItems
                                    for (CartItem cartItem : list) {
                                        String orderDetailId = userRef.collection("orders").document(orderId).collection("orderDetails").document().getId();
                                        OrderDetail orderDetail = new OrderDetail(orderDetailId, cartItem.getIdProduct(), orderId, cartItem.getQuantity());
                                        userRef.collection("orders")
                                                .document(orderId)
                                                .collection("orderDetails")
                                                .document(orderDetailId)
                                                .set(orderDetail)
                                                .addOnSuccessListener(aVoid2 -> {
                                                    // Đã lưu chi tiết đơn hàng thành công
                                                    Log.d("OrderDetail", "OrderDetail saved: " + orderDetailId);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("OrderDetailError", "Error saving OrderDetail", e);
                                                    Toast.makeText(PaymentActivity.this, "Không thể lưu chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                }
                            });

                            Toast.makeText(PaymentActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                            Dialog dialog = new Dialog(PaymentActivity.this);
                            dialog.setContentView(R.layout.dialog_sucess);
                            dialog.setCancelable(false);
                            // Áp dụng nền với bo góc
                            dialog.getWindow().setBackgroundDrawableResource(R.drawable.border_dialog);
                            // Tùy chỉnh kích thước dialog
                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            dialog.show();
                            goBack = dialog.findViewById(R.id.goBack);
                            goBack.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.cancel();
                                    // Xóa tất cả thẻ của người dùng từ Firestore
                                    db.collection("users").document(userId)
                                            .collection("carts") // Giả sử bạn lưu thẻ trong bộ sưu tập con "cards"
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                                for (DocumentSnapshot document : queryDocumentSnapshots) {
                                                    // Xóa từng thẻ
                                                    document.getReference().delete();
                                                }
                                                // Chuyển đến HomeActivity sau khi xóa thẻ
                                                Intent intent1 = new Intent(PaymentActivity.this, HomeActivity.class);
                                                startActivity(intent1);
                                                finishAffinity();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(PaymentActivity.this, "Không thể xóa giỏ", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            });


                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PaymentActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show();
                            Log.e("OrderError", "Error placing order", e);
                        });
            }
        });
    }

    private void getCartItems(String userId, Callback callback) {
        List<CartItem> cartItems = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .collection("carts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Tạo CartItem từ dữ liệu trong Firestore
                            String idProduct = document.getString("idProduct");
                            int quantity = document.getLong("quantity").intValue();
                            CartItem cartItem = new CartItem(document.getId(), userId, idProduct, quantity);
                            cartItems.add(cartItem);
                        }

                        callback.callback(cartItems);
                    }
                });
    }


    private void init() {
        constraintLayout = findViewById(R.id.constraintLayout);
        totalValue = findViewById(R.id.totalValue);
        tvNameReceiver = findViewById(R.id.tvNameReceiver);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvAddress = findViewById(R.id.tvAddress);
        totalValue2 = findViewById(R.id.totalValue2);
        totalValue3 = findViewById(R.id.totalValue3);
        deliveryValue = findViewById(R.id.deliveryValue);
        btnOrder = findViewById(R.id.btnOrder);
    }

    private void calculateTotalAmount() {
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        String userId = util.getUserIdFromSharedPreferences(this);

        // Lấy giỏ hàng của người dùng từ bảng 'users'
        if (userId != null && !userId.isEmpty()) {
            db.collection("users").document(userId)
                    .collection("carts")
                    .get() // Truy vấn subcollection "carts"
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Duyệt qua tất cả các tài liệu trong subcollection "carts"
                                AtomicReference<Integer> totalAmount = new AtomicReference<>(0);
                                for (DocumentSnapshot document : querySnapshot) {
                                    Log.d("Cart Item", document.getData().toString());
                                    // Giả sử bạn có CartItem trong tài liệu
                                    CartItem item = document.toObject(CartItem.class);

                                    if (item != null) {
                                        // Lấy giá của sản phẩm từ bảng 'products'
                                        db.collection("products").document(item.getIdProduct())
                                                .get()
                                                .addOnCompleteListener(productTask -> {
                                                    if (productTask.isSuccessful()) {
                                                        DocumentSnapshot productDoc = productTask.getResult();
                                                        if (productDoc.exists()) {
                                                            double price = productDoc.getDouble("price");
                                                            // Cập nhật tổng tiền sử dụng updateAndGet
                                                            int value = (int) price;
                                                            totalAmount.updateAndGet(v -> v + (value * item.getQuantity()));
                                                        }
                                                    }
                                                    // Sau khi tính tổng tiền cho sản phẩm, cập nhật giao diện
                                                    totalValue.setText(String.format("%,d VNĐ", totalAmount.get()));
                                                    String numberString = deliveryValue.getText().toString().replace(".", "").replace(" VNĐ", "");
                                                    int number = Integer.parseInt(numberString);
                                                    totalValue2.setText(String.format("%,d VNĐ", totalAmount.get() + number));
                                                    totalValue3.setText(String.format("%,d", totalAmount.get() + number));
                                                });
                                    }
                                }
                            } else {
                                // Nếu giỏ hàng trống, hiển thị thông báo hoặc xử lý theo cách khác
                                Log.d("CartActivity", "Giỏ hàng trống");
                                totalValue.setText(String.valueOf("0"));
                            }
                        } else {
                            // Xử lý lỗi nếu không lấy được giỏ hàng
                            Log.e("CartActivity", "Error getting user cart", task.getException());
                        }
                    });
        } else {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
        }
    }
}
