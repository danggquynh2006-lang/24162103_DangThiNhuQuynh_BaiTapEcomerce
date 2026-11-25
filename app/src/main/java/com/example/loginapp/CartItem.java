
package com.example.loginapp;

public class CartItem {

    public String productId;

    public String image;
    public String name;
    public String description;
    public String price;
    public String category;

    public int quantity;

    // Constructor rỗng cho Firestore
    public CartItem() {
    }

    // Constructor tạo sản phẩm trong giỏ hàng
    public CartItem(
            String productId,
            String image,
            String name,
            String description,
            String price,
            String category,
            int quantity
    ) {
        this.productId = productId;
        this.image = image;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.quantity = quantity;
    }
}
