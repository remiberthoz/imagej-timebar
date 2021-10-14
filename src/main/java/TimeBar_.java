import java.awt.Rectangle;

import java.awt.Color;
import java.awt.Font;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class TimeBar_ implements PlugIn {

    static final String TIME_BAR = "|TB|";
    
    private static final TimeBarConfiguration sConfig = new TimeBarConfiguration();
    private TimeBarConfiguration config = new TimeBarConfiguration(sConfig);
    
    ImagePlus imp;
    int nFrames;
    int currentFrame;
    int roiX, roiY, roiWidth, roiHeight;
    boolean userRoiExists;
    
    Rectangle hBackground = new Rectangle();
    Rectangle hBar = new Rectangle();
    Rectangle hText = new Rectangle();
    
	/**
	 * This method is called when the plugin is loaded. 'arg', which
	 * may be blank, is the argument specified for this plugin in
	 * IJ_Props.txt.
	 */
    public void run(String arg) {
        imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }
        // Snapshot before anything, so we can revert if the user cancels the action.
        imp.getProcessor().snapshot();

        // Get time info about image/
        nFrames = imp.getNFrames();
        currentFrame = imp.getFrame();

        userRoiExists = parseCurrentROI();
        boolean userOKed = askUserConfiguration(userRoiExists);
        
        if (!userOKed) {
            removeTimeBar();
            return;
        }
        
        if (!IJ.isMacro())
            persistConfiguration();

        updateTimeBar(false);
    }

    /**
	 * Remove the timebar drawn by this plugin.
	 * 
	 * If the timebar was drawn without the overlay by another
	 * instance of the plugin (it is drawn into the image), then
	 * we cannot remove it.
	 * 
	 * If the timebar was drawn using the overlay by another
	 * instance of the plugin, then we can remove it.
	 * 
	 * With or without the overlay, we can remove a timebar
	 * drawn by this instance of the plugin.
	 */
    void removeTimeBar() {
        // Revert with Undo, in case "Use Overlay" is not ticked
        imp.getProcessor().reset();
        imp.updateAndDraw();

        // Remove overlay drawn by this plugin, in case "Use Overlay" is ticked
        Overlay overlay = imp.getOverlay();
        if (overlay != null) {
            overlay.remove(TIME_BAR);
            imp.draw();
        }
    }

    /**
	 * If there is a user selected ROI, set the class variables {roiX}
	 * and {roiY}, {roiWidth}, {roiHeight} to the corresponding
	 * features of the ROI, and return true. Otherwise, return false.
	 */
    boolean parseCurrentROI() {
        Roi roi = imp.getRoi();
        if (roi == null) return false;

        Rectangle r = roi.getBounds();
        roiX = r.x;
        roiY = r.y;
		roiWidth = r.width;
		roiHeight = r.height;

        return true;
    }
    
	/**
	 * Genreate & draw the configuration dialog.
	 * 
	 * Return the value of dialog.wasOKed() when the user clicks OK
	 * or Cancel.
	 */
    boolean askUserConfiguration(boolean currentROIExists) {
		// Update the user configuration if there is an ROI.
		if (currentROIExists)
			config.location = TimeBarLocation.LOCATIONS.get(TimeBarLocation.Locations.AT_SELECTION.ordinal());
		
        if (IJ.isMacro())
            config.updateFrom(new TimeBarConfiguration());

		// Draw a first preview timebar, with the default or presisted
		// configuration.
		updateTimeBar(true);
		
		// Create & show the dialog, then return.
		GenericDialog dialog = new TimeBarDialog(config);
		DialogListener dialogListener = new TimeBarDialogListener(config, this);
		dialog.addDialogListener(dialogListener);
		dialog.showDialog();

		return dialog.wasOKed();
	}

	/**
	 * Store the active configuration into the static variable that
	 * is persisted across calls of the plugin.
	 * 
	 * The "active" configuration is normally the one reflected by
	 * the dialog.
	 */
	void persistConfiguration() {
		sConfig.updateFrom(config);
	}

	/**
	 * Create & draw the scalebar using an Overlay.
	 */
	Overlay createTimeBarOverlay(int frame) throws MissingRoiException {
		Overlay overlay = new Overlay();

		Color color = config.color.color;
		Color bcolor = config.bcolor.color;
		
		int fontType = config.boldText?Font.BOLD:Font.PLAIN;
		String face = config.serifFont?"Serif":"SanSerif";
		Font font = new Font(face, fontType, config.fontSize);
		ImageProcessor ip = imp.getProcessor();
		ip.setFont(font);

		setElementsPositions();

		if (bcolor != null) {
            Roi hBackgroundRoi = new Roi(hBackground.x, hBackground.y, hBackground.width, hBackground.height);
            hBackgroundRoi.setFillColor(bcolor);
            overlay.add(hBackgroundRoi, TIME_BAR);
		}

		if (!config.hideBar) {
            Roi hBarRoi = new Roi(hBar.x, hBar.y, hBar.width*(frame-1)/(nFrames-1), hBar.height);
            hBarRoi.setFillColor(color);
            overlay.add(hBarRoi, TIME_BAR);
        }
        
        TextRoi hTextRoi = new TextRoi(hText.x, hText.y, getTimeLabel(frame), font);
        hTextRoi.setStrokeColor(color);
        overlay.add(hTextRoi, TIME_BAR);

		return overlay;
	}

	/**
	 * Returns the text to draw near the timebar, for the specified {frame}
     * and using the format in the current configuration..
	 */
	String getTimeLabel(int frame) {
        String calibrationTimeUnit = imp.getCalibration().getTimeUnit();
        double calibrationTimeInterval = imp.getCalibration().frameInterval;

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
                IJ.log("Timebar plugin: Unknown time unit.");
                // TODO: Handle unknown units by simply writing, ie "120 munites"
                break;
        }

        long time = (long) ((frame-1) * calibrationTimeInterval * factor);
        return config.timeFormat.formatMillis(time, config.showUnits);
	}

	/**
	 * Returns the width of the box that contains the timebar and
	 * its label.
     * 
     * Labels can vary in width, this returns the largest width of
     * all labels.
	 */
    int getBoxWidthInPixels() {
		updateFont();
		ImageProcessor ip = imp.getProcessor();
        int hLabelWidth = -1;
        for (int f = 1; f <= nFrames; ++f) {
		    hLabelWidth = Math.max(hLabelWidth, ip.getStringWidth(getTimeLabel(f)));
        }
		return hLabelWidth;
	}

	/**
	 * Returns the height of the box that contains the timebar and
	 * its label.
	 */
	int getBoxHeightInPixels() {
		int hLabelHeight = config.fontSize;
		return (config.hideBar ? 0 : config.barThicknessInPixels) + (int) (hLabelHeight * 1.25);
	}

	/**
	 * Returns the size of margins that should be displayed between the timebar
	 * elements and the image edge.
	 */
    int getOuterMarginSizeInPixels() {
		int imageWidth = imp.getWidth();
		int imageHeight = imp.getHeight();
		return (imageWidth + imageHeight) / 100;
	}

	/**
	 * Retruns the size of margins that should be displayed between the timebar
	 * elements and the edge of the element's backround.
	 */
    int getInnerMarginSizeInPixels() {
		int width = getBoxWidthInPixels();
		int margin = Math.max(width/20, 2);
		return config.bcolor == null ? 0 : margin;
	}

    void updateFont() {
		int fontType = config.boldText?Font.BOLD:Font.PLAIN;
		String font = config.serifFont?"Serif":"SanSerif";
		ImageProcessor ip = imp.getProcessor();
		ip.setFont(new Font(font, fontType, config.fontSize));
		ip.setAntialiasedText(true);
	}

	/**
	 * Sets the positions x y of background, based on the current configuration.
	 */
    void setBackgroundBoxPosition() throws MissingRoiException {
		int imageWidth = imp.getWidth();
		int imageHeight = imp.getHeight();
		int boxWidth = getBoxWidthInPixels();
		int boxHeight = getBoxHeightInPixels();
		int outerMargin = getOuterMarginSizeInPixels();
		int innerMargin = getInnerMarginSizeInPixels();
		
		hBackground.width = innerMargin + boxWidth + innerMargin;
		hBackground.height = innerMargin + boxHeight + innerMargin;

		if (config.location == TimeBarLocation.LOCATIONS.get(TimeBarLocation.Locations.UPPER_RIGHT.ordinal())) {
			hBackground.x = imageWidth - outerMargin - innerMargin - boxWidth - innerMargin;
			hBackground.y = outerMargin;

		} else if (config.location == TimeBarLocation.LOCATIONS.get(TimeBarLocation.Locations.LOWER_RIGHT.ordinal())) {
			hBackground.x = imageWidth - outerMargin - innerMargin - boxWidth - innerMargin;
			hBackground.y = imageHeight - outerMargin - innerMargin - boxHeight - innerMargin;

		} else if (config.location == TimeBarLocation.LOCATIONS.get(TimeBarLocation.Locations.UPPER_LEFT.ordinal())) {
			hBackground.x = outerMargin;
			hBackground.y = outerMargin;

		} else if (config.location == TimeBarLocation.LOCATIONS.get(TimeBarLocation.Locations.LOWER_LEFT.ordinal())) {
			hBackground.x = outerMargin;
			hBackground.y = imageHeight - outerMargin - innerMargin - boxHeight - innerMargin;
            
		} else {
			if (!userRoiExists)
				throw new MissingRoiException();

			hBackground.x = roiX;
			hBackground.y = roiY;
		}
	}

	/**
	 * Sets the rectangles x y positions for timebar elements (bar, text),
	 * based on the current configuration. Also sets the width and height
     * of the rectangles.
	 * 
	 * The position of each rectangle is relative to background, so setBackgroundBoxPosition()
     * must run before this method computes positions.
	 * This method calls setBackgroundBoxPosition().
	 */
    void setElementsPositions() throws MissingRoiException {

		setBackgroundBoxPosition();

		int boxWidth = getBoxWidthInPixels();

		int innerMargin = getInnerMarginSizeInPixels();
		
		hBar.x = hBackground.x + innerMargin;
		hBar.y = hBackground.y + innerMargin;
		hBar.width = config.hideBar ? 0 : boxWidth;
		hBar.height = config.hideBar ? 0 : config.barThicknessInPixels;

		hText.height = config.fontSize;
		hText.width = boxWidth;
		hText.x = hBackground.x + innerMargin;
		hText.y = hBar.y + hBar.height;
	}

	/**
	 * Draw the timebar, based on the current configuration.
	 * 
	 * If {previewOnly} is true, only the active frame will be
	 * labeled with a timebar. If it is false, all frames of
	 * the stack will be labeled.
	 * 
	 * This method chooses whether to use an overlay or the
	 * drawing tool to create the scalebar.
	 */
	protected void updateTimeBar(boolean previewOnly) {
		removeTimeBar();

        Overlay impOverlay = imp.getOverlay();
		if (impOverlay == null)
			impOverlay = new Overlay();

        int fStart = previewOnly ? currentFrame : 1;
        int fEnd = previewOnly ? currentFrame + 1 : nFrames + 1;

        for (int c = 1; c <= imp.getNChannels(); ++c) {
            for (int s = 1; s <= imp.getNSlices(); ++s) {
                for (int f = 1; f <= nFrames; ++f) {

                    if (f < fStart || f >= fEnd)
                        continue;

                    Overlay scaleBarOverlay;
                    try {
                        scaleBarOverlay = createTimeBarOverlay(f);
                    } catch (MissingRoiException e) {
                        return; // Simply don't draw the scalebar.
                    }

                    if (config.useOverlay) {
                        for (Roi roi : scaleBarOverlay) {
                            roi.setPosition(c, s, f);
                            roi.setPosition(imp.getStackIndex(c, s, f));
                            impOverlay.add(roi);
                        }
                        imp.setOverlay(impOverlay);

                    } else {
                        ImageStack stack = imp.getStack();
                        int i = imp.getStackIndex(c, s, f);
                        ImageProcessor ip = stack.getProcessor(i);
                        drawOverlayOnProcessor(scaleBarOverlay, ip);
                        imp.updateAndDraw();
                    }
                }
            }
        }		
	}

	void drawOverlayOnProcessor(Overlay overlay, ImageProcessor processor) {
		if (processor.getBitDepth() == 8 || processor.getBitDepth() == 24) {
			// drawOverlay() only works for 8-bits and RGB
			processor.drawOverlay(overlay);
			return;
		}
		ImageProcessor ip = new ByteProcessor(imp.getWidth(), imp.getHeight());
		ip.drawOverlay(overlay);
		for (int y = 0; y < ip.getHeight(); y++)
			for (int x = 0; x < ip.getWidth(); x++) {
				int p = ip.get(x, y);
				if (p > 0)
					processor.putPixelValue(x, y, p / 255. * (processor.getMax() - processor.getMin()) + processor.getMin());
			}
	}

    class MissingRoiException extends Exception {
		MissingRoiException() {
			super("Scalebar location is set to AT_SELECTION but there is no selection on the image.");
		}
   } 
}
