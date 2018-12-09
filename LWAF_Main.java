
import lwaf.Display;

public class LWAF_Main {
    public static void main(String[] args) throws Display.WindowCreationError {
        Display display = new Display("LWAF Demo");

        display.setup();

        do {
            display.beginRenderFrame();
            display.finishRenderFrame();
            display.pollEvents();

        } while (!display.windowShouldClose());

        display.destroy();
    }
}
