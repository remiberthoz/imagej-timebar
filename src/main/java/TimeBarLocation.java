import java.util.Arrays;
import java.util.List;

class TimeBarLocation {

    protected enum Locations { UPPER_RIGHT, LOWER_RIGHT, LOWER_LEFT, UPPER_LEFT, AT_SELECTION }

    protected static final List<TimeBarLocation> LOCATIONS = Arrays.asList(
        new TimeBarLocation("Upper Right", Locations.UPPER_RIGHT),
        new TimeBarLocation("Lower Right", Locations.LOWER_RIGHT),
        new TimeBarLocation("Lower Left", Locations.LOWER_LEFT),
        new TimeBarLocation("Upper Left", Locations.UPPER_LEFT),
        new TimeBarLocation("At Selection", Locations.AT_SELECTION)
    );

    protected static String[] getLocationNames() {
        String[] names = new String[LOCATIONS.size()];
        for (int i = 0; i < LOCATIONS.size(); ++i) {
            names[i] = LOCATIONS.get(i).name;
        }
        return names;
    }

    protected final String name;
    protected final Locations location;

    public TimeBarLocation(String name, Locations location) {
        this.name = name;
        this.location = location;
    }
}