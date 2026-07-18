package Pieces;

import main.Board;

import java.awt.image.BufferedImage;

public class King extends Piece {
    public King(Board board, int col, int row, boolean isWhite) {
        super(board);
        this.col = col;
        this.row = row;
        this.xPos = col * board.tileSize;
        this.yPos = row * board.tileSize;

        this.isWhite = isWhite;
        this.name = "King";

        this.sprite = sheet.getSubimage(0*sheetScale, isWhite ? 0 : sheetScale, sheetScale, sheetScale).getScaledInstance(board.tileSize, board.tileSize, BufferedImage.SCALE_SMOOTH);
    }

    public boolean isValidMovement(int col, int row) {
        if (Math.abs((col - this.col) * (row - this.row)) == 1 || Math.abs(col - this.col) + Math.abs(row - this.row) == 1) {
            return true;
        }
  

    // Castling
     if (isFirstMove && row == this.row && Math.abs(col - this.col) == 2) {
            return canCastle(col);
        }
        return false;

            }

    // ==== ADDED: checks rook eligibility, clear path, and that the king isn't
    // currently in check, doesn't pass through check, and doesn't land in check ====
    private boolean canCastle(int col) {
        boolean kingSide = col > this.col;
        int rookCol = kingSide ? 7 : 0;

        Piece rook = board.getPiece(rookCol, this.row);
        if (rook == null || !rook.name.equals("Rook") || !rook.isFirstMove || rook.isWhite != this.isWhite) {
            return false;
        }

        int step = kingSide ? 1 : -1;
        for (int c = this.col + step; c != rookCol; c += step) {
            if (board.getPiece(c, this.row) != null) {
                return false;
            }
        }

        if (board.isSquareAttacked(this.col, this.row, !this.isWhite)) {
            return false;
        }
        if (board.isSquareAttacked(this.col + step, this.row, !this.isWhite)) {
            return false;
        }
        if (board.isSquareAttacked(col, this.row, !this.isWhite)) {
            return false;
        }
        return true;
    }
}
