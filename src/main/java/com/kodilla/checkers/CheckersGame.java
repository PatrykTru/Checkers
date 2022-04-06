package com.kodilla.checkers;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public class CheckersGame extends Application{
    public static final int TITLE_SIZE = 100;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;

    private Tile[][] board = new Tile[WIDTH][HEIGHT];

    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();


    public static void main(String[] args) {
        launch(args);
    }

    private Parent createContent(){
        Pane root = new Pane();
        root.setPrefSize(WIDTH*TITLE_SIZE ,HEIGHT*TITLE_SIZE);
        root.getChildren().addAll(tileGroup,pieceGroup);

        for(int i  = 0 ; i < HEIGHT ; i++)
        {
            for (int k = 0; k < WIDTH ; k++)
            {
                Tile tile = new Tile((i+ k)%2 == 0,i,k);
                board[i][k] = tile;

                tileGroup.getChildren().add(tile);

                Piece piece = null;

                if(k <= 2 && (i+ k)%2 != 0){
                    piece = makePiece(PieceType.BLACK, i,k);
                }
                if(k >= 5 && (i+ k)%2 != 0){
                    piece = makePiece(PieceType.WHITE, i,k);
                }
                if(piece != null) {
                    tile.setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }

            }

        }

        return root;
    }

    private MoveResult tryIt(Piece piece , int newX, int newY){
        if (board[newX][newY].hasPiece()  || (newX + newY) % 2 == 0 ) {
        return  new MoveResult(Movement.NONE);
        }

        int x0 = toBoard(piece.getOldX());
        int y0 = toBoard(piece.getOldY());

        if(Math.abs(newX - x0) == 1&& newY - y0 == piece.getType().directionOfMove){
            return  new MoveResult(Movement.NORMAL);
        }
        if(Math.abs(newX - x0) == 2&& newY - y0 == piece.getType().directionOfMove *2) {

            int x1 = x0 + (newX - x0) / 2;
            int y1 = y0 + (newY - y0) / 2;

            if (board[x1][y1].hasPiece() && board[x1][y1].getPiece().getType() != piece.getType()) {
                return new MoveResult(Movement.KILL , board[x1][y1].getPiece());
            }
        }
        if(Math.abs(newX - x0) == 2&& newY - y0 == piece.getType().directionOfMove *-2) {

            int x1 = x0 + (newX - x0) / 2;
            int y1 = y0 + (newY - y0) / 2;

            if (board[x1][y1].hasPiece() && board[x1][y1].getPiece().getType() != piece.getType()) {
                return new MoveResult(Movement.KILL , board[x1][y1].getPiece());
            }
        }

        return new MoveResult(Movement.NONE);
    }

    private int toBoard(double pixel){
        return (int)(pixel + TITLE_SIZE / 2)/ TITLE_SIZE;
    }

    private Piece makePiece(PieceType type, int x, int y){
        Piece piece = new Piece(type,x,y);
        piece.setOnMouseReleased(e -> {
            int newX = toBoard(piece.getLayoutX());
            int newY = toBoard(piece.getLayoutY());

            MoveResult result   = tryIt(piece, newX , newY);

            int x0 = toBoard(piece.getOldX());
            int y0 = toBoard(piece.getOldY());
            switch (result.getMovement()){
                case NONE:
                    piece.abortMove();
                    break;
                case NORMAL:
                    piece.move(newX , newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);

                    break;
                case KILL:
                    piece.move(newX , newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);
                    Piece otherPiece = result.getPiece();
                    board[toBoard(otherPiece.getOldX())][toBoard(otherPiece.getOldY())].setPiece(null);
                    pieceGroup.getChildren().remove(otherPiece);
                    break;
            }
        });

        return piece;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        

        primaryStage.setTitle("Checkers");
        primaryStage.setScene(new Scene(createContent()));
        primaryStage.show();

    }
}
