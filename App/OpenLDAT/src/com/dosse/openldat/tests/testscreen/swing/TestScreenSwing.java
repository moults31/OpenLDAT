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
package com.dosse.openldat.tests.testscreen.swing;

import com.dosse.openldat.Utils;
import com.dosse.openldat.tests.testscreen.ITestScreen;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * Fallback Swing implementation of TestScreen for devices that don't support
 * OpenGL
 *
 * @author dosse
 */
public class TestScreenSwing extends javax.swing.JFrame implements ITestScreen {

    private int screenW, screenH, hz;

    private Object targetMutex = new Object();
    private BufferedImage smallBlackTarget, largeBlackTarget, smallWhiteTarget, largeWhiteTarget, blackText, whiteText;
    private int smallTargetW, smallTargetH, largeTargetW, largeTargetH, textW, textH;
    private float targetX = 0, targetY = 0;
    private boolean targetVisible = false, targetBlack = false, targetSmall = false;

    private Object bkMutex = new Object();
    private Color bkColor = new Color(0f, 0f, 0f);
    private boolean flashOnClick = false;
    private Color flashColor = new Color(1f, 1f, 1f);
    private double flashMS = 0;
    private boolean flickering = false;

    private Robot r;
    private boolean stopAutoRender = false;
    private long nsPerFrame = 0, frame = 0;

