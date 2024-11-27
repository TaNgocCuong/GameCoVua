package org.example.chessfx;

import lombok.Getter;

import java.io.*;
import java.util.*;


public class StateGame {
    // Lấy trạng thái của bàn cờ
    @Getter
    public String[][] board;
    private Stack<StateHistory> history;
    private String currentTurn;

    // Nhập thành
    private boolean whiteKingMoved;
    private boolean whiteLeftRookMoved;
    private boolean whiteRightRookMoved;
    private boolean blackKingMoved;
    private boolean blackLeftRookMoved;
    private boolean blackRightRookMoved;
    public boolean checkMate;
    public boolean stateMate;
    public boolean whiteWin;
    public boolean blackWin;
    public boolean notWin;

    String selectedPiece;
    // Trạng thái bàn cờ
    public StateGame() {
        // Khởi tạo bàn cờ
        board = new String[][]{
                {"bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR"},
                {"bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"},
                {"wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR"}
        };
        history = new Stack<>();
        whiteKingMoved = false;
        whiteLeftRookMoved = false;
        whiteRightRookMoved = false;
        blackKingMoved = false;
        blackLeftRookMoved = false;
        blackRightRookMoved = false;
        checkMate = false;
        stateMate = false;
        currentTurn = "white";

        whiteWin = false;
        blackWin = false;
        notWin = false;
    }
    // Lấy quân cờ
    public String getPieceAt(int row, int col) {
        return board[row][col];
    }
    //giữu quân tại vị trí
    public void keepPieceAt(int row, int col, String piece) {
        board[row][col] = piece;
    }
    // Đặt quân kèm điều kiện
    public void setPieceAt(int row, int col, String piece) {
        handleEnPassant(row, col, piece);
        handleCastling(row, col, piece);
        board[row][col] = piece;
        System.out.println(row + " " + col + " " + piece);
        updateCastlingStatus(row, col, piece);
        if (isCheckMate(piece.startsWith("w") ? false : true)) {
            checkMate = true;
            System.out.println(true);
        } else {
            checkMate = false;
        }
        String enemyColor = piece.startsWith("w") ? "b" : "w";
        String[] listEnemyPieceNames = {enemyColor + "p", enemyColor + "R", enemyColor + "N", enemyColor + "B", enemyColor + "Q", enemyColor + "K"};
        List<int[]> enemyPossibleMoves = new ArrayList<>();
        // Iterate through the board to find all enemy pieces
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                String currentPiece = board[i][j];
                if (currentPiece != null && Arrays.asList(listEnemyPieceNames).contains(currentPiece)) {
                    List<int[]> moves = possibleMovesOfPiece(currentPiece, i, j);
                    enemyPossibleMoves.addAll(moves);
                }
            }
        }
        enemyPossibleMoves.removeIf(move -> board[move[0]][move[1]].charAt(0) == enemyColor.charAt(0));
        // In ra danh sách các nước đi của quân địch với tọa độ hàng và cột
        System.out.println("Các nước đi của quân địch:");
        for (int[] move : enemyPossibleMoves) {
            System.out.println("Hàng: " + move[0] + ", Cột: " + move[1] + "quân " + board[move[0]][move[1]]);
        }

        // Nếu không có kẻ địch nào có nước đi hợp lệ, hãy tuyên bố chiến thắng
        if (enemyPossibleMoves.isEmpty()) {
            stateMate = true;
            if (checkMate){
                System.out.println("Thắng cờ");
                if(piece.charAt(0) == 'b'){
                    blackWin = true;
                }else {
                    whiteWin = true;
                }
            }else {
                System.out.println("Hòa cờ");
                notWin = true;
            }
        } else {
            stateMate = false;
        }
        // In ra bàn cờ
        printBoard();

    }
    // Thực hiện bắt tốt qua đường
    private void handleEnPassant(int row, int col, String piece) {
        if (piece.equals("wp") && row == 2 && isValidPosition(row, col) && board[3][col].equals("bp")) {
            board[3][col] = "--"; // Xóa quân tốt đen bị bắt qua đường
        } else if (piece.equals("bp") && row == 5 && isValidPosition(row, col) && board[4][col].equals("wp")) {
            board[4][col] = "--"; // Xóa quân tốt trắng bị bắt qua đường
        }
    }

    // Thực hiện nhập thành
    private void handleCastling(int row, int col, String piece) {
        if (piece.equals("wK")) {
            if (col == 6 && !whiteKingMoved && !whiteRightRookMoved) { // Nhập thành bên phải
                board[7][5] = "wR"; // Di chuyển Xe phải trắng đến cột 5
                board[7][7] = "--"; // Xóa Xe phải trắng ở vị trí cũ
            } else if (col == 2 && !whiteKingMoved && !whiteLeftRookMoved) { // Nhập thành bên trái
                board[7][3] = "wR"; // Di chuyển Xe trái trắng đến cột 3
                board[7][0] = "--"; // Xóa Xe trái trắng ở vị trí cũ
            }
        } else if (piece.equals("bK")) {
            if (col == 6 && !blackKingMoved && !whiteRightRookMoved) { // Nhập thành bên phải
                board[0][5] = "bR"; // Di chuyển Xe phải đen đến cột 5
                board[0][7] = "--"; // Xóa Xe phải đen ở vị trí cũ
            } else if (col == 2 && !blackKingMoved && !whiteLeftRookMoved) { // Nhập thành bên trái
                board[0][3] = "bR"; // Di chuyển Xe trái đen đến cột 3
                board[0][0] = "--"; // Xóa Xe trái đen ở vị trí cũ
            }
        }
    }

    // Chơi lại game
    public void resetGame() {
        // Khởi tạo lại bàn cờ
        board = new String[][]{
                {"bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR"},
                {"bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"--", "--", "--", "--", "--", "--", "--", "--"},
                {"wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"},
                {"wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR"}
        };

        // Đặt lại các biến trạng thái
        currentTurn = "white"; // Bắt đầu lại với lượt của bên trắng
        whiteKingMoved = false;
        whiteLeftRookMoved = false;
        whiteRightRookMoved = false;
        blackKingMoved = false;
        blackLeftRookMoved = false;
        blackRightRookMoved = false;

        checkMate = false;
        stateMate = false;
        
        whiteWin = false;
        blackWin = false;
        notWin = false;

        // Xóa lịch sử trò chơi
        history.clear(); // Xóa tất cả các mục trong lịch sử
    }

    // Lưu trạng thái cờ
    public void saveState() {
        String[][] currentBoard = new String[board.length][];
        for (int i = 0; i < board.length; i++) {
            currentBoard[i] = board[i].clone(); // Sao chép từng hàng
        }
        StateHistory state = new StateHistory(
                currentBoard,
                currentTurn,
                whiteKingMoved,
                whiteLeftRookMoved,
                whiteRightRookMoved,
                blackKingMoved,
                blackLeftRookMoved,
                blackRightRookMoved,
                checkMate,
                stateMate
        );
        history.push(state);
    }

    // Hiện tình trạng bàn cờ trong hệ thống
    public void printBoard() {
        // In tiêu đề cột
        System.out.print("   .");
        for (int col = 0; col < board[0].length; col++) {
            System.out.print(col + "  "); // Thêm khoảng cách giữa các cột
        }
        System.out.println();

        for (int row = 0; row < board.length; row++) {
            // In số thứ tự hàng
            System.out.print(row + " | ");
            for (String piece : board[row]) {
                // In ra quân cờ hoặc dấu chấm nếu không có quân
                System.out.print(piece.equals("--") ? ".. " : piece + " ");
            }
            System.out.println("|"); // Xuống dòng sau mỗi hàng
        }

        System.out.print("   ");
        for (int col = 0; col < board[0].length; col++) {
            System.out.print("---"); // Đường kẻ cho cột
        }
        System.out.println();

        // In trạng thái di chuyển của quân cờ
        System.out.println("Trạng thái di chuyển:");
        System.out.println("Vua trắng đã di chuyển: " + whiteKingMoved);
        System.out.println("Xe trái trắng đã di chuyển: " + whiteLeftRookMoved);
        System.out.println("Xe phải trắng đã di chuyển: " + whiteRightRookMoved);
        System.out.println("Vua đen đã di chuyển: " + blackKingMoved);
        System.out.println("Xe trái đen đã di chuyển: " + blackLeftRookMoved);
        System.out.println("Xe phải đen đã di chuyển: " + blackRightRookMoved);
        System.out.println("vua bị chiếu: " + checkMate);
        System.out.println("Hêt nước đi: " + stateMate);
    }

    // Quay lại nước đi
    public void undo() {
        if (!history.isEmpty()) {
            StateHistory previousState = history.pop();
            this.board = previousState.getBoard(); // Cập nhật bàn cờ
            this.currentTurn = previousState.getCurrentTurn(); // Cập nhật lượt đi
            this.whiteKingMoved = previousState.isWhiteKingMoved();
            this.whiteLeftRookMoved = previousState.isWhiteLeftRookMoved();
            this.whiteRightRookMoved = previousState.isWhiteRightRookMoved();
            this.blackKingMoved = previousState.isBlackKingMoved();
            this.blackLeftRookMoved = previousState.isBlackLeftRookMoved();
            this.blackRightRookMoved = previousState.isBlackRightRookMoved();
            this.checkMate = previousState.isCheckMate();
            this.stateMate = previousState.isStateMate();
        } else {
            resetGame();
        }
    }

    // Lượt chơi hiện tại
    public boolean isCurrentTurn(String piece) {
        return (currentTurn.equals("white") && piece.startsWith("w")) ||
                (currentTurn.equals("black") && piece.startsWith("b"));
    }

    // Đổi lượt chơi
    public void switchTurn() {
        currentTurn = currentTurn.equals("white") ? "black" : "white";
    }

    // Lưu ván cờ
    public void saveGameToFile(String filePath) {
        // Sử dụng FileWriter với tham số thứ hai là false để ghi đè tệp
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            writer.write(currentTurn + "\n"); // Lưu thông tin về lượt đi

            // Lưu trạng thái bàn cờ
            for (String[] row : board) {
                for (String piece : row) {
                    writer.write(piece + " "); // Ghi từng quân cờ vào file
                }
                writer.write("\n"); // Xuống dòng sau mỗi hàng
            }

            // Lưu số lượng lịch sử
            writer.write(history.size() + "\n");
            for (StateHistory state : history) {
                // Lưu trạng thái bàn cờ từ GameState
                for (String[] row : state.getBoard()) {
                    for (String piece : row) {
                        writer.write(piece + " "); // Ghi từng quân cờ trong lịch sử
                    }
                    writer.write("\n"); // Xuống dòng sau mỗi hàng
                }
                // Lưu lượt đi hiện tại và trạng thái quân cờ từ GameState
                writer.write(state.getCurrentTurn() + "\n");
                writer.write(state.isWhiteKingMoved() + "\n");
                writer.write(state.isWhiteLeftRookMoved() + "\n");
                writer.write(state.isWhiteRightRookMoved() + "\n");
                writer.write(state.isBlackKingMoved() + "\n");
                writer.write(state.isBlackLeftRookMoved() + "\n");
                writer.write(state.isBlackRightRookMoved() + "\n");
            }
            System.out.println("Game saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving the game: " + e.getMessage());
        }
    }

    // Tải ván cờ
    public void loadGameFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            currentTurn = reader.readLine(); // Đọc thông tin về lượt đi

            // Đọc trạng thái bàn cờ
            for (int i = 0; i < 8; i++) {
                String[] row = reader.readLine().split(" ");
                System.arraycopy(row, 0, board[i], 0, row.length);
            }

            int historySize = Integer.parseInt(reader.readLine()); // Đọc số lượng lịch sử
            history.clear(); // Xóa lịch sử hiện tại

            // Đọc lịch sử
            for (int i = 0; i < historySize; i++) {
                String[][] boardState = new String[8][8];
                for (int j = 0; j < 8; j++) {
                    String[] row = reader.readLine().split(" ");
                    System.arraycopy(row, 0, boardState[j], 0, row.length);
                }

                // Đọc các trạng thái quân cờ từ file
                boolean whiteKingMoved = Boolean.parseBoolean(reader.readLine());
                boolean whiteLeftRookMoved = Boolean.parseBoolean(reader.readLine());
                boolean whiteRightRookMoved = Boolean.parseBoolean(reader.readLine());
                boolean blackKingMoved = Boolean.parseBoolean(reader.readLine());
                boolean blackLeftRookMoved = Boolean.parseBoolean(reader.readLine());
                boolean blackRightRookMoved = Boolean.parseBoolean(reader.readLine());

                // Tạo GameState và thêm vào lịch sử
                StateHistory state = new StateHistory(boardState, currentTurn,
                        whiteKingMoved, whiteLeftRookMoved, whiteRightRookMoved,
                        blackKingMoved, blackLeftRookMoved, blackRightRookMoved, checkMate, stateMate);
                history.push(state);
            }
            System.out.println("Game loaded successfully.");
        } catch (IOException e) {
            System.err.println("Error loading the game: " + e.getMessage());
        }
    }


    // Lấy tất cả nước đi quân địch
    public List<int[]> possibleMovesOfEnemies(boolean isWhite) {
        List<int[]> enemyMoves = new ArrayList<>();
        String enemyColorPrefix = isWhite ? "w" : "b";
        String kingColorPrefix = isWhite ? "b" : "w"; // Quân cờ của vua đối phương
        int[] kingPosition = findPiecePosition(kingColorPrefix + "K");

        // Bước 1: Tính các nước đi của tất cả quân địch
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                String piece = board[row][col];
                if (piece.startsWith(enemyColorPrefix)) { // Nếu là quân địch
                    char pieceType = piece.charAt(1);

                    if (pieceType == 'K') { // Vua
                        addKingMoves(row, col, enemyMoves, isWhite);
                    } else if (pieceType == 'Q') { // Hậu
                        addLinearMoves(row, col, enemyMoves, isWhite);
                        addDiagonalMoves(row, col, enemyMoves, isWhite);
                    } else if (pieceType == 'R') { // Xe
                        addLinearMoves(row, col, enemyMoves, isWhite);
                    } else if (pieceType == 'B') { // Tượng
                        addDiagonalMoves(row, col, enemyMoves, isWhite);
                    } else if (pieceType == 'N') { // Mã
                        addKnightMoves(row, col, enemyMoves, isWhite);
                    } else if (pieceType == 'p') { // Tốt
                        addPawnMoves(row, col, enemyMoves, isWhite, piece);
                        adjustPawnMoves(row, col, enemyMoves, isWhite);
                    }
                }
            }
        }

        return enemyMoves;
    }

    // Tùy chỉnh nước đi của tốt
    private void adjustPawnMoves(int row, int col, List<int[]> enemyMoves, boolean isWhite) {
        int direction = isWhite ? -1 : 1;
        enemyMoves.remove(new int[]{row + direction, col});
        enemyMoves.remove(new int[]{row + (2 * direction), col}); // Nước tiến lên hai ô
        enemyMoves.add(new int[]{row + direction, col - 1});
        enemyMoves.add(new int[]{row + direction, col + 1});
    }

    // Lấy nước đi quân cờ hiện tại
    public List<int[]> possibleMovesOfPiece(String piece, int row, int col) {
        List<int[]> possibleMoves = new ArrayList<>();

        // Kiểm tra loại quân cờ
        if (piece == null || piece.equals("--")) {
            return possibleMoves; // Nếu không có quân cờ ở vị trí hiện tại
        }

        char pieceType = piece.charAt(1); // Lấy loại quân cờ ('K', 'Q', 'R', 'B', 'N', 'p')
        boolean isWhite = piece.startsWith("w");
        // Lấy danh sách nước đi của quân địch
        List<int[]> enemyMoves = possibleMovesOfEnemies(!isWhite);

        // Lấy vị trí của vua
        int[] kingPosition = findPiecePosition(isWhite ? "wK" : "bK");

        // Vị trí di chuyển cơ bản cho các quân cờ
        switch (pieceType) {
            case 'K': // Vua
                addKingMoves(row, col, possibleMoves, isWhite);
                checkCastling(row, col, possibleMoves, isWhite);
                for (int[] enemyMove : enemyMoves) {
                    possibleMoves.removeIf(move -> move[0] == enemyMove[0] && move[1] == enemyMove[1]);
                }
                break;
            case 'Q': // Hậu
                addLinearMoves(row, col, possibleMoves, isWhite);
                addDiagonalMoves(row, col, possibleMoves, isWhite);
                break;

            case 'R': // Xe
                addLinearMoves(row, col, possibleMoves, isWhite);

                break;

            case 'B': // Tượng
                addDiagonalMoves(row, col, possibleMoves, isWhite);
                break;

            case 'N': // Mã
                addKnightMoves(row, col, possibleMoves, isWhite);
                break;

            case 'p': // Tốt
                addPawnMoves(row, col, possibleMoves, isWhite, piece);
                break;

            default:
                break;
        }

        if (checkMate && pieceType != 'K') {
            possibleMoves.removeIf(
                    move -> !areProtectKing(move[0], move[1], kingPosition[0], kingPosition[1], isWhite)
            );
        }
        // Xử lý khi quân cờ đang bảo vệ vua, xóa các nước không bảo vệ vua
        if (kingPosition != null && areProtectKing(row, col, kingPosition[0], kingPosition[1], isWhite)) {
            possibleMoves.removeIf(move ->
                    !arePointsCollinear(row, col, move[0], move[1], kingPosition[0], kingPosition[1])
            );
        }

        // Loại bỏ nước đi của vua nếu vị trí đó bị đe dọa bởi quân địch
        if (pieceType == 'K') {
            for (int[] enemyMove : enemyMoves) {
                possibleMoves.removeIf(move ->
                        move[0] == enemyMove[0] && move[1] == enemyMove[1]
                );
            }
            if (checkMate) {
                possibleMoves.removeIf(move ->
                        areProtectKing(move[0], move[1], row, col, isWhite)
                );
            }
        }

        if (possibleMoves.isEmpty()) {
            System.out.println("No possible moves found.");
        }

        return possibleMoves;
    }

    // Luật di chuyển quân tốt
    private void addPawnMoves(int row, int col, List<int[]> possibleMoves, boolean isWhite, String piece) {
        int direction = isWhite ? -1 : 1; // Tốt trắng đi lên, tốt đen đi xuống

        // Tốt có thể đi 1 ô nếu ô trước mặt trống
        if (isValidPosition(row + direction, col) && board[row + direction][col].equals("--")) {
//            possibleMoves.add(new int[]{row + direction, col});
            addMoveIfValid(row + direction, col, possibleMoves, isWhite, row, col);

            // Tốt có thể đi 2 ô nếu cả 2 ô đều trống và tốt chưa di chuyển
            if (isValidPosition(row + (2 * direction), col) && board[row + (2 * direction)][col].equals("--") &&
                    row == (isWhite ? 6 : 1)) {
//                possibleMoves.add(new int[]{row + (2 * direction), col});
                addMoveIfValid(row + (2 * direction), col, possibleMoves, isWhite, row, col);

            }
        }

        // Kiểm tra bắt quân bên trái
        if (isValidPosition(row + direction, col - 1) && !board[row + direction][col - 1].equals("--") &&
                !isSameColor(board[row + direction][col - 1], piece)) {
//            possibleMoves.add(new int[]{row + direction, col - 1});
            addMoveIfValid(row + direction, col - 1, possibleMoves, isWhite, row, col);

        }

        // Kiểm tra bắt quân bên phải
        if (isValidPosition(row + direction, col + 1) && !board[row + direction][col + 1].equals("--") &&
                !isSameColor(board[row + direction][col + 1], piece)) {
//            possibleMoves.add(new int[]{row + direction, col + 1});
            addMoveIfValid(row + direction, col + 1, possibleMoves, isWhite, row, col);

        }

        // Kiểm tra bắt tốt qua đường
        // Tốt có thể bắt qua đường khi ở hàng 3 (tốt trắng) hoặc hàng 4 (tốt đen)
        if (row == (isWhite ? 3 : 4)) {
            if (isValidPosition(row, col - 1) && board[row][col - 1].equals(isWhite ? "bp" : "wp")) {
//                possibleMoves.add(new int[]{row + direction, col - 1}); // Nước đi bắt qua đường bên trái
                addMoveIfValid(row + direction, col - 1, possibleMoves, isWhite, row, col);

            }
            if (isValidPosition(row, col + 1) && board[row][col + 1].equals(isWhite ? "bp" : "wp")) {
//                possibleMoves.add(new int[]{row + direction, col + 1}); // Nước đi bắt qua đường bên phải
                addMoveIfValid(row + direction, col + 1, possibleMoves, isWhite, row, col);

            }
        }
    }

    // Luật di chuyển quân mã
    private void addKnightMoves(int row, int col, List<int[]> possibleMoves, boolean isWhite) {
        int[][] knightMoves = {
                {-2, -1}, {-1, -2}, {1, -2}, {2, -1},
                {2, 1}, {1, 2}, {-1, 2}, {-2, 1}
        };
        for (int[] move : knightMoves) {
            addMoveIfValid(row + move[0], col + move[1], possibleMoves, isWhite, row, col);
        }
    }

    // Luật di chuyển của quân xe
    private void addLinearMoves(int row, int col, List<int[]> possibleMoves, boolean isWhite) {
        // Di chuyển theo chiều dọc và ngang
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] direction : directions) {
            for (int i = 1; i < 8; i++) {
                int newRow = row + i * direction[0];
                int newCol = col + i * direction[1];

                if (!addMoveIfValid(newRow, newCol, possibleMoves, isWhite, row, col)) {
                    break;
                }


            }
        }
    }

    // Luật di chuyển của quân tượng
    private void addDiagonalMoves(int row, int col, List<int[]> possibleMoves, boolean isWhite) {
        // Di chuyển theo đường chéo
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] direction : directions) {
            for (int i = 1; i < 8; i++) {
                int newRow = row + i * direction[0];
                int newCol = col + i * direction[1];

                if (!addMoveIfValid(newRow, newCol, possibleMoves, isWhite, row, col)) {
                    break;
                }

            }
        }
    }

    // Luật di chuyển quân vua
    private void addKingMoves(int row, int col, List<int[]> possibleMoves, boolean isWhite) {
        // Di chuyển thường của Vua
        int[][] kingMoves = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };
        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];

            addMoveIfValid(newRow, newCol, possibleMoves, isWhite, row, col);

        }
    }

    // Thêm tọa độ nước đi
    private boolean addMoveIfValid(int row, int col, List<int[]> possibleMoves, boolean isWhite, int fromRow, int fromCol) {
        if (isValidPosition(row, col)) {
            String targetPiece = board[row][col];
            if (targetPiece.equals("--") || !isSameColor(targetPiece, isWhite ? "w" : "b") || isSameColor(targetPiece, isWhite ? "w" : "b")) {
                possibleMoves.add(new int[]{row, col, fromRow, fromCol});
                // Xóa quân cờ khỏi danh sách được bảo vệ nếu ô đó hiện trống hoặc là quân địch
                return targetPiece.equals("--"); // Trả về true nếu ô trống, false nếu có quân cờ
            }
        }
        return false; // Nếu không hợp lệ
    }

    // Phạm vi di chuyển quân
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8; // Kiểm tra xem vị trí có hợp lệ không
    }

    //So sánh màu quân cờ
    private boolean isSameColor(String piece1, String piece2) {
        return piece1.startsWith(piece2.substring(0, 1)); // Kiểm tra màu sắc
    }

    // Cập nhật trạng thái nhập thành
    private void updateCastlingStatus(int row, int col, String piece) {
        if (piece.equals("wK")) {
            whiteKingMoved = true; // Vua trắng đã di chuyển
        } else if (piece.equals("bK")) {
            blackKingMoved = true; // Vua đen đã di chuyển
        } else if (piece.equals("wR")) {
            // Kiểm tra nếu Xe ở cột 0 (bên trái) hoặc cột 7 (bên phải)
            if (!board[7][0].equals("wR")) {
                whiteLeftRookMoved = true; // Xe trái trắng đã di chuyển
            } else if (!board[7][7].equals("wR")) {
                whiteRightRookMoved = true; // Xe phải trắng đã di chuyển
            }
        } else if (piece.equals("bR")) {
            // Kiểm tra nếu Xe ở cột 0 (bên trái) hoặc cột 7 (bên phải)
            if (!board[0][0].equals("bR")) {
                blackLeftRookMoved = true; // Xe trái đen đã di chuyển
            } else if (!board[0][7].equals("bR")) {
                blackRightRookMoved = true; // Xe phải đen đã di chuyển
            }
        }

    }

    // Kiểm tra điều kiện nhập thành
    private void checkCastling(int row, int col, List<int[]> possibleMoves, boolean isWhite) {
        // Kiểm tra nhập thành
        if (isWhite) {
            List<int[]> enemyMoves = possibleMovesOfEnemies(false);

            // Nhập thành cho Vua trắng
            if (!whiteKingMoved && !whiteRightRookMoved) {
                int[][] castlingAreaRight = {{7, 4}, {7, 5}, {7, 6}, {7, 7}}; // Nhập thành bên phải (cột + 2)
                int[][] betweenKingRook = {{7, 5}, {7, 6}};

                if (isCastlingPossible(betweenKingRook)) {
                    boolean isSafeForCastling = true;
                    for (int[] enemyMove : enemyMoves) {
                        for (int[] position : castlingAreaRight) {
                            if (enemyMove[0] == position[0] && enemyMove[1] == position[1]) {
                                isSafeForCastling = false; // Có quân địch tấn công vào khu vực nhập thành
                                break;
                            }
                        }
                        if (!isSafeForCastling) break;
                    }
                    if (isSafeForCastling) {
                        possibleMoves.add(new int[]{7, 6}); // Thêm nước đi nhập thành bên phải
                    }
                }
            }
            if (!whiteKingMoved && !whiteLeftRookMoved) {
                int[][] castlingAreaLeft = {{7, 4}, {7, 3}, {7, 2}, {7, 1}, {7, 0}}; // Nhập thành bên trái (cột - 2)
                int[][] betweenKingRook = {{7, 3}, {7, 2}, {7, 1}};
                if (isCastlingPossible(betweenKingRook)) {
                    boolean isSafeForCastling = true;
                    for (int[] enemyMove : enemyMoves) {
                        for (int[] position : castlingAreaLeft) {
                            if (enemyMove[0] == position[0] && enemyMove[1] == position[1]) {
                                isSafeForCastling = false; // Có quân địch tấn công vào khu vực nhập thành
                                break;
                            }
                        }
                        if (!isSafeForCastling) break;
                    }
                    if (isSafeForCastling) {
                        possibleMoves.add(new int[]{7, 2}); // Thêm nước đi nhập thành bên trái
                    }
                }
            }
        } else {
            List<int[]> enemyMoves = possibleMovesOfEnemies(true);
            // Nhập thành cho Vua đen
            if (!blackKingMoved && !blackRightRookMoved) {
                int[][] castlingAreaRight = {{0, 4}, {0, 5}, {0, 6}, {0, 7}}; // Nhập thành bên phải (cột + 2)
                int[][] betweenKingRook = {{0, 5}, {0, 6}};

                if (isCastlingPossible(betweenKingRook)) {
                    boolean isSafeForCastling = true;
                    for (int[] enemyMove : enemyMoves) {
                        for (int[] position : castlingAreaRight) {
                            if (enemyMove[0] == position[0] && enemyMove[1] == position[1]) {
                                isSafeForCastling = false; // Có quân địch tấn công vào khu vực nhập thành
                                break;
                            }
                        }
                        if (!isSafeForCastling) break;
                    }
                    if (isSafeForCastling) {
                        possibleMoves.add(new int[]{0, 6}); // Thêm nước đi nhập thành bên phải
                    }
                }
            }
            if (!blackKingMoved && !blackLeftRookMoved) {
                int[][] castlingAreaLeft = {{0, 4}, {0, 3}, {0, 2}, {0, 1}, {0, 0}}; // Nhập thành bên trái (cột - 2)
                int[][] betweenKingRook = {{0, 3}, {0, 2}, {0, 1}};

                if (isCastlingPossible(betweenKingRook)) {
                    boolean isSafeForCastling = true;
                    for (int[] enemyMove : enemyMoves) {
                        for (int[] position : castlingAreaLeft) {
                            if (enemyMove[0] == position[0] && enemyMove[1] == position[1]) {
                                isSafeForCastling = false; // Có quân địch tấn công vào khu vực nhập thành
                                break;
                            }
                        }
                        if (!isSafeForCastling) break;
                    }
                    if (isSafeForCastling) {
                        possibleMoves.add(new int[]{0, 2}); // Thêm nước đi nhập thành bên trái
                    }
                }
            }
        }
    }

    // Kiểm tra điều kiện vị trí nhập thành
    private boolean isCastlingPossible(int[][] castlingArea) {
        for (int[] position : castlingArea) {
            int row = position[0];
            int col = position[1];
            String piece = board[row][col];

            // Kiểm tra xem ô có quân cờ hay không
            if (!piece.equals("--")) {
                return false;
            }
        }
        return true; // Không có quân địch, nhập thành có thể thực hiện
    }

    // Có vua bị chiếu
    public boolean isCheckMate(boolean isWhite) {
        boolean isKingInCheck = false;
        int[] kingPosition = isWhite ? findPiecePosition("wK") : findPiecePosition("bK");
        String kingAttacker = null;
        int[] kingAttackerPosition = new int[2];

        // Lấy danh sách các nước đi của quân đối thủ
        List<int[]> enemyMoves = possibleMovesOfEnemies(!isWhite);

        // Kiểm tra xem vua có bị chiếu không
        for (int[] move : enemyMoves) {
            if (move.length >= 4 && move[0] == kingPosition[0] && move[1] == kingPosition[1]) {
                isKingInCheck = true;
                kingAttacker = board[move[2]][move[3]]; // Quân cờ tấn công vua
                kingAttackerPosition[0] = move[2]; // Tọa độ xuất phát của quân cờ tấn công
                kingAttackerPosition[1] = move[3];
                break;
            }
        }


        if (!isKingInCheck) {
            return false;
        }

        System.out.println("Checkmate!");
        return true;
    }

    // Tìm vị trí quân cờ
    private int[] findPiecePosition(String piece) {
        int[] piecePosition = new int[2];
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col].equals(piece)) {
                    piecePosition[0] = row;
                    piecePosition[1] = col;
                    return piecePosition;
                }
            }
        }
        return null; // Nếu không tìm thấy vua, trả về null
    }

    // Kiểm tra nước đi có an toàn cho vua không
    private boolean areProtectKing(int row, int col, int kingRow, int kingCol, boolean isWhite) {
        String enemyQueen = isWhite ? "bQ" : "wQ"; // Hậu địch
        String enemyRook = isWhite ? "bR" : "wR";   // Xe địch
        String enemyBishop = isWhite ? "bB" : "wB"; // Tượng địch

        // Tìm vị trí của quân địch có thể bảo vệ vua
        int[] enemyQueenPos = findPiecePosition(enemyQueen);
        int[] enemyRookPos = findPiecePosition(enemyRook);
        int[] enemyBishopPos = findPiecePosition(enemyBishop);

        // Kiểm tra xem quân chắn (ở row, col) có nằm giữa vua và quân địch hay không
        if ((enemyQueenPos != null && isProtecting(kingRow, kingCol, row, col, enemyQueenPos)) ||
                (enemyRookPos != null && isProtecting(kingRow, kingCol, row, col, enemyRookPos))) {
            return true;
        } else if (enemyBishopPos != null && enemyBishopPos[0] != kingRow && enemyBishopPos[1] != kingCol
                && isProtecting(kingRow, kingCol, row, col, enemyBishopPos)) {
            return true;
        }


        return false;
    }

    // Kiểm tra quân cờ có đang ở vị trí bảo vệ vua không
    private boolean isProtecting(int kingRow, int kingCol, int shieldRow, int shieldCol, int[] enemyPos) {
        // Kiểm tra 3 điểm thẳng hàng và quân chắn có nằm giữa vua và quân địch không
        if (arePointsCollinear(kingRow, kingCol, shieldRow, shieldCol, enemyPos[0], enemyPos[1]) &&
                isBInBetween(kingRow, kingCol, shieldRow, shieldCol, enemyPos[0], enemyPos[1])) {

            // Duyệt tất cả quân cờ để kiểm tra có quân nào khác cản đường hay không
            for (int row = 0; row < board.length; row++) {
                for (int col = 0; col < board[row].length; col++) {
                    if (!board[row][col].equals("--") && !(row == kingRow && col == kingCol) &&
                            !(row == shieldRow && col == shieldCol) && !(row == enemyPos[0] && col == enemyPos[1])) {
                        if (arePointsCollinear(kingRow, kingCol, row, col, enemyPos[0], enemyPos[1]) &&
                                isBInBetween(kingRow, kingCol, row, col, enemyPos[0], enemyPos[1])) {
                            return false; // Có quân cản đường, không thể bảo vệ vua
                        }
                    }
                }
            }
            return true; // Không có quân nào khác cản đường, quân chắn bảo vệ vua
        }
        return false;
    }

    // Kiểm tra thẳng hàng
    private boolean arePointsCollinear(int row1, int col1, int row2, int col2, int row3, int col3) {
        // Kiểm tra độ dốc giữa (row1, col1), (row2, col2) và (row3, col3)
        return (col2 - col1) * (row3 - row2) == (row2 - row1) * (col3 - col2);
    }

    // kiểm tra vị trí có ở giữa không
    private boolean isBInBetween(int row1, int col1, int row2, int col2, int row3, int col3) {
        return (row2 >= Math.min(row1, row3) && row2 <= Math.max(row1, row3)) &&
                (col2 >= Math.min(col1, col3) && col2 <= Math.max(col1, col3));
    }
    // Phương thức kiểm tra file rỗng
    public boolean isFileEmpty(String filePath) {
        File file = new File(filePath);
        // Kiểm tra nếu file không tồn tại hoặc kích thước là 0 thì là rỗng
        return !file.exists() || file.length() == 0;
    }


}


