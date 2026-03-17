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

    public Board() {
        this.setPreferredSize(new Dimension(cols * tileSize, rows * tileSize));

        this.addMouseListener(input);
        this.addMouseMotionListener(input);

        addPieces();
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
        move.piece.col = move.newCol;
        move.piece.row = move.newRow;
        move.piece.xPos = move.newCol * tileSize;
        move.piece.yPos = move.newRow * tileSize;

        move.piece.isFirstMove = false;
        capture(move);
    }

    public void capture(Move move) {
        pieceList.remove(move.capture);
    }

    public boolean isValidMove(Move move) {
        if (sameTeam(move.piece, move.capture)) {
            return false;
        }

        if (!move.piece.isValidMovement(move.newCol, move.newRow)) {
            return false;
        }
        if(move.piece.moveColidesWithPiece(move.newCol, move.newRow)) {
            return false;
        }
        return true;
    }

    public boolean sameTeam(Piece p1, Piece p2) {
        if (p1 == null || p2 == null) {
            return false;
        }
        return p1.isWhite == p2.isWhite;
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

        //paint pieces
        for (Piece piece : pieceList) {
            piece.paint(g2d);
        }
    }

}
