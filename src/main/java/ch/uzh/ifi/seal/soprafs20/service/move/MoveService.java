package ch.uzh.ifi.seal.soprafs20.service.move;

import ch.uzh.ifi.seal.soprafs20.constant.DevelopmentType;
import ch.uzh.ifi.seal.soprafs20.constant.ResourceType;
import ch.uzh.ifi.seal.soprafs20.constant.TileType;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.game.Player;
import ch.uzh.ifi.seal.soprafs20.entity.game.Tile;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.City;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.Settlement;
import ch.uzh.ifi.seal.soprafs20.entity.moves.*;
import ch.uzh.ifi.seal.soprafs20.entity.moves.development.*;
import ch.uzh.ifi.seal.soprafs20.entity.moves.first.FirstPassMove;
import ch.uzh.ifi.seal.soprafs20.entity.moves.first.FirstRoadMove;
import ch.uzh.ifi.seal.soprafs20.entity.moves.first.FirstSettlementMove;
import ch.uzh.ifi.seal.soprafs20.repository.MoveRepository;
import ch.uzh.ifi.seal.soprafs20.service.FirstStackService;
import ch.uzh.ifi.seal.soprafs20.service.GameService;
import ch.uzh.ifi.seal.soprafs20.service.PlayerService;
import ch.uzh.ifi.seal.soprafs20.service.QueueService;
import ch.uzh.ifi.seal.soprafs20.service.board.BoardService;
import ch.uzh.ifi.seal.soprafs20.service.board.TileService;
import ch.uzh.ifi.seal.soprafs20.service.move.handler.MoveHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;


@Service
@Transactional
public class MoveService {

    private final Logger log = LoggerFactory.getLogger(MoveService.class);

    private final MoveRepository moveRepository;

    private PlayerService playerService;
    private TileService tileService;
    private GameService gameService;
    private BoardService boardService;
    private QueueService queueService;
    private FirstStackService firstStackService;

    @Autowired
    public MoveService(@Qualifier("moveRepository") MoveRepository moveRepository) {
        this.moveRepository = moveRepository;
    }

    @Autowired
    public void setFirstStackService(FirstStackService firstStackService) {
        this.firstStackService = firstStackService;
    }

    @Autowired
    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Autowired
    public void setTileService(TileService tileService) {
        this.tileService = tileService;
    }

    @Autowired
    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    @Autowired
    public void setBoardService(BoardService boardService) {
        this.boardService = boardService;
    }

