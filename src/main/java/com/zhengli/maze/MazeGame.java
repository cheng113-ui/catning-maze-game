package com.zhengli.maze;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;

public final class MazeGame extends JFrame {

    private static final int MAX_LEVEL = 10;
    private static final int BASE_CELL_COUNT = 8;
    private static final int LEVEL_TIME_LIMIT_SECONDS = 120;

    private final MazePanel mazePanel;
    private final JButton hintButton = new JButton("💡");
    private final JLabel levelLabel;
    private final JLabel countdownLabel;
    private final Timer timer;
    private int level = 1;
    private int elapsedSeconds;
    private int remainingSeconds = LEVEL_TIME_LIMIT_SECONDS;
    private boolean solved;
    private boolean timedOut;

    private MazeGame() {
        super("猫宁迷宫");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 900);
        setMinimumSize(new Dimension(760, 820));
        setLocationRelativeTo(null);

        mazePanel = new MazePanel(
                cellCountForLevel(level),
                level,
                System.nanoTime()
        );
        levelLabel = new JLabel();
        countdownLabel = new JLabel();

        timer = new Timer(1000, event -> {
            if (!solved && !timedOut) {
                elapsedSeconds++;
                remainingSeconds--;
                updateCountdownLabel();
                if (remainingSeconds <= 0) {
                    onTimeOut();
                }
            }
        });

        mazePanel.setWinListener(this::onSolved);
        mazePanel.setResetListener(this::onLevelRestarted);
        mazePanel.setHintStateListener(visible ->
                hintButton.setText(visible ? "✕" : "💡")
        );

        add(buildHeader(), BorderLayout.NORTH);
        add(mazePanel, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        resetTimer();
        updateLevelLabel();
        updateCountdownLabel();
        timer.start();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(54, 88, 72));
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 16, 24));

        JLabel title = new JLabel("猫宁迷宫");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 30));

        levelLabel.setForeground(new Color(247, 232, 198));
        levelLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        countdownLabel.setForeground(Color.WHITE);
        countdownLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightSide.setOpaque(false);
        rightSide.add(levelLabel);
        rightSide.add(countdownLabel);

        header.add(title, BorderLayout.WEST);
        header.add(rightSide, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        footer.setBackground(new Color(248, 246, 239));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 18, 14, 18));

        hintButton.addActionListener(event -> {
            mazePanel.showHint();
        });
        hintButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        hintButton.setFocusPainted(false);
        hintButton.setPreferredSize(new Dimension(52, 36));

        JButton newButton = new JButton("↻");
        newButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        newButton.setFocusPainted(false);
        newButton.setPreferredSize(new Dimension(52, 36));
        newButton.addActionListener(event -> restartLevel());

        footer.add(hintButton);
        footer.add(newButton);
        return footer;
    }

    private void onSolved() {
        if (solved) {
            return;
        }
        solved = true;
        timer.stop();
        mazePanel.setInputEnabled(false);
        SwingUtilities.invokeLater(this::showLevelCompleteDialog);
    }

    private void showLevelCompleteDialog() {
        String summary = "恭喜你通关啦！\n"
                + "关卡 " + level + "\n"
                + "总步数：" + mazePanel.getMaze().getMoves() + "\n"
                + "用时：" + formatTime(elapsedSeconds);

        if (level < MAX_LEVEL) {
            int nextLevel = level + 1;
            int result = JOptionPane.showOptionDialog(
                    this,
                    summary,
                    "关卡 " + level,
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new Object[]{"→", "×"},
                    "→"
            );
            if (result == 0) {
                level = nextLevel;
                mazePanel.startLevel(cellCountForLevel(level), level);
            }
        } else {
            int result = JOptionPane.showOptionDialog(
                    this,
                    summary,
                    "关卡 10",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new Object[]{"↻", "×"},
                    "↻"
            );
            if (result == 0) {
                level = 1;
                mazePanel.startLevel(cellCountForLevel(level), level);
            }
        }
    }

    private void restartLevel() {
        mazePanel.newMaze();
    }

    private void onLevelRestarted() {
        solved = false;
        timedOut = false;
        resetTimer();
        remainingSeconds = LEVEL_TIME_LIMIT_SECONDS;
        mazePanel.setInputEnabled(true);
        timer.start();
        updateLevelLabel();
        updateCountdownLabel();
    }

    private void resetTimer() {
        elapsedSeconds = 0;
    }

    private void updateLevelLabel() {
        levelLabel.setText("关卡 " + level + "/" + MAX_LEVEL);
    }

    private void updateCountdownLabel() {
        countdownLabel.setText("⏱ " + formatTime(Math.max(0, remainingSeconds)));
        countdownLabel.setForeground(
                remainingSeconds <= 10
                        ? new Color(255, 196, 184)
                        : Color.WHITE
        );
    }

    private void onTimeOut() {
        if (timedOut || solved) {
            return;
        }
        timedOut = true;
        timer.stop();
        if (mazePanel.isHintVisible()) {
            mazePanel.showHint();
        }
        mazePanel.setInputEnabled(false);
        int result = JOptionPane.showOptionDialog(
                this,
                "时间到啦！\n"
                        + "关卡 " + level + "\n"
                        + "总步数：" + mazePanel.getMaze().getMoves(),
                "关卡 " + level,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                new Object[]{"↻", "×"},
                "↻"
        );
        if (result == 0) {
            restartLevel();
        }
    }

    private static int cellCountForLevel(int level) {
        return BASE_CELL_COUNT + level - 1;
    }

    private static String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MazeGame().setVisible(true));
    }
}
