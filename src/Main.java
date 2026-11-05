import console.ConsoleUI;
import service.OrderService;

// точка входа: создаёт объекты и запускает программу
public class Main {

    public static void main(String[] args) {
        OrderService service = new OrderService();   
        ConsoleUI ui = new ConsoleUI(service);  
        ui.run();          // запускаем меню
    }
}