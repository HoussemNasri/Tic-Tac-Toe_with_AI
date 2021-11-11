package tictactoe;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class Board {
    public static enum BoardCell {
        X('X'), O('O'), EMPTY('_');
        private final char value;

        BoardCell(char value) {
            this.value = value;
        }

        char getValue() {
            return value;
        }
    }

    private final char[][] boardState;
    private final Main.Player player1;
    private final Main.Player player2;

    private Main.Player currentPlayer;

    public Board(Main.Player player1, Main.Player player2, char[][] boardState) {
        this.player1 = player1;
        this.player2 = player2;
        this.boardState = boardState;
        this.currentPlayer = player1;
    }

    public Board(Main.Player player1, Main.Player player2) {
        this(player1, player2, new char[][] {
                {'_', '_', '_'},
                {'_', '_', '_'},
                {'_', '_', '_'}});
    }

    public char[][] getBoardState() {
        return boardState;
    }

    public void setCurrentPlayer(Main.Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Main.Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Main.Player getNOTCurrentPlayer() {
        return invertPlayer(currentPlayer);
    }

    public boolean isWinner(char playerSymbol) {
        for (int i = 0; i < 3; i++) {
            boolean isWin = true;
            for (int j = 0; j < 3; j++) {
                if (boardState[i][j] != playerSymbol) {
                    isWin = false;
                    break;
                }
            }
            if (isWin) {
                return true;
            }
        }

        for (int i = 0; i < 3; i++) {
            boolean isWin = true;
            for (int j = 0; j < 3; j++) {
                if (boardState[j][i] != playerSymbol) {
                    isWin = false;
                    break;
                }
            }
            if (isWin) {
                return true;
            }
        }

        return (boardState[0][0] == playerSymbol && boardState[1][1] == playerSymbol && boardState[2][2] == playerSymbol) ||
                (boardState[0][2] == playerSymbol && boardState[1][1] == playerSymbol && boardState[2][0] == playerSymbol);
    }

    public boolean isWinner(Main.Player player) {
        return isWinner(player.getSymbolChar());
    }

    public boolean isXWins() {
        return isWinner('X');
    }

    public boolean isOWins() {
        return isWinner('O');
    }

    public Main.Player invertPlayer(Main.Player player) {
        return player == player1 ? player2 : player1;
    }

    public List<int[]> getPossibleMoves() {
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (boardState[i][j] == '_') {
                    moves.add(new int[] {i + 1, j + 1});
                }
            }
        }
        return moves;
    }

    public Main.Player getPlayer1() {
        return player1;
    }

    public Main.Player getPlayer2() {
        return player2;
    }

    public void set(int i, int j, BoardCell boardCell) {
        boardState[i][j] = boardCell.getValue();
    }

    public void set(int i, int j, Main.Player player) {
        set(i, j, BoardCell.valueOf(player.getSymbolChar() + ""));
    }

    public char get(int i, int j) {
        return boardState[i][j];
    }

    public void nextTurn() {
        currentPlayer = getNOTCurrentPlayer();
    }

    public boolean isFull() {
        for (char[] row : boardState) {
            boolean hasEmptySpot = CharBuffer.wrap(row).chars().filter(c -> c == '_').findAny().isPresent();
            if (hasEmptySpot) {
                return false;
            }
        }
        return true;
    }

    public boolean isGameOver() {
        return isDraw() || isOWins() || isXWins();
    }

    public boolean isNotGameOver() {
        return !isGameOver();
    }

    public boolean isDraw() {
        return isFull() && !isXWins() && !isOWins();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("---------\n");
        for (char[] row : boardState) {
            stringBuilder.append("| ");
            for (char c : row) {
                stringBuilder.append(c == '_' ? "  " : Character.toUpperCase(c) + " ");
            }
            stringBuilder.append("|\n");
        }
        stringBuilder.append("---------");
        return stringBuilder.toString();
    }
}
