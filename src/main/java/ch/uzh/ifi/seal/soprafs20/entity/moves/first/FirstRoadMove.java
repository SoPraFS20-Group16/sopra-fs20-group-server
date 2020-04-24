package ch.uzh.ifi.seal.soprafs20.entity.moves.first;

import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.Road;
import ch.uzh.ifi.seal.soprafs20.entity.moves.BuildMove;
import ch.uzh.ifi.seal.soprafs20.entity.moves.Move;
import ch.uzh.ifi.seal.soprafs20.service.move.handler.first.FirstRoadMoveHandler;
import ch.uzh.ifi.seal.soprafs20.service.move.handler.MoveHandler;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "FIRST_ROAD_MOVE")
public class FirstRoadMove extends BuildMove {

    @Override
    public MoveHandler getMoveHandler() {
        return new FirstRoadMoveHandler();
    }
}
