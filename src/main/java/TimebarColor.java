import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class TimebarColor {

    protected static final List<TimebarColor> COLORS = Arrays.asList(
        new TimebarColor("Black", Color.black),
        new TimebarColor("Blue", Color.blue),
        new TimebarColor("(Light) Gray", Color.lightGray),
        new TimebarColor("Gray", Color.gray),
        new TimebarColor("(Dark) Gray", Color.darkGray),
        new TimebarColor("Green", Color.green),
        new TimebarColor("Red", Color.red),
        new TimebarColor("White", Color.white),
        new TimebarColor("Yellow", Color.yellow),
        new TimebarColor("None", null)
    );

    protected static String[] getColorNames(boolean withNone) {
        int offset = withNone ? 0 : 1;
        String[] names = new String[COLORS.size() - offset];
        for (int i = 0; i < COLORS.size() - offset; ++i) {
            names[i] = COLORS.get(i).name;
        }
        return names;
    }

    protected static int getColorIndex(TimebarColor color) {
        return COLORS.indexOf(color);
    }

    protected final String name;
    protected final Color color;

    public TimebarColor(String name, Color color) {
        this.name = name;
        this.color = color;
    }
    
}