    /**
     * Creates new form TestScreenSwing
     */
    public TestScreenSwing() {
        initComponents();
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        device.setFullScreenWindow(this);
        screenW = device.getDisplayMode().getWidth();
        screenH = device.getDisplayMode().getHeight();
        hz = device.getDisplayMode().getRefreshRate();
        if (hz == DisplayMode.REFRESH_RATE_UNKNOWN) {
            System.err.println("WARNING: unable to determine refresh rate, assuming 60");
            hz = 60;
        }
        nsPerFrame = (long) (1000000000.0 / (double) hz);
        largeTargetW = largeTargetH = (int) (Math.min(screenW, screenH) * 0.25);
        smallTargetW = smallTargetH = (int) (Math.min(screenW, screenH) * 0.1);
        largeWhiteTarget = loadBufferedImage("/com/dosse/openldat/tests/testscreen/target.png", largeTargetW, largeTargetH);
        smallWhiteTarget = loadBufferedImage("/com/dosse/openldat/tests/testscreen/target.png", smallTargetW, smallTargetH);
        largeBlackTarget = loadBufferedImage("/com/dosse/openldat/tests/testscreen/target.png", largeTargetW, largeTargetH);
        multiplyColor(largeBlackTarget, 0, 0, 0);
        smallBlackTarget = loadBufferedImage("/com/dosse/openldat/tests/testscreen/target.png", smallTargetW, smallTargetH);
        multiplyColor(smallBlackTarget, 0, 0, 0);
        whiteText = loadBufferedImage("/com/dosse/openldat/tests/testscreen/text.png", -1, -1);
        textW = whiteText.getWidth();
        textH = whiteText.getHeight();
        float tH = screenH * 0.1f, tW = (tH / textH) * textW;
        if (tW > screenW) {
            tW = screenW * 0.9f;
            tH = (tW / textW) * textH;
        }
        textW = (int) tW;
        textH = (int) tH;
        whiteText = loadBufferedImage("/com/dosse/openldat/tests/testscreen/text.png", textW, textH);
        blackText = loadBufferedImage("/com/dosse/openldat/tests/testscreen/text.png", textW, textH);
        multiplyColor(blackText, 0, 0, 0);
        try {
            setCursor(getToolkit().createCustomCursor(loadBufferedImage(null, 0, 0), new Point(0, 0), ""));
        } catch (Throwable t) {
            System.err.println("WARNING: unable to hide mouse cursor");
        }
        try {
            r = new Robot();
        } catch (Throwable ex) {
            r = null;
        }
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        onCancel();
                        break;
                    case KeyEvent.VK_ENTER:
                        onEnterPressed();
                        break;
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (flashOnClick) {
                    flashColor(1, 1, 1, 100);
                }
            }
        });
        new Thread() {
            @Override
            public void run() {
                long ts = 0;
                while (!stopAutoRender) {
                    long ts2 = System.nanoTime();
                    if (ts2 - ts >= nsPerFrame) {
                        getContentPane().repaint();
                        if (r != null) {
                            r.mouseMove(10 + (int) (frame % 2), 10);
                        }
                        ts = ts2;
                    } else {
                        try {
                            sleep(0, 1000);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
                r.mouseMove(screenW/2, screenH/2);
            }
        }.start();
        Utils.focusWindow(this);
    }

    private BufferedImage loadBufferedImage(String pathInClasspath, int w, int h) {
        try {
            Image i = ImageIO.read(Utils.class.getResource(pathInClasspath));
            if (w <= 0 || h <= 0) {
                w = i.getWidth(null);
                h = i.getHeight(null);
                BufferedImage ret = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics g = ret.getGraphics();
                g.drawImage(i, 0, 0, null);
                g.dispose();
                return ret;
            } else {
                BufferedImage ret = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics g = ret.getGraphics();
                g.drawImage(i.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
                g.dispose();
                return ret;
            }
        } catch (Throwable ex) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
    }

    private void multiplyColor(BufferedImage image, float rr, float gg, float bb) {
        int[] data = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        for (int i = 0; i < data.length; i++) {
            data[i] = (data[i] & 0xFF000000) | ((int) (((data[i] >> 16) & 0xFF) * rr) & 0xFF) | ((int) (((data[i] >> 8) & 0xFF) * gg) & 0xFF) | ((int) (((data[i]) & 0xFF) * bb) & 0xFF);
        }
        image.setRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, image.getWidth());
    }

    private Color black = new Color(0f, 0f, 0f);
    private long ts = System.nanoTime();

    private void render(Graphics g) {
        double msSinceLastFrame = (System.nanoTime() - ts) / 1000000.0;
        ts = System.nanoTime();
        synchronized (bkMutex) {
            if (flashMS > 0) {
                g.setColor(flashColor);
                flashMS -= msSinceLastFrame;
            } else if (flickering) {
                g.setColor(frame % 2 == 0 ? bkColor : black);
            } else {
                g.setColor(bkColor);
            }
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        synchronized (targetMutex) {
            if (targetVisible) {
                float targetSize = targetSmall ? smallTargetW : largeTargetW;
                int tX = (int) (targetX - targetSize * 0.5f),
                        tY = (int) (targetY - targetSize * 0.5f);
                g.drawImage(targetBlack ? (targetSmall ? smallBlackTarget : largeBlackTarget) : (targetSmall ? smallWhiteTarget : largeWhiteTarget), tX, tY, null);
                tX = (int) ((screenW - textW) * 0.5f);
                tY = (targetY > screenH * 0.5f) ? 0 : (screenH - textH);
                g.drawImage(targetBlack ? blackText : whiteText, tX, tY, null);
            }
        }
        frame++;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel(){
            @Override
            public void paint(Graphics g){
                render(g);
            }
        };

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridLayout());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void flashColor(float r, float g, float b, double ms) {
        synchronized (bkMutex) {
            flashColor = new Color(r, g, b);
            flashMS = ms;
        }
    }

    @Override
    public boolean setColor(float r, float g, float b) {
        synchronized (bkMutex) {
            Color newColor = new Color(r, g, b);
            if (bkColor.equals(newColor)) {
                return false;
            }
            bkColor = newColor;
            return true;
        }
    }

    @Override
    public void setTarget(float x, float y, float size, boolean black) {
        synchronized (targetMutex) {
            targetBlack = black;
            targetSmall = size < 0.15;
            targetX = x * screenW;
            targetY = y * screenH;
            targetVisible = true;
        }
    }

    @Override
    public void setTargetAbsolute(float x, float y, float size, boolean black) {
        synchronized (targetMutex) {
            targetBlack = black;
            targetSmall = size < 0.15;
            targetX = x;
            targetY = y;
            targetVisible = true;
        }
    }

    @Override
    public void hideTarget() {
        synchronized (targetMutex) {
            targetVisible = false;
        }
    }

    @Override
    public int getScreenW() {
        return screenW;
    }

    @Override
    public int getScreenH() {
        return screenH;
    }

    @Override
    public void setFlashOnClick(boolean flashOnClick) {
        this.flashOnClick = flashOnClick;
    }

    @Override
    public void setFlicker(boolean bkFlicker) {
        synchronized (bkMutex) {
            System.err.println("WARNING: Flickering is extremely unreliable on Swing backend");
            flickering = true;
        }
    }

    @Override
    public boolean getFlashOnClick() {
        return flashOnClick;
    }

    @Override
    public boolean isFlickering() {
        synchronized (bkMutex) {
            return flickering;
        }
    }

    @Override
    public void setFakeLoad(long cpuMs, long gpuMs) {
        System.err.println("WARNING: Fake loads not supported on Swing backend");
    }

    @Override
    public long getFakeCPULoad() {
        return 0;
    }

    @Override
    public long getFakeGPULoad() {
        return 0;
    }

    @Override
    public void close() {
        stopAutoRender = true;
        dispose();
    }

    @Override
    public void onEnterPressed() {
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        System.exit(1);
    }

    @Override
    public int getRefreshRate() {
        return hz;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
