package com.example.orderfoodgo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // nhớ thêm Glide để load ảnh
import com.example.orderfoodgo.R;
import com.example.orderfoodgo.model.OrderDetail;
import com.example.orderfoodgo.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {

    private Context context;
    private List<OrderDetail> orderDetailList;
    private FirebaseFirestore db;

    public OrderDetailAdapter(Context context, List<OrderDetail> orderDetailList) {
        this.context = context;
        this.orderDetailList = orderDetailList;
        db = FirebaseFirestore.getInstance(); // khởi tạo Firestore
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        OrderDetail orderDetail = orderDetailList.get(position);

        // Lấy Product từ Firestore theo idProduct
        db.collection("products").document(orderDetail.getIdProduct())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Product product = documentSnapshot.toObject(Product.class);

                        if (product != null) {
                            holder.tvName.setText(product.getName());
                            holder.tvCategory.setText(product.getCategory());
                            holder.tvPrice.setText(String.format("%,d VNĐ", product.getPrice()));
                            holder.itemQuantity.setText(String.valueOf(orderDetail.getQuantityProduct()));

                            // Load ảnh sản phẩm bằng Glide
                            Glide.with(context)
                                    .load(product.getImage())
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .into(holder.itemImage);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Nếu truy vấn thất bại
                    holder.tvName.setText("Không tìm thấy sản phẩm");
                });

        holder.itemQuantity.setText(String.valueOf(orderDetail.getQuantityProduct()));
    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    public static class OrderDetailViewHolder extends RecyclerView.ViewHolder {

        ImageView itemImage;
        TextView tvName, tvCategory, tvPrice, itemQuantity;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
        }
    }
}
