class TimeBarConfiguration {

    private static int defaultBarHeight = 4;

    boolean showHorizontal;
    int barThicknessInPixels;
    TimeBarLocation location;
    TimeBarColor color;
    TimeBarColor bcolor;
    TimeBarTimeFormat timeFormat;
    boolean boldText;
    boolean hideBar;
    boolean serifFont;
    boolean useOverlay;
    boolean showUnits;
    int fontSize;

    TimeBarConfiguration() {
        this.showHorizontal = true;
        this.barThicknessInPixels = defaultBarHeight;
        this.location = TimeBarLocation.LOCATIONS.get(0);  // TODO: Default value should not depend on the values are defined.
        this.color = TimeBarColor.COLORS.get(7);
        this.bcolor = TimeBarColor.COLORS.get(9);
        this.timeFormat = TimeBarTimeFormat.TIME_FORMATS.get(0);
        this.boldText = true;
        this.hideBar = false;
        this.serifFont = false;
        this.useOverlay = true;
        this.showUnits = true;
        this.fontSize = 14;
    }

    TimeBarConfiguration(TimeBarConfiguration model) {
        this.updateFrom(model);
    }
    
    void updateFrom(TimeBarConfiguration model) {
        this.showHorizontal = model.showHorizontal;
        this.barThicknessInPixels = model.barThicknessInPixels;
        this.location = model.location;
        this.color = model.color;
        this.bcolor = model.bcolor;
        this.timeFormat = model.timeFormat;
        this.boldText = model.boldText;
        this.serifFont = model.serifFont;
        this.hideBar = model.hideBar;
        this.useOverlay = model.useOverlay;
        this.showUnits = model.showUnits;
        this.fontSize = model.fontSize;
    }   
}
