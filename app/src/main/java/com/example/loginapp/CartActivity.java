
package com.example.loginapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CartActivity extends AppCompatActivity {

    private LinearLayout layoutCartItems;
    private TextView tvTotal;
    private TextView tvOrderSummary;
    private TextView tvEmpty;
    private Button btnCheckout;
    private TextView btnBackCart;
    private TextView tvCartCount;

    private FirebaseFirestore db;

    private final List<CartItem> cartItems = new ArrayList<>();
    private final Set<String> selectedProductIds = new HashSet<>();

    private boolean selectionInitialized = false;

    private final int purple = Color.rgb(91, 48, 145);
    private final int textPrimary = Color.rgb(43, 35, 55);
    private final int textSecondary = Color.rgb(119, 108, 132);
    private final int imageBackground = Color.rgb(248, 246, 251);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        db = FirebaseFirestore.getInstance();
        setupListeners();
    }

    // Ánh xạ View
    private void initViews() {
        layoutCartItems = findViewById(R.id.layoutCartItems);
        tvTotal = findViewById(R.id.tvTotal);
        tvOrderSummary = findViewById(R.id.tvOrderSummary);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBackCart = findViewById(R.id.btnBackCart);
        tvCartCount = findViewById(R.id.tvCartCount);
    }

    // Tải lại giỏ hàng khi mở hoặc quay lại trang
    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    // Thiết lập sự kiện
    private void setupListeners() {
        btnBackCart.setOnClickListener(v -> finish());
        btnCheckout.setOnClickListener(v -> checkoutCart());
    }

    // Đổi dp sang pixel
    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    // Tạo nền bo góc
    private GradientDrawable createBackground(
            int color, int strokeColor, int radius) {

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        if (strokeColor != Color.TRANSPARENT) {
            drawable.setStroke(dp(1), strokeColor);
        }

        return drawable;
    }

    // Tải giỏ hàng từ Firestore
    private void loadCart() {
        db.collection("cart_items")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    cartItems.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        CartItem item = document.toObject(CartItem.class);
                        item.productId = document.getId();
                        cartItems.add(item);
                    }

                    // Lần đầu mở giỏ hàng: chọn tất cả sản phẩm
                    if (!selectionInitialized) {
                        for (CartItem item : cartItems) {
                            selectedProductIds.add(item.productId);
                        }
                        selectionInitialized = true;
                    } else {
                        // Loại bỏ ID của sản phẩm đã bị xóa
                        Set<String> currentIds = new HashSet<>();

                        for (CartItem item : cartItems) {
                            currentIds.add(item.productId);
                        }

                        selectedProductIds.retainAll(currentIds);
                    }

                    displayCart();
                })
                .addOnFailureListener(e ->
                        showMessage("Không thể tải giỏ hàng: " + e.getMessage())
                );
    }

    // Hiển thị giỏ hàng
    private void displayCart() {
        layoutCartItems.removeAllViews();

        // Cập nhật số loại sản phẩm trong giỏ hàng
        tvCartCount.setText(
                "SẢN PHẨM TRONG GIỎ (" + cartItems.size() + ")"
        );

        if (cartItems.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(false);
            btnCheckout.setAlpha(0.55f);

            tvOrderSummary.setText("Chưa có sản phẩm trong giỏ hàng");
            tvTotal.setText("0 đ");
            return;
        }

        tvEmpty.setVisibility(View.GONE);

        for (CartItem item : cartItems) {
            createCartItemView(item);
        }

        updateOrderSummary();
    }

    // Tạo giao diện cho từng sản phẩm
    private void createCartItemView(CartItem item) {

        // Thẻ sản phẩm
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setBackground(createBackground(
                Color.WHITE, 0xFFE5D9F2, 16
        ));
        card.setElevation(dp(2));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, dp(4), 0, dp(12));
        card.setLayoutParams(cardParams);

        // Hàng chứa checkbox, ảnh và thông tin
        LinearLayout topLayout = new LinearLayout(this);
        topLayout.setOrientation(LinearLayout.HORIZONTAL);
        topLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Checkbox chọn sản phẩm
        CheckBox checkBox = new CheckBox(this);
        checkBox.setButtonTintList(new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        purple,
                        0xFFAAA1B5
                }
        ));

        checkBox.setChecked(selectedProductIds.contains(item.productId));

        LinearLayout.LayoutParams checkParams =
                new LinearLayout.LayoutParams(dp(30), dp(48));

        checkBox.setLayoutParams(checkParams);
        topLayout.addView(checkBox);

        // Khung ảnh
        LinearLayout imageBox = new LinearLayout(this);
        imageBox.setGravity(Gravity.CENTER);
        imageBox.setBackground(createBackground(
                imageBackground, Color.TRANSPARENT, 10
        ));

        imageBox.setLayoutParams(
                new LinearLayout.LayoutParams(dp(76), dp(82))
        );

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(
                new LinearLayout.LayoutParams(dp(70), dp(76))
        );
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        int imageId = getResources().getIdentifier(
                item.image,
                "drawable",
                getPackageName()
        );

        if (imageId != 0) {
            imageView.setImageResource(imageId);
        } else {
            imageView.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }

        imageBox.addView(imageView);
        topLayout.addView(imageBox);

        // Thông tin sản phẩm
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        infoLayout.setPadding(dp(10), 0, 0, 0);

        infoLayout.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        TextView tvName = new TextView(this);
        tvName.setText(item.name == null ? "" : item.name);
        tvName.setTextSize(14);
        tvName.setTextColor(textPrimary);
        tvName.setTypeface(null, Typeface.BOLD);
        infoLayout.addView(tvName);

        TextView tvDescription = new TextView(this);

        String description = item.description;

        if (description == null || description.trim().isEmpty()) {
            description = item.category == null ? "" : item.category;
        }

        tvDescription.setText(description);
        tvDescription.setTextSize(11);
        tvDescription.setTextColor(textSecondary);
        tvDescription.setMaxLines(2);
        tvDescription.setEllipsize(
                android.text.TextUtils.TruncateAt.END
        );

        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        descriptionParams.topMargin = dp(3);
        tvDescription.setLayoutParams(descriptionParams);
        infoLayout.addView(tvDescription);

        TextView tvPrice = new TextView(this);
        tvPrice.setText(formatPrice(getPrice(item.price)));
        tvPrice.setTextSize(14);
        tvPrice.setTextColor(purple);
        tvPrice.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams priceParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        priceParams.topMargin = dp(5);
        tvPrice.setLayoutParams(priceParams);
        infoLayout.addView(tvPrice);

        topLayout.addView(infoLayout);
        card.addView(topLayout);

        // Đường phân cách
        View divider = new View(this);
        divider.setBackgroundColor(0xFFEDE5F5);

        LinearLayout.LayoutParams dividerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                );

        dividerParams.topMargin = dp(9);
        dividerParams.bottomMargin = dp(8);
        card.addView(divider, dividerParams);

        // Hàng điều chỉnh số lượng và xóa
        LinearLayout bottomLayout = new LinearLayout(this);
        bottomLayout.setOrientation(LinearLayout.HORIZONTAL);
        bottomLayout.setGravity(Gravity.CENTER_VERTICAL);

        TextView quantityLabel = new TextView(this);
        quantityLabel.setText("Số lượng");
        quantityLabel.setTextSize(11);
        quantityLabel.setTextColor(textSecondary);
        bottomLayout.addView(quantityLabel);

        TextView btnDecrease = createQuantityButton("−");
        TextView tvQuantity = new TextView(this);

        tvQuantity.setText(String.valueOf(item.quantity));
        tvQuantity.setTextSize(13);
        tvQuantity.setTextColor(textPrimary);
        tvQuantity.setTypeface(null, Typeface.BOLD);
        tvQuantity.setGravity(Gravity.CENTER);
        tvQuantity.setMinWidth(dp(30));

        TextView btnIncrease = createQuantityButton("+");

        LinearLayout.LayoutParams quantityButtonParams =
                new LinearLayout.LayoutParams(dp(30), dp(30));

        quantityButtonParams.setMargins(dp(8), 0, dp(2), 0);
        btnDecrease.setLayoutParams(quantityButtonParams);

        bottomLayout.addView(btnDecrease);
        bottomLayout.addView(tvQuantity);

        LinearLayout.LayoutParams increaseParams =
                new LinearLayout.LayoutParams(dp(30), dp(30));

        increaseParams.setMargins(dp(2), 0, 0, 0);
        btnIncrease.setLayoutParams(increaseParams);

        bottomLayout.addView(btnIncrease);

        View space = new View(this);
        bottomLayout.addView(
                space,
                new LinearLayout.LayoutParams(0, 1, 1)
        );

        // Nút xóa sản phẩm bằng icon vector
        ImageView btnDelete = new ImageView(this);
        btnDelete.setImageResource(R.drawable.ic_delete);
        btnDelete.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btnDelete.setPadding(dp(7), dp(7), dp(7), dp(7));
        btnDelete.setContentDescription("Xóa sản phẩm");

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(dp(38), dp(38));

        btnDelete.setLayoutParams(deleteParams);
        btnDelete.setClickable(true);
        btnDelete.setFocusable(true);

        bottomLayout.addView(btnDelete);

        card.addView(bottomLayout);
        layoutCartItems.addView(card);

        // Chọn hoặc bỏ chọn sản phẩm
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                selectedProductIds.add(item.productId);
            } else {
                selectedProductIds.remove(item.productId);
            }

            updateOrderSummary();
        });

        btnDecrease.setOnClickListener(v ->
                changeQuantity(item, -1));

        btnIncrease.setOnClickListener(v ->
                changeQuantity(item, 1));

        btnDelete.setOnClickListener(v ->
                deleteItem(item));
    }

    // Tạo nút tăng giảm số lượng
    private TextView createQuantityButton(String text) {
        TextView button = new TextView(this);
        button.setText(text);
        button.setTextSize(17);
        button.setTextColor(purple);
        button.setTypeface(null, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setBackground(createBackground(
                0xFFEFE6FA, 0xFFDCC9F0, 9
        ));
        button.setClickable(true);
        button.setFocusable(true);

        return button;
    }

    // Cập nhật danh sách sản phẩm được chọn và tổng tiền
    private void updateOrderSummary() {
        StringBuilder summary = new StringBuilder();
        long total = 0;
        int selectedCount = 0;

        for (CartItem item : cartItems) {

            if (selectedProductIds.contains(item.productId)) {

                if (summary.length() > 0) {
                    summary.append("\n");
                }

                summary.append("• ")
                        .append(item.name)
                        .append(" x ")
                        .append(item.quantity);

                total += getPrice(item.price) * item.quantity;
                selectedCount++;
            }
        }

        if (selectedCount == 0) {
            tvOrderSummary.setText("Chưa chọn sản phẩm nào");
            tvTotal.setText("0 đ");
            btnCheckout.setEnabled(false);
            btnCheckout.setAlpha(0.55f);
        } else {
            tvOrderSummary.setText(summary.toString());
            tvTotal.setText(formatPrice(total));
            btnCheckout.setEnabled(true);
            btnCheckout.setAlpha(1f);
        }
    }

    // Định dạng giá tiền
    private String formatPrice(long price) {
        NumberFormat formatter =
                NumberFormat.getIntegerInstance(new Locale("vi", "VN"));

        return formatter.format(price) + " đ";
    }

    // Chuyển giá thành số
    private long getPrice(String price) {
        if (price == null || price.trim().isEmpty()) {
            return 0;
        }

        try {
            String number = price.replaceAll("[^0-9]", "");

            if (number.isEmpty()) {
                return 0;
            }

            return Long.parseLong(number);

        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Thay đổi số lượng trên Firestore
    private void changeQuantity(CartItem item, int change) {
        DocumentReference cartRef = db.collection("cart_items")
                .document(item.productId);

        db.runTransaction(transaction -> {

                    DocumentSnapshot snapshot = transaction.get(cartRef);

                    if (!snapshot.exists()) {
                        return null;
                    }

                    Long quantity = snapshot.getLong("quantity");
                    long currentQuantity = quantity == null ? 0 : quantity;
                    long newQuantity = currentQuantity + change;

                    if (newQuantity <= 0) {
                        transaction.delete(cartRef);
                    } else {
                        transaction.update(cartRef, "quantity", newQuantity);
                    }

                    return null;

                }).addOnSuccessListener(unused -> loadCart())
                .addOnFailureListener(e ->
                        showMessage(
                                "Không thể cập nhật số lượng: " + e.getMessage()
                        )
                );
    }

    // Xóa sản phẩm khỏi giỏ hàng
    private void deleteItem(CartItem item) {
        db.collection("cart_items")
                .document(item.productId)
                .delete()
                .addOnSuccessListener(unused -> {

                    selectedProductIds.remove(item.productId);

                    showMessage("Đã xóa sản phẩm khỏi giỏ hàng");
                    loadCart();
                })
                .addOnFailureListener(e ->
                        showMessage(
                                "Không thể xóa sản phẩm: " + e.getMessage()
                        )
                );
    }

    // Thanh toán các sản phẩm đã chọn

    // Thanh toán các sản phẩm đã chọn
    private void checkoutCart() {
        if (selectedProductIds.isEmpty()) {
            showMessage("Bạn chưa chọn sản phẩm để thanh toán!");
            return;
        }

        StringBuilder cartSummary = new StringBuilder();
        long total = 0;

        // Danh sách sản phẩm được chọn gửi sang PayActivity
        ArrayList<HashMap<String, Object>> selectedItems =
                new ArrayList<>();

        for (CartItem item : cartItems) {

            if (selectedProductIds.contains(item.productId)) {

                cartSummary.append(item.name)
                        .append(" x ")
                        .append(item.quantity)
                        .append("\n");

                total += getPrice(item.price) * item.quantity;

                // Lưu thông tin sản phẩm
                HashMap<String, Object> selectedItem = new HashMap<>();

                selectedItem.put("productId", item.productId);
                selectedItem.put("name", item.name);
                selectedItem.put("image", item.image);
                selectedItem.put("description", item.description);
                selectedItem.put("price", item.price);
                selectedItem.put("category", item.category);
                selectedItem.put("quantity", item.quantity);

                selectedItems.add(selectedItem);
            }
        }

        if (selectedItems.isEmpty()) {
            showMessage("Không tìm thấy sản phẩm đã chọn!");
            return;
        }

        Intent intent = new Intent(
                CartActivity.this,
                PayActivity.class
        );

        intent.putExtra("cartSummary", cartSummary.toString());
        intent.putExtra("cartTotal", total);

        // Gửi cả danh sách sản phẩm và ID Firestore
        intent.putExtra("selectedCartItems", selectedItems);

        startActivity(intent);
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
