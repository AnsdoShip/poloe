package com.ansdoship.poloe;

public class Poloe {

    private Poloe(){}

    public static final int CANVAS_WIDTH = 256;
    public static final int CANVAS_HEIGHT = 224;

    protected static volatile Delegate delegate;

    public interface Delegate {
        void initialize();
        void setPixel(int x, int y, int color);
        int getPixel(int x, int y);
        int getCanvasPixel(int x, int y);
        void update();
        int fps();
        void beep();
        void postRunnable(Runnable runnable);
        void setApplet(Applet applet);
        Applet getApplet();
        void setTitle(CharSequence title);
    }

    public interface Applet {
        void create();
        void start();
        void pause();
        void resume();
        void stop();
        void destroy();
        void render();
    }

    public static void setDelegate(Delegate delegate) {
        Poloe.delegate = delegate;
    }

    public static void initialize() {
        delegate.initialize();
    }

    public static void setPixel(int x, int y, int color) {
        delegate.setPixel(x, y, color);
    }

    public static int getPixel(int x, int y) {
        return delegate.getPixel(x, y);
    }

    public static int getCanvasPixel(int x, int y) {
        return delegate.getCanvasPixel(x, y);
    }

    public static void update() {
        delegate.update();
    }

    public static int fps() {
        return delegate.fps();
    }

    public static void beep() {
        delegate.beep();
    }

    public static void postRunnable(Runnable runnable) {
        delegate.postRunnable(runnable);
    }

    public static void setApplet(Applet applet) {
        delegate.setApplet(applet);
    }

    public static Applet getApplet() {
        return delegate.getApplet();
    }

    public static void setTitle(CharSequence title) {
        delegate.setTitle(title);
    }

}
