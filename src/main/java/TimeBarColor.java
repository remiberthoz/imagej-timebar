import java.util.Arrays;
import java.util.List;
import java.awt.Color;

class TimeBarColor {

    protected static final List<TimeBarColor> COLORS = Arrays.asList(
        new TimeBarColor("Black", Color.black),
        new TimeBarColor("Blue", Color.blue),
        new TimeBarColor("(Light) Gray", Color.lightGray),
        new TimeBarColor("Gray", Color.gray),
        new TimeBarColor("(Dark) Gray", Color.darkGray),
        new TimeBarColor("Green", Color.green),
        new TimeBarColor("Red", Color.red),
        new TimeBarColor("White", Color.white),
        new TimeBarColor("Yellow", Color.yellow),
        new TimeBarColor("None", null)
    );

    protected static String[] getColorNames(boolean withNone) {
        int offset = withNone ? 0 : 1;
        String[] names = new String[COLORS.size() - offset];
        for (int i = 0; i < COLORS.size() - offset; ++i) {
            names[i] = COLORS.get(i).name;
        }
        return names;
    }

    protected static int getColorIndex(TimeBarColor color) {
        return COLORS.indexOf(color);
    }

    protected final String name;
    protected final Color color;

    public TimeBarColor(String name, Color color) {
        this.name = name;
        this.color = color;
    }        
}