package com.example.orderfoodgo.model;

public class CartItem {
    private String id;        // ID của cart item
    private String idUser;    // ID của user
    private String idProduct; // ID của sản phẩm
    private int quantity;     // Số lượng sản phẩm

    public CartItem() {
        // Firestore cần constructor rỗng
    }

    public CartItem(String id, String idUser, String idProduct, int quantity) {
        this.id = id;
        this.idUser = idUser;
        this.idProduct = idProduct;
        this.quantity = quantity;
    }

    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public String getIdProduct() { return idProduct; }
    public void setIdProduct(String idProduct) { this.idProduct = idProduct; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
