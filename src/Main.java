import console.ConsoleUI;
import service.OrderService;

// точка входа: только создаёт объекты и запускает программу
public class Main {

    public static void main(String[] args) {
        OrderService service = new OrderService();   // создаём "повара"
        ConsoleUI ui = new ConsoleUI(service);       // создаём "официанта" и даём ему повара
        ui.run();                                    // запускаем меню
    }
}