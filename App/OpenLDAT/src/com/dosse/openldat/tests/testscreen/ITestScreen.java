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
package com.dosse.openldat.tests.testscreen;

/**
 *
 * @author dosse
 */
public interface ITestScreen {

    public void flashColor(float r, float g, float b, double ms);

    public boolean setColor(float r, float g, float b);

    public void setTarget(float x, float y, float size, boolean black);

    public void setTargetAbsolute(float x, float y, float size, boolean black);

    public void hideTarget();

    public int getScreenW();

    public int getScreenH();

    public void setFlashOnClick(boolean flashOnClick);

    public void setFlicker(boolean bkFlicker);

    public boolean getFlashOnClick();

    public boolean isFlickering();

    public void setFakeLoad(long cpuMs, long gpuMs);

    public long getFakeCPULoad();

    public long getFakeGPULoad();

    public void close();

    public void onEnterPressed();

    public void onCancel();

    public void onError(Exception e);

    public int getRefreshRate();
}
