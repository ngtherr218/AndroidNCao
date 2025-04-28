package com.example.orderfoodgo.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.orderfoodgo.R;
import com.example.orderfoodgo.activity.ProductDetailActivity;
import com.example.orderfoodgo.model.FavoriteProduct;
import com.example.orderfoodgo.model.Product;
import com.example.orderfoodgo.util.SharedPreferencesUtil;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public List<Product> productList;


    // Constructor
    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        SharedPreferencesUtil util = new SharedPreferencesUtil();
        Product product = productList.get(position);

        // Hiển thị thông tin sản phẩm
        holder.tvProductName.setText(product.getName());
        holder.tvProductCategory.setText(product.getCategory());
        holder.tvProductRating.setText(String.valueOf(product.getRating()));

        // Sử dụng thư viện như Glide để tải ảnh từ URL
        Glide.with(holder.itemView.getContext())
                .load(product.getImage())
                .into(holder.ivProduct);
        holder.ivProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), ProductDetailActivity.class);
                intent.putExtra("id", product.getId());
                intent.putExtra("name", product.getName());
                intent.putExtra("image", product.getImage());
                intent.putExtra("describe", product.getDescribe());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("rating", product.getRating());
                intent.putExtra("category", product.getCategory());
                holder.itemView.getContext().startActivity(intent);
            }
        });

        // Thiết lập trạng thái yêu thích ngay khi vào trang
        String productId = product.getId();
        String userId = util.getUserIdFromSharedPreferences(holder.itemView.getContext());

        if (productId != null && !productId.isEmpty() && userId != null && !userId.isEmpty()) {

            DocumentReference userRef = db.collection("users").document(userId);
            CollectionReference favoritesRef = userRef.collection("favorites");

            // Kiểm tra trạng thái yêu thích của sản phẩm
            favoritesRef.document(productId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    // Nếu sản phẩm đã có trong yêu thích, đặt icon yêu thích
                    holder.ivFavorite.setImageResource(R.drawable.baseline_favorite_25);
                } else {
                    // Nếu sản phẩm chưa có trong yêu thích, đặt icon chưa yêu thích
                    holder.ivFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                }
            });

            // Xử lý sự kiện click vào nút yêu thích
            holder.ivFavorite.setOnClickListener(view -> {
                if (!userId.isEmpty()) {
                    favoritesRef.document(productId).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                // Nếu sản phẩm đã có trong yêu thích, xóa nó
                                favoritesRef.document(productId).delete().addOnCompleteListener(deleteTask -> {
                                    if (deleteTask.isSuccessful()) {
                                        holder.ivFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                                        Toast.makeText(holder.itemView.getContext(), "Đã xóa " + product.getName() + " khỏi yêu thích", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(holder.itemView.getContext(), "Không thể xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // Nếu sản phẩm chưa có trong yêu thích, thêm nó
                                FavoriteProduct favoriteProduct = new FavoriteProduct(productId, userId);
                                favoritesRef.document(productId).set(favoriteProduct).addOnCompleteListener(addTask -> {
                                    if (addTask.isSuccessful()) {
                                        holder.ivFavorite.setImageResource(R.drawable.baseline_favorite_25);
                                        Toast.makeText(holder.itemView.getContext(), "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(holder.itemView.getContext(), "Không thể thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Không kiểm tra được trạng thái", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Người dùng không đăng nhập", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle error if productId or userId is invalid
            Toast.makeText(holder.itemView.getContext(), "Invalid product or user ID", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct, ivFavorite;
        TextView tvProductName, tvProductCategory, tvProductRating;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvProductRating = itemView.findViewById(R.id.tvProductRating);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }

}
