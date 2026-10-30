package model;

// клиент службы доставки
public class Customer {

    // поля private и final: снаружи не изменить, после создания не меняются
    private final String name;
    private final String phone;

    // конструктор: если данные плохие, бросаем исключение и объект не создаётся
    public Customer(String name, String phone) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя клиента не может быть пустым");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Телефон клиента не может быть пустым");
        }
        if (!isValidPhone(phone.trim())) {
            throw new IllegalArgumentException("Телефон должен содержать от 5 до 15 цифр "
                    + "и может включать только символы + - ( ) и пробелы");
        }
        this.name = name.trim();     // trim() убирает пробелы по краям
        this.phone = phone.trim();
    }

    // проверка телефона: разрешённые символы и количество цифр
    private static boolean isValidPhone(String phone) {
        // в телефоне могут быть только цифры, плюс, минус, скобки и пробелы
        if (!phone.matches("[0-9+()\\- ]+")) {
            return false;
        }
        // считаем цифры: убираем всё, что не цифра, и смотрим длину
        int digits = phone.replaceAll("\\D", "").length();
        return digits >= 5 && digits <= 15;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    // toString() вызывается автоматически, когда объект печатают
    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}