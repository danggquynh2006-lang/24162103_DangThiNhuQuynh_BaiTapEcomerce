
package com.example.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    private TextView btnMenu;
    private TextView btnLogout;
    private TextView tvWelcome;
    private TextView tvCategoryTitle;

    private EditText edtSearch;
    private ListView listView;

    private MaterialButton categoryAll;
    private MaterialButton categoryPhone;
    private MaterialButton categoryLaptop;
    private MaterialButton categoryAccessory;
    private MaterialButton categoryUsed;

    private List<Product> products = new ArrayList<>();

    private String selectedCategory = "Tất cả sản phẩm";
    private boolean filterUsedOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupListeners();
        displayUsername();
        loadProducts();
    }

    // Ánh xạ các View
    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);

        btnMenu = findViewById(R.id.btnMenu);
        btnLogout = findViewById(R.id.btnLogout);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);

        edtSearch = findViewById(R.id.edtSearch);
        listView = findViewById(R.id.listView);

        categoryAll = findViewById(R.id.categoryAll);
        categoryPhone = findViewById(R.id.categoryPhone);
        categoryLaptop = findViewById(R.id.categoryLaptop);
        categoryAccessory = findViewById(R.id.categoryAccessory);
        categoryUsed = findViewById(R.id.categoryUsed);
    }

    // Thiết lập các sự kiện
    private void setupListeners() {
        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(Gravity.LEFT));

        btnLogout.setOnClickListener(v -> logout());

        setupSearch();
        setupProductClick();
        setupCategoryButtons();
        setupNavigation();
    }

    // Hiển thị tên người dùng
    private void displayUsername() {
        SharedPreferences prefs =
                getSharedPreferences("MyPrefs", MODE_PRIVATE);

        String username = prefs.getString("username", "");

        if (!username.isEmpty()) {
            tvWelcome.setText("Xin chào, " + username + "!");
        }
    }

    // Tải sản phẩm từ Firestore
    private void loadProducts() {
        DatabaseInitializer.initialize(this,
                new DatabaseInitializer.OnDatabaseReady() {

                    @Override
                    public void onReady(List<Product> productList) {
                        products = productList;
                        filterProducts(edtSearch.getText().toString());
                    }

                    @Override
                    public void onError(Exception e) {
                        showMessage("Không thể tải sản phẩm: " + e.getMessage());
                    }
                });
    }

    // Đăng xuất
    private void logout() {
        getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .edit()
                .remove("username")
                .apply();

        Intent intent = new Intent(this, LoginActivity.class);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    // Tìm kiếm sản phẩm
    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Mở trang chi tiết sản phẩm
    private void setupProductClick() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Product product = (Product) parent.getItemAtPosition(position);

            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
    }

    // Thiết lập các nút danh mục
    private void setupCategoryButtons() {
        setCategoryClick(R.id.menuAll, "Tất cả sản phẩm");
        setCategoryClick(R.id.menuPhone, "Điện thoại");
        setCategoryClick(R.id.menuLaptop, "Laptop");
        setCategoryClick(R.id.menuAccessory, "Phụ kiện");

        setCategoryClick(R.id.categoryAll, "Tất cả sản phẩm");
        setCategoryClick(R.id.categoryPhone, "Điện thoại");
        setCategoryClick(R.id.categoryLaptop, "Laptop");
        setCategoryClick(R.id.categoryAccessory, "Phụ kiện");
        setCategoryClick(R.id.categoryUsed, "Đã qua sử dụng");
    }

    // Thiết lập thanh điều hướng
    private void setupNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v ->
                selectCategory("Tất cả sản phẩm"));

        findViewById(R.id.navFavorites).setOnClickListener(v ->
                startActivity(new Intent(this, FavoritesActivity.class)));

        findViewById(R.id.navCart).setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class)));

        findViewById(R.id.navAccount).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    // Gán sự kiện chung cho nút danh mục
    private void setCategoryClick(int viewId, String category) {
        findViewById(viewId).setOnClickListener(v -> {
            selectCategory(category);
            closeMenu();
        });
    }

    // Chọn danh mục
    private void selectCategory(String category) {
        selectedCategory = category;
        filterUsedOnly = category.equals("Đã qua sử dụng");

        updateCategoryButtonStyles();

        edtSearch.setText("");
        filterProducts("");
    }

    // Lọc sản phẩm theo tên, danh mục và tình trạng
    private void filterProducts(String keyword) {
        List<Product> filteredProducts = new ArrayList<>();

        String searchKeyword = keyword.trim().toLowerCase(Locale.ROOT);

        for (Product product : products) {

            boolean matchesName = product.getName() != null
                    && product.getName().toLowerCase(Locale.ROOT)
                    .contains(searchKeyword);

            boolean matchesCategory = selectedCategory.equals("Tất cả sản phẩm")
                    || filterUsedOnly
                    || selectedCategory.equals(product.getCategory());

            boolean matchesCondition = !filterUsedOnly
                    || "USED".equalsIgnoreCase(product.getCondition());

            if (matchesName && matchesCategory && matchesCondition) {
                filteredProducts.add(product);
            }
        }

        ProductAdapter adapter = new ProductAdapter(
                this,
                filteredProducts.toArray(new Product[0])
        );

        listView.setAdapter(adapter);
        updateCategoryTitle(filteredProducts.size());
    }

    // Cập nhật tiêu đề danh sách
    private void updateCategoryTitle(int resultCount) {
        if (resultCount == 0) {
            tvCategoryTitle.setText("Không tìm thấy sản phẩm");
            return;
        }

        tvCategoryTitle.setText(
                selectedCategory + " (" + resultCount + ")"
        );
    }

    // Cập nhật màu các nút danh mục
    private void updateCategoryButtonStyles() {
        MaterialButton[] buttons = {
                categoryAll,
                categoryPhone,
                categoryLaptop,
                categoryAccessory,
                categoryUsed
        };

        String[] categories = {
                "Tất cả sản phẩm",
                "Điện thoại",
                "Laptop",
                "Phụ kiện",
                "Đã qua sử dụng"
        };

        for (int i = 0; i < buttons.length; i++) {
            boolean selected = selectedCategory.equals(categories[i]);

            buttons[i].setBackgroundTintList(
                    ColorStateList.valueOf(getColor(
                            selected
                                    ? R.color.purple_primary
                                    : R.color.purple_light
                    ))
            );

            buttons[i].setTextColor(getColor(
                    selected
                            ? R.color.white
                            : R.color.purple_primary
            ));
        }
    }

    // Đóng Drawer
    private void closeMenu() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

    // Hiển thị thông báo
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
