package com.kodilla.checkers;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

import static com.kodilla.checkers.CheckersGame.TITLE_SIZE;
public class Piece extends StackPane {

    private PieceType type;

    private double mouseX, mouseY;
    private double oldX, oldY;

    public double getOldX() {
        return oldX;
    }

    public double getOldY() {
        return oldY;
    }

    public PieceType getType() {
        return type;
    }

    public Piece(PieceType type, int x, int y) {
        this.type = type;

        move(x , y );

        Ellipse bg = new Ellipse(TITLE_SIZE * 0.3125, TITLE_SIZE * 0.26);
        bg.setFill(type == PieceType.BLACK
                ? Color.BLACK : Color.WHITE);

        bg.setStroke(Color.GRAY);
        bg.setStrokeWidth(TITLE_SIZE * 0.03);

        bg.setTranslateX((TITLE_SIZE - TITLE_SIZE * 0.3125 * 2) / 2);
        bg.setTranslateY((TITLE_SIZE - TITLE_SIZE * 0.26 * 2) / 2 + TITLE_SIZE * 0.07);

        Ellipse ellipse = new Ellipse(TITLE_SIZE * 0.3125, TITLE_SIZE * 0.26);
        ellipse.setFill(type == PieceType.BLACK
                ? Color.BLACK : Color.WHITE);

        ellipse.setStroke(Color.GRAY);
        ellipse.setStrokeWidth(TITLE_SIZE * 0.03);

        ellipse.setTranslateX((TITLE_SIZE - TITLE_SIZE * 0.3125 * 2) / 2);
        ellipse.setTranslateY((TITLE_SIZE - TITLE_SIZE * 0.26 * 2) / 2);


        getChildren().addAll(bg, ellipse);

        if(type.canMove) {
            setOnMousePressed(e -> {
                mouseX = e.getSceneX();
                mouseY = e.getSceneY();
            });


            setOnMouseDragged(e -> {
                relocate(e.getSceneX() - mouseX + oldX, e.getSceneY() - mouseY + oldY);
            });
        }


    }
    public void move(int x ,  int y){
        oldX = x* TITLE_SIZE;
        oldY = y * TITLE_SIZE;
        relocate(oldX,oldY);
    }
    public void abortMove(){
        relocate(oldX,oldY);
    }
}
