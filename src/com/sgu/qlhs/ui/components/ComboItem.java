package com.sgu.qlhs.ui.components; // hoặc com.sgu.qlhs.ui.components, tùy nơi anh muốn đặt

/**
 * Dùng để hiển thị dữ liệu (value + label) trong JComboBox.
 */
public class ComboItem {
    private int value;      // giá trị (ID)
    private String label;   // tên hiển thị

    public ComboItem(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label; // hiển thị tên trong combobox
    }
}
