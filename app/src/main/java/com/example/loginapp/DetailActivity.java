
package com.example.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DetailActivity extends AppCompatActivity {

    private ImageView imgDetail;

    private TextView tvName;
    private TextView tvCondition;
    private TextView tvShortDescription;
    private TextView tvDescription;
    private TextView tvPrice;
    private TextView tvHighlights;
    private TextView tvWarranty;
    private TextView btnBackDetail;
    private TextView btnFavoriteDetail;

    // Đổi từ TextView thành LinearLayout
    private LinearLayout tvSpecifications;

    private Button btnBuyNow;
    private Button btnAddToCart;

    private Product product;
    private FirebaseFirestore db;

    // Lưu danh sách yêu thích trên thiết bị
    private SharedPreferences favoritePreferences;
    private static final String FAVORITE_KEY = "favorite_product_ids";

    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initViews();
        getSelectedProduct();

        if (product == null) {
            showProductError();
            return;
        }

        db = FirebaseFirestore.getInstance();

        // Khởi tạo nơi lưu yêu thích
        favoritePreferences = getSharedPreferences(
                "favorite_preferences",
                MODE_PRIVATE
        );

        displayProduct();
        loadFavoriteState();
        setupListeners();
    }

    // Ánh xạ giao diện
    private void initViews() {
        imgDetail = findViewById(R.id.imgDetail);

        tvName = findViewById(R.id.tvName);
        tvCondition = findViewById(R.id.tvCondition);
        tvShortDescription = findViewById(R.id.tvShortDescription);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);
        tvHighlights = findViewById(R.id.tvHighlights);

        // Đây là LinearLayout trong XML
        tvSpecifications = findViewById(R.id.tvSpecifications);

        tvWarranty = findViewById(R.id.tvWarranty);

        btnBackDetail = findViewById(R.id.btnBackDetail);
        btnFavoriteDetail = findViewById(R.id.btnFavoriteDetail);

        btnBuyNow = findViewById(R.id.btnBuyNow);
        btnAddToCart = findViewById(R.id.btnAddToCart);
    }

    // Nhận sản phẩm từ HomeActivity
    private void getSelectedProduct() {
        product = (Product) getIntent().getSerializableExtra("product");
    }

    // Hiển thị thông tin sản phẩm
    private void displayProduct() {
        displayProductImage();

        tvName.setText(getSafeText(product.getName()));
        tvCondition.setText(getConditionLabel());

        tvShortDescription.setText(
                getSafeText(product.getShortDescription())
        );

        tvDescription.setText(
                getSafeText(product.getDescription())
        );

        tvPrice.setText(formatPrice(product.getPrice()));

        displayHighlights();
        displaySpecifications();

        tvWarranty.setText(
                getSafeText(product.getWarranty())
        );
    }

    // Hiển thị nhãn tình trạng
    private String getConditionLabel() {
        if ("USED".equalsIgnoreCase(product.getCondition())) {
            return "USED · Đã qua sử dụng";
        }

        return "NEW · Sản phẩm mới";
    }

    // Tránh hiển thị null
    private String getSafeText(String value) {
        return value == null || value.trim().isEmpty()
                ? "Chưa có thông tin"
                : value;
    }

    // Hiển thị ảnh sản phẩm
    private void displayProductImage() {
        int imageId = getResources().getIdentifier(
                product.getImage(),
                "drawable",
                getPackageName()
        );

        if (imageId != 0) {
            imgDetail.setImageResource(imageId);
        } else {
            imgDetail.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }
    }

    // Định dạng giá tiền
    private String formatPrice(String price) {
        if (price == null) {
            return "0 đ";
        }

        try {
            String number = price.replaceAll("[^0-9]", "");

            if (number.isEmpty()) {
                return price;
            }

            long amount = Long.parseLong(number);

            NumberFormat formatter =
                    NumberFormat.getIntegerInstance(new Locale("vi", "VN"));

            return formatter.format(amount) + " đ";

        } catch (Exception e) {
            return price;
        }
    }

    // Hiển thị các đặc điểm nổi bật
    private void displayHighlights() {
        if (product.getHighlights() == null
                || product.getHighlights().isEmpty()) {

            tvHighlights.setText("Chưa có thông tin nổi bật.");
            return;
        }

        StringBuilder content = new StringBuilder();

        for (String highlight : product.getHighlights()) {
            content.append("• ")
                    .append(highlight)
                    .append("\n");
        }

        tvHighlights.setText(content.toString().trim());
    }

    // Hiển thị thông số kỹ thuật thành bảng 2 cột
    private void displaySpecifications() {
        tvSpecifications.removeAllViews();

        Map<String, String> specifications =
                product.getSpecifications();

        if (specifications == null || specifications.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Chưa có thông số kỹ thuật.");
            emptyText.setTextSize(14);
            emptyText.setTextColor(getColor(R.color.text_secondary));

            tvSpecifications.addView(emptyText);
            return;
        }

        for (Map.Entry<String, String> item : specifications.entrySet()) {

            // Tạo một hàng ngang
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(10), 0, dp(10));

            // Cột tên thông số
            TextView nameText = new TextView(this);
            nameText.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    )
            );
            nameText.setText(item.getKey());
            nameText.setTextSize(13);
            nameText.setTextColor(getColor(R.color.text_secondary));

            // Cột giá trị
            TextView valueText = new TextView(this);
            valueText.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    )
            );
            valueText.setText(getSafeText(item.getValue()));
            valueText.setTextSize(13);
            valueText.setTextColor(getColor(R.color.text_primary));
            valueText.setGravity(Gravity.END);

            row.addView(nameText);
            row.addView(valueText);

            tvSpecifications.addView(row);

            // Đường phân cách giữa các hàng
            View divider = new View(this);
            divider.setBackgroundColor(
                    getColor(R.color.purple_light)
            );

            tvSpecifications.addView(
                    divider,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(1)
                    )
            );
        }
    }

    // Đổi dp sang pixel
    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    // Đọc trạng thái yêu thích đã lưu
    private void loadFavoriteState() {
        Set<String> savedFavorites = favoritePreferences.getStringSet(
                FAVORITE_KEY,
                new HashSet<>()
        );

        isFavorite = savedFavorites != null
                && savedFavorites.contains(product.getId());

        updateFavoriteIcon();
    }

    // Cập nhật biểu tượng trái tim
    private void updateFavoriteIcon() {
        if (isFavorite) {
            btnFavoriteDetail.setText("♥");
            btnFavoriteDetail.setTextColor(
                    Color.parseColor("#E53935")
            );
            btnFavoriteDetail.setContentDescription(
                    "Bỏ khỏi yêu thích"
            );
        } else {
            btnFavoriteDetail.setText("♡");
            btnFavoriteDetail.setTextColor(
                    getColor(R.color.purple_primary)
            );
            btnFavoriteDetail.setContentDescription(
                    "Thêm vào yêu thích"
            );
        }
    }

    // Thêm hoặc bỏ sản phẩm khỏi danh sách yêu thích
    private void toggleFavorite() {
        if (product.getId() == null || product.getId().isEmpty()) {
            showMessage("Không xác định được sản phẩm!");
            return;
        }

        Set<String> savedFavorites = new HashSet<>(
                favoritePreferences.getStringSet(
                        FAVORITE_KEY,
                        new HashSet<>()
                )
        );

        if (savedFavorites.contains(product.getId())) {
            savedFavorites.remove(product.getId());
            isFavorite = false;
            showMessage("Đã bỏ sản phẩm khỏi yêu thích!");
        } else {
            savedFavorites.add(product.getId());
            isFavorite = true;
            showMessage("Đã thêm vào danh sách yêu thích!");
        }

        favoritePreferences.edit()
                .putStringSet(FAVORITE_KEY, savedFavorites)
                .apply();

        updateFavoriteIcon();
    }

    // Thiết lập sự kiện
    private void setupListeners() {
        btnBackDetail.setOnClickListener(v -> finish());

        btnAddToCart.setOnClickListener(v -> addToCart());

        btnBuyNow.setOnClickListener(v -> buyNow());

        btnFavoriteDetail.setOnClickListener(v -> toggleFavorite());
    }

    // Chuyển sang trang thanh toán
    private void buyNow() {
        Intent intent = new Intent(this, PayActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    // Thêm sản phẩm vào giỏ hàng
    private void addToCart() {
        if (product.getId() == null || product.getId().isEmpty()) {
            showMessage("Không xác định được sản phẩm!");
            return;
        }

        DocumentReference cartRef = db.collection("cart_items")
                .document(product.getId());

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(cartRef);

            if (snapshot.exists()) {
                increaseQuantity(transaction, cartRef, snapshot);
            } else {
                createCartItem(transaction, cartRef);
            }

            return null;

        }).addOnSuccessListener(unused ->
                showMessage("Đã thêm sản phẩm vào giỏ hàng!")
        ).addOnFailureListener(e ->
                showMessage("Không thể thêm sản phẩm: " + e.getMessage())
        );
    }

    // Tăng số lượng sản phẩm đã có trong giỏ
    private void increaseQuantity(
            Transaction transaction,
            DocumentReference cartRef,
            DocumentSnapshot snapshot
    ) {
        Long quantity = snapshot.getLong("quantity");
        long currentQuantity = quantity == null ? 0 : quantity;

        transaction.update(cartRef, "quantity", currentQuantity + 1);
    }

    // Tạo sản phẩm mới trong giỏ hàng
    private void createCartItem(
            Transaction transaction,
            DocumentReference cartRef
    ) {
        Map<String, Object> cartItem = new HashMap<>();

        cartItem.put("productId", product.getId());
        cartItem.put("image", product.getImage());
        cartItem.put("name", product.getName());
        cartItem.put("shortDescription", product.getShortDescription());
        cartItem.put("description", product.getDescription());
        cartItem.put("price", product.getPrice());
        cartItem.put("category", product.getCategory());
        cartItem.put("condition", product.getCondition());
        cartItem.put("quantity", 1);

        transaction.set(cartRef, cartItem);
    }

    // Hiển thị thông báo
    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    // Xử lý khi không nhận được sản phẩm
    private void showProductError() {
        showMessage("Không tìm thấy thông tin sản phẩm!");
        finish();
    }
}
