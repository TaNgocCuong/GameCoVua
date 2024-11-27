package org.example.chessfx;

import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;




import java.net.URL;
import java.util.*;

public class Main extends Application {

    final int GRID_SIZE = 8; // Số ô trong mỗi hàng/cột
    final double canvasSize = 600; // Kích thước của bàn cờ
    double cellSize = canvasSize / GRID_SIZE; // Kích thước mỗi ô
    private Color lightColor = Color.WHITE;  // Màu ô sáng
    private Color darkColor = Color.DARKGRAY;  // Màu ô tối
    Image bR, bN, bB, bQ, bK, bp, wR, wN, wB, wQ, wK, wp;
    private int[] selectedRowCol = new int[2];
    StateGame stateGame = new StateGame();
    AI ai = new AI();

    enum GameMode {PLAYER_VS_PLAYER, PLAYER_VS_AI}

    GameMode gameMode;

    @Override
    public void start(Stage primaryStage) throws Exception {
        showMainMenu(primaryStage);
    }

    private void showMainMenu(Stage primaryStage) {
        // Tạo layout cho menu chính
        VBox menuBox = new VBox(10);
        menuBox.setAlignment(Pos.CENTER);

        // Tạo các nút menu
        Button loadGameButton = new Button("Tiếp tục");
        Button vsPlayerButton = new Button("Chơi hai người");
        Button vsAIButton = new Button("Chơi với AI");
        Button exitButton = new Button("Thoát");

        // Kiểm tra nếu file lưu có rỗng, ẩn nút "Tải ván cờ" nếu file rỗng
        if (stateGame.isFileEmpty("Log.txt")) {
            loadGameButton.setVisible(false);
        }
        // Thiết lập hành động cho các nút
        vsPlayerButton.setOnAction(e -> {
            gameMode = GameMode.PLAYER_VS_PLAYER; // Đặt chế độ chơi hai người
            stateGame.resetGame();                // Khởi tạo ván cờ mới
            showGameScene(primaryStage);          // Chuyển sang scene chơi cờ
        });

        vsAIButton.setOnAction(e -> {
            gameMode = GameMode.PLAYER_VS_AI;     // Đặt chế độ chơi với AI
            stateGame.resetGame();                // Khởi tạo ván cờ mới
            showGameScene(primaryStage);          // Chuyển sang scene chơi cờ
        });

        loadGameButton.setOnAction(e -> {
            // Kiểm tra nếu có tệp lưu và tải ván cờ
            stateGame.loadGameFromFile("Log.txt");
            showGameScene(primaryStage);  // Chuyển sang scene chơi game với ván đã lưu
        });

        exitButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận thoát");
            alert.setHeaderText("Bạn có chắc chắn muốn thoát?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Tự động lưu trạng thái hiện tại của game trước khi thoát
                stateGame.saveGameToFile("Log.txt");
                primaryStage.close();
            }
        });

        // Thêm các nút vào menu
        menuBox.getChildren().addAll(loadGameButton, vsPlayerButton, vsAIButton,  exitButton);

        // Tạo ảnh nền
        Image backgroundImage = new Image(getClass().getResourceAsStream("/imgs-80px/ChessMG.jpg")); // Đường dẫn tới ảnh nền
        BackgroundSize backgroundSize = new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true);
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        menuBox.setBackground(new Background(background));
        // Tạo scene và hiển thị menu
        Scene menuScene = new Scene(menuBox, 300, 400);
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Menu Chính");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/imgs-80px/bK.png")));
        primaryStage.show();
    }

    // Tạo và hiển thị giao diện chính của trò chơi
    private void showGameScene(Stage primaryStage) {

        Canvas canvas = new Canvas(canvasSize, canvasSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        loadPieceImages();
        drawBoardAndPieces(gc);
        canvas.setOnMousePressed(event -> handleMousePressed(event, gc));
        canvas.setOnMouseDragged(event -> handleMouseDragged(event, gc));
        canvas.setOnMouseReleased(event -> handleMouseReleased(event, gc, primaryStage));

        HBox buttonBox = initGameButtons(primaryStage, gc);
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setBottom(buttonBox);

        Scene gameScene = new Scene(root, canvasSize, canvasSize + 50);
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("Game Cờ Vua");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/imgs-80px/bK.png")));
    }

    // Tạo và trả về 1 Hbox chứa các nút điều khiển trò chơi
    private HBox initGameButtons(Stage primaryStage, GraphicsContext gc) {
        Button resetButton = new Button("Ván mới");
        Button undoButton = new Button("Đi lại quân");
        Button changeColorButton = new Button("Option");
        Button backButton = new Button("Quay lại Menu");

        resetButton.setOnAction(e -> resetGame(gc));
        undoButton.setOnAction(e -> undoMove(gc));
        changeColorButton.setOnAction(e -> openColorChoiceDialog(gc));
        backButton.setOnAction(e -> showMainMenu(primaryStage));

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(resetButton, undoButton, changeColorButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        return buttonBox;
    }



    public void showWinNotification(Stage primaryStage, boolean whiteWin, boolean blackWin, boolean notWin, boolean checkMate) {
        if (!whiteWin && !blackWin && !notWin && !checkMate) {
            // Không hiển thị thông báo nếu không có bên nào thắng và cũng không hòa
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kết Quả Trận Đấu");
        alert.setHeaderText(null);

        if (whiteWin) {
            alert.setContentText("Quân trắng thắng!");
        } else if (blackWin) {
            alert.setContentText("Quân đen thắng!");
        } else if (notWin) {
            alert.setContentText("Hòa cờ!");
        } else if (checkMate && !whiteWin && !blackWin && !notWin) {
            alert.setContentText("Chiếu tướng!");
            alert.setResizable(false);  // Đảm bảo cửa sổ không thể thay đổi kích thước
        }

        // Thiết lập `Alert` hiển thị ở giữa màn hình
        alert.initOwner(primaryStage);

        // Kiểm tra nếu là "Chiếu tướng", tự động đóng sau một vài giây
        if (checkMate && !whiteWin && !blackWin && !notWin ) {
            // Sử dụng PauseTransition để tự động đóng alert sau 3 giây
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(event -> alert.close());
            delay.play();
        }

        alert.showAndWait();
    }




    private void drawBoardAndPieces(GraphicsContext gc) {
        drawBoard(gc);
        drawPieces(gc);
    }

    private void resetGame(GraphicsContext gc) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn chơi lại ván mới");
        alert.setContentText("Nhấn OK để chuyển.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stateGame.resetGame();
            drawBoardAndPieces(gc);
        }
    }

    private void undoMove(GraphicsContext gc) {
        stateGame.undo();
        drawBoardAndPieces(gc);
    }

    private void promotePawn(int row, int col, String piece) {
        // Danh sách lựa chọn quân cờ mới
        List<String> choices = Arrays.asList("Hậu", "Xe", "Tượng", "Mã");

        // Hiển thị hộp thoại lựa chọn
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Hậu", choices);
        dialog.setTitle("Phong quân");
        dialog.setHeaderText("Chọn quân cờ bạn muốn phong");
        dialog.setContentText("Chọn quân cờ:");

        // Lấy lựa chọn của người chơi
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newPiece;
            switch (result.get()) {
                case "Hậu":
                    newPiece = piece.charAt(0) + "Q";  // Phong thành Hậu
                    break;
                case "Xe":
                    newPiece = piece.charAt(0) + "R";  // Phong thành Xe
                    break;
                case "Tượng":
                    newPiece = piece.charAt(0) + "B";  // Phong thành Tượng
                    break;
                case "Mã":
                    newPiece = piece.charAt(0) + "N";  // Phong thành Mã
                    break;
                default:
                    newPiece = piece.charAt(0) + "Q"; // Mặc định phong thành Hậu nếu không chọn
            }

            stateGame.setPieceAt(row, col, newPiece);  // Cập nhật quân cờ mới sau khi phong
        }
        stateGame.setPieceAt(row, col, piece.charAt(0) + "Q");
    }

    private void openColorChoiceDialog(GraphicsContext gc) {
        List<String> choices = Arrays.asList("Màu Trắng/Đen", "Màu Xanh/Đen", "Màu Vàng/Nâu");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Màu Trắng/Đen", choices);
        dialog.setTitle("Chọn màu bàn cờ");
        dialog.setHeaderText("Chọn kiểu màu cho bàn cờ:");
        dialog.setContentText("Màu:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String selectedColor = result.get();
            switch (selectedColor) {
                case "Màu Trắng/Đen":
                    lightColor = Color.WHITE;
                    darkColor = Color.DARKGRAY;
                    break;
                case "Màu Xanh/Đen":
                    lightColor = Color.LIGHTBLUE;
                    darkColor = Color.BLACK;
                    break;
                case "Màu Vàng/Nâu":
                    lightColor = Color.BEIGE;
                    darkColor = Color.SADDLEBROWN;
                    break;

            }
            drawBoard(gc);  // Vẽ lại bàn cờ sau khi thay đổi màu sắc
            drawPieces(gc);
        }
    }

    public void playSound(String soundFile) {
        try {
            // Lấy đường dẫn của tệp âm thanh
            URL resource = getClass().getResource(soundFile);
            if (resource == null) {
                throw new RuntimeException("Không tìm thấy tệp âm thanh: " + soundFile);
            }

            // Tạo đối tượng AudioClip từ URL
            AudioClip clip = new AudioClip(resource.toString());

            // Phát âm thanh
            clip.play();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Không thể phát âm thanh.");
        }
    }

    private void loadPieceImages() {
        bR = new Image(getClass().getResourceAsStream("/imgs-80px/bR.png"));
        bN = new Image(getClass().getResourceAsStream("/imgs-80px/bN.png"));
        bB = new Image(getClass().getResourceAsStream("/imgs-80px/bB.png"));
        bQ = new Image(getClass().getResourceAsStream("/imgs-80px/bQ.png"));
        bK = new Image(getClass().getResourceAsStream("/imgs-80px/bK.png"));
        bp = new Image(getClass().getResourceAsStream("/imgs-80px/bp.png"));
        wR = new Image(getClass().getResourceAsStream("/imgs-80px/wR.png"));
        wN = new Image(getClass().getResourceAsStream("/imgs-80px/wN.png"));
        wB = new Image(getClass().getResourceAsStream("/imgs-80px/wB.png"));
        wQ = new Image(getClass().getResourceAsStream("/imgs-80px/wQ.png"));
        wK = new Image(getClass().getResourceAsStream("/imgs-80px/wK.png"));
        wp = new Image(getClass().getResourceAsStream("/imgs-80px/wp.png"));
    }

    public void drawBoard(GraphicsContext gc) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if ((row + col) % 2 == 0) {
                    gc.setFill(lightColor); // Ô trắng
                } else {
                    gc.setFill(darkColor); // Ô đen
                }
                gc.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
            }
        }
    }

    private void drawPieces(GraphicsContext gc) {
        String[][] board = stateGame.board;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                String piece = board[row][col];
                if (!piece.equals("--")) {
                    drawPiece(gc, piece, col, row);
                }
            }
        }
    }

    private void drawPiece(GraphicsContext gc, String piece, int col, int row) {
        Image image = null;
        switch (piece) {
            case "bR":
                image = bR;
                break;
            case "bN":
                image = bN;
                break;
            case "bB":
                image = bB;
                break;
            case "bQ":
                image = bQ;
                break;
            case "bK":
                image = bK;
                break;
            case "bp":
                image = bp;
                break;
            case "wR":
                image = wR;
                break;
            case "wN":
                image = wN;
                break;
            case "wB":
                image = wB;
                break;
            case "wQ":
                image = wQ;
                break;
            case "wK":
                image = wK;
                break;
            case "wp":
                image = wp;
                break;
        }

        if (image != null) {
            gc.drawImage(image, col * cellSize, row * cellSize, cellSize, cellSize); // Vẽ quân cờ vào ô tương ứng
        }
    }

    private void highlightSquares(GraphicsContext gc, int selectedRow, int selectedCol, List<int[]> possibleMoves) {
        // Vẽ ô đã chọn
        gc.setFill(Color.YELLOW.deriveColor(1, 1, 1, 0.5)); // Màu vàng nhạt cho ô đã chọn
        gc.fillRect(selectedCol * cellSize, selectedRow * cellSize, cellSize, cellSize);

        // Kiểm tra màu của quân hiện tại
        String currentColor = String.valueOf(stateGame.selectedPiece.charAt(0));

        // Vẽ các ô có thể di chuyển
        for (int[] move : possibleMoves) {
            int row = move[0];
            int col = move[1];
            String targetPiece = stateGame.board[row][col];

            // Kiểm tra điều kiện để vẽ ô
            if (targetPiece.equals("--")) {
                // Ô trống
                gc.setFill(Color.YELLOW.deriveColor(1, 1, 1, 0.5)); // Màu vàng nhạt cho ô trống
            } else if (!targetPiece.startsWith(currentColor)) {
                // Ô có quân địch
                gc.setFill(Color.YELLOW.deriveColor(1, 1, 1, 0.7));
            } else {
                // Ô có quân đồng đội (không làm gì)
                continue; // Không vẽ gì cho ô có quân đồng đội
            }

            // Vẽ ô
            gc.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
        }
    }

    // Xử lý sự kiện chuột nhấn
    public void handleMousePressed(MouseEvent event, GraphicsContext gc) {
        int col = (int) (event.getX() / cellSize);
        int row = (int) (event.getY() / cellSize);

        stateGame.saveState();
        String piece = stateGame.getPieceAt(row, col); // Lấy quân cờ từ StateGame
        if (stateGame.isCurrentTurn(piece)) {
            if (!piece.equals("--")) {
                selectedRowCol[0] = row; // Lưu hàng
                selectedRowCol[1] = col; // Lưu cột


                stateGame.selectedPiece = stateGame.getPieceAt(row, col);
                stateGame.keepPieceAt(row, col, "--");  // Xóa quân cờ khỏi vị trí cũ

                drawBoard(gc);
                drawPieces(gc);

                List<int[]> possibleMoves = stateGame.possibleMovesOfPiece(stateGame.selectedPiece, row, col);
                highlightSquares(gc, row, col, possibleMoves);
            }
        }
    }

    // Xử lý sự kiện chuột kéo
    public void handleMouseDragged(MouseEvent event, GraphicsContext gc) {
        if (stateGame.selectedPiece != null) {
            // Lấy vị trí chuột hiện tại
            double mouseX = event.getX();
            double mouseY = event.getY();
            Image pieceImage = new Image(getClass().getResourceAsStream("/imgs-80px/" + stateGame.selectedPiece + ".png"));

            drawBoard(gc);
            drawPieces(gc);
            gc.drawImage(pieceImage, mouseX - cellSize / 2, mouseY - cellSize / 2, cellSize, cellSize);


            // Lấy hàng và cột đã chọn
            int selectedRow = selectedRowCol[0];
            int selectedCol = selectedRowCol[1];
            List<int[]> possibleMoves = stateGame.possibleMovesOfPiece(stateGame.selectedPiece, selectedRow, selectedCol);
            highlightSquares(gc, selectedRow, selectedCol, possibleMoves);
        }
    }

    // Xử lý sự kiện chuột thả
    public void handleMouseReleased(MouseEvent event, GraphicsContext gc,Stage primaryStage) {
        if (stateGame.selectedPiece != null) {
            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);

            // Nếu vị trí trước và sau khi thả chuột giống nhau, giữ nguyên quân cờ tại chỗ
            if (row == selectedRowCol[0] && col == selectedRowCol[1]) {
                stateGame.keepPieceAt(row, col, stateGame.selectedPiece);
            } else {
                // Lấy danh sách di chuyển hợp lệ cho quân cờ đã chọn
                List<int[]> possibleMoves = stateGame.possibleMovesOfPiece(stateGame.selectedPiece, selectedRowCol[0], selectedRowCol[1]);
                boolean isValidMove = false;

                // Kiểm tra xem vị trí nhả chuột có phải là vị trí hợp lệ không
                for (int[] move : possibleMoves) {
                    if (move[0] == row && move[1] == col && stateGame.getPieceAt(row, col).charAt(0) != stateGame.selectedPiece.charAt(0)) {
                        isValidMove = true;
                        break;
                    }
                }

                // Nếu di chuyển hợp lệ, cập nhật vị trí quân cờ
                if (isValidMove) {
                    // Kiểm tra xem vị trí đích có quân cờ nào không trước khi di chuyển
                    boolean isCapture = !stateGame.getPieceAt(row, col).equals("--");

                    // Di chuyển quân cờ
                    stateGame.setPieceAt(row, col, stateGame.selectedPiece);

                    // Phát âm thanh tương ứng
                    if (isCapture) {
                        playSound("/sounds/capture.wav");  // Phát âm thanh khi có quân cờ bị ăn
                    } else {
                        playSound("/sounds/move.wav");     // Phát âm thanh khi ô trống
                    }

                    // Kiểm tra phong quân
                    if ((stateGame.selectedPiece.equals("wp") && row == 0) || (stateGame.selectedPiece.equals("bp") && row == 7)) {
                        // Thực hiện phong quân trước khi đặt lại selectedPiece
                        promotePawn(row, col, stateGame.selectedPiece);
                    }
                        stateGame.switchTurn();

                    // Chuyển lượt sau khi di chuyển hợp lệ

                    // Nếu chơi với AI, AI thực hiện nước đi sau khi người chơi đã di chuyển
                    if (gameMode == GameMode.PLAYER_VS_AI && !stateGame.whiteWin && !stateGame.blackWin && !stateGame.notWin) {
                        makeAIMove(gc);
                        // Vẽ lại bàn cờ và các quân cờ sau khi di chuyển
                        drawBoard(gc);
                        drawPieces(gc);
                        stateGame.switchTurn();
                    }
                } else {

                    // Nếu không hợp lệ, đặt quân cờ về vị trí ban đầu
                    stateGame.keepPieceAt(selectedRowCol[0], selectedRowCol[1], stateGame.selectedPiece);
                }
            }

            // Vẽ lại bàn cờ và quân cờ
            drawBoard(gc);
            drawPieces(gc);
            showWinNotification(primaryStage,stateGame.whiteWin,stateGame.blackWin, stateGame.notWin, stateGame.checkMate);

            // Reset quân đã chọn sau khi phong quân (nếu có)
            stateGame.selectedPiece = null;
        }

    }

    private void makeAIMove(GraphicsContext gc) {
        boolean moveMade = false;

        while (!moveMade) {
            // Chọn ngẫu nhiên một quân cờ của AI (ví dụ quân đen)
            int randomRow = (int) (Math.random() * GRID_SIZE);
            int randomCol = (int) (Math.random() * GRID_SIZE);
            String piece = stateGame.getPieceAt(randomRow, randomCol);


            // Bỏ qua nếu ô này không phải quân của AI hoặc là ô trống
            if (piece.startsWith("w") || piece.equals("--")) {
                continue;
            }

            List<int[]> possibleMoves = stateGame.possibleMovesOfPiece(piece, randomRow, randomCol);
            List<int[]> enemyMoves = stateGame.possibleMovesOfEnemies(true); // Đúng: Lấy nước đi của quân địch (quân trắng)


            if (possibleMoves.isEmpty()) {
                continue;
            }


            // Tìm nước đi có điểm cao nhất
            int highestScore = Integer.MIN_VALUE;
            int[] bestMove = null;

            for (int[] move : possibleMoves) {
                // Bỏ qua nếu ô đến có quân của AI
                if (stateGame.getPieceAt(move[0], move[1]).charAt(0) == 'b') {
                    continue;
                }


                // Kiểm tra nếu quân vua (bK) bị tấn công
                if (piece.equals("bK")) {
                    for (int[] enemyMove : enemyMoves) {
                        if (move[0] == enemyMove[0] && move[1] == enemyMove[1]) {
                            // Nếu nước đi của quân địch trùng với nước đi của quân vua, bỏ qua nước đi này
                            continue;
                        }
                    }
                }

                int score = ai.evaluateMove(stateGame.board, move, piece);
                if (score > highestScore) {
                    highestScore = score;
                    bestMove = move;
                }
            }

            // Thực hiện nước đi tốt nhất nếu có
            if (bestMove != null) {
                stateGame.setPieceAt(bestMove[0], bestMove[1], piece);
                stateGame.keepPieceAt(randomRow, randomCol, "--");
                moveMade = true;
            }
        }


    }




    public static void main(String[] args) {
        launch();
    }
}
