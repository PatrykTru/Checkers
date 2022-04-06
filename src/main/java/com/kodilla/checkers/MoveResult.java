package com.kodilla.checkers;

public class MoveResult {

    private Movement movement;
    private Piece piece;

    public Movement getMovement() {
        return movement;
    }

    public Piece getPiece() {
        return piece;
    }

    public MoveResult(Movement movement, Piece piece) {
        this.movement = movement;
        this.piece = piece;
    }

    public MoveResult(Movement movement){
        this(movement, null);
    }
}
