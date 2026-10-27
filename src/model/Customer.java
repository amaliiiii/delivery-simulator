package model;

/**
 * Клиент службы доставки.
 */
public class Customer {

    // Поля private: снаружи напрямую не изменить.
    // final: после создания не меняются (клиент не "переименовывается").
    private final String name;
    private final String phone;

    /**
     * Конструктор: вызывается при new Customer(...).
     * Если данные некорректны, бросаем исключение и объект НЕ создаётся.
     */
    public Customer(String name, String phone) {
        // name == null: строка вообще не передана.
        // name.isBlank(): строка пустая или состоит из одних пробелов.
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя клиента не может быть пустым");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Телефон клиента не может быть пустым");
        }
        this.name = name.trim();     // trim() убирает пробелы по краям
        this.phone = phone.trim();
    }

    // Геттеры: только читать, сеттеров нет, потому что менять нельзя.
    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    /**
     * toString() вызывается автоматически, когда объект печатают
     * (System.out.println(customer)). Без него выйдет что-то вроде Customer@1a2b3c.
     */
    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}