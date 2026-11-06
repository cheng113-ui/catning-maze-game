package com.zhengli.maze;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public final class MazePanelPreview {

    private MazePanelPreview() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        SwingUtilities.invokeAndWait(() -> {
            try {
                MazePanel panel = new MazePanel(12, 3, 20261107L);
                panel.setSize(820, 760);

                MazeModel maze = panel.getMaze();
                panel.showHint();
                BufferedImage hintImage = new BufferedImage(
                        820,
                        760,
                        BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D hintGraphics = hintImage.createGraphics();
                panel.paint(hintGraphics);
                hintGraphics.dispose();
                ImageIO.write(
                        hintImage,
                        "png",
                        new File("build/maze-hint-preview.png")
                );

                List<MazeModel.Cell> path = maze.findShortestPath();
                MazeModel.Cell previous = path.get(0);
                for (int index = 1; index < path.size(); index++) {
                    MazeModel.Cell current = path.get(index);
                    maze.move(
                            current.row() - previous.row(),
                            current.col() - previous.col()
                    );
                    previous = current;
                }

                BufferedImage image = new BufferedImage(
                        820,
                        760,
                        BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D graphics = image.createGraphics();
                panel.paint(graphics);
                graphics.dispose();

                File output = new File("build/maze-preview.png");
                ImageIO.write(image, "png", output);
                System.out.println(output.getAbsolutePath());
                System.out.println(
                        new File("build/maze-hint-preview.png").getAbsolutePath()
                );
                System.exit(0);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        });
    }
}
