package robot;

public class DepositedObject {
    private final String name;

    public DepositedObject(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Object name must not be blank.");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
