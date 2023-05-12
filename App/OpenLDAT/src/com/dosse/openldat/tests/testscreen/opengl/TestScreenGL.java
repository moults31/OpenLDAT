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
package com.dosse.openldat.tests.testscreen.opengl;

import com.dosse.openldat.Config;
import com.dosse.openldat.Main;
import com.dosse.openldat.Utils;
import com.dosse.openldat.tests.testscreen.ITestScreen;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Math.round;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.lwjgl.BufferUtils;
import static org.lwjgl.BufferUtils.createByteBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.stb.STBImageResize.STBIR_ALPHA_CHANNEL_NONE;
import static org.lwjgl.stb.STBImageResize.STBIR_COLORSPACE_SRGB;
import static org.lwjgl.stb.STBImageResize.STBIR_EDGE_CLAMP;
import static org.lwjgl.stb.STBImageResize.STBIR_FILTER_MITCHELL;
import static org.lwjgl.stb.STBImageResize.STBIR_FLAG_ALPHA_PREMULTIPLIED;
import static org.lwjgl.stb.STBImageResize.stbir_resize_uint8_generic;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * OpenGL implementation of TestScreen
 *
 * @author dosse
 */
public class TestScreenGL implements ITestScreen {

    private long window = -1;

    private int whiteTarget, blackTarget, whiteText, blackText;
    private int targetTextureW = 0, targetTextureH = 0, targetTextureComp = 0, textTextureW = 0, textTextureH = 0, textTextureComp = 0;
    private int screenW, screenH, hz;

    private boolean flashOnClick = false;

    private final Object bkMutex = new Object();
    private float rr = 0, gg = 0, bb = 0;
    private float or = 0, og = 0, ob = 0;
    private boolean bkFlicker = false;
    private double flashMsLeft = 0;
    private final Object targetMutex = new Object();
    private float targetX = 0, targetY = 0, targetSize = 512;
    private boolean targetVisible = false, targetBlack = false;

    private long fakeCPULoadMs = 0, fakeGPULoadMs = 0;

    public static final int VSYNC_OFF = 0, VSYNC_ON = 1, VSYNC_ON_ALT = 2;

    private long ts = 0;

    private boolean macos_runnable_done = false;

