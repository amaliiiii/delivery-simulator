package model;

import delivery.DeliveryMethod;   // импорт: класс лежит в другом пакете
import java.util.ArrayList;
import java.util.List;

// заказ: главный объект программы
public class Order {

    private final int id;                      // номер заказа
    private final Customer customer;           // клиент (композиция: заказ содержит клиента)
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

    // добавить позицию в заказ
    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Позиция не может быть пустой");
        }
        // менять состав можно только пока заказ новый
        if (status != OrderStatus.NEW) {
            throw new IllegalStateException("Добавлять позиции можно только в новый заказ");
        }
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

    // выбрать способ доставки
    public void chooseDeliveryMethod(DeliveryMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Способ доставки не выбран");
        }
        // менять доставку можно, пока заказ не уехал
        if (status != OrderStatus.NEW && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Сейчас менять способ доставки нельзя");
        }
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
        // не проверяем тип доставки: каждый способ считает сам (полиморфизм)
        return deliveryMethod.calculateCost(getItemsTotal());
    }

    // итоговая стоимость: товары + доставка
    public double getTotalCost() {
        return getItemsTotal() + getDeliveryCost();
    }

    // назначить курьера
    public void assignCourier(Courier newCourier) {
        if (newCourier == null) {
            throw new IllegalArgumentException("Курьер не выбран");
        }
        if (deliveryMethod == null) {
            throw new IllegalStateException("Сначала выберите способ доставки");
        }
        // спрашиваем у способа доставки, нужен ли курьер (никаких if по типу)
        if (!deliveryMethod.requiresCourier()) {
            throw new IllegalStateException("Для способа \"" + deliveryMethod.getName()
                    + "\" курьер не нужен");
        }
        if (status != OrderStatus.NEW && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Сейчас менять курьера нельзя");
        }
        newCourier.markBusy();        // если курьер занят, тут вылетит исключение
        if (courier != null) {
            courier.release();        // прошлого курьера освобождаем
        }
        courier = newCourier;
    }

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
        if (next == OrderStatus.CONFIRMED) {
            if (items.isEmpty()) {
                throw new IllegalStateException("Нельзя подтвердить заказ без позиций");
            }
            if (deliveryMethod == null) {
                throw new IllegalStateException("Нельзя подтвердить заказ без способа доставки");
            }
        }
        if (next == OrderStatus.IN_DELIVERY) {
            if (deliveryMethod.requiresCourier() && courier == null) {
                throw new IllegalStateException("Нельзя отправить заказ без курьера");
            }
        }
        status = next;   // все проверки прошли, меняем статус
        // заказ закончился: освобождаем курьера, чтобы он мог взять другой
        if ((next == OrderStatus.DELIVERED || next == OrderStatus.CANCELLED) && courier != null) {
            courier.release();
        }
    }

    // геттеры: только чтение
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