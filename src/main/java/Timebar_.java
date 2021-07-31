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

import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;

public class Timebar_ implements PlugIn {

    // Define user config:
    // The static will be kept accross plugin instances, it provides
    // default values for the non-static below. User choices are made static
    // only when the dialog is closed with validation (not when it is
    // canceled).
    private static final TimebarConfiguration sConfiguration = new TimebarConfiguration();
    private TimebarConfiguration configuration = new TimebarConfiguration(sConfiguration);

    private static final String TIME_BAR = "|TIME_BAR|";
    private int labelWidthInPixels;

    // ImagePlus currently open in ImageJ, that we are working on.
    private ImagePlus imp;
    // ImagePlus dimensions (width, height, bit depth, frames, slices, channels).
    private int imWidth;
    private int imHeight;
    private int imFrames;
    private int imSlices;
    private int imChannels;  
    // Frame, slice and channel for the image currently shown in the GUI.
    private int currentFrame;
    private int currentSlice;
    private int currentChannel;

    private int xloc;
    private int yloc;
    private int roiX=-1;
    private int roiY;

    public void run(String arg) {
        getCurrentImage();
        boolean roiExists = getCurrentROI();

        boolean wasOKed = askUserConfiguration(roiExists);
        if (!wasOKed) {
            removeTimebar();
            return;
        }
        persistConfiguration();
        updateTimebar(true);
    }
    

    private void getCurrentImage() {
        imp = IJ.getImage();
        currentFrame = imp.getFrame();
        currentSlice = imp.getSlice();
        currentChannel = imp.getChannel();
        imWidth = imp.getWidth();
        imHeight = imp.getHeight();
        imFrames = imp.getNFrames();
        imSlices = imp.getNSlices();
        imChannels = imp.getNChannels();
    }

    private boolean getCurrentROI() {
        Roi roi = imp.getRoi();
        if (roi == null) {
            return false;
        }
        Rectangle r = roi.getBounds();
        roiX = r.x;
        roiY = r.y;
        return true;
    }


    private boolean askUserConfiguration(boolean roiExists) {

        // Reset the timebar location if it is set to AT_SELECTION but there is no ROI.
        if (!roiExists && configuration.location == TimebarConfiguration.LOCATIONS[TimebarConfiguration.AT_SELECTION]) {
            configuration.location = TimebarConfiguration.LOCATIONS[0];
        }

        TimebarDialogOnUpdateCallback onUpdateCallback = new TimebarDialogOnUpdateCallback() {public void onDialogUpdated() {updateTimebar(false);}};
        TimebarDialog dialog = new TimebarDialog(configuration, onUpdateCallback);
        updateTimebar(false);  // Draw the preview timebar.
        dialog.showDialog();
        return dialog.wasOKed();
    }

    private void persistConfiguration() {
        sConfiguration.updateFrom(configuration);
    }


    private void updateTimebar(boolean allSlices) {
        updateFont();
        measureLabelWidth();
        updateLocation();
        createOverlay(imp, allSlices);
    }

    private String getTimeLabel(int frame) {
        String calibrationTimeUnit = imp.getCalibration().getTimeUnit();
        long calibrationTimeInterval = (long) imp.getCalibration().frameInterval;

        long factor;
        switch(calibrationTimeUnit) {
            case "ms":
            case "milisecond":
            case "miliseconds":
                factor = 1l;
                break;
            case "s":
            case "sec":
            case "second":
            case "seconds":
                factor = 1000 * 1l;
                break;
            case "m":
            case "min":
            case "minute":
            case "minutes":
                factor = 1000 * 60 * 1l;
                break;
            case "h":
            case "hr":
            case "hrs":
            case "hour":
            case "hours":
                factor = 1000 * 60 * 60 * 1l;
                break;
            default:
                factor = 1l;
                IJ.log("Timebar plugin: Unknown time unit."); // TODO: Actually raise and handle warning. Check resulting behaviour and tell user.
                break;
        }

        long time = (frame-1) * calibrationTimeInterval * factor;
        return configuration.timeFormat.formatMillis(time, configuration.showUnits);
    }