    public TestScreenGL(int vsyncMode) {
        if (Utils.isMac()) {
            Main.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    main(vsyncMode);
                    macos_runnable_done = true;
                }
            });
            while (!macos_runnable_done && window == -1) {
                Utils.sleep(1);
            }
        } else {
            Thread t = new Thread() {
                @Override
                public void run() {
                    main(vsyncMode);
                }
            };
            t.start();
            while (t.isAlive() && window == -1) {
                Utils.sleep(1);
            }
        }

    }

    public void main(int vsyncMode) {
        if (Utils.isLinux()) {
            //used to defuse "xorg bomb" left over by glfw
            if (!Config.X11_NOHACKS) {
                saveX11ErrorHandler();
            }
        }
        glfwSetErrorCallback(new GLFWErrorCallback() {
            @Override
            public void invoke(int i, long l) {
                onError(new Exception("GLFW error " + Integer.toHexString(i)));
            }
        });
        if (!glfwInit()) {
            onError(new Exception("GLFW error: Unable to initialize GLFW"));
            return;
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        screenW = mode.width();
        screenH = mode.height();
        hz = mode.refreshRate();
        glfwWindowHint(GLFW_REFRESH_RATE, mode.refreshRate());
        glfwWindowHint(GLFW_RED_BITS, mode.redBits());
        glfwWindowHint(GLFW_GREEN_BITS, mode.greenBits());
        glfwWindowHint(GLFW_BLUE_BITS, mode.blueBits());
        if (vsyncMode == VSYNC_ON_ALT) {
            glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE); //forces compositor to stay on so we can turn vsync off in the application and use the compositor's own vsync
        }
        if (Utils.isMac()) {
            window = glfwCreateWindow(300, 300, "OpenLDAT Test Screen", NULL, NULL);
        } else {
            window = glfwCreateWindow(screenW, screenH, "OpenLDAT Test Screen", glfwGetPrimaryMonitor(), NULL);
        }
        if (window == NULL) {
            onError(new RuntimeException("Failed to create the GLFW window"));
            return;
        }
        glfwSetWindowMonitor(window, glfwGetPrimaryMonitor(), 0, 0, screenW, screenH, mode.refreshRate());
        System.out.println(screenW + "x" + screenH + "," + mode.redBits() + "" + mode.greenBits() + "" + mode.blueBits() + "," + mode.refreshRate() + "Hz");
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetInputMode(window, GLFW_STICKY_KEYS, GLFW_TRUE);
        glfwSetInputMode(window, GLFW_STICKY_MOUSE_BUTTONS, GLFW_TRUE);
        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if ((key == GLFW_KEY_ENTER || key == GLFW_KEY_F || scancode == 36) && action == GLFW_PRESS) {
                    onEnterPressed();
                }
                if ((key == GLFW_KEY_ESCAPE || key == GLFW_KEY_X || scancode == 9) && action == GLFW_PRESS) {
                    onCancel();
                }
            }
        });
        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (flashOnClick) {
                    if (action == GLFW_PRESS) {
                        flashColor(1.0f, 1.0f, 1.0f, 100);
                    }
                }
            }
        });
        glfwShowWindow(window);
        //load textures
        ByteBuffer whiteTargetImageBuffer, blackTargetImageBuffer, whiteTextImageBuffer, blackTextImageBuffer, whiteIslandImageBuffer, blackIslandImageBuffer;
        try {
            whiteTargetImageBuffer = ioResourceToByteBuffer("com/dosse/openldat/tests/testscreen/target.png", 8 * 1024);
            blackTargetImageBuffer = whiteTargetImageBuffer.duplicate();
            whiteTextImageBuffer = ioResourceToByteBuffer("com/dosse/openldat/tests/testscreen/text.png", 8 * 1024);
            blackTextImageBuffer = whiteTextImageBuffer.duplicate();
            MemoryStack stack = stackPush();
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);
            whiteTargetImageBuffer = stbi_load_from_memory(whiteTargetImageBuffer, w, h, comp, 0);
            if (whiteTargetImageBuffer == null) {
                onError(new RuntimeException("Failed to load image: " + stbi_failure_reason()));
                TestScreenGL.this.close();
                return;
            }
            blackTargetImageBuffer = stbi_load_from_memory(blackTargetImageBuffer, w, h, comp, 0);
            if (blackTargetImageBuffer == null) {
                onError(new RuntimeException("Failed to load image: " + stbi_failure_reason()));
                TestScreenGL.this.close();
                return;
            }
            targetTextureW = w.get(0);
            targetTextureH = h.get(0);
            targetTextureComp = comp.get(0);
            whiteTextImageBuffer = stbi_load_from_memory(whiteTextImageBuffer, w, h, comp, 0);
            if (whiteTextImageBuffer == null) {
                onError(new RuntimeException("Failed to load image: " + stbi_failure_reason()));
                TestScreenGL.this.close();
                return;
            }
            blackTextImageBuffer = stbi_load_from_memory(blackTextImageBuffer, w, h, comp, 0);
            if (blackTextImageBuffer == null) {
                onError(new RuntimeException("Failed to load image: " + stbi_failure_reason()));
                TestScreenGL.this.close();
                return;
            }
            textTextureW = w.get(0);
            textTextureH = h.get(0);
            textTextureComp = comp.get(0);
        } catch (Exception e) {
            onError(e);
            TestScreenGL.this.close();
            return;
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        switch (vsyncMode) {
            case VSYNC_OFF:
                glfwSwapInterval(0);
                glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_FALSE);
                break;
            case VSYNC_ON:
                glfwSwapInterval(1);
                glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);
                break;
            case VSYNC_ON_ALT:
                glfwSwapInterval(0);
                glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_FALSE);
                break;
        }

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0.0, screenW, screenH, 0.0, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);
        glViewport(0, 0, screenW, screenH);

        whiteTarget = createTexture(whiteTargetImageBuffer, targetTextureW, targetTextureH, targetTextureComp);
        multiplyColor(blackTargetImageBuffer, targetTextureW, targetTextureW, targetTextureComp, 0.0f, 0.0f, 0.0f);
        blackTarget = createTexture(blackTargetImageBuffer, targetTextureW, targetTextureH, targetTextureComp);
        whiteText = createTexture(whiteTextImageBuffer, textTextureW, textTextureH, textTextureComp);
        multiplyColor(blackTextImageBuffer, textTextureW, textTextureH, textTextureComp, 0.0f, 0.0f, 0.0f);
        blackText = createTexture(blackTextImageBuffer, textTextureW, textTextureH, textTextureComp);

        glEnable(GL_TEXTURE_2D);
        ts = System.nanoTime();
        long frame = 0;
        while (!glfwWindowShouldClose(window)) {
            double msSinceLastFrame = (System.nanoTime() - ts) / 1000000.0;
            //System.out.println(1000.0 / msSinceLastFrame);
            ts = System.nanoTime();
            glfwPollEvents();
            if (fakeCPULoadMs > 0) {
                Utils.sleep(fakeCPULoadMs);
            }
            synchronized (bkMutex) {
                if (flashMsLeft > 0) {
                    glClearColor(rr, gg, bb, 1.0f);
                    flashMsLeft -= msSinceLastFrame;
                } else {
                    if (bkFlicker) {
                        glClearColor(frame % 2 == 0 ? or : 0, frame % 2 == 0 ? og : 0, frame % 2 == 0 ? ob : 0, 1.0f);
                    } else {
                        glClearColor(or, og, ob, 1.0f);
                    }
                }
            }
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            synchronized (targetMutex) {
                if (targetVisible) {
                    glBindTexture(GL_TEXTURE_2D, targetBlack ? blackTarget : whiteTarget);
                    glPushMatrix();
                    float animOff = (float) (Math.abs(Math.sin(((double) System.currentTimeMillis()) / 150.0) * 0.015)) * targetSize;
                    glTranslatef(targetX - (targetSize + animOff) * 0.5f, targetY - (targetSize + animOff) * 0.5f, 0.0f);
                    glBegin(GL_QUADS);
                    {
                        glTexCoord2f(0.0f, 0.0f);
                        glVertex2f(0.0f, 0.0f);

                        glTexCoord2f(1.0f, 0.0f);
                        glVertex2f(targetSize + animOff, 0.0f);

                        glTexCoord2f(1.0f, 1.0f);
                        glVertex2f(targetSize + animOff, targetSize + animOff);

                        glTexCoord2f(0.0f, 1.0f);
                        glVertex2f(0.0f, targetSize + animOff);
                    }
                    glEnd();
                    glPopMatrix();
                    glBindTexture(GL_TEXTURE_2D, targetBlack ? blackText : whiteText);
                    glPushMatrix();
                    float tH = screenH * 0.1f, tW = (tH / textTextureH) * textTextureW;
                    if (tW > screenW) {
                        tW = screenW * 0.9f;
                        tH = (tW / textTextureW) * textTextureH;
                    }
                    glTranslatef((screenW - tW) * 0.5f, (targetY > screenH * 0.5f) ? 0 : (screenH - tH), 0.0f);
                    glBegin(GL_QUADS);
                    {
                        glTexCoord2f(0.0f, 0.0f);
                        glVertex2f(0.0f, 0.0f);

                        glTexCoord2f(1.0f, 0.0f);
                        glVertex2f(tW, 0.0f);

                        glTexCoord2f(1.0f, 1.0f);
                        glVertex2f(tW, tH);

                        glTexCoord2f(0.0f, 1.0f);
                        glVertex2f(0.0f, tH);
                    }
                    glEnd();
                    glPopMatrix();
                }
            }
            if (fakeGPULoadMs > 0) {
                Utils.sleep(fakeGPULoadMs);
            }
            glFlush();
            glfwSwapBuffers(window);
            frame++;
        }

        glDisable(GL_TEXTURE_2D);
        glDeleteTextures(whiteTarget);
        glDeleteTextures(blackTarget);
        glDeleteTextures(whiteText);
        glDeleteTextures(blackText);

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
        if (Utils.isLinux()) {
            //defuse "xorg bomb" left over by glfw
            if (!Config.X11_NOHACKS) {
                restoreX11ErrorHandler();
            }
        }
    }

    @Override
    public void flashColor(float r, float g, float b, double ms) {
        synchronized (bkMutex) {
            rr = r;
            gg = g;
            bb = b;
            flashMsLeft = ms;
        }
    }

    @Override
    public boolean setColor(float r, float g, float b) {
        synchronized (bkMutex) {
            if (r == or && g == og && b == ob) {
                return false;
            }
            or = r;
            og = g;
            ob = b;
            return true;
        }
    }

    @Override
    public void setTarget(float x, float y, float size, boolean black) {
        synchronized (targetMutex) {
            targetX = x * screenW;
            targetY = y * screenH;
            targetSize = size * Math.min(screenW, screenH);
            targetBlack = black;
            targetVisible = true;
        }
    }

    @Override
    public void setTargetAbsolute(float x, float y, float size, boolean black) {
        synchronized (targetMutex) {
            targetX = x;
            targetY = y;
            targetSize = size * Math.min(screenW, screenH);
            targetBlack = black;
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
            this.bkFlicker = bkFlicker;
        }
    }

    @Override
    public boolean getFlashOnClick() {
        return flashOnClick;
    }

    @Override
    public boolean isFlickering() {
        synchronized (bkMutex) {
            return bkFlicker;
        }
    }

    @Override
    public void setFakeLoad(long cpuMs, long gpuMs) {
        fakeCPULoadMs = cpuMs;
        fakeGPULoadMs = gpuMs;
    }

    @Override
    public long getFakeCPULoad() {
        return fakeCPULoadMs;
    }

    @Override
    public long getFakeGPULoad() {
        return fakeGPULoadMs;
    }

    @Override
    public void close() {
        glfwSetWindowShouldClose(window, true);
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

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try ( SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
                while (fc.read(buffer) != -1);
            }
        } else {
            try (
                     InputStream source = TestScreenGL.class.getClassLoader().getResourceAsStream(resource);  ReadableByteChannel rbc = Channels.newChannel(source)) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
                    }
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    public static void glfwInvoke(
            long window,
            GLFWWindowSizeCallbackI windowSizeCB,
            GLFWFramebufferSizeCallbackI framebufferSizeCB
    ) {
        try ( MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);

            if (windowSizeCB != null) {
                glfwGetWindowSize(window, w, h);
                windowSizeCB.invoke(window, w.get(0), h.get(0));
            }

            if (framebufferSizeCB != null) {
                glfwGetFramebufferSize(window, w, h);
                framebufferSizeCB.invoke(window, w.get(0), h.get(0));
            }
        }
    }

    private void premultiplyAlpha(ByteBuffer image, int w, int h, int comp) {
        int stride = w * 4;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * stride + x * 4;
                float alpha = (image.get(i + 3) & 0xFF) / 255.0f;
                image.put(i + 0, (byte) round(((image.get(i + 0) & 0xFF) * alpha)));
                image.put(i + 1, (byte) round(((image.get(i + 1) & 0xFF) * alpha)));
                image.put(i + 2, (byte) round(((image.get(i + 2) & 0xFF) * alpha)));
            }
        }
    }

    private void multiplyColor(ByteBuffer image, int w, int h, int comp, float cr, float cg, float cb) {
        for (int i = 0; i < w * h; i++) {
            image.put(comp * i + 0, (byte) round(((image.get(comp * i + 0) & 0xFF) * cr)));
            image.put(comp * i + 1, (byte) round(((image.get(comp * i + 1) & 0xFF) * cg)));
            image.put(comp * i + 2, (byte) round(((image.get(comp * i + 2) & 0xFF) * cb)));
        }
    }

    private int createTexture(ByteBuffer image, int w, int h, int comp) {
        int texID = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, texID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        int format;
        if (comp == 3) {
            if ((w & 3) != 0) {
                glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (w & 1));
            }
            format = GL_RGB;
        } else {
            premultiplyAlpha(image, w, h, comp);

            glEnable(GL_BLEND);
            glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

            format = GL_RGBA;
        }

        glTexImage2D(GL_TEXTURE_2D, 0, format, w, h, 0, format, GL_UNSIGNED_BYTE, image);

        ByteBuffer input_pixels = image;
        int input_w = w;
        int input_h = h;
        int mipmapLevel = 0;
        while (1 < input_w || 1 < input_h) {
            int output_w = Math.max(1, input_w >> 1);
            int output_h = Math.max(1, input_h >> 1);

            ByteBuffer output_pixels = memAlloc(output_w * output_h * comp);
            stbir_resize_uint8_generic(
                    input_pixels, input_w, input_h, input_w * comp,
                    output_pixels, output_w, output_h, output_w * comp,
                    comp, comp == 4 ? 3 : STBIR_ALPHA_CHANNEL_NONE, STBIR_FLAG_ALPHA_PREMULTIPLIED,
                    STBIR_EDGE_CLAMP,
                    STBIR_FILTER_MITCHELL,
                    STBIR_COLORSPACE_SRGB
            );

            if (mipmapLevel == 0) {
                stbi_image_free(image);
            } else {
                memFree(input_pixels);
            }

            glTexImage2D(GL_TEXTURE_2D, ++mipmapLevel, format, output_w, output_h, 0, format, GL_UNSIGNED_BYTE, output_pixels);

            input_pixels = output_pixels;
            input_w = output_w;
            input_h = output_h;
        }
        if (mipmapLevel == 0) {
            stbi_image_free(image);
        } else {
            memFree(input_pixels);
        }

        return texID;
    }

    /**
     * The following abomination works around a bug triggered by GLFW and Xorg
     * that causes occasional crashes after running a test (linux only)
     */
    private static Object savedX11ErrorHandler = null;

    public static final void saveX11ErrorHandler() {
        if (savedX11ErrorHandler != null) {
            return;
        }
        try {
            Class x11 = Class.forName("sun.awt.X11.XlibWrapper");
            Method setToolkitErrorHandler = x11.getDeclaredMethod("SetToolkitErrorHandler", new Class[]{});
            setToolkitErrorHandler.setAccessible(true);
            savedX11ErrorHandler = setToolkitErrorHandler.invoke(null, new Object[]{});
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static final void restoreX11ErrorHandler() {
        if (savedX11ErrorHandler == null) {
            return;
        }
        try {
            Class x11 = Class.forName("sun.awt.X11.XlibWrapper");
            Method XSetErrorHandler = x11.getDeclaredMethod("XSetErrorHandler", new Class[]{Long.TYPE});
            XSetErrorHandler.setAccessible(true);
            XSetErrorHandler.invoke(null, new Object[]{savedX11ErrorHandler});
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

}
