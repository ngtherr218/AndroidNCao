package com.example.orderfoodgo.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.activity.OrderDetailActivity;
import com.example.orderfoodgo.model.Order;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orderList;
    String nameReceiver;
    String formattedDate;
    String deliveryId;


    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderIdTextView.setText(order.getId());
        holder.statusTextView.setText(order.getStatus());
        // Lấy đối tượng Timestamp từ Firestore
        Timestamp timestamp = order.getCreateAt();

        // Kiểm tra nếu Timestamp không null
        if (timestamp != null) {
            // Chuyển đổi Timestamp thành Date
            Date date = timestamp.toDate();

            // Định dạng ngày tháng
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            formattedDate = dateFormat.format(date);

            // Hiển thị thời gian đã định dạng
            holder.timeTextView.setText(formattedDate);
        } else {
            holder.timeTextView.setText("Không có thời gian");
        }
        holder.totalMoneyTextView.setText(String.format("%,2d VNĐ", order.getTotalMoney()));

        String orderId = order.getId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        String userId = util.getUserIdFromSharedPreferences(holder.itemView.getContext());
        DocumentReference orderRef = db.collection("users").document(userId)
                .collection("orders").document(orderId);

        // Truy vấn đơn hàng để lấy deliveryId
        orderRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Lấy deliveryId từ đơn hàng
                deliveryId = documentSnapshot.getString("deliveryId");

                // Sau khi có deliveryId, bạn có thể lấy thông tin giao hàng từ bảng deliveryInfos
                if (deliveryId != null) {
                    DocumentReference deliveryInfoRef = db.collection("deliveryInfos").document(deliveryId);
                    deliveryInfoRef.get().addOnSuccessListener(deliverySnapshot -> {
                        if (deliverySnapshot.exists()) {
                            // Lấy tên người nhận từ thông tin giao hàng
                            String receiverName = deliverySnapshot.getString("receiverName");
                            // Cập nhật thông tin người nhận vào TextView
                            holder.tvNameReceiver.setText(receiverName);
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("Error", "Không thể lấy thông tin giao hàng", e);
                    });
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Error", "Không thể lấy đơn hàng", e);
        });

        // Tạo tham chiếu đến tài liệu Firestore dựa trên deliveryId
        DocumentReference deliveryRef = db.collection("users").document(userId).collection("deliveryinfos").document(order.getIdDelivery());

        // Truy vấn tài liệu và lấy thông tin
        deliveryRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Lấy nameReceiver từ tài liệu
                    nameReceiver = document.getString("name");
                    // Cập nhật TextView với nameReceiver
                    holder.tvNameReceiver.setText(nameReceiver);
                } else {
                    // Xử lý nếu tài liệu không tồn tại
                    holder.tvNameReceiver.setText("Thông tin người nhận không có");
                }
            } else {
                // Xử lý lỗi nếu truy vấn không thành công
                holder.tvNameReceiver.setText("Lỗi khi lấy thông tin");
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), OrderDetailActivity.class);
                intent.putExtra("idOrder", order.getId());
                intent.putExtra("deliveryId",order.getIdDelivery());
                intent.putExtra("nameReceiver", nameReceiver);
                intent.putExtra("formattedDate", formattedDate);
                intent.putExtra("deliveryFee", order.getDeliveryFee());
                intent.putExtra("totalMoney", order.getTotalMoney());
                intent.putExtra("status", order.getStatus());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, statusTextView, totalMoneyTextView, timeTextView, tvNameReceiver;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameReceiver = itemView.findViewById(R.id.tvNameReceiver);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            totalMoneyTextView = itemView.findViewById(R.id.totalMoneyTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }
}

