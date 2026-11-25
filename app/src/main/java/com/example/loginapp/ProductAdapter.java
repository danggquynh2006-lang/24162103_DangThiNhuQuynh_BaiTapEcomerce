
package com.example.loginapp;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ProductAdapter extends ArrayAdapter<Product> {

    private final Activity context;
    private final Product[] products;

    public ProductAdapter(Activity context, Product[] products) {
        super(context, R.layout.item_product, products);

        this.context = context;
        this.products = products;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();

            rowView = inflater.inflate(
                    R.layout.item_product,
                    parent,
                    false
            );
        }

        ImageView imgProduct =
                rowView.findViewById(R.id.imgProduct);

        TextView tvProductName =
                rowView.findViewById(R.id.tvProductName);

        TextView tvProductDescription =
                rowView.findViewById(R.id.tvProductDescription);

        TextView tvProductCondition =
                rowView.findViewById(R.id.tvProductCondition);

        TextView tvProductPrice =
                rowView.findViewById(R.id.tvProductPrice);

        Product product = products[position];

        // Product image
        int imageId = context.getResources().getIdentifier(
                product.getImage(),
                "drawable",
                context.getPackageName()
        );

        if (imageId != 0) {
            imgProduct.setImageResource(imageId);
        } else {
            imgProduct.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }

        // Product name
        String name = product.getName();

        tvProductName.setText(
                name == null ? "" : name
        );

        // Short description
        String description = product.getShortDescription();

        if (description == null || description.trim().isEmpty()) {
            description = product.getDescription();
        }

        tvProductDescription.setText(
                description == null ? "" : description
        );

        // Product condition: NEW / USED
        String condition = product.getCondition();

        if (condition == null || condition.trim().isEmpty()) {

            tvProductCondition.setVisibility(View.GONE);

        } else {

            tvProductCondition.setVisibility(View.VISIBLE);

            if ("USED".equalsIgnoreCase(condition.trim())) {

                tvProductCondition.setText("USED");

                tvProductCondition.setTextColor(
                        android.graphics.Color.parseColor("#8A4B12")
                );

                tvProductCondition.setBackgroundResource(
                        R.drawable.bg_chip_selected
                );

                tvProductCondition.setBackgroundTintList(
                        ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#FCE8D5")
                        )
                );

            } else if ("NEW".equalsIgnoreCase(condition.trim())) {

                tvProductCondition.setText("NEW");

                tvProductCondition.setTextColor(
                        context.getColor(R.color.purple_primary)
                );

                tvProductCondition.setBackgroundResource(
                        R.drawable.bg_chip_selected
                );

                tvProductCondition.setBackgroundTintList(
                        ColorStateList.valueOf(
                                context.getColor(R.color.purple_light)
                        )
                );

            } else {
                tvProductCondition.setVisibility(View.GONE);
            }
        }

        // Product price
        tvProductPrice.setText(
                formatPrice(product.getPrice())
        );

        return rowView;
    }

    private String formatPrice(String price) {

        if (price == null || price.trim().isEmpty()) {
            return "Liên hệ";
        }

        try {
            long amount = Long.parseLong(price);

            DecimalFormatSymbols symbols =
                    new DecimalFormatSymbols(Locale.US);

            symbols.setGroupingSeparator('.');

            DecimalFormat formatter =
                    new DecimalFormat("#,###", symbols);

            return formatter.format(amount) + " VNĐ";

        } catch (NumberFormatException e) {
            return price;
        }
    }
}
