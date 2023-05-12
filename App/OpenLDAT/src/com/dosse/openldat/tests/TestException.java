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
package com.dosse.openldat.tests;

/**
 *
 * @author dosse
 */
public class TestException extends Exception{
    
    public static final int USER_ABORT=1, ANALYSIS_FAILED=2, INSUFFICIENT_CONTRAST=3, INVALID_SETTINGS=4, INCOMPATIBLE_DEVICE=5,
            CUSTOM_ERROR=100;
    
    private int type=CUSTOM_ERROR;

    public TestException(String message) {
        super(message);
        type=CUSTOM_ERROR;
    }

    public TestException(int type) {
        super();
        this.type=type;
    }

    public int getType() {
        return type;
    }
    
}
