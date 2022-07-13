package com.ansdoship.poloe.j2se;

import com.ansdoship.poloe.Poloe;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

public class PoloeFrame extends Frame implements Poloe.Delegate {

    protected final BufferedImage tmpBuffer;
    protected final BufferedImage canvasBuffer;
    protected boolean requestUpdate = false;
    protected volatile boolean looping = false;
    protected volatile int fps = 0;
    public static final int MAX_SLEEP_TIME = Math.round(1000 / 16.67f); /* 60 FPS */
    protected volatile long elapsed = 0;
    protected volatile boolean initialized = false;
    private final List<Runnable> preRunnables = new LinkedList<>();
    private Poloe.Applet applet;

    public boolean isLooping() {
        return looping;
    }

    public PoloeFrame() throws HeadlessException {
        this("");
    }

    public PoloeFrame(GraphicsConfiguration gc) {
        this("", gc);
    }

    public PoloeFrame(String title) throws HeadlessException {
        this(title, null);
    }

    public PoloeFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
        setLocationByPlatform(true);
        setBackground(Color.BLACK);
        setMinimumSize(new Dimension(Poloe.CANVAS_WIDTH, Poloe.CANVAS_HEIGHT));
        tmpBuffer = new BufferedImage(Poloe.CANVAS_WIDTH, Poloe.CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
        canvasBuffer = new BufferedImage(Poloe.CANVAS_WIDTH, Poloe.CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
        addWindowListener(new WindowListener() {
            @Override
            public void windowActivated(WindowEvent e) {
                if (applet != null) applet.resume();
            }
            @Override
            public void windowDeactivated(WindowEvent e) {
                if (applet != null) applet.pause();
            }
            @Override
            public void windowOpened(WindowEvent e) {
                if (applet != null) applet.start();
            }
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
            @Override
            public void windowClosed(WindowEvent e) {
                if (applet != null) {
                    applet.stop();
                }
                looping = false;
            }
            @Override
            public void windowIconified(WindowEvent e) {
                if (applet != null) applet.stop();
            }
            @Override
            public void windowDeiconified(WindowEvent e) {
                if (applet != null) applet.start();
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                update(false);
            }
        });
    }

    @Override
    public synchronized void initialize() {
        if (isLooping()) return;
        looping = true;
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                while (looping) {
                    if (applet != null) applet.render();
                    repaint();
                    long sleepTime = MAX_SLEEP_TIME - elapsed;
                    if (sleepTime > 0) {
                        fps = MAX_SLEEP_TIME;
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        fps = (int) (1000 / elapsed);
                    }
                }
                if (applet != null) applet.destroy();
                System.exit(0);
            }
        });
        setVisible(true);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (Runnable runnable : preRunnables) {
                    EventQueue.invokeLater(runnable);
                }
            }
        });
        initialized = true;
    }

    @Override
    public void paint(Graphics g) {
        if (requestUpdate) {
            int width = getWidth();
            int height = getHeight();
            float ratio = Math.min((float) width / Poloe.CANVAS_WIDTH, (float) height / Poloe.CANVAS_HEIGHT);
            int targetWidth = Math.round(Poloe.CANVAS_WIDTH * ratio);
            int targetHeight = Math.round(Poloe.CANVAS_HEIGHT * ratio);
            g.drawImage(canvasBuffer, (width - targetWidth) / 2, (height - targetHeight) / 2, targetWidth, targetHeight, null);
        }
    }

    @Override
    public void update(Graphics g) {
        long time = System.currentTimeMillis();
        if (requestUpdate) {
            Image tmp = createImage(getWidth(), getHeight());
            Graphics gTmp = tmp.getGraphics();
            gTmp.setColor(getBackground());
            gTmp.fillRect(0, 0, getWidth(), getHeight());
            paint(gTmp);
            gTmp.dispose();
            g.drawImage(tmp, 0, 0, null);
            requestUpdate = false;
        }
        long now = System.currentTimeMillis();
        elapsed = now - time;
    }

    public void swapBuffer() {
        Graphics gCanvas = canvasBuffer.getGraphics();
        gCanvas.drawImage(tmpBuffer, 0, 0, null);
        gCanvas.dispose();
    }

    @Override
    public void setPixel(int x, int y, int color) {
        tmpBuffer.setRGB(x, y, color);
    }

    @Override
    public int getPixel(int x, int y) {
        return tmpBuffer.getRGB(x, y);
    }

    @Override
    public int getCanvasPixel(int x, int y) {
        return canvasBuffer.getRGB(x, y);
    }

    @Override
    public void update() {
        update(true);
    }

    public synchronized void update(boolean swapBuffer) {
        this.requestUpdate = true;
        if (swapBuffer) swapBuffer();
        repaint();
    }

    @Override
    public int fps() {
        return fps;
    }

    @Override
    public void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    @Override
    public void postRunnable(Runnable runnable) {
        if (!initialized) preRunnables.add(runnable);
        else EventQueue.invokeLater(runnable);
    }

    @Override
    public synchronized void setApplet(Poloe.Applet applet) {
        if (this.applet != null) {
            this.applet.pause();
            this.applet.stop();
            this.applet.destroy();
        }
        this.applet = applet;
        applet.create();
        applet.start();
        applet.resume();
    }

    @Override
    public synchronized Poloe.Applet getApplet() {
        return applet;
    }

    @Override
    public void setTitle(CharSequence title) {
        postRunnable(new Runnable() {
            @Override
            public void run() {
                setTitle(title.toString());
            }
        });
    }

}
