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

import com.dosse.openldat.device.Device;
import com.dosse.openldat.ui.deviceselector.DeviceSelector;
import com.dosse.openldat.ui.errordialog.ApplicationError;
import com.dosse.openldat.ui.errordialog.ErrorDialog;
import com.dosse.openldat.ui.errordialog.UncaughtExceptionUI;
import com.dosse.openldat.ui.mainmenu.MainMenu;
import com.dosse.openldat.ui.laf.DarkMetalTheme;
import com.fazecast.jSerialComm.SerialPort;
import java.awt.EventQueue;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.lwjgl.system.Configuration;

/**
 *
 * @author dosse
 */
public class Main {

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionUI());
        try {
            Locale.setDefault(new Locale("en", "US"));
        } catch (Throwable t) {
            System.err.println("WARNING: failed to set locale");
        }
        if (Utils.isLinux()) {
            System.setProperty("sun.java2d.opengl", "true");
        }
        if (Utils.isMac()) {
            Config.TESTSCREEN_GL = false;
        }
        Config.loadConfig();
        if (Utils.isMac()) {
            try {
                System.setProperty("apple.awt.fullscreenhidecursor", "true");
            } catch (Throwable t) {
            }
        }
        try {
            MetalLookAndFeel.setCurrentTheme(new DarkMetalTheme());
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Throwable ex) {
            System.out.println("WARNING: Failed to load look and feel. The app will look like shit");
        }
        Configuration.GLFW_CHECK_THREAD0.set(false);
        if (!lockInstance()) {
            new ErrorDialog(new ApplicationError(null, "OpenLDAT is already running", null)) {
                @Override
                public void onClose() {
                    System.exit(0);
                }
            };
            return;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                DeviceSelector sel = new DeviceSelector() {
                    @Override
                    public void onDeviceSelected(SerialPort p) {
                        if (p == null) {
                            new ErrorDialog(new ApplicationError(null, "No supported devices found", null)) {
                                @Override
                                public void onClose() {
                                    System.exit(0);
                                }
                            };
                        } else {
                            try {
                                new MainMenu(new Device(p));
                            } catch (Throwable ex) {
                                new ErrorDialog(ex) {
                                    @Override
                                    public void onClose() {
                                        System.exit(0);
                                    }
                                };
                            }
                        }
                    }
                };
            }
        });
        while (true) {
            synchronized (tasksToRunOnMainThread) {
                for (Runnable r : tasksToRunOnMainThread) {
                    r.run();
                }
                tasksToRunOnMainThread.clear();
            }
            Utils.sleep(10);
        }
    }

    //adapted from https://stackoverflow.com/a/2002948
    private static boolean lockInstance() {
        try {
            String path = null;
            if (Utils.isWindows()) {
                path = System.getenv("LOCALAPPDATA");
                if (path == null || path.isEmpty()) {
                    path = System.getenv("APPDATA");
                }
            } else if (Utils.isLinux()) {
                path = "/tmp/";
            } else if (Utils.isMac()) {
                path = System.getenv("TMPDIR");
            }
            if (path == null) {
                path = "";
            } else if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            path += "openldat.lock";
            final File file = new File(path);
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final FileLock fileLock = randomAccessFile.getChannel().tryLock();
            if (fileLock != null) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            fileLock.release();
                            randomAccessFile.close();
                            file.delete();
                        } catch (Exception e) {
                        }
                    }
                });
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static final ArrayList<Runnable> tasksToRunOnMainThread = new ArrayList<>();

    public static void runOnMainThread(Runnable r) {
        if (r == null) {
            return;
        }
        synchronized (tasksToRunOnMainThread) {
            tasksToRunOnMainThread.add(r);
        }
    }

}
