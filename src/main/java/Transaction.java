import java.io.Serializable;
import java.time.LocalDate;

public final class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    public int id;
    public LocalDate date;
    public String type, category, description, paymentMethod;
    public double amount;

    public Transaction(int id, LocalDate date, String type, String category, String description, String paymentMethod, double amount) {
        this.id=id; this.date=date; this.type=type; this.category=category; this.description=description; this.paymentMethod=paymentMethod; this.amount=amount;
    }
}
