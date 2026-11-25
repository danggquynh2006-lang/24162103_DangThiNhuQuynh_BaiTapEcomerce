
package com.example.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PayActivity extends AppCompatActivity {

    private TextView tvPayUsername;
    private TextView tvPayProduct;
    private TextView tvPayPrice;
    private TextView btnBack;

    private EditText edtName;
    private EditText edtPhone;
    private EditText edtAddress;

    private Button btnPayment;
    private RadioGroup rgPayment;

    private FirebaseFirestore db;

    private String username = "";
    private String paymentProductSummary = "";
    private long paymentTotal = 0;

    // Danh sách sản phẩm được chọn từ giỏ hàng
    private ArrayList<HashMap<String, Object>> selectedCartItems;

    // Sản phẩm mua ngay từ trang chi tiết
    private Product singleProduct;

    // Xác định nguồn thanh toán
    private boolean isCartCheckout = false;

    // Tránh người dùng nhấn đặt hàng nhiều lần
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        getWindow().setStatusBarColor(Color.rgb(79, 43, 126));
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility()
                        | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        db = FirebaseFirestore.getInstance();

        initViews();
        displayUsername();
        displayPaymentInfo();
        setupListeners();
    }

    // Ánh xạ các View
    private void initViews() {
        tvPayUsername = findViewById(R.id.tvPayUsername);
        tvPayProduct = findViewById(R.id.tvPayProduct);
        tvPayPrice = findViewById(R.id.tvPayPrice);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);

        btnPayment = findViewById(R.id.btnPayment);
        btnBack = findViewById(R.id.btnBack);

        rgPayment = findViewById(R.id.rgPayment);
    }

    // Hiển thị tên tài khoản
    private void displayUsername() {
        SharedPreferences prefs = getSharedPreferences(
                "MyPrefs",
                MODE_PRIVATE
        );

        username = prefs.getString("username", "");

        if (username == null || username.trim().isEmpty()) {
            tvPayUsername.setText(
                    "Thông tin tài khoản chưa được cập nhật"
            );
            username = "";
        } else {
            tvPayUsername.setText("Tài khoản: " + username);
        }
    }

    // Hiển thị thông tin thanh toán
    private void displayPaymentInfo() {
        String cartSummary = getIntent().getStringExtra("cartSummary");

        if (cartSummary != null) {
            isCartCheckout = true;
            displayCartPayment(cartSummary);
        } else {
            isCartCheckout = false;
            displaySingleProductPayment();
        }
    }

    // Hiển thị thông tin từ giỏ hàng
    @SuppressWarnings("unchecked")
    private void displayCartPayment(String cartSummary) {
        selectedCartItems = (ArrayList<HashMap<String, Object>>)
                getIntent().getSerializableExtra("selectedCartItems");

        paymentProductSummary = cartSummary;

        paymentTotal = getIntent().getLongExtra(
                "cartTotal",
                0
        );

        tvPayProduct.setText(cartSummary);
        tvPayPrice.setText(formatPrice(paymentTotal));

        if (selectedCartItems == null || selectedCartItems.isEmpty()) {
            btnPayment.setEnabled(false);
            btnPayment.setAlpha(0.5f);
        }
    }

    // Hiển thị thông tin sản phẩm mua ngay
    private void displaySingleProductPayment() {
        singleProduct = (Product) getIntent()
                .getSerializableExtra("product");

        if (singleProduct == null) {
            paymentProductSummary = "";
            paymentTotal = 0;

            tvPayProduct.setText(
                    "Không tìm thấy thông tin sản phẩm"
            );

            tvPayPrice.setText(formatPrice(0));

            btnPayment.setEnabled(false);
            btnPayment.setAlpha(0.5f);
            return;
        }

        paymentProductSummary =
                singleProduct.getName() + " x 1";

        paymentTotal = parsePrice(singleProduct.getPrice());

        tvPayProduct.setText(paymentProductSummary);
        tvPayPrice.setText(formatPrice(paymentTotal));
    }

    // Chuyển chuỗi giá thành số
    private long parsePrice(String price) {
        if (price == null || price.trim().isEmpty()) {
            return 0;
        }

        try {
            String number = price.replaceAll("[^0-9]", "");

            if (number.isEmpty()) {
                return 0;
            }

            return Long.parseLong(number);

        } catch (Exception e) {
            return 0;
        }
    }

    // Định dạng tiền Việt Nam
    private String formatPrice(long price) {
        NumberFormat formatter =
                NumberFormat.getIntegerInstance(
                        new Locale("vi", "VN")
                );

        return formatter.format(price) + " đ";
    }

    // Thiết lập sự kiện
    private void setupListeners() {
        btnPayment.setOnClickListener(v -> processPayment());

        btnBack.setOnClickListener(v -> finish());
    }

    // Kiểm tra thông tin trước khi đặt hàng
    private void processPayment() {
        if (isProcessing) {
            return;
        }

        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (paymentProductSummary.isEmpty() || paymentTotal <= 0) {
            showMessage("Thông tin đơn hàng không hợp lệ!");
            return;
        }

        if (name.isEmpty()) {
            edtName.setError("Vui lòng nhập họ tên");
            edtName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            edtPhone.requestFocus();
            return;
        }

        if (!phone.matches("[0-9]{10,11}")) {
            edtPhone.setError(
                    "Số điện thoại phải có 10 hoặc 11 chữ số"
            );
            edtPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            edtAddress.setError("Vui lòng nhập địa chỉ");
            edtAddress.requestFocus();
            return;
        }

        int selectedPayment = rgPayment.getCheckedRadioButtonId();

        if (selectedPayment == -1) {
            showMessage("Vui lòng chọn phương thức thanh toán!");
            return;
        }

        String paymentMethod;

        if (selectedPayment == R.id.rbCOD) {
            paymentMethod = "Thanh toán khi nhận hàng (COD)";
        } else {
            paymentMethod = "Chuyển khoản ngân hàng";
        }

        // Bắt đầu lưu đơn hàng
        saveOrder(name, phone, address, paymentMethod);
    }

    // Tạo dữ liệu sản phẩm mua ngay
    private HashMap<String, Object> createSingleProductData() {
        HashMap<String, Object> item = new HashMap<>();

        item.put("productId", singleProduct.getId());
        item.put("name", singleProduct.getName());
        item.put("image", singleProduct.getImage());
        item.put("price", singleProduct.getPrice());
        item.put("category", singleProduct.getCategory());
        item.put("condition", singleProduct.getCondition());
        item.put("quantity", 1);

        return item;
    }

    // Lưu đơn hàng và xóa sản phẩm đã chọn bằng một WriteBatch
    private void saveOrder(
            String name,
            String phone,
            String address,
            String paymentMethod
    ) {
        isProcessing = true;
        btnPayment.setEnabled(false);
        btnPayment.setText("ĐANG XỬ LÝ...");

        ArrayList<HashMap<String, Object>> orderProducts;

        if (isCartCheckout) {
            if (selectedCartItems == null || selectedCartItems.isEmpty()) {
                restorePaymentButton();
                showMessage("Không tìm thấy sản phẩm đã chọn!");
                return;
            }

            orderProducts = selectedCartItems;
        } else {
            orderProducts = new ArrayList<>();
            orderProducts.add(createSingleProductData());
        }

        // Tạo dữ liệu đơn hàng
        HashMap<String, Object> order = new HashMap<>();

        order.put("username", username);
        order.put("receiverName", name);
        order.put("phone", phone);
        order.put("address", address);
        order.put("paymentMethod", paymentMethod);
        order.put("products", orderProducts);
        order.put("productSummary", paymentProductSummary);
        order.put("total", paymentTotal);
        order.put("status", "Đang xử lý");
        order.put("createdAt", FieldValue.serverTimestamp());

        // Tạo mã đơn hàng tự động
        DocumentReference orderRef = db.collection("orders").document();

        // Tạo batch
        WriteBatch batch = db.batch();

        // Thêm đơn hàng vào Firestore
        batch.set(orderRef, order);

        // Nếu mua từ giỏ hàng, xóa đúng sản phẩm đã chọn
        if (isCartCheckout) {
            for (HashMap<String, Object> item : orderProducts) {
                Object productId = item.get("productId");

                if (productId == null
                        || productId.toString().trim().isEmpty()) {

                    restorePaymentButton();
                    showMessage("Thiếu mã sản phẩm trong giỏ hàng!");
                    return;
                }

                DocumentReference cartRef = db.collection("cart_items")
                        .document(productId.toString());

                batch.delete(cartRef);
            }
        }

        // Thực hiện lưu đơn hàng và xóa giỏ hàng
        batch.commit()
                .addOnSuccessListener(unused -> {
                    showMessage("Đặt hàng thành công!");

                    // Trở về trang chủ
                    Intent intent = new Intent(
                            PayActivity.this,
                            HomeActivity.class
                    );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    );

                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    restorePaymentButton();

                    showMessage(
                            "Không thể đặt hàng: " + e.getMessage()
                    );
                });
    }

    // Khôi phục nút nếu lưu đơn hàng thất bại
    private void restorePaymentButton() {
        isProcessing = false;
        btnPayment.setEnabled(true);
        btnPayment.setText("XÁC NHẬN ĐẶT HÀNG");
        btnPayment.setAlpha(1f);
    }

    // Hiển thị thông báo
    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
