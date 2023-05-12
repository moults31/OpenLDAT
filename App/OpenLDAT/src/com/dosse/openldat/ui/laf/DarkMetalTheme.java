/*
 * Copyright (C) 2021 Federico Dossena
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

 /*
    Adapted from https://github.com/apache/netbeans/blob/6a9d4bdfa39bfdbbd1f60d22218818d3169348c5/platform/o.n.swing.laf.dark/src/org/netbeans/swing/laf/dark/DarkMetalTheme.java
 */
package com.dosse.openldat.ui.laf;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalTheme;
import com.dosse.openldat.Config;
import com.dosse.openldat.Utils;

/**
 *
 */
public class DarkMetalTheme extends MetalTheme {

    private final ColorUIResource primary1 = new ColorUIResource(121, 121, 125);
    private final ColorUIResource primary2 = new ColorUIResource(128, 128, 136);
    private final ColorUIResource primary3 = new ColorUIResource(99, 99, 99);
    private final ColorUIResource secondary1 = new ColorUIResource(113, 113, 113);
    private final ColorUIResource secondary2 = new ColorUIResource(91, 91, 95);
    private final ColorUIResource secondary3 = new ColorUIResource(51, 51, 55);
    private final ColorUIResource black = new ColorUIResource(222, 222, 222);
    private final ColorUIResource white = new ColorUIResource(18, 18, 30);

    private static final float DPI_SCALE = Utils.getDPIScaling();

    @Override
    public String getName() {
        return "OpenLDAT Theme";
    }

    @Override
    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);
        UIManager.put("selection.highlight", new Color(202, 152, 0));
        UIManager.put("textArea.background", new Color(51, 51, 55));
        UIManager.put("MenuItem.acceleratorForeground", new Color(198, 198, 198));
        UIManager.put("CheckBoxMenuItem.acceleratorForeground", new Color(198, 198, 198));
        UIManager.put("RadioButtonMenuItem.acceleratorForeground", new Color(198, 198, 198));
        UIManager.put("ScrollBar.width", (int) (16 * DPI_SCALE));
        UIManager.put("TestButton.background", secondary3.brighter());
        UIManager.put("CheckBox.textIconGap", (int) (8 * DPI_SCALE));
        UIManager.put("Button.textIconGap", (int) (8 * DPI_SCALE));
        UIManager.put("RadioButton.textIconGap", (int) (8 * DPI_SCALE));
    }

    @Override
    protected ColorUIResource getPrimary1() {
        return primary1;
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return primary2;
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return primary3;
    }

    @Override
    protected ColorUIResource getSecondary1() {
        return secondary1;
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return secondary2;
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return secondary3;
    }

    @Override
    protected ColorUIResource getWhite() {
        return white;
    }

    @Override
    protected ColorUIResource getBlack() {
        return black;
    }

    @Override
    public FontUIResource getControlTextFont() {
        return DEFAULT_FONT;
    }

    @Override
    public FontUIResource getSystemTextFont() {
        return DEFAULT_FONT;
    }

    @Override
    public FontUIResource getUserTextFont() {
        return DEFAULT_FONT;
    }

    @Override
    public FontUIResource getMenuTextFont() {
        return DEFAULT_FONT;
    }

    @Override
    public FontUIResource getWindowTitleFont() {
        return DEFAULT_FONT;
    }

    @Override
    public FontUIResource getSubTextFont() {
        return DEFAULT_FONT;
    }

    public static final FontUIResource DEFAULT_FONT = new FontUIResource("Dialog", Font.BOLD, (int) (Config.FONT_SIZE * DPI_SCALE));

}
