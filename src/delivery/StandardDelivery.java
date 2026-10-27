package delivery;

// стандартная доставка курьером
public class StandardDelivery implements DeliveryMethod {

    // static final = константа: одна на весь класс, менять нельзя
    private static final double BASE_COST = 200;       // обычная цена доставки
    private static final double FREE_FROM = 3000;      // от этой суммы доставка бесплатна
    private static final int DAYS = 3;                 // срок в днях

    @Override
    public String getName() {
        return "Стандартная доставка курьером";
    }

    @Override
    public double calculateCost(double itemsTotal) {
        // если заказ дорогой, доставка бесплатная
        if (itemsTotal >= FREE_FROM) {
            return 0;
        }
        return BASE_COST;
    }

    @Override
    public int estimateDays() {
        return DAYS;
    }

    @Override
    public boolean requiresCourier() {
        return true;   // курьер нужен
    }
}