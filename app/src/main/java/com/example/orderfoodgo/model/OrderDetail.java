package com.example.orderfoodgo.model;

public class OrderDetail {
    String id;
    String idProduct;
    String idOrder;
    int quantityProduct;

    public OrderDetail(String id, String idProduct, String idOrder, int quantityProduct) {
        this.id = id;
        this.idProduct = idProduct;
        this.idOrder = idOrder;
        this.quantityProduct = quantityProduct;
    }

    public OrderDetail() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(String idProduct) {
        this.idProduct = idProduct;
    }

    public String getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(String idOrder) {
        this.idOrder = idOrder;
    }

    public int getQuantityProduct() {
        return quantityProduct;
    }

    public void setQuantityProduct(int quantityProduct) {
        this.quantityProduct = quantityProduct;
    }
}
