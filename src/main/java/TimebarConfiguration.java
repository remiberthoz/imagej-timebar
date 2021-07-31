// Timebar: A time-stamper plugin for ImageJ/Fiji
// Copyright (C) 2021 Rémi Berthoz

// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

public class TimebarConfiguration {
    
    // Define lists of choices in the menu.
    public static final String[] TIME_FORMATS = {"D-HH:mm:ss.SSS", "D-HH:mm:ss", "D-HH:mm", "HH:mm:ss.SSS", "HH:mm:ss", "HH:mm", "mm:ss.SSS", "mm:ss", "ss.SSS"};
    public static final String[] LOCATIONS = {"Upper Right", "Lower Right", "Lower Left", "Upper Left", "At Selection"};
    public static final int UPPER_RIGHT=0, LOWER_RIGHT=1, LOWER_LEFT=2, UPPER_LEFT=3, AT_SELECTION=4;
    public static final String[] FCOLORS = {"White","Black","Light Gray","Gray","Dark Gray","Red","Green","Blue","Yellow"};
    public static final String[] BCOLORS = {"None","Black","White","Dark Gray","Gray","Light Gray","Yellow","Blue","Green","Red"};

    public int barHeightInPixels;
    public String timeFormat;
    public String location;
    public String fcolor;
    public String bcolor;
    public boolean boldText;
    public boolean serifFont;
    public boolean hideBar;
    public boolean showUnits;
    public int fontSize;

    public TimebarConfiguration() {
        this.barHeightInPixels = 14;
        this.timeFormat = TIME_FORMATS[0];
        this.location = LOCATIONS[UPPER_RIGHT];
        this.fcolor = FCOLORS[0];
        this.bcolor = BCOLORS[0];
        this.boldText = true;
        this.serifFont = false;
        this.hideBar = false;
        this.showUnits = true;
        this.fontSize = 14;
    }

    public TimebarConfiguration(TimebarConfiguration model) {
        this.updateFrom(model);
    }

    public void updateFrom(TimebarConfiguration model) {
        this.barHeightInPixels = model.barHeightInPixels;
        this.timeFormat = model.timeFormat;
        this.location = model.location;
        this.fcolor = model.fcolor;
        this.bcolor = model.bcolor;
        this.boldText = model.boldText;
        this.serifFont = model.serifFont;
        this.hideBar = model.hideBar;
        this.showUnits = model.showUnits;
        this.fontSize = model.fontSize;
    }
}
