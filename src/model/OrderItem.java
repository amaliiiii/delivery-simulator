package model;

// одна позиция заказа: например, "Пицца, 2 шт., 450 руб."
public class OrderItem {

    private final String productName;   // название товара
    private final int quantity;         // количество (целое число)
    private final double price;         // цена за штуку (дробное число)

    public OrderItem(String productName, int quantity, double price) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Название товара не может быть пустым");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля");
        }
        // NaN и бесконечность тоже нельзя: parseDouble пропускает слова NaN и Infinity
        if (Double.isNaN(price) || Double.isInfinite(price)) {
            throw new IllegalArgumentException("Цена должна быть обычным числом");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }
        this.productName = productName.trim();
        this.quantity = quantity;
        this.price = price;
    }

    // стоимость позиции = количество * цена за штуку
    public double getTotal() {
        return quantity * price;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        // %s - строка, %d - целое, %.2f - дробное с двумя знаками
        return String.format("%s x%d по %.2f = %.2f",
                productName, quantity, price, getTotal());
    }
}