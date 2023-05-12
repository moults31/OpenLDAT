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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dosse
 */
public class Config {

    public static boolean CHART_ANTIALIASING = false;
    public static final int FONT_SIZE = 12, LARGE_FONT_SIZE = 16;
    public static boolean TESTSCREEN_GL = true;
    public static boolean X11_NOHACKS = false;

    private static String getConfigPath() {
        String path = null;
        if (Utils.isWindows()) {
            path = System.getenv("LOCALAPPDATA");
            if (path == null || path.isEmpty()) {
                path = System.getenv("APPDATA");
            }
            if (path != null) {
                path += "\\openldat\\";
            }
        } else if (Utils.isLinux()) {
            path = System.getProperty("user.home")+"/.openldat/";
        } else if (Utils.isMac()) {
            path = System.getProperty("user.home")+"/Library/Application Support/com.dosse.openldat/";
        }
        if (path == null) {
            path = "";
        } else if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        path += "openldat.cfg";
        return path;
    }

    public static void saveConfig() {
        try {
            Map<String, Object> m = new HashMap();
            m.put("CHART_ANTIALIASING", CHART_ANTIALIASING);
            m.put("TESTSCREEN_GL", TESTSCREEN_GL);
            m.put("X11_NOHACKS", X11_NOHACKS);
            File f=new File(getConfigPath());
            if(!f.exists()){
                f.getAbsoluteFile().getParentFile().mkdirs();
                f.createNewFile();
            }
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(m);
            oos.flush();
            oos.close();
        } catch (Throwable t) {
        }
    }

    public static void loadConfig() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getConfigPath()));
            Map<String, Object> m = (Map<String, Object>) ois.readObject();
            ois.close();
            try {
                CHART_ANTIALIASING = (boolean) m.get("CHART_ANTIALIASING");
            } catch (Throwable t) {
            }
            try {
                TESTSCREEN_GL = (boolean) m.get("TESTSCREEN_GL");
            } catch (Throwable t) {
            }
            try {
                X11_NOHACKS = (boolean) m.get("X11_NOHACKS");
            } catch (Throwable t) {
            }
        } catch (Throwable t) {
        }
    }
}
