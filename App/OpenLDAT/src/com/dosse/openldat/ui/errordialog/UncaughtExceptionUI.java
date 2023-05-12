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
package com.dosse.openldat.ui.errordialog;

/**
 *
 * @author dosse
 */
public class UncaughtExceptionUI implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, Throwable thrwbl) {
        System.err.println("Thread " + thread.getId() + " (" + thread.getName() + ") has crashed");
        thrwbl.printStackTrace();
        ErrorDialog err = new ErrorDialog(thrwbl) {
            @Override
            public void onClose() {
                System.exit(3);
            }
        };
        if (!(thrwbl instanceof NoClassDefFoundError || thrwbl instanceof ClassNotFoundException)) {
            err.setErrorMessage("<html>swkotor.exe has stopped working</html>", "<html>Error details have been printed to stderr (twice, for good measure)</html>", "/com/dosse/openldat/ui/errordialog/error_ee.png");
        }
    }

}
