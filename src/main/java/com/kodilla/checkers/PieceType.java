package com.kodilla.checkers;

public enum PieceType {
    BLACK(1,false) , WHITE(-1,true);
    final int directionOfMove;
    final boolean canMove ;


    PieceType(int directionOfMove, boolean canMove) {
        this.directionOfMove = directionOfMove;
        this.canMove = canMove;
    }

}
