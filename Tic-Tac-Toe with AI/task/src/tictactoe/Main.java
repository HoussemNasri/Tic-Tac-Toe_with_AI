package tictactoe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Main {

    abstract static class Result {
        static class Success<TT> extends Result {
            private final TT result;

            public Success(TT result) {
                this.result = result;
            }
        }

        static class Error extends Result {
            private final String error;

            public Error(String error) {
                this.error = error;
            }
        }

        public boolean isSuccess() {
            return this instanceof Success && ((Success) this).result != null;
        }

        public <T> T success() {
            return ((Success<T>) this).result;
        }

        public String error() {
            return ((Error) this).error;
        }
    }

    private enum Level {
        EASY, MEDIUM, HARD
    }

    static class Player {
        private enum Symbol {
            X, O
        }

        private final String name;
        private final Runnable makeMoveFunc;
        private final Symbol symbol;

        public Player(String name, Runnable makeMoveFunc, Symbol symbol) {
            this.name = name;
            this.makeMoveFunc = makeMoveFunc;
            this.symbol = symbol;
        }

        private void makeMove() {
            makeMoveFunc.run();
        }

        public char getSymbolChar() {
            return symbol.toString().charAt(0);
        }
    }

    private static final Scanner scanner = new Scanner(System.in);

    private final List<String> computers = List.of("easy", "medium", "hard");

    private Board board;

    private String input(String prompt) {
        System.out.printf("%s: > ", prompt);
        return scanner.nextLine();
    }

    private Result parseCoordinateInput(String input) {
        if (input.replaceAll("\\s", "").chars().filter(c -> !Character.isDigit(c)).findAny().isPresent()) {
            return new Result.Error("You should enter numbers!");
        }

        String[] parts = input.split("\\s+");

        String part1 = parts[0];
        String part2 = parts[1];

        int yCoord = Integer.parseInt(part1);
        int xCoord = Integer.parseInt(part2);

        if (xCoord < 1 || xCoord > 3 || yCoord < 1 || yCoord > 3) {
            return new Result.Error("Coordinates should be from 1 to 3!");
        }

        if (board.get(yCoord - 1, xCoord - 1) != '_') {
            return new Result.Error("This cell is occupied! Choose another one!");
        }

        return new Result.Success<>(new int[] {yCoord, xCoord});
    }

    private Result parseBoardStateInput(String input) {
        Result.Error error = new Result.Error("Error!");
        boolean hasIllegalCharacter = input.chars().filter(c -> "OX_ox".indexOf(c) == -1).findAny().isPresent();
        if (hasIllegalCharacter) {
            return error;
        } else if (input.length() != 9) {
            return error;
        } else {
            char[][] result = new char[3][3];
            IntStream.range(0, 9).forEach(i -> result[i / 3][i % 3] = Character.toUpperCase(input.charAt(i)));
            return new Result.Success<>(result);
        }
    }

    private void makeMediumLevelMove() {
        int[] winningMove = findAWinningMoveFor(board.getCurrentPlayer().getSymbolChar());
        if (winningMove != null) {
            makeMove(winningMove);
        } else {
            int[] opponentWinningMove = findAWinningMoveFor(board.getNOTCurrentPlayer().getSymbolChar());
            if (opponentWinningMove != null) {
                makeMove(opponentWinningMove);
            } else {
                makeRandomMove();
            }
        }
        System.out.printf("Making move level \"%s\"%n", "medium");
    }

    private int[] findAWinningMoveFor(char playerSymbol) {
        for (int i = 0; i < 3; i++) {
            List<Character> row = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                row.add(board.get(i, j));
            }
            int hole = findHole(row, playerSymbol);
            if (hole != -1) {
                return new int[] {i + 1, hole + 1};
            }
        }

        for (int i = 0; i < 3; i++) {
            List<Character> column = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                column.add(board.get(j, i));
            }
            int hole = findHole(column, playerSymbol);
            if (hole != -1) {
                return new int[] {hole + 1, i + 1};
            }
        }

        List<Character> diag1 = List.of(board.get(0, 0), board.get(1, 1), board.get(2, 2));
        int x = findHole(diag1, playerSymbol);
        if (x != -1) {
            return new int[] {x + 1, x + 1};
        }

        List<Character> diag2 = List.of(board.get(0, 2), board.get(1, 1), board.get(2, 0));
        int y = findHole(diag2, playerSymbol);
        if (y != -1) {
            return new int[][] {{1, 3}, {2, 2}, {3, 1}}[y];
        }
        return null;
    }

    private int findHole(List<Character> chars, char symbol) {
        long symbolCount = chars.stream().filter(c -> c == symbol).count();
        int holeIndex = chars.indexOf('_');
        if (symbolCount == 2 && holeIndex != -1) {
            return holeIndex;
        }
        return -1;
    }

    private void makeRandomMove() {
        List<int[]> possibleMoves = board.getPossibleMoves();
        makeMove(possibleMoves.get(new Random().nextInt(possibleMoves.size())));
    }

    private void makeEasyLevelMove() {
        makeRandomMove();
        System.out.printf("Making move level \"%s\"%n", "easy");
    }

    private void makeHardLevelMove() {
        makeMove(new Minimax(board).minmax().move);
        System.out.printf("Making move level \"%s\"%n", "hard");
    }

    private void makeMove(int[] coordinate) {
        board.set(coordinate[0] - 1, coordinate[1] - 1, board.getCurrentPlayer());
    }

    /*
     *  repeat the request until input is valid
     * */
    private <T> T requestValidInput(Function<String, Result> parseFunc, String prompt) {
        Result result;
        do
        {
            result = parseFunc.apply(input(prompt));
            if (!result.isSuccess()) {
                System.out.println(result.error());
            }
        } while (!result.isSuccess());
        return result.success();
    }

    private void printGameState() {
        if (board.isDraw()) {
            System.out.println("Draw");
        } else if (board.isWinner('X')) {
            System.out.println("X wins");
        } else if (board.isWinner('O')) {
            System.out.println("O wins");
        } else {
            System.out.println("Game not finished");
        }
    }

    private Result parseCommand(String input) {
        try {
            String[] parts = input.split("\\s+");
            if (parts[0].equals("start")) {
                return new Result.Success<>(parseStartCommand(parts));
            } else if (parts[0].equals("exit")) {
                return new Result.Success<>(parseExitCommand(parts));
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            return new Result.Error("Bad parameters!");
        }
    }

    private String[] parseStartCommand(String[] parts) throws Exception {
        List<String> lst = new ArrayList<>(computers);
        lst.add("user");
        if (parts.length == 3) {
            if (!lst.contains(parts[1]) ||
                    (!lst.contains(parts[2]))) {
                throw new Exception();
            }
        } else {
            throw new Exception();
        }
        return parts;
    }

    private String[] parseExitCommand(String[] parts) throws Exception {
        if (parts.length != 1) {
            throw new Exception();
        }
        return parts;
    }

    private void executeCommand(String[] command) {
        switch (command[0]) {
            case "start":
                executeStartCommand(Arrays.copyOfRange(command, 1, command.length));
                break;
            case "exit":
                executeExitCommand();
                break;
        }
    }

    private void executeStartCommand(String[] parameters) {
        Player player1 = createPlayer(parameters[0], true);
        Player player2 = createPlayer(parameters[1], false);

        this.board = new Board(player1, player2);
    }

    private Player createPlayer(String param, boolean isFirstPlayer) {
        if (computers.contains(param)) {
            Level computerLevel = Level.valueOf(param.toUpperCase());
            switch (computerLevel) {
                case EASY:
                    return new Player("easy", this::makeEasyLevelMove, isFirstPlayer ? Player.Symbol.X : Player.Symbol.O);
                case MEDIUM:
                    return new Player("medium", this::makeMediumLevelMove, isFirstPlayer ? Player.Symbol.X : Player.Symbol.O);
                case HARD:
                    return new Player("hard", this::makeHardLevelMove, isFirstPlayer ? Player.Symbol.X : Player.Symbol.O);
                default:
                    return null;
            }
        } else {
            return new Player("user", this::makeHumanMove, isFirstPlayer ? Player.Symbol.X : Player.Symbol.O);
        }
    }

    private void executeExitCommand() {
        System.exit(0);
    }

    private void makeHumanMove() {
        int[] coordinate = requestValidInput(this::parseCoordinateInput, "Enter the coordinates");
        makeMove(coordinate);
    }

    private void run() {
        String[] command;
        do
        {
            command = requestValidInput(this::parseCommand, "Input command");
            executeCommand(command);
            System.out.println(board);

            while (board.isNotGameOver()) {
                board.getCurrentPlayer().makeMove();
                board.nextTurn();
                System.out.println(board);
            }
            printGameState();
        } while (!command[0].equals("exit"));
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
