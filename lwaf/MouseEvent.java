package lwaf;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class MouseEvent {
    public final vec2f start;
    public final int button;
    public final int modifier;
    public vec2f last, position;
    public boolean moved;

    private final List<MouseCallback> onDrag = new ArrayList<>(),
                                      onUp   = new ArrayList<>();

    MouseEvent(vec2f position, int button, int modifier) {
        this.start = position;
        this.last = position;
        this.position = position;
        this.button = button;
        this.modifier = modifier;
    }

    void move(vec2f position) {
        this.last = this.position;
        this.position = position;

        for (MouseCallback callback : onDrag) {
            callback.apply(position);
        }
    }

    void up(vec2f position) {
        for (MouseCallback callback : onUp) {
            callback.apply(position);
        }
    }

    public MouseEvent onDrag(MouseCallback callback) {
        onDrag.add(callback);
        return this;
    }

    public MouseEvent onUp(MouseCallback callback) {
        onUp.add(callback);
        return this;
    }

    @FunctionalInterface
    public interface MouseCallback {
        void apply(vec2f position);
    }
}
