package model;   // этот файл лежит в папке model

public enum OrderStatus {

    NEW("Новый"),
    CONFIRMED("Подтверждён"),
    IN_DELIVERY("В доставке"),
    DELIVERED("Доставлен"),
    CANCELLED("Отменён");

    private final String title;

    OrderStatus(String title) {
        this.title = title; 
    }

    public String getTitle() {
        return title;
    }

    public boolean canChangeTo(OrderStatus next) {

        return switch (this) {
            case NEW         -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED   -> next == IN_DELIVERY || next == CANCELLED;
            case IN_DELIVERY -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;  
        };
    }
}