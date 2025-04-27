package com.example.orderfoodgo.model;

public class Product {
    private String id; // ID của sản phẩm
    private String category;
    private String describe;
    private String image;
    private String name;
    private int price;
    private double rating;

    // Constructor không tham số
    public Product() {}

    // Constructor có tham số
    public Product(String id, String category, String describe, String image, String name, int price, double rating) {
        this.id = id;
        this.category = category;
        this.describe = describe;
        this.image = image;
        this.name = name;
        this.price = price;
        this.rating = rating;
    }

    // Getter và Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
