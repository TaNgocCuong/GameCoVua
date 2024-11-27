package org.example.chessfx;

import java.util.Map;

public class AI {

    // Hàm đánh giá lợi ích của một nước đi (tính điểm dựa trên việc bắt quân đối thủ)
    public int evaluateMove(String[][] board, int[] move, String currentPiece) {
        String targetPiece = board[move[0]][move[1]];
        int score = 0;

        // Nếu ô đích có quân cờ của đối thủ (quân trắng)
        if (targetPiece.startsWith("w")) {
            Map<String, Integer> pieceValues = Map.of(
                    "p", 10, "N", 30, "B", 30, "R", 50, "Q", 90, "K", 900
            );
            // Tính điểm dựa trên giá trị quân cờ bắt được
            score += pieceValues.getOrDefault(targetPiece.substring(1), 0);

            // Kiểm tra nếu quân cờ đối thủ có giá trị cao hơn quân hiện tại
            if (targetPiece.startsWith("w")) {
                int targetPieceValue = pieceValues.getOrDefault(targetPiece.substring(1), 0);
                int currentPieceValue = pieceValues.getOrDefault(currentPiece.substring(1), 0);

                // Nếu quân đối thủ có giá trị cao hơn quân cờ của ta
                if (targetPieceValue > currentPieceValue) {
                    score += (targetPieceValue - currentPieceValue); // Thêm điểm chênh lệch vào score
                }
            }
        }

        // Cộng thêm điểm nếu di chuyển vào trung tâm (giá trị cao hơn cho trung tâm)
        if (move[0] > 1 && move[0] < 6 && move[1] > 1 && move[1] < 6) {
            score += 5; // Ưu tiên kiểm soát trung tâm
        }


        return score; // Trả về điểm của nước đi
    }












}
