
package com.example.loginapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Product implements Serializable {

    public String id;
    public String image;
    public String name;
    public String shortDescription;
    public String description;
    public String price;
    public String category;
    public String condition;
    public ArrayList<String> highlights;
    public LinkedHashMap<String, String> specifications;
    public String warranty;

    public Product() {
        highlights = new ArrayList<>();
        specifications = new LinkedHashMap<>();
    }

    // Constructor cũ
    public Product(String id, String image, String name,
                   String description, String price,
                   String category) {

        this(id, image, name, "", description, price, category, "");
    }

    // Constructor có mô tả ngắn
    public Product(String id, String image, String name,
                   String shortDescription, String description,
                   String price, String category) {

        this(id, image, name, shortDescription,
                description, price, category, "");
    }

    // Constructor đầy đủ thông tin cơ bản
    public Product(String id, String image, String name,
                   String shortDescription, String description,
                   String price, String category, String condition) {

        this.id = id;
        this.image = image;
        this.name = name;
        this.shortDescription = shortDescription;
        this.description = description;
        this.price = price;
        this.category = category;
        this.condition = condition;

        highlights = new ArrayList<>();
        specifications = new LinkedHashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getCondition() {
        return condition;
    }

    public ArrayList<String> getHighlights() {
        return highlights;
    }

    public LinkedHashMap<String, String> getSpecifications() {
        return specifications;
    }

    public String getWarranty() {
        return warranty;
    }
}
