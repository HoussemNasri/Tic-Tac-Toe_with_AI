package tictactoe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Minimax {
    private final Board board;
    private final Main.Player minimaxPlayer;

    public Minimax(Board board) {
        this.board = board;
        this.minimaxPlayer = board.getCurrentPlayer();
    }

    public MinimaxMove minimax(Main.Player player) {
        List<int[]> possibleMoves = board.getPossibleMoves();

        if (board.isWinner(minimaxPlayer)) {
            return new MinimaxMove(null, 10);
        } else if (board.isWinner(otherPlayer())) {
            return new MinimaxMove(null, -10);
        } else if (possibleMoves.size() == 0) {
            return new MinimaxMove(null, 0);
        }

        List<MinimaxMove> moves = new ArrayList<>();
        for (int[] move : possibleMoves) {
            board.set(move[0] - 1, move[1] - 1, player);
            int moveScore;
            if (player == minimaxPlayer) {
                moveScore = minimax(otherPlayer()).score;
            } else {
                moveScore = minimax(minimaxPlayer).score;
            }
            // Reset the board state
            board.set(move[0] - 1, move[1] - 1, Board.BoardCell.EMPTY);

            moves.add(new MinimaxMove(move, moveScore));
        }
        MinimaxMove bestMove;
        if (player == minimaxPlayer) {
            bestMove = moves.stream().max(Comparator.comparingInt(o -> o.score)).get();
        } else {
            bestMove = moves.stream().min(Comparator.comparingInt(o -> o.score)).get();
        }
        return bestMove;
    }

    public MinimaxMove minmax() {
        return minimax(minimaxPlayer);
    }

    private Main.Player otherPlayer() {
        return board.invertPlayer(minimaxPlayer);
    }

    public static class MinimaxMove implements Comparable<MinimaxMove> {
        public int[] move;
        public int score;

        public MinimaxMove(int[] move, int score) {
            this.move = move;
            this.score = score;
        }

        @Override
        public int compareTo(MinimaxMove o) {
            return Integer.compare(score, o.score);
        }
    }
}
