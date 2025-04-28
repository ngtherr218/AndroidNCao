package com.example.orderfoodgo.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteProductViewHolder> {

    private Context context;
    private List<FavoriteProduct> favoriteProductList;
    // Initialize Firebase Firestore instance
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Constructor
    public FavoriteAdapter(Context context, List<FavoriteProduct> favoriteProductList) {
        this.context = context;
        this.favoriteProductList = favoriteProductList;
    }

    @NonNull
    @Override
    public FavoriteProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteProductViewHolder holder, int position) {
        // Get the favorite product from the list
        FavoriteProduct favoriteProduct = favoriteProductList.get(position);

        // Check if the product ID is valid (not null)
        String productId = favoriteProduct.getProductId();
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(context, "Invalid product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get product data from Firestore using the valid product ID
        DocumentReference productRef = db.collection("products").document(productId);

        // Retrieve product data from Firestore
        productRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    Product product = documentSnapshot.toObject(Product.class);

                    // Ensure product data is not null
                    if (product != null) {
                        // Set product information to the views
                        holder.tvProductName.setText(product.getName());
                        holder.tvProductCategory.setText(product.getCategory());
                        holder.tvProductRating.setText(String.valueOf(product.getRating()));

                        // Load product image using Glide
                        Glide.with(context)
                                .load(product.getImage())
                                .into(holder.ivProduct);

                        // Handle item click event
                        holder.itemView.setOnClickListener(view -> {
                            Intent intent = new Intent(context, ProductDetailActivity.class);
                            intent.putExtra("id", product.getId());
                            intent.putExtra("name", product.getName());
                            intent.putExtra("price", product.getPrice());
                            intent.putExtra("rating", product.getRating());
                            intent.putExtra("image", product.getImage());
                            intent.putExtra("category", product.getCategory());
                            intent.putExtra("describe", product.getDescribe());
                            context.startActivity(intent);
                        });
                    }
                } else {
                    Toast.makeText(context, "Product not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Tải chi tiết không thành công", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteProductList.size();
    }

    public static class FavoriteProductViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProduct;
        TextView tvProductName, tvProductCategory, tvProductRating, tvIdProduct;

        public FavoriteProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIdProduct = itemView.findViewById(R.id.tvIdProduct);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvName);
            tvProductCategory = itemView.findViewById(R.id.tvCategory);
            tvProductRating = itemView.findViewById(R.id.tvRating);
        }
    }
}
