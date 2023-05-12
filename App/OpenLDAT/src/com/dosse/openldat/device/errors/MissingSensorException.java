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
package com.dosse.openldat.device.errors;

/**
 *
 * @author dosse
 */
public class MissingSensorException extends Exception {

    public static final int LIGHT_SENSOR=1,
            CUSTOM_ERROR=100;
    
    private int type=CUSTOM_ERROR;
    
    public MissingSensorException(int type) {
        this.type=type;
    }

    public MissingSensorException(String message) {
        super(message);
        type=CUSTOM_ERROR;
    }

    public int getType() {
        return type;
    }

}
