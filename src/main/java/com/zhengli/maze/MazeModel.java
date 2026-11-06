package com.zhengli.maze;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class MazeModel {

    private static final int[][] DIRECTIONS = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
    };

    private final int cellCount;
    private final int trapCount;
    private final boolean[][] walls;
    private final Random random;
    private final Set<Cell> traps = new HashSet<>();
    private Cell player;
    private Cell exit;
    private int moves;
    private int trapHits;
    private boolean lastMoveHitTrap;
    private List<Cell> hintPath = List.of();

    public MazeModel(int cellCount, long seed) {
        this(cellCount, 0, seed);
    }

    public MazeModel(int cellCount, int trapCount, long seed) {
        if (cellCount < 4) {
            throw new IllegalArgumentException("迷宫尺寸不能小于 4");
        }
        if (trapCount < 0) {
            throw new IllegalArgumentException("陷阱数量不能为负数");
        }
        this.cellCount = cellCount;
        this.trapCount = trapCount;
        this.walls = new boolean[cellCount * 2 + 1][cellCount * 2 + 1];
        this.random = new Random(seed);
        generate();
    }

    public static MazeModel random(int cellCount) {
        return new MazeModel(cellCount, System.nanoTime());
    }

    private void generate() {
        for (boolean[] row : walls) {
            java.util.Arrays.fill(row, true);
        }

        boolean[][] visited = new boolean[cellCount][cellCount];
        Deque<Cell> stack = new ArrayDeque<>();
        Cell start = new Cell(0, 0);
        stack.push(start);
        visited[0][0] = true;
        carveCell(start);

        while (!stack.isEmpty()) {
            Cell current = stack.peek();
            List<Cell> neighbors = unvisitedNeighbors(current, visited);
            if (neighbors.isEmpty()) {
                stack.pop();
                continue;
            }

            Cell next = neighbors.get(random.nextInt(neighbors.size()));
            carveWallBetween(current, next);
            carveCell(next);
            visited[next.row()][next.col()] = true;
            stack.push(next);
        }

        player = start;
        exit = new Cell(cellCount - 1, cellCount - 1);
        moves = 0;
        trapHits = 0;
        lastMoveHitTrap = false;
        hintPath = List.of();
        placeTraps();
    }

    private List<Cell> unvisitedNeighbors(Cell cell, boolean[][] visited) {
        List<Cell> result = new ArrayList<>(4);
        for (int[] direction : DIRECTIONS) {
            int row = cell.row() + direction[0];
            int col = cell.col() + direction[1];
            if (row >= 0 && row < cellCount && col >= 0 && col < cellCount
                    && !visited[row][col]) {
                result.add(new Cell(row, col));
            }
        }
        return result;
    }

    private void carveCell(Cell cell) {
        walls[cell.row() * 2 + 1][cell.col() * 2 + 1] = false;
    }

    private void carveWallBetween(Cell from, Cell to) {
        int wallRow = from.row() * 2 + 1 + (to.row() - from.row());
        int wallCol = from.col() * 2 + 1 + (to.col() - from.col());
        walls[wallRow][wallCol] = false;
    }

    private void placeTraps() {
        traps.clear();
        Set<Cell> protectedPath = new HashSet<>(findShortestPath());
        List<Cell> candidates = new ArrayList<>();
        for (int row = 0; row < cellCount; row++) {
            for (int col = 0; col < cellCount; col++) {
                Cell cell = new Cell(row, col);
                if (!cell.equals(player)
                        && !cell.equals(exit)
                        && !protectedPath.contains(cell)) {
                    candidates.add(cell);
                }
            }
        }
        Collections.shuffle(candidates, random);
        int trapsToPlace = Math.min(trapCount, candidates.size());
        for (int index = 0; index < trapsToPlace; index++) {
            traps.add(candidates.get(index));
        }
    }

    public boolean move(int rowDelta, int colDelta) {
        if (isSolved()) {
            return false;
        }
        if (!canMove(rowDelta, colDelta)) {
            return false;
        }
        Cell next = new Cell(player.row() + rowDelta, player.col() + colDelta);
        player = next;
        moves++;
        lastMoveHitTrap = traps.contains(next);
        if (lastMoveHitTrap) {
            trapHits++;
            player = new Cell(0, 0);
        }
        hintPath = List.of();
        return true;
    }

    public boolean canMove(int rowDelta, int colDelta) {
        if (Math.abs(rowDelta) + Math.abs(colDelta) != 1) {
            return false;
        }
        return canTraverse(
                player,
                new Cell(player.row() + rowDelta, player.col() + colDelta)
        );
    }

    public boolean hasOpenPassage(Cell from, Cell to) {
        return canTraverse(from, to);
    }

    public List<Cell> findShortestPath() {
        return findPathTo(exit);
    }

    public List<Cell> findPathTo(Cell target) {
        Map<Cell, Cell> parent = new HashMap<>();
        Set<Cell> visited = new HashSet<>();
        Deque<Cell> queue = new ArrayDeque<>();
        queue.add(player);
        visited.add(player);

        while (!queue.isEmpty()) {
            Cell current = queue.removeFirst();
            if (current.equals(target)) {
                break;
            }
            for (int[] direction : DIRECTIONS) {
                Cell next = new Cell(
                        current.row() + direction[0],
                        current.col() + direction[1]
                );
                if (traps.contains(next) && !next.equals(target)) {
                    continue;
                }
                if (canTraverse(current, next) && visited.add(next)) {
                    parent.put(next, current);
                    queue.addLast(next);
                }
            }
        }

        if (!visited.contains(target)) {
            return List.of();
        }

        List<Cell> path = new ArrayList<>();
        Cell current = target;
        while (!current.equals(player)) {
            path.add(current);
            current = parent.get(current);
        }
        path.add(player);
        Collections.reverse(path);
        return path;
    }

    public boolean showHint() {
        List<Cell> path = findShortestPath();
        hintPath = path.size() > 1 ? List.copyOf(path.subList(1, path.size())) : List.of();
        return !hintPath.isEmpty();
    }

    public void clearHint() {
        hintPath = List.of();
    }

    public boolean consumeTrapHit() {
        boolean hit = lastMoveHitTrap;
        lastMoveHitTrap = false;
        return hit;
    }

    public boolean isSolved() {
        return player.equals(exit);
    }

    private boolean isOpen(Cell cell) {
        return cell.row() >= 0 && cell.row() < cellCount
                && cell.col() >= 0 && cell.col() < cellCount
                && !walls[cell.row() * 2 + 1][cell.col() * 2 + 1];
    }

    private boolean canTraverse(Cell from, Cell to) {
        if (!isOpen(to)) {
            return false;
        }
        int rowDelta = to.row() - from.row();
        int colDelta = to.col() - from.col();
        if (Math.abs(rowDelta) + Math.abs(colDelta) != 1) {
            return false;
        }
        int wallRow = from.row() * 2 + 1 + rowDelta;
        int wallCol = from.col() * 2 + 1 + colDelta;
        return !walls[wallRow][wallCol];
    }

    public int getCellCount() {
        return cellCount;
    }

    public int getTrapCount() {
        return trapCount;
    }

    public boolean isWall(int row, int col) {
        return walls[row][col];
    }

    public int getWallRows() {
        return walls.length;
    }

    public int getWallCols() {
        return walls[0].length;
    }

    public Cell getPlayer() {
        return player;
    }

    public Cell getExit() {
        return exit;
    }

    public int getMoves() {
        return moves;
    }

    public int getTrapHits() {
        return trapHits;
    }

    public Set<Cell> getTraps() {
        return Collections.unmodifiableSet(traps);
    }

    public boolean isTrap(Cell cell) {
        return traps.contains(cell);
    }

    public List<Cell> getHintPath() {
        return hintPath;
    }

    public record Cell(int row, int col) {
    }
}
