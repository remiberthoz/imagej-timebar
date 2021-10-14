import ij.gui.GenericDialog;

class TimeBarDialog extends GenericDialog {

    static final String[] checkboxLabels = {"Bold Text", "Hide bar", "Serif Font", "Overlay", "Show units"};
    boolean[] checkboxStates = new boolean[5];

    TimeBarDialog(TimeBarConfiguration config) {
        super("Scale Bar");

        addNumericField("Thickness in pixels: ", config.barThicknessInPixels, 0);
        addNumericField("Font size: ", config.fontSize, 0);
        addChoice("Color: ", TimeBarColor.getColorNames(false), config.color.name);
        addChoice("Background: ", TimeBarColor.getColorNames(true), config.bcolor.name);
        addChoice("Location: ", TimeBarLocation.getLocationNames(), config.location.name);
        addChoice("Time format: ", TimeBarTimeFormat.getTimeFormatDescs(), config.timeFormat.description);
        checkboxStates[0] = config.boldText; checkboxStates[1] = config.hideBar;
        checkboxStates[2] = config.serifFont; checkboxStates[3] = config.useOverlay;
        checkboxStates[4] = config.showUnits;
        setInsets(10, 25, 0);
        addCheckboxGroup(3, 2, checkboxLabels, checkboxStates);
    }
}