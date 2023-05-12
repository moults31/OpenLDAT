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
package com.dosse.openldat;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 *
 * @author dosse
 */
public class Utils {

    public static final boolean sleep(long ms) {
        try {
            Thread.sleep(ms);
            return false;
        } catch (InterruptedException ex) {
            return true;
        }
    }

    private static float dpiScaling = -1;

    public static final float getDPIScaling() {
        /*
        DisplayMode d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        return Math.max(1, (Math.min(d.getHeight(), d.getWidth()) * 0.085f) / 96f);
         */
        if (dpiScaling > 0) {
            return dpiScaling;
        } else {
            try {
                if (Utils.isWindows()) { //on windows, this function works properly
                    dpiScaling = (float) Toolkit.getDefaultToolkit().getScreenResolution() / 96f;
                } else if (Utils.isLinux()) { //on linux, it's broken so we try to do it on our own
                    String scale = System.getenv("GDK_SCALE");
                    if (scale == null) {
                        scale = System.getenv("GDK_DPI_SCALE");
                    }
                    if (scale == null) {
                        try {
                            Process p = Runtime.getRuntime().exec("xrdb -query");
                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            while (true) {
                                String s = br.readLine();
                                if (s == null) {
                                    break;
                                }
                                if (s.startsWith("Xft.dpi:")) {
                                    scale = s.split(":")[1].trim();
                                }
                            }
                            br.close();
                            p.waitFor(100, TimeUnit.MILLISECONDS);
                        } catch (Throwable t) {
                            scale = null;
                        }
                    }
                    if (scale != null) {
                        dpiScaling = Float.parseFloat(scale) / 96f;
                    } else {
                        dpiScaling = 1;
                    }
                } else if (Utils.isMac()) { //mac is retarded and always applies its own blurry scaling so we don't scale anything
                    dpiScaling = 1;
                } else { //unknown OS, don't even try
                    dpiScaling = 1;
                }
            } catch (Throwable t) { //error, give up
                dpiScaling = 1;
            }
            return dpiScaling;
        }
    }

    private static final BufferedImage nullImage; //empty Image, mostly used for errors

    static {
        nullImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        nullImage.setRGB(0, 0, 0);
    }

    public static final ImageIcon loadAndScaleIcon(String pathInClasspath, int w, int h) {
        try {
            Image i = ImageIO.read(Utils.class.getResource(pathInClasspath));
            return new ImageIcon(i.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Throwable ex) {
            return new ImageIcon(nullImage.getScaledInstance(w, h, Image.SCALE_FAST));
        }
    }

    private static boolean isWindows, isLinux, isMac;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        isWindows = os.startsWith("windows");
        isLinux = os.startsWith("linux");
        isMac = os.startsWith("mac");
    }

    public static final boolean isWindows() {
        return isWindows;
    }

    public static final boolean isLinux() {
        return isLinux;
    }

    public static final boolean isMac() {
        return isMac;
    }

    public static final void focusWindow(JFrame ui) {
        if (!isLinux() || Config.X11_NOHACKS) {
            try {
                ui.requestFocus();
            } catch (Throwable t) {
            }
        } else {
            while (true) {
                try {
                    ui.requestFocus();
                    break;
                } catch (Throwable t) {
                }
            }
        }
    }

}
