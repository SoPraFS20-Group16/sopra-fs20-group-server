package ch.uzh.ifi.seal.soprafs20.service;


import ch.uzh.ifi.seal.soprafs20.constant.GameConstants;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.GameSummary;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.entity.game.Board;
import ch.uzh.ifi.seal.soprafs20.entity.game.Player;
import ch.uzh.ifi.seal.soprafs20.entity.game.PlayerQueue;
import ch.uzh.ifi.seal.soprafs20.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs20.repository.GameSummaryRepository;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import ch.uzh.ifi.seal.soprafs20.service.board.BoardService;
import ch.uzh.ifi.seal.soprafs20.service.move.MoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameSummaryRepository gameSummaryRepository;

    private BoardService boardService;
    private PlayerService playerService;
    private QueueService queueService;
    private MoveService moveService;
    private FirstStackService firstStackService;
    private BotService botService;

    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository,
                       @Qualifier("playerRepository") PlayerRepository playerRepository,
                       @Qualifier("gameSummaryRepository") GameSummaryRepository gameSummaryRepository) {

        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.gameSummaryRepository = gameSummaryRepository;
    }

    @Autowired
    public void setMoveService(MoveService moveService) {
        this.moveService = moveService;
    }

    @Autowired
    public void setFirstStackService(FirstStackService firstStackService) {
        this.firstStackService = firstStackService;
    }

    @Autowired
    public void setBoardService(BoardService boardService) {
        this.boardService = boardService;
    }

    @Autowired
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }

    @Autowired
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Autowired
    public void setBotService(BotService botService) {
        this.botService = botService;
    }

    /**
     * Returns all the games currently in the database
     *
     * @return the games
     */
    public List<Game> getGames() {

        return gameRepository.findAll();
    }

    /**
     * Creates a new game
     * <p>
     * The game input has to hold the following fields:
     * -CreatorId
     * -WithBots
     * -Name
     *
     * @param gameInput the game input
     * @return the game
     */
    public Game createGame(Game gameInput) {

        //Check for conflict
        //findGame is specified to work with all the games primary keys
        Game conflictGame = findGame(gameInput);

        //Returns null if there is a conflict
        if (conflictGame != null) {
            return null;
        }

        //save game to give it an id
        Game savedGame = gameRepository.saveAndFlush(gameInput);

        // creator of game is creatorPlayer
        Player creatorPlayer = playerService.createPlayer(savedGame.getCreatorId(), savedGame.getId());

        // add creator to players list and set as current player
        savedGame.addPlayer(creatorPlayer);
        savedGame.setCurrentPlayer(creatorPlayer);


        //Add a Board
        Board newBoard = boardService.createBoard(savedGame.getId());
        savedGame.setBoard(newBoard);

        //Save the game to persist with board
        savedGame = gameRepository.saveAndFlush(savedGame);


        //Create Player queue and add the player to it
        PlayerQueue queue = new PlayerQueue();
        queue.addUserId(creatorPlayer.getUserId());
        queue.setGameId(savedGame.getId());
        queueService.save(queue);


        //Calculate the first moves
        moveService.makeSetupRecalculations(savedGame);

        //Return the saved game
        return savedGame;
    }

    public Game findGame(Game gameInput) {

        if (gameInput == null) {
            return null;
        }

        Game foundGame = null;

        if (gameInput.getId() != null) {
            //Must be able to find game from Id
            Optional<Game> foundGameOptional = gameRepository.findById(gameInput.getId());
            foundGame = foundGameOptional.orElse(null);
        }

        //find by name if not found already
        if (foundGame == null && gameInput.getName() != null) {
            foundGame = gameRepository.findByName(gameInput.getName());
        }


        //If no game exists found game is null
        return foundGame;
    }

    /**
     * A boolean stating if a user can access the game
     *
     * @param user the user as returned by the userService
     * @param game the game as returned by the gameService
     * @return the boolean
     */
    public boolean userCanAccessGame(@NotNull User user, @NotNull Game game) {

        Player player = playerRepository.findByUserId(user.getId());
        if (player == null) {
            return false;
        }
        return game.isPlayer(player);
    }

    public Game findGameById(Long gameId) {
        return gameRepository.findById(gameId).orElse(null);
    }

    public void addPlayerToGame(Player createdPlayer, Game game) {

        //Add the player to the queue of the game
        queueService.addPlayerToQueue(game.getId(), createdPlayer.getUserId());

        //Add player to game and save
        game.addPlayer(createdPlayer);
        gameRepository.saveAndFlush(game);

        //Recalculate moves
        moveService.makeSetupRecalculations(game);
    }

    public Game save(Game game) {
        return gameRepository.saveAndFlush(game);
    }

    public Game findGameOfUser(Long userId) {

        List<Game> games = gameRepository.findAll();
        for (Game game : games) {
            for (Player player : game.getPlayers()) {
                if (player.getUserId().equals(userId)) {
                    return game;
                }
            }
        }
        return null;
    }

    public void teardownGameWithId(Long gameId) {

        // create game summary
        createGameSummary(gameId);

        //delete moves
        moveService.deleteAllMovesForGame(gameId);

        //delete queue
        queueService.deleteQueueForGame(gameId);

        //delete stack
        firstStackService.deleteStackForGame(gameId);

        //delete game
        this.deleteGameWithId(gameId);
    }

    private void createGameSummary(Long gameId) {

        Game game = findGameById(gameId);

        GameSummary summary = new GameSummary();

        summary.setGameId(gameId);

        // set winner
        summary.setWinner("WINNER: " + game.getCurrentPlayer().getUsername() + ": " +
                game.getCurrentPlayer().getVictoryPoints() + " points");

        // set all
        List<String> players = new ArrayList<>();
        game.getPlayers().forEach(player -> players.add(player.getUsername() + ": " +
                player.getVictoryPoints() + " points"));
        summary.setPlayers(players);

        // save summary
        gameSummaryRepository.saveAndFlush(summary);
    }

    public GameSummary findGameSummary(Long gameId) {

        return gameSummaryRepository.findByGameId(gameId);
    }

    private void deleteGameWithId(Long gameId) {
        Optional<Game> game = gameRepository.findById(gameId);
        if (game.isPresent()) {
            gameRepository.delete(game.get());
            gameRepository.flush();
        }
    }

    public void fillWithBots(Game game) {

        int playerCount = game.getPlayers().size();

        for (int i = playerCount; i < GameConstants.DEFAULT_PLAYER_MAX; i++) {

            botService.createBot(game.getId());
        }
    }
}
