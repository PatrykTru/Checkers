package com.kodilla.checkers;


import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {

    private Piece piece;

    public boolean hasPiece(){
        return piece != null;
    }
     public Piece getPiece(){
        return piece;
     }
     public void setPiece(Piece piece){
        this.piece = piece;
     }

    public Tile(boolean light , int x , int y){
        setWidth(CheckersGame.TITLE_SIZE);
        setHeight(CheckersGame.TITLE_SIZE);

        relocate(x* CheckersGame.TITLE_SIZE,y* CheckersGame.TITLE_SIZE);

        setFill(light ? Color.valueOf("#feb") : Color.valueOf("#588"));
    }
}
