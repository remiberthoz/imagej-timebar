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

import ij.gui.GenericDialog;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.TextEvent;

public class TimebarDialog extends GenericDialog {

    private static final String[] CHECKBOX_LABELS = {"Bold Text", "Hide Bar", "Serif Font", "Show units"};

    private TimebarConfiguration configuration;
    private TimebarDialogOnUpdateCallback onUpdateCallback;

    public TimebarDialog(TimebarConfiguration configuration, TimebarDialogOnUpdateCallback onUpdateCallback) {
        super("Time Bar");

        this.configuration = configuration;
        this.onUpdateCallback = onUpdateCallback;

        addNumericField("Height in pixels: ", configuration.barHeightInPixels, 0);
        addNumericField("Font size: ", configuration.fontSize, 0);
        addChoice("Color: ", TimebarConfiguration.FCOLORS, configuration.fcolor);
        addChoice("Background: ", TimebarConfiguration.BCOLORS, configuration.bcolor);
        addChoice("Location: ", TimebarConfiguration.LOCATIONS, configuration.location);
        addChoice("Format: ", TimebarTimeFormat.getTimeFormatDescs(), TimebarTimeFormat.getTimeFormatDescs()[TimebarTimeFormat.getTimeFormatIndex(configuration.timeFormat)]);
        setInsets(10, 25, 0);
        addCheckboxGroup(2, 2, CHECKBOX_LABELS, new boolean[] {configuration.boldText, configuration.hideBar, configuration.serifFont, configuration.showUnits});
    }
    

    @Override
    public void textValueChanged(TextEvent e) {
        processConfiguration();
        onUpdateCallback.onDialogUpdated();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        processConfiguration();
        onUpdateCallback.onDialogUpdated();
    }


    private void processConfiguration() {
        TextField tf;
        tf = (TextField) numberField.elementAt(0);
        Double barHeightInPixels = getValue(tf.getText());
        tf = (TextField) numberField.elementAt(1);
        Double fontSize = getValue(tf.getText());
        if (barHeightInPixels == null || fontSize == null)
            return;
        configuration.barHeightInPixels = barHeightInPixels.intValue();
        configuration.fontSize = fontSize.intValue();
        if (configuration.fontSize <= 5)
            configuration.fontSize = 5;

        Choice c;
        c = (Choice) choice.elementAt(0);
        configuration.fcolor = c.getSelectedItem();
        c = (Choice) choice.elementAt(1);
        configuration.bcolor = c.getSelectedItem();
        c = (Choice) choice.elementAt(2);
        configuration.location = c.getSelectedItem();
        c = (Choice) choice.elementAt(3);
        configuration.timeFormat = TimebarTimeFormat.TIME_FORMATS.get(c.getSelectedIndex());

        Checkbox cb;
        cb = (Checkbox) checkbox.elementAt(0);
        configuration.boldText = cb.getState();
        cb = (Checkbox) checkbox.elementAt(1);
        configuration.hideBar = cb.getState();
        cb = (Checkbox) checkbox.elementAt(2);
        configuration.serifFont = cb.getState();
        cb = (Checkbox) checkbox.elementAt(3);
        configuration.showUnits = cb.getState();
    }

    public TimebarConfiguration getConfiguration() {
        return configuration;
    }
}
