package console;

import delivery.DeliveryMethod;
import model.Courier;
import model.Order;
import model.OrderItem;
import model.OrderStatus;
import service.OrderService;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

// консольный интерфейс: показывает меню и читает ввод пользователя
public class ConsoleUI {

    private final OrderService service;                     // сервис, которому передаём все дела
    private final Scanner scanner = new Scanner(System.in); // читает ввод с клавиатуры

    public ConsoleUI(OrderService service) {
        this.service = service;
    }

    // главный цикл программы
    public void run() {
        boolean running = true;   // пока true, программа работает
        while (running) {
            printMenu();
            try {
                int choice = readInt("Ваш выбор: ");
                switch (choice) {
                    case 1 -> createOrder();
                    case 2 -> addItemsToOrder();
                    case 3 -> showOrders();
                    case 4 -> changeStatus();
                    case 5 -> chooseDelivery();
                    case 6 -> assignCourier();
                    case 0 -> {
                        running = false;   // выходим из цикла
                        System.out.println("Работа программы завершена. До свидания!");
                    }
                    default -> System.out.println("Такого пункта нет, выберите из списка");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                // ловим только наши ошибки и показываем понятное сообщение
                System.out.println("Ошибка: " + e.getMessage());
            } catch (NoSuchElementException e) {
                // ввод закончился (например, закрыли поток ввода): завершаем программу
                System.out.println("Ввод закончен, программа завершается");
                running = false;
            }
        }
    }

    // ---------- меню ----------

    private void printMenu() {
        System.out.println();
        System.out.println("===== Служба доставки =====");
        System.out.println("1. Создать заказ");
        System.out.println("2. Добавить позиции в заказ");
        System.out.println("3. Показать все заказы");
        System.out.println("4. Изменить статус заказа");
        System.out.println("5. Выбрать способ доставки");
        System.out.println("6. Назначить курьера");
        System.out.println("0. Выход");
    }

    // ---------- действия меню ----------

    // пункт 1: создать заказ и добавить позиции
    private void createOrder() {
        System.out.println("Создание заказа (в любой момент 0 - вернуться в главное меню)");
        Order order = null;   // пока заказа нет
        while (order == null) {
            String name = readText("Имя клиента: ");
            if (name == null) {
                printCancelled();
                return;
            }
            String phone = readText("Телефон клиента: ");
            if (phone == null) {
                printCancelled();
                return;
            }
            String address = readText("Адрес доставки: ");
            if (address == null) {
                printCancelled();
                return;
            }
            try {
                order = service.createOrder(name, phone, address);
            } catch (IllegalArgumentException e) {
                // данные плохие: показываем причину и просим ввести всё заново
                System.out.println("Ошибка: " + e.getMessage() + ". Введите данные заново");
            }
        }
        System.out.println("Создан заказ №" + order.getId());
        addItemsLoop(order);
    }

    // пункт 2: добавить позиции в уже созданный заказ
    private void addItemsToOrder() {
        int orderId = readInt("Номер заказа (0 - назад): ");
        if (orderId == 0) {
            return;
        }
        Order order = service.findOrder(orderId);
        order.checkCanAddItems();   // если нельзя, сразу скажет почему
        addItemsLoop(order);
    }

    // общий цикл добавления позиций (используется в пунктах 1 и 2)
    private void addItemsLoop(Order order) {
        System.out.println("Добавьте позиции (пустое название = закончить)");
        while (true) {
            System.out.print("Название товара: ");
            String product = scanner.nextLine().trim();
            if (product.isEmpty()) {
                break;   // пустая строка: выходим из цикла
            }
            int quantity = readInt("Количество: ");
            double price = readDouble("Цена за штуку: ");
            try {
                service.addItem(order.getId(), product, quantity, price);
                System.out.println("Позиция добавлена");
            } catch (IllegalArgumentException e) {
                // плохая позиция не ломает весь заказ, просто вводим следующую
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        printOrder(order);
    }

    // пункт 3: показать все заказы
    private void showOrders() {
        List<Order> orders = service.getOrders();
        if (orders.isEmpty()) {
            System.out.println("Заказов пока нет");
            return;   // выходим из метода досрочно
        }
        for (Order order : orders) {
            printOrder(order);
        }
    }

    // пункт 4: изменить статус заказа
    private void changeStatus() {
        int orderId = readInt("Номер заказа (0 - назад): ");
        if (orderId == 0) {
            return;
        }
        Order order = service.findOrder(orderId);

        // собираем только те статусы, в которые из текущего можно перейти
        List<OrderStatus> allowed = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {   // values() = все значения enum
            if (order.getStatus().canChangeTo(status)) {
                allowed.add(status);
            }
        }
        if (allowed.isEmpty()) {
            System.out.println("Заказ в статусе \"" + order.getStatus().getTitle()
                    + "\", дальше менять нельзя");
            return;
        }
        System.out.println("Текущий статус: " + order.getStatus().getTitle());
        System.out.println("Можно перевести в:");
        for (int i = 0; i < allowed.size(); i++) {
            System.out.println((i + 1) + ". " + allowed.get(i).getTitle());
        }
        System.out.println("0. Назад");
        int number = readChoice("Выберите статус: ", 0, allowed.size());
        if (number == 0) {
            printCancelled();
            return;
        }
        service.changeStatus(orderId, allowed.get(number - 1));   // -1 потому что список с нуля
        printOrder(order);
    }

    // пункт 5: выбрать способ доставки
    private void chooseDelivery() {
        int orderId = readInt("Номер заказа (0 - назад): ");
        if (orderId == 0) {
            return;
        }
        Order order = service.findOrder(orderId);
        order.checkCanChooseDelivery();   // если нельзя, сразу скажет почему
        List<DeliveryMethod> methods = service.getDeliveryMethods();

        // показываем все способы с рассчитанной стоимостью и сроком
        System.out.println("Способы доставки для заказа №" + orderId + ":");
        for (int i = 0; i < methods.size(); i++) {
            DeliveryMethod method = methods.get(i);
            double cost = method.calculateCost(order.getItemsTotal());   // каждый считает по-своему
            String note = "";
            if (!service.isDeliveryAvailable(order, method)) {
                note = "  [сейчас нет свободных курьеров]";
            }
            System.out.printf("%d. %s - %.2f руб., срок: %d дн.%s%n",
                    i + 1, method.getName(), cost, method.estimateDays(), note);
        }
        System.out.println("0. Назад");

        // спрашиваем, пока пользователь не выберет доступный способ или не выйдет
        while (true) {
            int number = readChoice("Выберите способ: ", 0, methods.size());
            if (number == 0) {
                printCancelled();
                return;
            }
            try {
                service.chooseDeliveryMethod(orderId, methods.get(number - 1));
                break;   // получилось, выходим из цикла
            } catch (IllegalStateException e) {
                // например, все курьеры заняты: показываем причину и просим выбрать другой
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        printOrder(order);
    }

    // пункт 6: назначить курьера
    private void assignCourier() {
        int orderId = readInt("Номер заказа (0 - назад): ");
        if (orderId == 0) {
            return;
        }
        Order order = service.findOrder(orderId);
        order.checkCanAssignCourier();   // если нельзя, сразу скажет почему

        List<Courier> free = service.getFreeCouriers();
        if (free.isEmpty()) {
            System.out.println("Сейчас все курьеры заняты. Подождите, пока какой-нибудь заказ "
                    + "перейдёт в статус \"Доставлен\", или выберите другой способ доставки "
                    + "(например, самовывоз)");
            return;
        }
        System.out.println("Свободные курьеры:");
        for (int i = 0; i < free.size(); i++) {
            System.out.println((i + 1) + ". " + free.get(i).getName() + " (" + free.get(i).getPhone() + ")");
        }
        System.out.println("0. Назад");
        int number = readChoice("Выберите курьера: ", 0, free.size());
        if (number == 0) {
            printCancelled();
            return;
        }
        service.assignCourier(orderId, free.get(number - 1).getId());
        printOrder(order);
    }

    // ---------- вывод ----------

    private void printCancelled() {
        System.out.println("Действие отменено, возвращаемся в главное меню");
    }

    // красиво печатает один заказ
    private void printOrder(Order order) {
        System.out.println("----------------------------------------");
        System.out.println("Заказ №" + order.getId() + " | статус: " + order.getStatus().getTitle());
        System.out.println("Клиент: " + order.getCustomer());
        System.out.println("Адрес: " + order.getAddress());

        System.out.println("Позиции:");
        if (order.getItems().isEmpty()) {
            System.out.println("  (позиций пока нет)");
        }
        for (OrderItem item : order.getItems()) {
            System.out.println("  - " + item);   // вызовется toString() позиции
        }
        System.out.printf("Сумма товаров: %.2f руб.%n", order.getItemsTotal());

        DeliveryMethod method = order.getDeliveryMethod();
        String courierText = "не назначен";
        if (method == null) {
            System.out.println("Доставка: не выбрана");
        } else {
            System.out.printf("Доставка: %s, %.2f руб., срок: %d дн.%n",
                    method.getName(), order.getDeliveryCost(), method.estimateDays());
            if (!method.requiresCourier()) {
                courierText = "не нужен";
            }
        }
        if (order.getCourier() != null) {
            courierText = order.getCourier().getName();
        }
        System.out.println("Курьер: " + courierText);
        System.out.printf("ИТОГО: %.2f руб.%n", order.getTotalCost());
        System.out.println("----------------------------------------");
    }

    // ---------- чтение ввода с проверкой ----------

    // читает непустую строку; если ввели 0, возвращает null (это значит "назад")
    private String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.equals("0")) {
                return null;
            }
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Строка не должна быть пустой, попробуйте ещё раз (0 - отмена)");
        }
    }

    // читает целое число, пока пользователь не введёт его правильно
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);   // пробуем превратить текст в число
            } catch (NumberFormatException e) {
                // если там буквы, parseInt упадёт, мы это ловим
                System.out.println("Нужно ввести целое число, попробуйте ещё раз");
            }
        }
    }

    // читает дробное число (можно писать и с запятой, и с точкой)
    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().replace(',', '.');   // запятую меняем на точку
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Нужно ввести число, например 199.90");
            }
        }
    }

    // читает число из диапазона [min; max]
    private int readChoice(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("Введите число от " + min + " до " + max);
        }
    }
}