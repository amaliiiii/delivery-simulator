package model;

/**
 * Одна позиция заказа: например, "Пицца, 2 шт., 450 руб."
 */
public class OrderItem {

    private final String productName;
    private final int quantity;       // int: целое число
    private final double price;       // double: число с дробной частью

    public OrderItem(String productName, int quantity, double price) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Название товара не может быть пустым");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }
        this.productName = productName.trim();
        this.quantity = quantity;
        this.price = price;
    }

    /** Стоимость позиции = количество * цена за штуку. */
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
        // String.format подставляет значения в шаблон.
        // %s = строка, %d = целое, %.2f = число с 2 знаками после запятой.
        return String.format("%s x%d по %.2f = %.2f",
                productName, quantity, price, getTotal());
    }
}