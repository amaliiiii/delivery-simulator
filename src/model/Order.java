package model;

import delivery.DeliveryMethod;   // импорт: класс лежит в другом пакете
import java.util.ArrayList;
import java.util.List;

// заказ: главный объект программы
public class Order {

    private final int id;                      // номер заказа
    private final Customer customer;           // клиент (заказ содержит клиента)
    private final String address;              // адрес доставки
    private final List<OrderItem> items = new ArrayList<>();   // позиции заказа
    private DeliveryMethod deliveryMethod;     // способ доставки (пока null, не выбран)
    private Courier courier;                   // курьер (пока null, не назначен)
    private OrderStatus status = OrderStatus.NEW;   // новый заказ всегда в статусе NEW

    public Order(int id, Customer customer, String address) {
        if (customer == null) {
            throw new IllegalArgumentException("У заказа должен быть клиент");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Адрес доставки не может быть пустым");
        }
        this.id = id;
        this.customer = customer;
        this.address = address.trim();
    }

    // ---------- позиции ----------

    // проверка: можно ли сейчас добавлять позиции (если нет, бросает исключение с причиной)
    public void checkCanAddItems() {
        if (status != OrderStatus.NEW) {
            throw new IllegalStateException("Нельзя добавить позиции: заказ уже в статусе \""
                    + status.getTitle() + "\" (добавлять можно только в новый заказ)");
        }
    }

    // добавить позицию в заказ
    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Позиция не может быть пустой");
        }
        checkCanAddItems();
        items.add(item);
    }

    // сумма всех товаров без доставки
    public double getItemsTotal() {
        double sum = 0;                       // сюда копим сумму
        for (OrderItem item : items) {        // идём по каждой позиции
            sum += item.getTotal();           // sum = sum + стоимость позиции
        }
        return sum;
    }

    // ---------- способ доставки ----------

    // проверка: можно ли сейчас выбирать способ доставки
    public void checkCanChooseDelivery() {
        if (status == OrderStatus.NEW) {
            throw new IllegalStateException("Нельзя выбрать способ доставки: "
                    + "заказ ещё не подтверждён. Сначала подтвердите его");
        }
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Нельзя выбрать способ доставки: заказ уже в статусе \""
                    + status.getTitle() + "\"");
        }
    }

    // выбрать способ доставки
    public void chooseDeliveryMethod(DeliveryMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Способ доставки не выбран");
        }
        checkCanChooseDelivery();
        this.deliveryMethod = method;
        // если новому способу курьер не нужен, а он уже назначен, отпускаем его
        if (!method.requiresCourier() && courier != null) {
            courier.release();
            courier = null;
        }
    }

    // стоимость доставки (если способ не выбран, то 0)
    public double getDeliveryCost() {
        if (deliveryMethod == null) {
            return 0;
        }
        // тип доставки не проверяем: каждый способ считает сам (полиморфизм)
        return deliveryMethod.calculateCost(getItemsTotal());
    }

    // итоговая стоимость: товары + доставка
    public double getTotalCost() {
        return getItemsTotal() + getDeliveryCost();
    }

    // ---------- курьер ----------

    // проверка: можно ли сейчас назначать курьера (если нет, бросает исключение с причиной)
    public void checkCanAssignCourier() {
        if (status == OrderStatus.NEW) {
            throw new IllegalStateException("Нельзя назначить курьера: "
                    + "заказ ещё не подтверждён. Сначала подтвердите его");
        }
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Нельзя назначить курьера: заказ уже в статусе \""
                    + status.getTitle() + "\"");
        }
        if (deliveryMethod == null) {
            throw new IllegalStateException("Нельзя назначить курьера: "
                    + "не выбран способ доставки. Сначала выберите его");
        }
        // спрашиваем у способа доставки, нужен ли курьер (никаких if по типу)
        if (!deliveryMethod.requiresCourier()) {
            throw new IllegalStateException("Нельзя назначить курьера: для способа \""
                    + deliveryMethod.getName() + "\" курьер не нужен");
        }
    }

    // назначить курьера
    public void assignCourier(Courier newCourier) {
        if (newCourier == null) {
            throw new IllegalArgumentException("Курьер не выбран");
        }
        checkCanAssignCourier();
        newCourier.markBusy();        // если курьер занят, тут вылетит исключение
        if (courier != null) {
            courier.release();        // прошлого курьера освобождаем
        }
        courier = newCourier;
    }

    // ---------- статус ----------

    // перевести заказ в новый статус
    public void changeStatus(OrderStatus next) {
        if (next == null) {
            throw new IllegalArgumentException("Статус не выбран");
        }
        // сначала смотрим общую схему переходов из enum
        if (!status.canChangeTo(next)) {
            throw new IllegalStateException("Нельзя перевести заказ из статуса \""
                    + status.getTitle() + "\" в \"" + next.getTitle() + "\"");
        }
        // дальше проверяем условия для конкретных статусов
        if (next == OrderStatus.CONFIRMED && items.isEmpty()) {
            throw new IllegalStateException("Нельзя подтвердить заказ без позиций. "
                    + "Сначала добавьте хотя бы одну");
        }
        if (next == OrderStatus.IN_DELIVERY) {
            if (deliveryMethod == null) {
                throw new IllegalStateException("Нельзя отправить заказ в доставку: "
                        + "не выбран способ доставки");
            }
            if (deliveryMethod.requiresCourier() && courier == null) {
                throw new IllegalStateException("Нельзя отправить заказ в доставку: "
                        + "не назначен курьер");
            }
        }
        status = next;   // все проверки прошли, меняем статус
        // заказ закончился: освобождаем курьера, чтобы он мог взять другой
        if ((next == OrderStatus.DELIVERED || next == OrderStatus.CANCELLED) && courier != null) {
            courier.release();
        }
    }

    // ---------- геттеры: только чтение ----------

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getAddress() {
        return address;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);   // отдаём копию, чтобы снаружи список не сломали
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public Courier getCourier() {
        return courier;
    }

    public OrderStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Заказ №" + id + ", " + customer.getName() + ", " + status.getTitle();
    }
}