package com.example.orderfoodgo.model;

public class FavoriteProduct {
    private String productId;
    private String userId;

    // Constructor
    public FavoriteProduct(String productId, String userId) {
        this.productId = productId;
        this.userId = userId;
    }

    public  FavoriteProduct(){

    }

    // Getter và Setter
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