    private void measureLabelWidth() {
        int max = -1;
        ImageProcessor ip = imp.getProcessor();
        ip.setAntialiasedText(true);
        for (int f = 1; f <= imFrames; ++f) {
            String timeLabel = getTimeLabel(f);
            int swidth = ip.getStringWidth(timeLabel);
            if (swidth > max) {
                max = swidth;
            }
        }
        labelWidthInPixels = max;
    }

    private void createOverlay(ImagePlus imp, boolean allSlices) {
        Overlay overlay = imp.getOverlay();
        if (overlay == null) {
            // Make sure we have an overlay
            overlay = new Overlay();
        } else {
            // And clean it from stuff we created
            removeTimebar();
        }

        // Get positions
        int x = xloc;
        int y = yloc;

        // Get colors
        Color fcolor = getFColor();
        Color bcolor = getBColor();

        // Set font
        int fontType = configuration.boldText ? 1 : 0;
        String face = configuration.serifFont ? "Serif" : "SansSerif";
        Font font = new Font(face, fontType, configuration.fontSize);
        ImageProcessor ip = imp.getProcessor();
        ip.setFont(font);

        // Draw background if needed
        int yoffset = configuration.barHeightInPixels*(configuration.hideBar?0:1) + (configuration.fontSize + configuration.fontSize/4);
        if (bcolor != null) {
            int w = labelWidthInPixels;
            int h = yoffset;
            if (w < labelWidthInPixels) {
                w = labelWidthInPixels;
            }
            int margin = (w/20)<2 ? 2 : (w/20);
            int x2 = x - margin;
            int y2 = y - margin;
            w += margin * 2;
            h += margin * 2;
            Roi background = new Roi(x2, y2, w, h);
            background.setFillColor(bcolor);
            overlay.add(background, TIME_BAR);
        }

        if (allSlices) {
            // Draw labels
            for (int c = 1; c <= imChannels; ++c) {
                for (int s = 1; s <= imSlices; ++s) {
                    for (int f = 1; f <= imFrames; ++f) {
                        String timeLabel = getTimeLabel(f);
                        if (!configuration.hideBar) {
                            Roi bar = new Roi(x, y, labelWidthInPixels*(f-1)/(imFrames-1), configuration.barHeightInPixels);
                            bar.setFillColor(fcolor);
                            bar.setPosition(c, s, f);
                            bar.setPosition(imp.getStackIndex(c, s, f));
                            overlay.add(bar, TIME_BAR);
                        }

                        TextRoi text = new TextRoi(x, y + configuration.barHeightInPixels*(configuration.hideBar?0:1), timeLabel, font);
                        text.setStrokeColor(fcolor);
                        text.setPosition(c, s, f);
                        text.setPosition(imp.getStackIndex(c, s, f));
                        overlay.add(text, TIME_BAR);
                    }
                }
            }
        } else {
            String timeLabel = getTimeLabel(currentFrame);
            if (!configuration.hideBar) {
                Roi bar = new Roi(x, y, labelWidthInPixels*(currentFrame-1)/(imFrames-1), configuration.barHeightInPixels);
                bar.setFillColor(fcolor);
                bar.setPosition(currentChannel, currentSlice, currentFrame);
                bar.setPosition(imp.getStackIndex(currentChannel, currentSlice, currentFrame));
                overlay.add(bar, TIME_BAR);
            }

            TextRoi text = new TextRoi(x, y + configuration.barHeightInPixels*(configuration.hideBar?0:1), timeLabel, font);
            text.setStrokeColor(fcolor);
            text.setPosition(currentChannel, currentSlice, currentFrame);
            text.setPosition(imp.getStackIndex(currentChannel, currentSlice, currentFrame));
            overlay.add(text, TIME_BAR);
        }

        imp.setOverlay(overlay);
    }

