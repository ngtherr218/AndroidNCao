package com.example.orderfoodgo.model;

public class DeliveryInfo {
    private String name;
    private String phone;
    private String address;

    // Bắt buộc phải có constructor trống cho Firestore
    public DeliveryInfo() {
    }

    public DeliveryInfo(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    // Getter và Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
