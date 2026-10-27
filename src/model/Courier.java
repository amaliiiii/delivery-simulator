package model;

// курьер службы доставки
public class Courier {

    private final int id;          // номер курьера, чтобы выбирать его в меню
    private final String name;     // имя
    private final String phone;    // телефон
    private boolean busy;          // занят ли сейчас (не final, потому что меняется)

    public Courier(int id, String name, String phone) {
        // проверяем данные, как и у клиента
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя курьера не может быть пустым");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Телефон курьера не может быть пустым");
        }
        this.id = id;
        this.name = name.trim();
        this.phone = phone.trim();
        this.busy = false;   // новый курьер свободен
    }

    // пометить курьера занятым (метод без public: виден только внутри пакета model)
    void markBusy() {
        if (busy) {
            throw new IllegalStateException("Курьер " + name + " уже занят");
        }
        busy = true;
    }

    // освободить курьера (тоже виден только внутри пакета model)
    void release() {
        busy = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isBusy() {
        return busy;
    }

    @Override
    public String toString() {
        // тернарный оператор: условие ? если_да : если_нет
        String state = busy ? "занят" : "свободен";
        return id + ". " + name + " (" + phone + ") - " + state;
    }
}