    private boolean updateLocation() {
        int margin = (imWidth + imHeight) / 100;

        int x;
        int y;
        if (configuration.location.equals(TimebarConfiguration.LOCATIONS[TimebarConfiguration.UPPER_RIGHT])) {
            x = imWidth - margin - labelWidthInPixels;
            y = margin;
        } else if (configuration.location.equals(TimebarConfiguration.LOCATIONS[TimebarConfiguration.LOWER_RIGHT])) {
            x = imWidth - margin - labelWidthInPixels;
            y = imHeight - margin - configuration.barHeightInPixels*(configuration.hideBar?0:1) - configuration.fontSize;
        } else if (configuration.location.equals(TimebarConfiguration.LOCATIONS[TimebarConfiguration.UPPER_LEFT])) {
            x = margin;
            y = margin;
        } else if (configuration.location.equals(TimebarConfiguration.LOCATIONS[TimebarConfiguration.LOWER_LEFT])) {
            x = margin;
            y = imHeight - margin - configuration.barHeightInPixels*(configuration.hideBar?0:1) - configuration.fontSize;
        } else {
            if (roiX==-1) {
                return false;
            }
            x = roiX;
            y = roiY;
        }

        xloc = x;
        yloc = y;
        return true;
    }

    private Color getFColor() {
        Color c = Color.black;
        if (configuration.fcolor.equals(TimebarConfiguration.FCOLORS[0])) c = Color.white;
        else if (configuration.fcolor.equals(TimebarConfiguration.FCOLORS[2])) c = Color.lightGray;
        else if (configuration.fcolor.equals(TimebarConfiguration.FCOLORS[3])) c = Color.gray;
        else if (configuration.fcolor.equals(TimebarConfiguration.FCOLORS[4])) c = Color.darkGray;
        else if (configuration.fcolor.equals(TimebarConfiguration.FCOLORS[5])) c = Color.red;
        else if (configuration.fcolor.equals(TimebarConfiguration.FCOLORS[6])) c = Color.green;
        else if (configuration.fcolor.equals(TimebarConfiguration.FCOLORS[7])) c = Color.blue;
        else if (configuration.fcolor.equals(TimebarConfiguration.FCOLORS[8])) c = Color.yellow;
       return c;
    }

    private Color getBColor() {
        if (configuration.bcolor == null || configuration.bcolor.equals(TimebarConfiguration.BCOLORS[0])) return null;
        Color bc = Color.white;
        if (configuration.bcolor.equals(TimebarConfiguration.BCOLORS[1])) bc = Color.black;
        else if (configuration.bcolor.equals(TimebarConfiguration.BCOLORS[3])) bc = Color.darkGray;
        else if (configuration.bcolor.equals(TimebarConfiguration.BCOLORS[4])) bc = Color.gray;
        else if (configuration.bcolor.equals(TimebarConfiguration.BCOLORS[5])) bc = Color.lightGray;
        else if (configuration.bcolor.equals(TimebarConfiguration.BCOLORS[6])) bc = Color.yellow;
        else if (configuration.bcolor.equals(TimebarConfiguration.BCOLORS[7])) bc = Color.blue;
        else if (configuration.bcolor.equals(TimebarConfiguration.BCOLORS[8])) bc = Color.green;
        else if (configuration.bcolor.equals(TimebarConfiguration.BCOLORS[9])) bc = Color.red;
        return bc;
    }

    private void updateFont() {
        int fontType = configuration.boldText ? 1 : 0;
        String face = configuration.serifFont ? "Serif" : "SansSerif";
        Font font = new Font(face, fontType, configuration.fontSize);
        ImageProcessor ip = imp.getProcessor();
        ip.setFont(font);
    }


    private void removeTimebar() {
        // Remove overlay we created
        Overlay overlay = imp.getOverlay();
        if (overlay != null) {
            overlay.remove(TIME_BAR);
            imp.setOverlay(overlay);
            imp.draw();
        }
    }
}