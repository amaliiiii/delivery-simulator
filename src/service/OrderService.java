package service;

import delivery.DeliveryMethod;
import delivery.ExpressDelivery;
import delivery.PickupDelivery;
import delivery.StandardDelivery;
import model.Courier;
import model.Customer;
import model.Order;
import model.OrderItem;
import model.OrderStatus;
import java.util.ArrayList;
import java.util.List;

// сервис: хранит заказы и курьеров и выполняет операции над ними
public class OrderService {

    private final List<Order> orders = new ArrayList<>();                    // все заказы
    private final List<Courier> couriers = new ArrayList<>();                // все курьеры
    private final List<DeliveryMethod> deliveryMethods = new ArrayList<>();  // доступные способы доставки
    private int nextOrderId = 1;                                             // номер следующего заказа

    public OrderService() {
        // заранее заводим курьеров, чтобы было кого назначать
        couriers.add(new Courier(1, "Иван Петров", "+7 900 111-11-11"));
        couriers.add(new Courier(2, "Анна Смирнова", "+7 900 222-22-22"));
        couriers.add(new Courier(3, "Олег Кузнецов", "+7 900 333-33-33"));
        // заводим способы доставки (тип списка - интерфейс, внутри разные классы)
        deliveryMethods.add(new StandardDelivery());
        deliveryMethods.add(new ExpressDelivery());
        deliveryMethods.add(new PickupDelivery());
    }

    // ---------- заказы ----------

    // создать новый заказ
    public Order createOrder(String customerName, String customerPhone, String address) {
        Customer customer = new Customer(customerName, customerPhone);   // тут проверятся имя и телефон
        Order order = new Order(nextOrderId, customer, address);         // тут проверится адрес
        nextOrderId++;           // увеличиваем номер только если заказ создался без ошибок
        orders.add(order);
        return order;
    }

    // добавить позицию в заказ
    public void addItem(int orderId, String productName, int quantity, double price) {
        Order order = findOrder(orderId);
        OrderItem item = new OrderItem(productName, quantity, price);
        order.addItem(item);
    }

    // изменить статус заказа
    public void changeStatus(int orderId, OrderStatus next) {
        findOrder(orderId).changeStatus(next);
    }

    // найти заказ по номеру
    public Order findOrder(int orderId) {
        for (Order order : orders) {
            if (order.getId() == orderId) {
                return order;   // нашли, сразу возвращаем
            }
        }
        // дошли до конца и не нашли
        throw new IllegalArgumentException("Заказ с номером " + orderId + " не найден");
    }

    // список заказов (копия, чтобы снаружи его не испортили)
    public List<Order> getOrders() {
        return List.copyOf(orders);
    }

    // ---------- доставка ----------

    // способы доставки (копия списка)
    public List<DeliveryMethod> getDeliveryMethods() {
        return List.copyOf(deliveryMethods);
    }

    // можно ли выбрать этот способ для заказа прямо сейчас
    public boolean isDeliveryAvailable(Order order, DeliveryMethod method) {
        // способу не нужен курьер (самовывоз): всегда можно
        if (!method.requiresCourier()) {
            return true;
        }
        // у заказа уже есть свой курьер: тоже можно
        if (order.getCourier() != null) {
            return true;
        }
        // иначе нужен хотя бы один свободный курьер
        return hasFreeCourier();
    }

    // выбрать способ доставки для заказа
    public void chooseDeliveryMethod(int orderId, DeliveryMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Способ доставки не выбран");
        }
        Order order = findOrder(orderId);
        order.checkCanChooseDelivery();   // сначала проверяем статус заказа
        if (!isDeliveryAvailable(order, method)) {
            throw new IllegalStateException("Сейчас все курьеры заняты. Подождите, пока какой-нибудь "
                    + "заказ перейдёт в статус \"Доставлен\", или выберите другой способ доставки "
                    + "(например, самовывоз)");
        }
        order.chooseDeliveryMethod(method);
    }

    // ---------- курьеры ----------

    // есть ли хотя бы один свободный курьер
    public boolean hasFreeCourier() {
        return !getFreeCouriers().isEmpty();
    }

    // только свободные курьеры
    public List<Courier> getFreeCouriers() {
        List<Courier> free = new ArrayList<>();
        for (Courier courier : couriers) {
            if (!courier.isBusy()) {
                free.add(courier);
            }
        }
        return free;
    }

    // назначить курьера на заказ
    public void assignCourier(int orderId, int courierId) {
        Order order = findOrder(orderId);
        order.checkCanAssignCourier();   // сначала проверяем, можно ли вообще назначать
        Courier courier = findCourier(courierId);
        order.assignCourier(courier);
    }

    // найти курьера по номеру
    public Courier findCourier(int courierId) {
        for (Courier courier : couriers) {
            if (courier.getId() == courierId) {
                return courier;
            }
        }
        throw new IllegalArgumentException("Курьер с номером " + courierId + " не найден");
    }

    public List<Courier> getCouriers() {
        return List.copyOf(couriers);
    }
}