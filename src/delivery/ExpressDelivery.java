package delivery;

// экспресс-доставка: быстро, но дороже
public class ExpressDelivery implements DeliveryMethod {

    private static final double BASE_COST = 500;       // фиксированная часть цены
    private static final double PERCENT = 0.10;        // плюс 10% от суммы товаров
    private static final int DAYS = 1;                 // привезём за день

    @Override
    public String getName() {
        return "Экспресс-доставка";
    }

    @Override
    public double calculateCost(double itemsTotal) {
        // 500 рублей + 10% от суммы товаров
        return BASE_COST + itemsTotal * PERCENT;
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