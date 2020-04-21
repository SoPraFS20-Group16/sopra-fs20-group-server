package ch.uzh.ifi.seal.soprafs20.service.move.handler;

import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.moves.Move;
import ch.uzh.ifi.seal.soprafs20.service.move.MoveCalculator;
import ch.uzh.ifi.seal.soprafs20.service.move.MoveService;

import java.util.List;

public interface MoveHandler {

    /**
     * Calls the correct method from the MoveService according to the Move subclass it belongs to.
     *
     * @param move    the move
     * @param service the service
     */
    void perform(Move move, MoveService service);


    default List<Move> calculateNextMoves(Game game) {
        return MoveCalculator.calculateAllStandardMoves(game);
    }

}

