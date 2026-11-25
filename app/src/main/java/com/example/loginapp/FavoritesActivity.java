
package com.example.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity {

    private ListView listViewFavorites;
    private LinearLayout tvEmptyFavorites;
    private TextView btnBackFavorites;

    private List<Product> favoriteProducts = new ArrayList<>();

    private SharedPreferences favoritePreferences;

    private static final String FAVORITE_KEY = "favorite_product_ids";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        initViews();
        setupListeners();

        favoritePreferences = getSharedPreferences(
                "favorite_preferences",
                MODE_PRIVATE
        );
    }

    // Tải lại danh sách mỗi khi quay về trang Favorites
    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteProducts();
    }

    // Ánh xạ giao diện
    private void initViews() {
        listViewFavorites = findViewById(R.id.listViewFavorites);
        tvEmptyFavorites = findViewById(R.id.tvEmptyFavorites);
        btnBackFavorites = findViewById(R.id.btnBackFavorites);
    }

    // Thiết lập sự kiện
    private void setupListeners() {
        btnBackFavorites.setOnClickListener(v -> finish());

        listViewFavorites.setOnItemClickListener(
                (parent, view, position, id) -> {
                    Product product = (Product) parent.getItemAtPosition(position);

                    Intent intent = new Intent(
                            this,
                            DetailActivity.class
                    );

                    intent.putExtra("product", product);
                    startActivity(intent);
                }
        );

        findViewById(R.id.navHome).setOnClickListener(v -> finish());

        findViewById(R.id.navFavorites).setOnClickListener(v ->
                showMessage("Bạn đang ở mục Yêu thích")
        );

        findViewById(R.id.navCart).setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class))
        );
        findViewById(R.id.navAccount).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );
    }

    // Đọc ID yêu thích và tải sản phẩm từ Firestore
    private void loadFavoriteProducts() {
        Set<String> savedFavorites = new HashSet<>(
                favoritePreferences.getStringSet(
                        FAVORITE_KEY,
                        new HashSet<>()
                )
        );

        if (savedFavorites.isEmpty()) {
            favoriteProducts.clear();
            displayFavoriteProducts();
            return;
        }

        DatabaseInitializer.initialize(
                this,
                new DatabaseInitializer.OnDatabaseReady() {

                    @Override
                    public void onReady(List<Product> productList) {
                        favoriteProducts.clear();

                        for (Product product : productList) {
                            if (product.getId() != null
                                    && savedFavorites.contains(product.getId())) {

                                favoriteProducts.add(product);
                            }
                        }

                        displayFavoriteProducts();
                    }

                    @Override
                    public void onError(Exception e) {
                        showMessage(
                                "Không thể tải sản phẩm: " + e.getMessage()
                        );
                    }
                }
        );
    }

    // Hiển thị danh sách yêu thích
    private void displayFavoriteProducts() {
        if (favoriteProducts.isEmpty()) {
            listViewFavorites.setVisibility(View.GONE);
            tvEmptyFavorites.setVisibility(View.VISIBLE);
            return;
        }

        listViewFavorites.setVisibility(View.VISIBLE);
        tvEmptyFavorites.setVisibility(View.GONE);

        ProductAdapter adapter = new ProductAdapter(
                this,
                favoriteProducts.toArray(new Product[0])
        );

        listViewFavorites.setAdapter(adapter);
    }

    // Hiển thị thông báo
    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
