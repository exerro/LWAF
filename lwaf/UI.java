package lwaf;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class UI {
    private vec2f position;
    private vec3f colour;

    protected UI(vec2f position, vec3f colour) {
        this.position = position;
        this.colour = colour;
    }

    public vec2f getPosition() {
        return position;
    }

    public vec3f getColour() {
        return colour;
    }

    public void moveTo(vec2f position) {
        this.position = position;
    }

    public void moveBy(vec2f direction) {
        this.position = position.add(direction);
    }

    public void setColour(vec3f colour) {
        this.colour = colour;
    }

    abstract void draw();

    void update(float dt) {

    }
}
