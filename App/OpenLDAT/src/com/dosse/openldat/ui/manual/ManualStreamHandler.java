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

import com.dosse.openldat.Utils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author dosse
 */
public class ManualStreamHandler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        URL resourceUrl = new URL(url.toExternalForm());
        if (!resourceUrl.getFile().endsWith(".html")) {
            return resourceUrl.openConnection();
        }
        float DPI_SCALE = Utils.getDPIScaling();
        String data = new String(resourceUrl.openStream().readAllBytes());
        Pattern p = Pattern.compile("!AUTO!\\d+");
        Matcher m = p.matcher(data);
        ArrayList<int[]> matches = new ArrayList<>();
        while (m.find()) {
            matches.add(new int[]{m.start(), m.end()});
        }
        for (int i = matches.size() - 1; i >= 0; i--) {
            int[] x = matches.get(i);
            int val = (int) Math.round((Double.parseDouble((String) data.subSequence(x[0] + 6, x[1])) * DPI_SCALE));
            data = data.substring(0, x[0]) + val + data.substring(x[1]);
        }
        return new ByteArrayURLConnection(data.getBytes(), url);
    }

    private class ByteArrayURLConnection extends URLConnection {

        private byte[] data;

        public ByteArrayURLConnection(byte[] data, URL url) {
            super(url);
            this.data = data;
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

    }

}
