package com.zhengli.maze;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class MazePanel extends JPanel {

    private static final Color BACKGROUND = new Color(248, 246, 239);
    private static final Color WALL = new Color(52, 82, 67);
    private static final Color PATH = new Color(255, 253, 248);
    private static final Color EXIT = new Color(229, 169, 65);
    private static final Color HINT = new Color(213, 55, 48, 220);
    private static final Color TRAP = new Color(181, 102, 47);

    private MazeModel maze;
    private int cellCount;
    private int trapCount;
    private Runnable moveListener;
    private Runnable winListener;
    private Runnable trapListener;
    private Runnable resetListener;
    private int animationFrame;
    private int lookRow = 1;
    private int lookCol;
    private boolean inputEnabled = true;
    private boolean hintVisible;
    private BufferedImage catImage;
    private Consumer<Boolean> hintStateListener;

    public MazePanel(int cellCount, int trapCount, long seed) {
        this.cellCount = cellCount;
        this.trapCount = trapCount;
        this.maze = new MazeModel(cellCount, trapCount, seed);
        setPreferredSize(new Dimension(820, 760));
        setBackground(BACKGROUND);
        setFocusable(true);
        loadCatImage();
        bindKeys();
        Timer animationTimer = new Timer(100, event -> {
            animationFrame = (animationFrame + 1) % 40;
            repaint();
        });
        animationTimer.start();
    }

    private void loadCatImage() {
        try (InputStream input = MazePanel.class.getResourceAsStream(
                "/images/cat-player.png"
        )) {
            if (input != null) {
                catImage = ImageIO.read(input);
            }
        } catch (IOException ignored) {
            catImage = null;
        }
    }

    private void bindKeys() {
        bindMove("UP", -1, 0, "W");
        bindMove("DOWN", 1, 0, "S");
        bindMove("LEFT", 0, -1, "A");
        bindMove("RIGHT", 0, 1, "D");

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("R"), "newMaze");
        getActionMap().put("newMaze", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                newMaze();
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("H"), "hint");
        getActionMap().put("hint", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showHint();
            }
        });
    }

    private void bindMove(String key, int rowDelta, int colDelta, String alternate) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key), "move-" + key);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(alternate), "move-" + key);
        getActionMap().put("move-" + key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                movePlayer(rowDelta, colDelta);
            }
        });
    }

    private void movePlayer(int rowDelta, int colDelta) {
        if (!inputEnabled) {
            return;
        }
        lookRow = rowDelta;
        lookCol = colDelta;
        if (maze.move(rowDelta, colDelta)) {
            setHintVisible(false);
            if (moveListener != null) {
                moveListener.run();
            }
            if (maze.consumeTrapHit() && trapListener != null) {
                trapListener.run();
            }
            if (maze.isSolved() && winListener != null) {
                winListener.run();
            }
            repaint();
        }
    }

    public void newMaze() {
        maze = new MazeModel(cellCount, trapCount, System.nanoTime());
        inputEnabled = true;
        setHintVisible(false);
        if (resetListener != null) {
            resetListener.run();
        }
        repaint();
        requestFocusInWindow();
    }

    public void startLevel(int cellCount, int trapCount) {
        this.cellCount = cellCount;
        this.trapCount = trapCount;
        maze = new MazeModel(cellCount, trapCount, System.nanoTime());
        inputEnabled = true;
        setHintVisible(false);
        if (resetListener != null) {
            resetListener.run();
        }
        repaint();
        requestFocusInWindow();
    }

    public boolean showHint() {
        if (!inputEnabled) {
            return false;
        }
        if (hintVisible) {
            maze.clearHint();
            setHintVisible(false);
            repaint();
            return false;
        }
        boolean found = maze.showHint();
        setHintVisible(found);
        repaint();
        return found;
    }

    public boolean isHintVisible() {
        return hintVisible;
    }

    public void setHintStateListener(Consumer<Boolean> hintStateListener) {
        this.hintStateListener = hintStateListener;
    }

    private void setHintVisible(boolean visible) {
        hintVisible = visible;
        if (hintStateListener != null) {
            hintStateListener.accept(visible);
        }
    }

    public MazeModel getMaze() {
        return maze;
    }

    public void setMoveListener(Runnable moveListener) {
        this.moveListener = moveListener;
    }

    public void setWinListener(Runnable winListener) {
        this.winListener = winListener;
    }

    public void setTrapListener(Runnable trapListener) {
        this.trapListener = trapListener;
    }

    public void setResetListener(Runnable resetListener) {
        this.resetListener = resetListener;
    }

    public void setInputEnabled(boolean inputEnabled) {
        this.inputEnabled = inputEnabled;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        int rows = maze.getWallRows();
        int cols = maze.getWallCols();
        int margin = 28;
        double tile = Math.min(
                (getWidth() - margin * 2.0) / cols,
                (getHeight() - margin * 2.0) / rows
        );
        double offsetX = (getWidth() - tile * cols) / 2;
        double offsetY = (getHeight() - tile * rows) / 2;

        g2.setColor(new Color(236, 229, 213));
        g2.fill(new RoundRectangle2D.Double(
                offsetX - 10,
                offsetY - 10,
                tile * cols + 20,
                tile * rows + 20,
                24,
                24
        ));

        Set<MazeModel.Cell> hintCells = new HashSet<>(maze.getHintPath());
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = offsetX + col * tile;
                double y = offsetY + row * tile;
                if (maze.isWall(row, col)) {
                    g2.setColor(WALL);
                    g2.fill(new RoundRectangle2D.Double(
                            x + 0.5,
                            y + 0.5,
                            tile - 1,
                            tile - 1,
                            3,
                            3
                    ));
                } else {
                    g2.setColor(PATH);
                    g2.fillRect((int) Math.round(x), (int) Math.round(y),
                            (int) Math.ceil(tile), (int) Math.ceil(tile));
                }
            }
        }

        for (MazeModel.Cell trap : maze.getTraps()) {
            drawTrapMarker(g2, trap, offsetX, offsetY, tile);
        }

        for (MazeModel.Cell cell : hintCells) {
            g2.setColor(HINT);
            double diameter = tile * 0.42;
            double centerX = offsetX + (cell.col() * 2 + 1.5) * tile;
            double centerY = offsetY + (cell.row() * 2 + 1.5) * tile;
            g2.fill(new Ellipse2D.Double(
                    centerX - diameter / 2,
                    centerY - diameter / 2,
                    diameter,
                    diameter
            ));
        }

        drawExitMarker(g2, maze.getExit(), offsetX, offsetY, tile);
        drawPlayerCat(g2, maze.getPlayer(), offsetX, offsetY, tile);
        if (maze.isSolved()) {
            drawWinOverlay(g2);
        }
        g2.dispose();
    }

    private void drawTrapMarker(
            Graphics2D g2,
            MazeModel.Cell cell,
            double offsetX,
            double offsetY,
            double tile
    ) {
        double centerX = offsetX + (cell.col() * 2 + 1.5) * tile;
        double centerY = offsetY + (cell.row() * 2 + 1.5) * tile;
        double size = tile * 0.48;
        Polygon warning = new Polygon(
                new int[]{
                        (int) centerX,
                        (int) (centerX - size / 2),
                        (int) (centerX + size / 2)
                },
                new int[]{
                        (int) (centerY - size / 2),
                        (int) (centerY + size / 2),
                        (int) (centerY + size / 2)
                },
                3
        );
        g2.setColor(TRAP);
        g2.fill(warning);
        g2.setColor(new Color(76, 45, 28));
        g2.setStroke(new BasicStroke(Math.max(1f, (float) (tile * 0.05))));
        g2.draw(warning);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(
                "Microsoft YaHei",
                Font.BOLD,
                Math.max(8, (int) (tile * 0.28))
        ));
        int markWidth = g2.getFontMetrics().stringWidth("!");
        g2.drawString(
                "!",
                (float) (centerX - markWidth / 2.0),
                (float) (centerY + tile * 0.11)
        );
    }

    private void drawExitMarker(
            Graphics2D g2,
            MazeModel.Cell cell,
            double offsetX,
            double offsetY,
            double tile
    ) {
        double diameter = tile * 0.72;
        double centerX = offsetX + (cell.col() * 2 + 1.5) * tile;
        double centerY = offsetY + (cell.row() * 2 + 1.5) * tile;
        g2.setColor(EXIT);
        g2.fill(new Ellipse2D.Double(
                centerX - diameter / 2,
                centerY - diameter / 2,
                diameter,
                diameter
        ));
        g2.setColor(new Color(92, 67, 25));
        g2.setStroke(new BasicStroke(1.5f));
        g2.setFont(new Font("Microsoft YaHei", Font.BOLD, Math.max(9, (int) (tile * 0.28))));
        int width = g2.getFontMetrics().stringWidth("终");
        int height = g2.getFontMetrics().getAscent();
        g2.drawString("终", (float) (centerX - width / 2.0), (float) (centerY + height / 3.0));
    }

    private void drawPlayerCat(
            Graphics2D g2,
            MazeModel.Cell cell,
            double offsetX,
            double offsetY,
            double tile
    ) {
        double centerX = offsetX + (cell.col() * 2 + 1.5) * tile;
        double centerY = offsetY + (cell.row() * 2 + 1.5) * tile;
        double bob = Math.sin(animationFrame * 0.55) * tile * 0.045;
        centerY += bob;

        if (catImage != null) {
            double imageSize = tile * 0.95;
            AffineTransform previousTransform = g2.getTransform();
            g2.translate(centerX, centerY);
            if (lookCol < 0) {
                g2.scale(-1, 1);
            }
            g2.drawImage(
                    catImage,
                    (int) Math.round(-imageSize / 2),
                    (int) Math.round(-imageSize / 2),
                    (int) Math.round(imageSize),
                    (int) Math.round(imageSize),
                    null
            );
            g2.setTransform(previousTransform);
            return;
        }

        double headSize = tile * 0.72;
        double left = centerX - headSize / 2;
        double top = centerY - headSize / 2;
        Color catBlue = new Color(95, 113, 125);
        Color catDark = new Color(48, 59, 66);

        Polygon leftEar = new Polygon(
                new int[]{
                        (int) (centerX - headSize * 0.40),
                        (int) (centerX - headSize * 0.43),
                        (int) (centerX - headSize * 0.08)
                },
                new int[]{
                        (int) (top + headSize * 0.28),
                        (int) (top - headSize * 0.20),
                        (int) (top + headSize * 0.12)
                },
                3
        );
        Polygon rightEar = new Polygon(
                new int[]{
                        (int) (centerX + headSize * 0.40),
                        (int) (centerX + headSize * 0.43),
                        (int) (centerX + headSize * 0.08)
                },
                new int[]{
                        (int) (top + headSize * 0.28),
                        (int) (top - headSize * 0.20),
                        (int) (top + headSize * 0.12)
                },
                3
        );
        g2.setColor(catBlue);
        g2.fill(leftEar);
        g2.fill(rightEar);
        g2.setColor(catDark);
        g2.setStroke(new BasicStroke(Math.max(1f, (float) (tile * 0.035))));
        g2.draw(leftEar);
        g2.draw(rightEar);

        g2.setColor(catBlue);
        g2.fill(new Ellipse2D.Double(left, top, headSize, headSize));
        g2.setColor(catDark);
        g2.draw(new Ellipse2D.Double(left, top, headSize, headSize));

        double eyeSize = headSize * 0.22;
        double eyeOffset = headSize * 0.20;
        double eyeY = top + headSize * 0.38;
        drawEye(g2, centerX - eyeOffset, eyeY, eyeSize);
        drawEye(g2, centerX + eyeOffset, eyeY, eyeSize);

        g2.setColor(new Color(244, 233, 211));
        g2.fill(new Ellipse2D.Double(
                centerX - headSize * 0.22,
                centerY + headSize * 0.02,
                headSize * 0.44,
                headSize * 0.28
        ));
        g2.setColor(catDark);
        g2.fill(new Ellipse2D.Double(
                centerX - headSize * 0.055,
                centerY + headSize * 0.10,
                headSize * 0.11,
                headSize * 0.08
        ));
        g2.drawLine(
                (int) (centerX - headSize * 0.02),
                (int) (centerY + headSize * 0.23),
                (int) (centerX),
                (int) (centerY + headSize * 0.29)
        );

        g2.setColor(new Color(188, 61, 55));
        g2.fill(new Ellipse2D.Double(
                centerX - headSize * 0.12,
                centerY + headSize * 0.30,
                headSize * 0.24,
                headSize * 0.09
        ));
    }

    private void drawEye(Graphics2D g2, double centerX, double centerY, double eyeSize) {
        g2.setColor(new Color(250, 238, 210));
        g2.fill(new Ellipse2D.Double(
                centerX - eyeSize / 2,
                centerY - eyeSize / 2,
                eyeSize,
                eyeSize
        ));
        g2.setColor(Color.WHITE);
        double pupilShiftX = lookCol * eyeSize * 0.12;
        double pupilShiftY = lookRow * eyeSize * 0.12;
        g2.fill(new Ellipse2D.Double(
                centerX - eyeSize * 0.18 + pupilShiftX,
                centerY - eyeSize * 0.18 + pupilShiftY,
                eyeSize * 0.36,
                eyeSize * 0.36
        ));
        g2.setColor(new Color(34, 40, 43));
        g2.fill(new Ellipse2D.Double(
                centerX - eyeSize * 0.08 + pupilShiftX,
                centerY - eyeSize * 0.08 + pupilShiftY,
                eyeSize * 0.16,
                eyeSize * 0.16
        ));
    }

    private void drawWinOverlay(Graphics2D g2) {
        int boxWidth = Math.min(460, getWidth() - 90);
        int boxHeight = 90;
        int x = (getWidth() - boxWidth) / 2;
        int y = (getHeight() - boxHeight) / 2 - 60;

        g2.setColor(new Color(31, 52, 42, 218));
        g2.fill(new RoundRectangle2D.Double(x, y, boxWidth, boxHeight, 26, 26));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Microsoft YaHei", Font.BOLD, 26));
        drawCenteredString(g2, "恭喜你通关啦！", y + 55);
    }

    private void drawCenteredString(Graphics2D g2, String text, int baselineY) {
        int width = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (getWidth() - width) / 2, baselineY);
    }
}
