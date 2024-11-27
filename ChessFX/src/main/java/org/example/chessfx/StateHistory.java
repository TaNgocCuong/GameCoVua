package org.example.chessfx;

public class StateHistory {
    private String[][] board; // Bàn cờ
    private String currentTurn; // Lượt đi hiện tại

    // Các thuộc tính cho quân cờ
    private boolean whiteKingMoved;
    private boolean whiteLeftRookMoved;
    private boolean whiteRightRookMoved;
    private boolean blackKingMoved;
    private boolean blackLeftRookMoved;
    private boolean blackRightRookMoved;
    private boolean checkMate;
    private boolean stateMate;

    public String[][] getBoard() {
        return board;
    }

    public void setBoard(String[][] board) {
        this.board = board;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(String currentTurn) {
        this.currentTurn = currentTurn;
    }

    public boolean isWhiteKingMoved() {
        return whiteKingMoved;
    }

    public void setWhiteKingMoved(boolean whiteKingMoved) {
        this.whiteKingMoved = whiteKingMoved;
    }

    public boolean isWhiteLeftRookMoved() {
        return whiteLeftRookMoved;
    }

    public void setWhiteLeftRookMoved(boolean whiteLeftRookMoved) {
        this.whiteLeftRookMoved = whiteLeftRookMoved;
    }

    public boolean isWhiteRightRookMoved() {
        return whiteRightRookMoved;
    }

    public void setWhiteRightRookMoved(boolean whiteRightRookMoved) {
        this.whiteRightRookMoved = whiteRightRookMoved;
    }

    public boolean isBlackKingMoved() {
        return blackKingMoved;
    }

    public void setBlackKingMoved(boolean blackKingMoved) {
        this.blackKingMoved = blackKingMoved;
    }

    public boolean isBlackLeftRookMoved() {
        return blackLeftRookMoved;
    }

    public void setBlackLeftRookMoved(boolean blackLeftRookMoved) {
        this.blackLeftRookMoved = blackLeftRookMoved;
    }

    public boolean isBlackRightRookMoved() {
        return blackRightRookMoved;
    }

    public void setBlackRightRookMoved(boolean blackRightRookMoved) {
        this.blackRightRookMoved = blackRightRookMoved;
    }

    public boolean isCheckMate() {
        return checkMate;
    }

    public void setCheckMate(boolean checkMate) {
        this.checkMate = checkMate;
    }

    public boolean isStateMate() {
        return stateMate;
    }

    public void setStateMate(boolean stateMate) {
        this.stateMate = stateMate;
    }

    public StateHistory(String[][] board, String currentTurn, boolean whiteKingMoved, boolean whiteLeftRookMoved, boolean whiteRightRookMoved, boolean blackKingMoved, boolean blackLeftRookMoved, boolean blackRightRookMoved, boolean checkMate, boolean stateMate) {
        this.board = board;
        this.currentTurn = currentTurn;
        this.whiteKingMoved = whiteKingMoved;
        this.whiteLeftRookMoved = whiteLeftRookMoved;
        this.whiteRightRookMoved = whiteRightRookMoved;
        this.blackKingMoved = blackKingMoved;
        this.blackLeftRookMoved = blackLeftRookMoved;
        this.blackRightRookMoved = blackRightRookMoved;
        this.checkMate = checkMate;
        this.stateMate = stateMate;
    }
}
