package delivery;

// общий контракт для любого способа доставки
public interface DeliveryMethod {

    // название способа, чтобы показать пользователю
    String getName();

    // сколько стоит доставка, если товары в заказе стоят itemsTotal
    double calculateCost(double itemsTotal);

    // за сколько дней доставим
    int estimateDays();

    // нужен ли курьер при этом способе
    boolean requiresCourier();
}