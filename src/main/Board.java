package main;

import Pieces.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Board extends JPanel {

    public int tileSize = 85;

    int cols = 8;
    int rows = 8;

    ArrayList<Piece> pieceList = new ArrayList<>();

    public Piece selectedPiece;

    Input input = new Input(this);


    public int enPassantTile = -1;

    public boolean isWhiteToMove = true;
    public King whiteKing;
    public King blackKing;
    public boolean gameOver = false;


    public Board() {
        this.setPreferredSize(new Dimension(cols * tileSize, rows * tileSize));

        this.addMouseListener(input);
        this.addMouseMotionListener(input);

        addPieces();

        whiteKing = (King) getPiece(4, 0);
        blackKing = (King) getPiece(4, 7);

    }

    public Piece getPiece(int col, int row) {

        for (Piece piece : pieceList) {
            if (piece.col == col && piece.row == row) {
                return piece;
            }
        }
        return null;
    }

    public void makeMove(Move move) {

        if(move.piece.name.equals("Pawn")) {
            movePawn(move);
        } else if (move.piece.name.equals("King") && Math.abs(move.newCol-move.oldCol) ==2)
            castleKing(move);
        else {
            move.piece.col = move.newCol;
            move.piece.row = move.newRow;
            move.piece.xPos = move.newCol * tileSize;
            move.piece.yPos = move.newRow * tileSize;

            move.piece.isFirstMove = false;
            capture(move.capture);
        }

        isWhiteToMove = !isWhiteToMove;
        updateGameState();
    }

    private void castleKing(Move move) {
        move.piece.col = move.newCol;
        move.piece.row = move.newRow;
        move.piece.xPos = move.newCol * tileSize;
        move.piece.yPos = move.newRow * tileSize;
        move.piece.isFirstMove = false;

        boolean kingSide = move.newCol > move.oldCol;
        int rookOldCol = kingSide ? 7 : 0;
        int rookNewCol = kingSide ? 5 : 3;

        Piece rook = getPiece(rookOldCol, move.oldRow);
        if (rook != null) {
            rook.col = rookNewCol;
            rook.row = move.oldRow;
            rook.xPos = rookNewCol * tileSize;
            rook.yPos = move.oldRow * tileSize;
            rook.isFirstMove = false;
        }
    }

    private void movePawn(Move move) {
        //en passant
        int colorIndex = move.piece.isWhite ? -1 : 1;

        if (getTileNum(move.newCol, move.newRow) == enPassantTile) {
            move.capture = getPiece(move.newCol, move.newRow + colorIndex);
        }
        if (Math.abs(move.piece.row - move.newRow) == 2) {
            enPassantTile = getTileNum(move.newCol, move.newRow + colorIndex);
        } else {
            enPassantTile = -1;
        }

        //promotion
        colorIndex = move.piece.isWhite ? 7 : 0;
        if(move.newRow == colorIndex) {
            promotePawn(move);
        }

        move.piece.col = move.newCol;
        move.piece.row = move.newRow;
        move.piece.xPos = move.newCol * tileSize;
        move.piece.yPos = move.newRow * tileSize;

        move.piece.isFirstMove = false;
        capture(move.capture);
    }

    public void promotePawn(Move move) {
                String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(this, "Choose promotion piece", "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        Piece promoted;
        switch (choice) {
            case 1:
                promoted = new Rook(this, move.newCol, move.newRow, move.piece.isWhite);
                break;
            case 2:
                promoted = new Bishop(this, move.newCol, move.newRow, move.piece.isWhite);
                break;
            case 3:
                promoted = new Knight(this, move.newCol, move.newRow, move.piece.isWhite);
                break;
            default:
                promoted = new Queen(this, move.newCol, move.newRow, move.piece.isWhite);
        }
        pieceList.add(promoted);
        capture(move.piece);
    }

    public void capture(Piece piece) {
        pieceList.remove(piece);
    }

    public boolean isValidMove(Move move) {
        if (gameOver) {
            return false;
        }

        if (sameTeam(move.piece, move.capture)) {
            return false;
        }

        if (!move.piece.isValidMovement(move.newCol, move.newRow)) {
            return false;
        }
        if(move.piece.moveColidesWithPiece(move.newCol, move.newRow)) {
            return false;
        }
        if (move.piece.isWhite != isWhiteToMove) {
            return false;
        }
        if (isKingChecked(move)) {
            return false;
        }
        return true;
    }
    
    // Returns true if any piece of color `byWhite` currently attacks (col,row).
    public boolean isSquareAttacked(int col, int row, boolean byWhite) {
        for (Piece piece : pieceList) {
            if (piece.isWhite != byWhite) {
                continue;
            }

            if (piece.name.equals("Pawn")) {
                int colorIndex = piece.isWhite ? -1 : 1;
                if ((col == piece.col - 1 || col == piece.col + 1) && row == piece.row - colorIndex) {
                    return true;
                }
                continue;
            }

            if (piece.name.equals("King")) {
                if (!(col == piece.col && row == piece.row)
                        && Math.abs(col - piece.col) <= 1 && Math.abs(row - piece.row) <= 1) {
                    return true;
                }
                continue;
            }

            if (piece.isValidMovement(col, row) && !piece.moveColidesWithPiece(col, row)) {
                return true;
            }
        }
        return false;
    }

    // Simulates the move on the real board, checks if it leaves the mover's king in check, then reverts it.
    public boolean isKingChecked(Move move) {
        int prevCol = move.piece.col;
        int prevRow = move.piece.row;

        boolean captureRemoved = false;
        if (move.capture != null && pieceList.contains(move.capture)) {
            pieceList.remove(move.capture);
            captureRemoved = true;
        }

        // handle en passant, whose captured pawn isn't on the destination square
        Piece enPassantCapture = null;
        if (move.piece.name.equals("Pawn") && move.capture == null
                && getTileNum(move.newCol, move.newRow) == enPassantTile) {
            int colorIndex = move.piece.isWhite ? -1 : 1;
            enPassantCapture = getPiece(move.newCol, move.newRow + colorIndex);
            if (enPassantCapture != null) {
                pieceList.remove(enPassantCapture);
            }
        }

        move.piece.col = move.newCol;
        move.piece.row = move.newRow;

        King king = move.piece.isWhite ? whiteKing : blackKing;
        boolean inCheck = isSquareAttacked(king.col, king.row, !move.piece.isWhite);

        move.piece.col = prevCol;
        move.piece.row = prevRow;
        if (captureRemoved) {
            pieceList.add(move.capture);
        }
        if (enPassantCapture != null) {
            pieceList.add(enPassantCapture);
        }

        return inCheck;
    }

    // Does color `white` have any legal move at all?
    private boolean hasAnyLegalMove(boolean white) {
        for (Piece piece : new ArrayList<>(pieceList)) {
            if (piece.isWhite != white) {
                continue;
            }
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (isValidMove(new Move(this, piece, c, r))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Called after every move to check for checkmate/stalemate.
    public void updateGameState() {
        King king = isWhiteToMove ? whiteKing : blackKing;
        boolean inCheck = isSquareAttacked(king.col, king.row, !isWhiteToMove);
        boolean hasMoves = hasAnyLegalMove(isWhiteToMove);

        if (!hasMoves) {
            gameOver = true;
            String message = inCheck
                    ? (isWhiteToMove ? "Black" : "White") + " wins by checkmate!"
                    : "Stalemate! The game is a draw.";
            JOptionPane.showMessageDialog(this, message);
        }
    }


    public boolean sameTeam(Piece p1, Piece p2) {
        if (p1 == null || p2 == null) {
            return false;
        }
        return p1.isWhite == p2.isWhite;
    }

    public int getTileNum(int col, int row) {
        return row * rows + col;
    }

    public void addPieces() {
        boolean isWhite = true;
        for (int i = 0; i < 8; i += 7) {
            pieceList.add(new Rook(this, 0, i, isWhite));
            pieceList.add(new Knight(this, 1, i, isWhite));
            pieceList.add(new Bishop(this, 2, i, isWhite));
            pieceList.add(new Queen(this, 3, i, isWhite));
            pieceList.add(new King(this, 4, i, isWhite));
            pieceList.add(new Bishop(this, 5, i, isWhite));
            pieceList.add(new Knight(this, 6, i, isWhite));
            pieceList.add(new Rook(this, 7, i, isWhite));
            isWhite = false;
        }
        isWhite = true;
        for (int j = 1; j < 8; j += 5) {
            for (int i = 0; i < 8; i++) {
                pieceList.add(new Pawn(this, i, j, isWhite));
            }
            isWhite = false;
        }
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        //paint board
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                g2d.setColor((c + r) % 2 == 0 ? new Color(202, 234, 248) : new Color(5, 73, 136));
                g2d.fillRect(r * tileSize, c * tileSize, tileSize, tileSize);
            }

        //paint highlights
        if(selectedPiece != null)
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++) {
                    if (isValidMove(new Move(this, selectedPiece, c, r))) {

                        g2d.setColor(new Color(68, 180, 57, 190));
                        g2d.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
                    }
                }

        //highlight king if in check
        King checkedKing = isWhiteToMove ? whiteKing : blackKing;
        if (checkedKing != null && isSquareAttacked(checkedKing.col, checkedKing.row, !isWhiteToMove)) {
            g2d.setColor(new Color(220, 20, 60, 160));
            g2d.fillRect(checkedKing.col * tileSize, checkedKing.row * tileSize, tileSize, tileSize);
        }

        //paint pieces
        for (Piece piece : pieceList) {
            piece.paint(g2d);
        }
    }

}
