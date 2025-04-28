package com.example.orderfoodgo.model;

import com.google.firebase.Timestamp;
import com.google.type.DateTime;

import java.util.Date;

public class Order {
    String id;
    String idUser;
    String idDelivery;
    Timestamp createAt;
    int deliveryFee;
    int totalMoney;
    String status;

    public Order(String id, String idUser, String idDelivery, Timestamp createAt, int deliveryFee, int totalMoney, String status) {
        this.id = id;
        this.idUser = idUser;
        this.idDelivery = idDelivery;
        this.createAt = createAt;
        this.deliveryFee = deliveryFee;
        this.totalMoney = totalMoney;
        this.status = status;
    }

    public Order() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdDelivery() {
        return idDelivery;
    }

    public void setIdDelivery(String idDelivery) {
        this.idDelivery = idDelivery;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public int getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(int deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(int totalMoney) {
        this.totalMoney = totalMoney;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
