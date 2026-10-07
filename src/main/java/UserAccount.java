import java.io.Serializable;

public final class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int id;
    public final String username;
    public final String passwordHash;
    public double monthlyTarget;

    public UserAccount(int id, String username, String passwordHash) {
        this(id, username, passwordHash, 0.0);
    }

    public UserAccount(int id, String username, String passwordHash, double monthlyTarget) {
        this.id=id; this.username=username; this.passwordHash=passwordHash; this.monthlyTarget=monthlyTarget;
    }
}
