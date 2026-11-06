package com.zhengli.maze;

import java.util.List;

public final class MazeModelTest {

    private MazeModelTest() {
    }

    public static void main(String[] args) {
        for (long seed = 1; seed <= 20; seed++) {
            MazeModel maze = new MazeModel(12, seed);
            List<MazeModel.Cell> path = maze.findShortestPath();
            require(!path.isEmpty(), "迷宫必须存在可行路径，seed=" + seed);
            require(
                    path.get(0).equals(maze.getPlayer()),
                    "最短路径必须从玩家所在位置开始"
            );
            require(
                    path.get(path.size() - 1).equals(maze.getExit()),
                    "最短路径必须到达出口"
            );
            for (int index = 1; index < path.size(); index++) {
                MazeModel.Cell previous = path.get(index - 1);
                MazeModel.Cell current = path.get(index);
                require(
                        Math.abs(previous.row() - current.row())
                                + Math.abs(previous.col() - current.col()) == 1,
                        "提示路径的相邻步骤必须上下左右相连"
                );
                require(
                        maze.hasOpenPassage(previous, current),
                        "提示路径不能穿过墙体"
                );
            }
        }

        MazeModel maze = new MazeModel(10, 20261107L);
        int movesBefore = maze.getMoves();
        boolean moved = false;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] direction : directions) {
            if (maze.move(direction[0], direction[1])) {
                moved = true;
                break;
            }
        }
        require(moved, "起点周围应至少存在一个可移动方向");
        require(maze.getMoves() == movesBefore + 1, "移动后步数应增加");
        require(maze.showHint(), "路径提示应可用");
        require(!maze.getHintPath().isEmpty(), "路径提示不能为空");
        maze.clearHint();
        require(maze.getHintPath().isEmpty(), "第二次切换应清除路径提示");

        MazeModel trappedMaze = new MazeModel(10, 3, 20261109L);
        require(trappedMaze.getTraps().size() == 3, "第 3 关应放置 3 个陷阱");
        List<MazeModel.Cell> safePath = trappedMaze.findShortestPath();
        for (MazeModel.Cell trap : trappedMaze.getTraps()) {
            require(
                    !safePath.contains(trap),
                    "安全提示路径不能经过陷阱"
            );
        }

        MazeModel.Cell trap = trappedMaze.getTraps().iterator().next();
        List<MazeModel.Cell> pathToTrap = trappedMaze.findPathTo(trap);
        require(!pathToTrap.isEmpty(), "陷阱应位于可到达的通路中");
        for (int index = 1; index < pathToTrap.size(); index++) {
            MazeModel.Cell previous = pathToTrap.get(index - 1);
            MazeModel.Cell current = pathToTrap.get(index);
            trappedMaze.move(
                    current.row() - previous.row(),
                    current.col() - previous.col()
            );
        }
        require(trappedMaze.getTrapHits() == 1, "踩中陷阱后应记录一次");
        require(
                trappedMaze.getPlayer().equals(new MazeModel.Cell(0, 0)),
                "踩中陷阱后应回到起点"
        );

        MazeModel collisionMaze = new MazeModel(12, 20261108L);
        MazeModel.Cell originalPlayer = collisionMaze.getPlayer();
        boolean foundBlockedDirection = false;
        for (int[] direction : directions) {
            if (!collisionMaze.canMove(direction[0], direction[1])) {
                require(!collisionMaze.move(direction[0], direction[1]), "墙体应阻止移动");
                require(
                        collisionMaze.getPlayer().equals(originalPlayer),
                        "撞墙后玩家位置不能改变"
                );
                foundBlockedDirection = true;
                break;
            }
        }
        require(foundBlockedDirection, "测试迷宫应至少存在一个被墙阻挡的方向");

        System.out.println("MazeModelTest PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
