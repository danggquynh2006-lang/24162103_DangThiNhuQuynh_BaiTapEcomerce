
package com.example.loginapp;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatabaseInitializer {

    public interface OnDatabaseReady {
        void onReady(List<Product> products);

        void onError(Exception e);
    }

    public static void initialize(
            Context context,
            OnDatabaseReady callback
    ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<Product> products = new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        products.add(convertDocumentToProduct(document));
                    }

                    callback.onReady(products);
                })
                .addOnFailureListener(callback::onError);
    }

    // Chuyển dữ liệu Firestore thành Product
    private static Product convertDocumentToProduct(
            QueryDocumentSnapshot document
    ) {
        Product product = new Product();

        product.id = document.getId();
        product.name = document.getString("name");
        product.shortDescription = document.getString("shortDescription");
        product.description = document.getString("description");
        product.category = document.getString("category");
        product.condition = document.getString("condition");
        product.warranty = document.getString("warranty");

        Object price = document.get("price");
        product.price = price == null ? "0" : String.valueOf(price);

        Object image = document.get("image");
        product.image = image instanceof String ? (String) image : "";

        // Đọc danh sách đặc điểm nổi bật
        Object highlights = document.get("highlights");

        if (highlights instanceof List<?>) {
            for (Object item : (List<?>) highlights) {
                if (item != null) {
                    product.highlights.add(String.valueOf(item));
                }
            }
        }

        // Đọc bảng thông số kỹ thuật
        Object specifications = document.get("specifications");

        if (specifications instanceof Map<?, ?>) {
            Map<?, ?> data = (Map<?, ?>) specifications;

            for (Map.Entry<?, ?> entry : data.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    product.specifications.put(
                            String.valueOf(entry.getKey()),
                            String.valueOf(entry.getValue())
                    );
                }
            }
        }

        return product;
    }
}
