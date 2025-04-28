package com.example.orderfoodgo.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.orderfoodgo.R;
import com.example.orderfoodgo.activity.PaymentActivity;
import com.example.orderfoodgo.model.DeliveryInfo;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class DeliveryInfoAdapter extends RecyclerView.Adapter<DeliveryInfoAdapter.DeliveryInfoViewHolder> {
    private List<DeliveryInfo> deliveryInfoList;
    private List<String> deliveryIds;
    private int key;

    // Constructor
    public DeliveryInfoAdapter(List<DeliveryInfo> deliveryInfoList, int key, List<String> deliveryIds) {
        this.deliveryInfoList = deliveryInfoList;
        this.deliveryIds = deliveryIds;
        this.key = key;
    }

    // ViewHolder to bind views
    public static class DeliveryInfoViewHolder extends RecyclerView.ViewHolder {
        TextView nameReceiverTextView, addressTextView, phoneNumberTextView;
        ImageView deleteButton;

        public DeliveryInfoViewHolder(View itemView) {
            super(itemView);
            nameReceiverTextView = itemView.findViewById(R.id.tvNameReceiver);
            addressTextView = itemView.findViewById(R.id.tvAddress);
            phoneNumberTextView = itemView.findViewById(R.id.tvPhoneNumber);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }

    @Override
    public DeliveryInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivery_info, parent, false);
        return new DeliveryInfoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DeliveryInfoViewHolder holder, int position) {
        DeliveryInfo info = deliveryInfoList.get(position);
        // Bind the delivery info data to the views
        DeliveryInfo deliveryInfo = deliveryInfoList.get(position);
        holder.nameReceiverTextView.setText(deliveryInfo.getName());
        holder.addressTextView.setText(deliveryInfo.getAddress());
        holder.phoneNumberTextView.setText(deliveryInfo.getPhone());
        holder.deleteButton.setOnClickListener(v -> {
            deleteDeliveryInfo(holder, info, position);
        });

        if (key == 1) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getAdapterPosition();  // Lấy vị trí chính xác tại thời điểm click
                    if (pos != RecyclerView.NO_POSITION) {  // Kiểm tra hợp lệ
                        DeliveryInfo deliveryInfo = deliveryInfoList.get(pos);
                        String id = deliveryIds.get(pos);  // Lấy id đúng vị trí mới

                        Intent intent = new Intent(holder.itemView.getContext(), PaymentActivity.class);
                        intent.putExtra("receiverName", deliveryInfo.getName());
                        intent.putExtra("address", deliveryInfo.getAddress());
                        intent.putExtra("phone", deliveryInfo.getPhone());
                        intent.putExtra("deliveryId", id);  // truyền id
                        holder.itemView.getContext().startActivity(intent);
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return deliveryInfoList.size();
    }

    // Hàm xóa delivery info trong Firestore và local list
    private void deleteDeliveryInfo(DeliveryInfoViewHolder holder, DeliveryInfo info, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        String userId = util.getUserIdFromSharedPreferences(holder.itemView.getContext());
        db.collection("users")
                .document(userId)
                .collection("deliveryinfos")
                .whereEqualTo("name", info.getName())
                .whereEqualTo("address", info.getAddress())
                .whereEqualTo("phone", info.getPhone())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            document.getReference().delete()
                                    .addOnSuccessListener(unused -> {
                                        // Xóa thành công -> remove khỏi list
                                        deliveryInfoList.remove(position);
                                        notifyItemRemoved(position);
                                        Toast.makeText(holder.itemView.getContext(), "Xóa địa chỉ thành công", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(holder.itemView.getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Lỗi khi truy vấn xóa", Toast.LENGTH_SHORT).show();
                });
    }

}
