package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.game.Board;
import ch.uzh.ifi.seal.soprafs20.entity.game.Tile;
import ch.uzh.ifi.seal.soprafs20.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.BeanMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebAppConfiguration
@SpringBootTest
public class BoardServiceIntegrationTest {

    @Qualifier("coordinateRepository")
    @Autowired
    CoordinateRepository coordinateRepository;

    @Qualifier("tileRepository")
    @Autowired
    TileRepository tileRepository;

    @Qualifier("boardRepository")
    @Autowired
    BoardRepository boardRepository;

    @Qualifier("moveRepository")
    @Autowired
    MoveRepository moveRepository;

    @Qualifier("gameRepository")
    @Autowired
    GameRepository gameRepository;

    @Autowired
    BoardService boardService;

    @Autowired
    TileService tileService;

    @Autowired
    EntityManager entityManager;

    @BeforeEach
    public void setup() {
        gameRepository.deleteAll();
        boardRepository.deleteAll();
        tileRepository.deleteAll();

        moveRepository.deleteAll();
    }

    @AfterEach
    public void teardown() {
        entityManager.clear();
    }

    @Test
    public void testCreateBoard() {

        Board result = boardService.createBoard();

        assertNotNull(result, "The result should not be null!");

        List<Tile> tiles = result.getTiles();
        assertEquals(19, tiles.size(), "There should be 19 tiles");

        assertEquals(54, coordinateRepository.findAll().size(), "There should be 54 coordinates");
    }
}
