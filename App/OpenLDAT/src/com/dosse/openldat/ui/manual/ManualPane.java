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
package com.dosse.openldat.ui.manual;

import com.dosse.openldat.Config;
import com.dosse.openldat.Utils;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 *
 * @author dosse
 */
public class ManualPane extends JScrollPane {

    private final JEditorPane contents;
    private float DPI_SCALE;
    private ArrayList<URL> history = new ArrayList<>();
    private boolean openLinksInBrowser = false;

    {
        contents = new JEditorPane() {
            @Override
            public void paint(Graphics g) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                super.paint(g);
            }
        };
        HTMLEditorKit k = new HTMLEditorKit();
        contents.setEditorKit(k);
        contents.setEditable(false);
        contents.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent he) {
                if (he.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    if (he.getURL().toString().endsWith("/!BACK!")) {
                        if (history.size() >= 2) {
                            URL u = history.get(history.size() - 2);
                            history.remove(history.size() - 1);
                            history.remove(history.size() - 1);
                            setPage(u);
                        }
                    } else {
                        if (openLinksInBrowser) {
                            try {
                                Desktop.getDesktop().browse(he.getURL().toURI());
                            } catch (Throwable t) {
                            }
                        } else {
                            setPage(he.getURL());
                        }
                    }
                }
            }
        });
        DPI_SCALE = Utils.getDPIScaling();
        StyleSheet css = k.getStyleSheet();
        //css.addRule("body{color:#"+String.format("%6X",contents.getForeground().getRGB())+"}"); //it doesn't automatically use the foreground color
        css.addRule("body{color:#303030;}");
        contents.setBackground(new Color(0xEEEEEE));
        css.addRule("body{font-family:\"Dialog\"; font-size:" + ((int) (Config.FONT_SIZE * DPI_SCALE * 0.925)) + "px;}");
        css.addRule("h1{font-size:" + ((int) (Config.FONT_SIZE * 2.5 * DPI_SCALE * 0.9)) + "px; margin-top:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px; margin-bottom:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px;}");
        css.addRule("h2{font-size:" + ((int) (Config.FONT_SIZE * 2.25 * DPI_SCALE * 0.9)) + "px; margin-top:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px; margin-bottom:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px;}");
        css.addRule("h3{font-size:" + ((int) (Config.FONT_SIZE * 2 * DPI_SCALE * 0.9)) + "px; margin-top:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px; margin-bottom:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px;}");
        css.addRule("h4{font-size:" + ((int) (Config.FONT_SIZE * 1.75 * DPI_SCALE * 0.9)) + "px; margin-top:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px; margin-bottom:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px;}");
        css.addRule("h5{font-size:" + ((int) (Config.FONT_SIZE * 1.5 * DPI_SCALE * 0.9)) + "px; margin-top:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px; margin-bottom:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px;}");
        css.addRule("h6{font-size:" + ((int) (Config.FONT_SIZE * 1.25 * DPI_SCALE * 0.9)) + "px; margin-top:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px; margin-bottom:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px;}");
        css.addRule("li{padding-bottom:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px;}");
        css.addRule("pre{margin-top:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px; margin-bottom:" + ((int) (Config.FONT_SIZE * 0.5 * DPI_SCALE * 0.9)) + "px;}");
        css.addRule("p{margin-top:0; margin-bottom:0;}");
        k.setStyleSheet(css);
        setViewportView(contents);
        MouseWheelListener[] l = getMouseWheelListeners();
        for (MouseWheelListener ll : l) {
            removeMouseWheelListener(ll);
        }
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) {
                getVerticalScrollBar().setValue((int) (getVerticalScrollBar().getValue() + (mwe.getPreciseWheelRotation() * mwe.getScrollAmount() * 3 * DPI_SCALE)));
                mwe.consume();
            }
        });
    }

    @Override
    public boolean isFocusOwner() {
        return super.isFocusOwner() || (contents != null && contents.isFocusOwner());
    }

    public void setPage(URL page) {
        try {
            if (contents.isFocusOwner()) {
                requestFocus();
            }
            if (contents.getPage() != null && page.getPath().equals(contents.getPage().getPath())) { //page already loaded
                contents.setPage(page);
                history.add(page);
                return;
            }
            page=new URL(null, page.toExternalForm(), new ManualStreamHandler());
            contents.setPage(page);
            history.add(page);
        } catch (Throwable ex) {
            System.err.println("Failed to display manual page because " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void clearHistory() {
        history.clear();
    }

    public void setOpenLinksInBrowser(boolean openLinksInBrowser) {
        this.openLinksInBrowser = openLinksInBrowser;
    }

}
