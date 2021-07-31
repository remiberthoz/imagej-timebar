import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class TimebarTimeFormat {

    protected static final List<TimebarTimeFormat> TIME_FORMATS = Arrays.asList(
        new TimebarTimeFormat("D-HH:mm:ss.SSS", "d-HH:mm:ss.SSS", "d'd'HH'h'mm'm'ss'.'SSS's'"),
        new TimebarTimeFormat("D-HH:mm:ss"    , "d-HH:mm:ss"    , "d'd'HH'h'mm'm'ss's'"      ),
        new TimebarTimeFormat("D-HH:mm"       , "d-HH:mm"       , "d'd'HH'h'mm'm'"           ),
        new TimebarTimeFormat(  "HH:mm:ss.SSS",   "HH:mm:ss.SSS",     "HH'h'mm'm'ss'.'SSS's'"),
        new TimebarTimeFormat(  "HH:mm:ss"    ,   "HH:mm:ss"    ,     "HH'h'mm'm'ss's'"      ),
        new TimebarTimeFormat(  "HH:mm"       ,   "HH:mm"       ,     "HH'h'mm'm'"           ),
        new TimebarTimeFormat(     "mm:ss.SSS",      "mm:ss.SSS",          "mm'm'ss'.'SSS's'"),
        new TimebarTimeFormat(     "mm:ss"    ,      "mm:ss"    ,          "mm'm'ss's'"      ),
        new TimebarTimeFormat(        "ss.SSS",         "ss.SSS",               "ss'.'SSS's'")
    );

    protected static String[] getTimeFormatDescs() {
        String[] descriptions = new String[TIME_FORMATS.size()];
        for (int i = 0; i < TIME_FORMATS.size(); ++i) {
            descriptions[i] = TIME_FORMATS.get(i).description;
        }
        return descriptions;
    }

    protected static int getTimeFormatIndex(TimebarTimeFormat tf) {
        return TIME_FORMATS.indexOf(tf);
    }
    
    protected final String description;
    private String witoutUnitsFormat;
    private String withUnitsFormat;

    public TimebarTimeFormat(String description, String witoutUnitsFormat, String withUnitsFormat) {
        this.description = description;
        this.witoutUnitsFormat = witoutUnitsFormat;
        this.withUnitsFormat = withUnitsFormat;
    }

    @Override
    public String toString() {
        return description;
    }

    public String formatMillis(long timeInMillis, boolean withUnits) {
        String f = withUnits ? withUnitsFormat : witoutUnitsFormat;
        return DurationFormatUtils.formatDuration(timeInMillis, f);
    }
}
