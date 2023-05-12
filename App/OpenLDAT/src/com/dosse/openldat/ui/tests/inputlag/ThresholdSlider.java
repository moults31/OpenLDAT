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
package com.dosse.openldat.ui.tests.inputlag;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JSlider;
import com.dosse.openldat.Utils;
import java.awt.Dimension;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import javax.swing.event.ChangeListener;

/**
 *
 * @author dosse
 */
public class ThresholdSlider extends JSlider {

    private float DPI_SCALE = 1;
    public boolean horizontalFlipped = true;

    public ThresholdSlider() {
        super(JSlider.VERTICAL);
        DPI_SCALE = Utils.getDPIScaling();
        setPreferredSize(new Dimension((int) (12 * DPI_SCALE), getPreferredSize().height));
        for (MouseListener l : getMouseListeners()) {
            removeMouseListener(l);
        }
        for (MouseMotionListener l : getMouseMotionListeners()) {
            removeMouseMotionListener(l);
        }
        MouseAdapter a = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isFocusOwner()) {
                    requestFocus();
                }
                double f = (double) e.getY() / (double) getHeight();
                if (!getInverted()) {
                    f = 1 - f;
                }
                setValue((int) (getMinimum() + (getMaximum() - getMinimum()) * f));
                for (ChangeListener l : getChangeListeners()) {
                    l.stateChanged(null);
                }
            }
        };
        addMouseListener(a);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent me) {
                a.mousePressed(me);
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(isFocusOwner() ? getBackground().brighter() : getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(getForeground());
        double y = getHeight() * (1.0 - ((double) (getValue() - getMinimum()) / (getMaximum() - getMinimum())));
        if (horizontalFlipped) {
            g.fillPolygon(new int[]{getWidth() - 1, getWidth() - 1, 0}, new int[]{(int) (y - 6 * DPI_SCALE), (int) (y + 6 * DPI_SCALE), (int) y}, 3);
        } else {
            g.fillPolygon(new int[]{0, 0, getWidth() - 1}, new int[]{(int) (y - 6 * DPI_SCALE), (int) (y + 6 * DPI_SCALE), (int) y}, 3);
        }
    }
}
