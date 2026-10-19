package robot;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private final Terrain defaultTerrain = new Terrain();
    private final Map<Position, Robot> robots = new HashMap<>();
    private final Map<Position, DepositedObject> objects = new HashMap<>();

    public Object getAt(Position position) {
        requirePosition(position);
        Robot robot = robots.get(position);
        if (robot != null) {
            return robot;
        }
        DepositedObject object = objects.get(position);
        return object != null ? object : defaultTerrain;
    }

    public DepositedObject getDepositedObjectAt(Position position) {
        requirePosition(position);
        return objects.get(position);
    }

    public void placeElement(Position position, Object element) {
        requirePosition(position);
        if (element instanceof Robot) {
            Robot existing = robots.get(position);
            if (existing != null && existing != element) {
                throw new IllegalStateException("This position already contains a robot.");
            }
            robots.put(position, (Robot) element);
        } else if (element instanceof DepositedObject) {
            DepositedObject existing = objects.get(position);
            if (existing != null && existing != element) {
                throw new IllegalStateException("This position already contains an object.");
            }
            objects.put(position, (DepositedObject) element);
        } else {
            throw new IllegalArgumentException("Element must be a robot or a deposited object.");
        }
    }

    public void clearElement(Position position) {
        requirePosition(position);
        if (robots.containsKey(position)) {
            robots.remove(position);
        } else {
            objects.remove(position);
        }
    }

    public void removeDepositedObject(Position position) {
        requirePosition(position);
        objects.remove(position);
    }

    private void requirePosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Position must not be null.");
        }
    }
}
