import ij.gui.GenericDialog;
import ij.gui.MultiLineLabel;
import ij.gui.DialogListener;
import java.awt.AWTEvent;

class TimeBarDialogListener implements DialogListener {

    TimeBarConfiguration config;
    TimeBar_ plugin;

    public TimeBarDialogListener(TimeBarConfiguration config, TimeBar_ plugin) {
        super();
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        config.frameOffset = gd.getNextNumber();
        config.usePredefinedTimestamps = gd.getNextBoolean();
        config.predefinedTimestamps = gd.getNextString();
        config.barThicknessInPixels = (int) gd.getNextNumber();
        config.fontSize = (int) gd.getNextNumber();
        config.color = TimeBarColor.COLORS.get(gd.getNextChoiceIndex());
        config.bcolor = TimeBarColor.COLORS.get(gd.getNextChoiceIndex());
        config.location = TimeBarLocation.LOCATIONS.get(gd.getNextChoiceIndex());
        config.timeFormat = TimeBarTimeFormat.TIME_FORMATS.get(gd.getNextChoiceIndex());
        config.boldText = gd.getNextBoolean();
        config.hideBar = gd.getNextBoolean();
        config.serifFont = gd.getNextBoolean();
        config.useOverlay = gd.getNextBoolean();
        config.showUnits = gd.getNextBoolean();

        if (config.usePredefinedTimestamps) {
            String[] timestamps = config.predefinedTimestamps.split(",");
            if (timestamps.length != plugin.nFrames) {
                ((MultiLineLabel) gd.getMessage()).setText(
                    "First frame: INVALID PRE-DEFINED\n" +
                    "Last frame: INVALID PRE-DEFINED");
            }
        }

        ((MultiLineLabel) gd.getMessage()).setText(
            "First frame: " + plugin.getTimeLabel(1) + "\n" +
            "Last frame: " + plugin.getTimeLabel(plugin.nFrames));

        plugin.updateTimeBar(true);

        return true;
    }
}
