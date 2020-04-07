package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.entity.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository("moveRepository")
public interface MoveRepository extends JpaRepository<Move, Long> {

    //Finds all moves of a specific player of the game
    List<Move> findAllByGameIdAndPlayerId(Long gameId, Long playerId);
}
