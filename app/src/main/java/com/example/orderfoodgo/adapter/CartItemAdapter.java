package com.example.orderfoodgo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.orderfoodgo.R;
import com.example.orderfoodgo.model.CartItem;
import com.example.orderfoodgo.model.Product;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private List<Product> products;
    private FirebaseFirestore db;

    public CartItemAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
        this.products = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CartItemViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        // Lấy thông tin sản phẩm từ Firestore
        String productId = cartItem.getIdProduct();
        db.collection("products").document(productId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Chuyển đổi dữ liệu từ Firestore thành Product
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                // Hiển thị thông tin sản phẩm
                                holder.tvName.setText(product.getName());
                                holder.tvPrice.setText(String.format("%,d VNĐ", product.getPrice()));
                                holder.tvCategory.setText(String.format(product.getCategory()));

                                // Tải ảnh sản phẩm bằng Glide
                                Glide.with(context)
                                        .load(product.getImage())  // Link hình ảnh sản phẩm từ Firestore
                                        .into(holder.itemImage);

                                // Hiển thị số lượng sản phẩm trong giỏ hàng
                                holder.itemQuantity.setText(String.valueOf(cartItem.getQuantity()));
                            }
                        }
                    }
                });

        holder.decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity = Integer.parseInt(holder.itemQuantity.getText().toString());
                if (currentQuantity > 1) {
                    currentQuantity--;
                    holder.itemQuantity.setText(String.valueOf(currentQuantity));
                }
            }
        });

        holder.decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity = Integer.parseInt(holder.itemQuantity.getText().toString());
                if (currentQuantity > 1) {
                    currentQuantity--;
                    holder.itemQuantity.setText(String.valueOf(currentQuantity));

                    // Cập nhật số lượng mới vào Firestore
                    updateQuantityInFirestore(cartItem.getId(), currentQuantity);
                }
            }
        });

        holder.increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity = Integer.parseInt(holder.itemQuantity.getText().toString());
                currentQuantity++;
                holder.itemQuantity.setText(String.valueOf(currentQuantity));

                // Cập nhật số lượng mới vào Firestore
                updateQuantityInFirestore(cartItem.getId(), currentQuantity);
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Xóa sản phẩm khỏi giỏ hàng trong Firestore
                deleteCartItemFromFirestore(cartItem.getId(), holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvPrice, itemQuantity;
        ImageButton increaseQuantity, decreaseQuantity;
        ImageView deleteButton;
        ImageView itemImage;

        public CartItemViewHolder(View itemView) {
            super(itemView);

            itemImage = itemView.findViewById(R.id.itemImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            increaseQuantity = itemView.findViewById(R.id.increaseQuantity);
            decreaseQuantity = itemView.findViewById(R.id.decreaseQuantity);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
    private void updateQuantityInFirestore(String cartItemId, int newQuantity) {
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        String userId = util.getUserIdFromSharedPreferences(context); // context lấy từ Adapter

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("carts")
                .document(cartItemId)
                .update("quantity", newQuantity)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật thành công
                    Toast.makeText(context, "Cập nhật số lượng thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Lỗi
                    Log.e("CartItemAdapter", "Failed to update quantity", e);
                });
    }

    private void deleteCartItemFromFirestore(String cartItemId, int position) {
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        String userId = util.getUserIdFromSharedPreferences(context);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("carts")
                .document(cartItemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xóa thành công, cập nhật luôn RecyclerView
                    cartItems.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Xóa sản phẩm thành công khỏi giỏ", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("CartItemAdapter", "Failed to delete item", e);
                });
    }
}
