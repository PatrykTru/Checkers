/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kodilla.checkers;

import java.util.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class BoardLogic {
    // graphics parameters
    private final double pieceMargin, startX, startY, sideLength, unitLength;
    private Board board;             // board configuration
    private List<BoardPos> legalPos; // a list of active (highlighted) legal positions
    // internal logic parameters, color true if black, type true if human
    boolean lastColor, gameOver, opponentSet;


    public BoardLogic(double _startX, double _startY, double _sideLength,
                      double _pieceMargin, int sideCount, int startCount) {
        // simple copying
        startX = _startX;
        startY = _startY;
        sideLength = _sideLength;
        pieceMargin = _pieceMargin;
        // calculating single unit's side length in pixels for further use
        unitLength = _sideLength / sideCount;
        // board configuration & initial logic initialization
        board = new Board(sideCount, startCount);
        legalPos = new ArrayList<>();
        lastColor = true;
        gameOver = false;
        opponentSet = false;
    }

    public BoardLogic(BoardLogic boardLogic) {
        startX = boardLogic.startX;
        startY = boardLogic.startY;
        sideLength = boardLogic.sideLength;
        pieceMargin = boardLogic.pieceMargin;
        unitLength = boardLogic.unitLength;
        board = new Board(boardLogic.board);
        legalPos = new ArrayList<>(boardLogic.legalPos);
        lastColor = boardLogic.lastColor;
        gameOver = boardLogic.gameOver;
        opponentSet = boardLogic.opponentSet;
    }

    public void update(BoardLogic boardLogic) {
        board = new Board(boardLogic.board);
        legalPos = new ArrayList<>(boardLogic.legalPos);
        lastColor = boardLogic.lastColor;
        gameOver = boardLogic.gameOver;
        opponentSet = boardLogic.opponentSet;
    }

    public void reset() {
        board.reset();
        lastColor = true;
        gameOver = false;
        opponentSet = false;
    }


    public void highlightMoves(BoardPos from) {
        List<BoardPos> longest = longestAvailableMoves(2, !lastColor);

        // no strikes available - player chooses some regular move
        if (longest.isEmpty() && from.inBounds(board.side()) &&
                !board.get(from).isEmpty() && board.get(from).color() != lastColor)
            legalPos = getMoves(from);
            // some strikes available - player chooses from the longest ones
        else for (BoardPos strike : longest)
            legalPos.addAll(getMoves(strike));
    }

    public void attemptMove(BoardPos to) {
        if (legalPos.contains(to)) {
            lastColor = !lastColor; // next turn
            // move striking piece to the end position
            board.set(to, board.get(legalPos.get(legalPos.indexOf(to)).getRouteLast()));
            // clear positions "en route" - pieces to strike and the initial position
            for (BoardPos step : legalPos.get(legalPos.indexOf(to)).getRoute())
                board.get(step).setEmpty();
            // promote qualifying pieces to crown
            findCrown();
        }

        legalPos.clear(); // next turn - no highlights
    }


    public void draw(GraphicsContext gc) {
        // draw the base grid
        gc.setFill(Color.LIGHTGREY);
        for (int i = 0; i < board.side(); i++)
            for (int j = (i % 2 == 0) ? 1 : 0; j < board.side(); j += 2)
                gc.fillRect(startX + j * unitLength, startY + i * unitLength,
                        unitLength, unitLength);

        // draw boundaries
        gc.setStroke(Color.BLACK);
        gc.strokeRect(startX, startY, sideLength, sideLength);

        // highlight legal positions
        for (BoardPos pos : legalPos) {
            gc.setFill(Color.ORANGE);
            gc.fillRect(startX + pos.getX() * unitLength,
                    startY + pos.getY() * unitLength, unitLength, unitLength);
            gc.setFill(Color.LIGHTYELLOW);
            if (pos.route != null)
                for (BoardPos step : pos.route)
                    gc.fillRect(startX + step.getX() * unitLength,
                            startY + step.getY() * unitLength, unitLength, unitLength);
        }

        // draw pieces
        for (int i = 0; i < board.side(); i++)
            for (int j = 0; j < board.side(); j++)
                board.get(i, j).draw(gc, startX + i * unitLength,
                        startY + j * unitLength, pieceMargin, unitLength);
    }


    public String message() {
        if (!longestAvailableMoves(2, !lastColor).isEmpty())
            return "Strike available";
        else if (isGameOver())
            return "Game over! Click somewhere to continue";
        else return "Turn: " + (lastColor ? "White" : "Black");
    }


    public boolean someLegalPos() {
        return !legalPos.isEmpty();
    }


    public boolean isGameOverDelayed() {
        return gameOver;
    }

    public boolean getLastColor() {
        return lastColor;
    }

    public BoardPos decodeMouse(double mouseX, double mouseY) {
        if (mouseX > startX && mouseY > startY && mouseX < startX + sideLength &&
                mouseY < startY + sideLength) // range check
            return new BoardPos( (int)((mouseX - startX) / unitLength),
                    (int)((mouseY - startY) / unitLength ));
        else return null;
    }

    public List<BoardPos> getMoves(BoardPos from) {
        List<BoardPos> result;

        // strike check
        if (board.get(from).isCrown())
            result = getStrikesCrown(from);
        else result = getStrikes(from);

        // regular moves
        final int[] shifts = {-1, 1};
        if (result.isEmpty() && !board.get(from).isEmpty()) {
            if (board.get(from).isCrown())
                for (int shiftX : shifts)
                    for (int shiftY : shifts) {
                        BoardPos to = from.add(shiftX, shiftY);
                        while (to.inBounds(board.side()) && board.get(to).isEmpty()) {
                            result.add(to);
                            to = to.add(shiftX, shiftY);
                        }
                    }
            else for (int shift : shifts) { // add adjacent empty positions
                BoardPos move = from.add(new BoardPos(shift,
                        board.get(from).color() ? 1 : -1));
                if (board.get(move) != null && board.get(move).isEmpty())
                    result.add(new BoardPos(move));
            } }

        // complete by adding the start position to every legal route, so that
        // it will be cleared as well when the player will move
        for (BoardPos pos : result)
            pos.addToRoute(new BoardPos(from));

        return result;
    }

    private List<BoardPos> getStrikes(BoardPos from) {
        Queue<BoardPos> search = new LinkedList<>(); search.add(from);
        List<BoardPos> result = new ArrayList<>();
        final int[] offsets = {-2, 2};

        // below is essentially a level-order tree transverse algorithm
        while (!search.isEmpty()) {
            // some new positions found from the current search position?
            boolean finalPos = true;
            // go in all 4 directions, to corresponding potential next position
            for (int offX : offsets)
                for (int offY : offsets) {
                    BoardPos to = new BoardPos(search.peek().getX() + offX,
                            search.peek().getY() + offY);
                    // copy route up to this point
                    to.setRoute(search.peek().getRoute());

                    // position between the current search and potential next one
                    // contains a piece that can be stricken for the first time
                    // in this route (no infinite loops)
                    if (to.inBounds(board.side()) && board.get(to).isEmpty() &&
                            !board.get(to.avg(search.peek())).isEmpty() &&
                            board.get(from).color() !=
                                    board.get(to.avg(search.peek())).color() &&
                            !to.getRoute().contains(to.avg(search.peek()))) {
                        to.addToRoute(new BoardPos(to.avg(search.peek())));
                        search.add(to);
                        finalPos = false;
                    }
                }

            // only add positions at the end of the route to result
            if (finalPos && !search.peek().equals(from))
                result.add(search.peek());

            // next element search
            search.poll();
        }

        // filter strikes shorter than maximum length
        return filterShorter(result);
    }

    private List<BoardPos> getStrikesCrown(BoardPos from) {
        Queue<BoardPos> search = new LinkedList<>(); search.add(from);
        List<BoardPos> result = new ArrayList<>();
        final int[] direction = {-1, 1};

        // below is essentially a level-order tree transverse algorithm
        while (!search.isEmpty()) {
            // some new positions found from the current search position?
            boolean finalPos = true;
            // go in all 4 orthogonal directions
            for (int dirX : direction)
                for (int dirY : direction) {
                    // initial next position to check in this direction
                    BoardPos pos = search.peek().add(dirX, dirY);
                    // some pieces already stricken in this direction
                    BoardPos strike = null;
                    // copy route up to this point
                    pos.setRoute(new ArrayList<>(search.peek().getRoute()));

                    // this goes through all potential legal positions in this
                    // direction, before and after first(!) strike
                    while (pos.inBounds(board.side()) &&
                            (board.get(pos).isEmpty() ||
                                    (pos.add(dirX, dirY).inBounds(board.side()) &&
                                            board.get(pos.add(dirX, dirY)).isEmpty() &&
                                            board.get(from).color() != board.get(pos).color()))) {
                        // this position contains a piece that can be stricken
                        // for the first time in this route (no infinite loops)
                        if (!board.get(pos).isEmpty() && board.get(from).color()
                                != board.get(pos).color() && !pos.getRoute().contains(pos) &&
                                pos.add(dirX, dirY).inBounds(board.side()) &&
                                board.get(pos.add(dirX, dirY)).isEmpty()) {
                            strike = new BoardPos(pos);
                            finalPos = false;
                            pos = pos.add(dirX, dirY);
                            // stricken pieces added to route so that they will
                            // be highlighted & removed later
                            pos.addToRoute(strike);
                        }
                        // add all positions after strike to
                        if (strike != null && !pos.equals(strike))
                            search.add(pos);

                        // next position in current direction
                        pos = pos.add(dirX, dirY);
                    }
                }

            if (finalPos && !search.peek().equals(from))
                result.add(search.peek());

            // next element in search
            search.poll();
        }

        // filter strikes shorter than maximum length
        return filterShorter(result);
    }

    public List<BoardPos> longestAvailableMoves(int minDepth, boolean color) {
        List<BoardPos> result = new ArrayList<>();

        for (int i = 0; i < board.side(); i++)
            for (int j = 0; j < board.side(); j++)
                if (!board.get(i, j).isEmpty() &&
                        board.get(i, j).color() == color) {
                    List<BoardPos> _legalPos = getMoves(new BoardPos(i, j));
                    // some moves are available from the current position...
                    if (!_legalPos.isEmpty()) {
                        // ...with routes longer then  the last longest...
                        if (_legalPos.get(0).routeLen() > minDepth) {
                            // contains positions with routes shorter than new
                            // longest, so clear it
                            result.clear();
                            // update last longest route length
                            minDepth = _legalPos.get(0).routeLen();
                        }
                        // ...and equal to the last longest
                        if (_legalPos.get(0).routeLen() == minDepth)
                            result.add(new BoardPos(i, j));
                    }
                }

        return result;
    }


    private void findCrown() {
        // iterate over all x-positions
        for (int i = 0; i < board.side(); i++) {
            // only extreme elements are relevant
            if (!board.get(i, 0).isEmpty() && !board.get(i, 0).color())
                board.get(i, 0).setCrown();
            if (!board.get(i, board.side() - 1).isEmpty() &&
                    board.get(i, board.side() - 1).color())
                board.get(i, board.side() - 1).setCrown();
        }
    }

    private boolean isGameOver() {
        // either all black or all white pieces don't have any moves left, save
        // to internal field so that a delayed status is available
        gameOver = longestAvailableMoves(1, true).isEmpty() ||
                longestAvailableMoves(1, false).isEmpty();
        return gameOver;
    }

    private List<BoardPos> filterShorter(List<BoardPos> route) {
        int maxDepth = route.isEmpty() ? 0 : route.get(route.size() - 1).routeLen();
        Iterator<BoardPos> it = route.iterator();

        while (it.hasNext()) {
            BoardPos pos = it.next();
            if (pos.routeLen() != maxDepth)
                it.remove();
        }

        return route;
    }

    // work in progress below
    public Board getBoard() {
        return board;
    }

    public boolean isOpponentSet() {
        return opponentSet;
    }

    public boolean turn() {
        return !lastColor;
    }

    public void setOpponent() {
        opponentSet = true;
    }
}