    @Autowired
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }


    /**
     * Gets the passed Move from the moveRepository
     *
     * @param moveId the moveId
     * @return the move
     */
    public Move findMoveById(Long moveId) {
        Optional<Move> optionalMove = moveRepository.findById(moveId);
        return optionalMove.orElse(null);
    }

    /**
     * Gets the correct move handler form the move
     * passes the move an the MoveService (this) to the handler
     *
     * @param move the move
     */
    public void performMove(Move move) {

        MoveHandler handler = move.getMoveHandler();
        handler.perform(move, this);

        // delete all recent moves of the game
        deleteAllMovesForGame(move.getGameId());

        //Get the game
        Game game = gameService.findGameById(move.getGameId());

        //Make the recalculations
        makeRecalculations(game, handler, move);
    }

    //Is performed after performMove terminates
    public void makeRecalculations(Game game, MoveHandler handler, Move move) {

        // update the victory points of the current player
        Player player = game.getCurrentPlayer();

        int devPoints = playerService.getPointsFromDevelopmentCards(player);
        int buildingPoints = boardService.getPointsFromBuildings(game, player);

        int victoryPoints = devPoints + buildingPoints;

        player.setVictoryPoints(victoryPoints);

        //save player
        playerService.save(player);

        //If the player has more 10 or more points, then the game is over
        if (player.getVictoryPoints() >= 10) {
            deleteAllMovesForGame(game.getId());
            return;
        }

        //Calculate all new possible moves and saves them to the move repository
        List<Move> nextMoves = handler.calculateNextMoves(game, move);

        //Save the game (necessary to do here because of first part ->
        // randomly select first player in FirstPassMoveHandler)
        gameService.save(game);

        moveRepository.saveAll(nextMoves);
        moveRepository.flush();
    }

    public void makeSetupRecalculations(Game game) {

        //Calculate the first move
        if (game.getPlayers().size() >= game.getPlayerMinimum()) {
            List<Move> startMoves = MoveCalculator.calculateStartMoves(game);
            moveRepository.saveAll(startMoves);
            moveRepository.flush();
        }
    }

    /**
     * Performs a DiceMove
     * Is called from the DiceMoveHandler
     *
     * imitates dice roll and distributes resources to designated players
     *
     * @param diceMove the DiceMove that is passed to the handler
     */
    public void performDiceMove(DiceMove diceMove) {

        // roll dice1 & dice2
        int min = 1;    // inclusive
        int max = 7;    // exclusive

        int dice1;
        int dice2;

        dice1 = ThreadLocalRandom.current().nextInt(min, max);
        dice2 = ThreadLocalRandom.current().nextInt(min, max);

        int diceRoll = dice1 + dice2;

        // get tile(s) with rolled number
        List<Tile> tiles = boardService.getTilesWithNumber(diceMove.getGameId(), diceRoll);

        // update wallet of every player with building on tile
        for (Tile tile : tiles) {

            // get tile type
            TileType tileType = tile.getType();

            // convert into resource
            ResourceType resourceType = tileService.convertToResource(tileType);

            // get playerIDs with settlements on tile & update their wallets
            List<Long> playersWithSettlement = boardService.getPlayerIDsWithSettlement(diceMove.getGameId(), tile);
            playerService.updateWallet(playersWithSettlement, resourceType, new Settlement().getBuildingFactor());

            // get playerIDs with settlements on tile & update their wallets
            List<Long> playersWithCity = boardService.getPlayerIDsWithCity(diceMove.getGameId(), tile);
            playerService.updateWallet(playersWithCity, resourceType, new City().getBuildingFactor());
        }
    }

    public void deleteAllMovesForGame(Long gameId) {
        List<Move> expiredMoves = moveRepository.findAllByGameId(gameId);
        moveRepository.deleteAll(expiredMoves);
        moveRepository.flush();
    }

    /**
     * Performs a PassMove
     * Is called from the PassMoveHandler
     *
     * will set the next player to the current player (current player passes to make another move)
     *
     * @param passMove the PassMove that is passed from the handler
     */
    public void performPassMove(PassMove passMove) {

        Game game = gameService.findGameById(passMove.getGameId());

        Long queueReturn = queueService.getNextForGame(game.getId());

        Player nextPlayer = playerService.findPlayerByUserId(queueReturn);

        game.setCurrentPlayer(nextPlayer);

        gameService.save(game);
    }

    public void performFirstPassMove(FirstPassMove firstPassMove) {
        Game game = gameService.findGameById(firstPassMove.getGameId());

        //Find the userId for the next player in the game!
        Long nextUserId = firstStackService.getNextPlayerInGame(game.getId());

        Player nextPlayer = playerService.findPlayerByUserId(nextUserId);

        game.setCurrentPlayer(nextPlayer);

        gameService.save(game);
    }

    /**
     * Performs a TradeMove
     * Is called from the TradeMoveHandler
     *
     * the player can trade resources to get a designated resource
     *
     * @param tradeMove the TradeMove that is passed from the handler
     */
    public void performTradeMove(TradeMove tradeMove) {

        // player must pay for needed resourceType
        playerService.payForResource(tradeMove);

        // new resource gets added to the players wallet
        playerService.addResource(tradeMove);

    }

    /**
     * Performs a purchaseMove
     * Is called from the PurchaseMoveHandler
     *
     * the player can purchase a (random) development card with resources
     *
     * @param purchaseMove the PurchaseMove that is passed from the handler
     */
    public void performPurchaseMove(PurchaseMove purchaseMove) {

        // player must pay for the development card
        playerService.payForDevelopmentCard(purchaseMove);

        // add the development card to the player
        playerService.addDevelopmentCard(purchaseMove);

    }

    // -- card moves section --

    // removes invoked development card from player
    public void performCardMove(CardMove cardMove) {

        // get the development card type from the move
        DevelopmentType type = cardMove.getDevelopmentCard().getDevelopmentType();

        // remove development card from player, if it's not a victoryPoint card
        if (type != DevelopmentType.VICTORYPOINT) {
            playerService.removeDevelopmentCard(cardMove);
        }
    }

    // performs monopoly move
    public void performMonopolyMove(MonopolyMove monopolyMove) {

        playerService.monopolizeResources(monopolyMove);
    }

    // performs plenty move
    public void performPlentyMove(PlentyMove plentyMove) {

        playerService.plentyResources(plentyMove);
    }

    // performs roadProgress move
    public void performRoadProgressMove(RoadProgressMove roadProgressMove) {

        // since the player does not have to pay for road, it gets directly build
        boardService.build(roadProgressMove);
    }

    // performs knight move (relocates the robber on board)
    public void performKnightMove(KnightMove knightMove) {

        // TODO: implement functionality

        // get tile where robber will be placed

        // set robber on board
    }

    // performs stealing move (usually invoked after knight move)
    public void performStealMove(StealMove stealMove) {

        // deduct a random resource from victim and add it to player wallet
        playerService.stealResource(stealMove);
    }

    // -- end card moves section--

    /**
     * Performs a BuildMove
     * Is called from the BuildMoveHandler
     *
     * the player pays for and builds a building (road, settlement or city)
     *
     * @param buildMove the BuildMove that is passed from the handler
     */
    public void performBuildMove(BuildMove buildMove) {

        //Player must pay for the building
        playerService.payForBuilding(buildMove);

        //Build the building on the board
        boardService.build(buildMove);
    }

    public List<Move> getMovesForPlayerWithUserId(Long gameId, Long userId) {
        return moveRepository.findAllByGameIdAndUserId(gameId, userId);
    }

    public void performFirstSettlementMove(FirstSettlementMove firstSettlementMove) {
        boardService.build(firstSettlementMove);
    }

    public void performFirstRoadMove(FirstRoadMove firstRoadMove) {
        boardService.build(firstRoadMove);
    }

    public boolean canExitFirstPart(Long gameId) {

        Game game = gameService.findGameById(gameId);

        int numberOfPlayers = game.getPlayers().size();
        int numberOfRoads = game.getBoard().getRoads().size();

        return (numberOfRoads / 2) == numberOfPlayers;
    }

    public void performStartMove(StartMove startMove) {

        firstStackService.createStackForGameWithId(startMove.getGameId());
    }

}
