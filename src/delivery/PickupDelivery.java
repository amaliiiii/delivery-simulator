package delivery;

// самовывоз: клиент сам забирает заказ
public class PickupDelivery implements DeliveryMethod {

    private static final int DAYS = 1;   // заказ соберут за день

    @Override
    public String getName() {
        return "Самовывоз";
    }

    @Override
    public double calculateCost(double itemsTotal) {
        return 0;   // за самовывоз платить не надо
    }

    @Override
    public int estimateDays() {
        return DAYS;
    }

    @Override
    public boolean requiresCourier() {
        return false;   // курьер не нужен
    }
}