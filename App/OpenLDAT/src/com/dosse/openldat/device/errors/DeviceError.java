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
public class DeviceError extends Exception {

    public static final int FAILED_TO_CONNECT=1, UNSUPPORTED_MODEL=2, NOT_OPENLDAT_DEVICE=3, DEVICE_ID_FAILED=4,
            FIRMWARE_BUILT_WITH_SERIALPLOT=100, FIRMWARE_NEEDS_NEWER_DRIVER=101, FIRMWARE_UNKNOWN=102, FIRMWARE_LIGHTSENSOR_MISSING_BUFSIZES=103,
            CUSTOM_ERROR=201;
    
    private int type=CUSTOM_ERROR;

    public DeviceError(int type) {
        super();
        this.type=type;
    }

    public DeviceError(String message) {
        super(message);
        this.type=CUSTOM_ERROR;
    }

    public int getType() {
        return type;
    }
    
}
