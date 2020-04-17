package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.DevelopmentType;
import ch.uzh.ifi.seal.soprafs20.constant.ErrorMsg;
import ch.uzh.ifi.seal.soprafs20.constant.ResourceType;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.entity.game.Player;
import ch.uzh.ifi.seal.soprafs20.entity.game.ResourceWallet;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.Building;
import ch.uzh.ifi.seal.soprafs20.entity.game.cards.DevelopmentCard;
import ch.uzh.ifi.seal.soprafs20.entity.moves.BuildMove;
import ch.uzh.ifi.seal.soprafs20.entity.moves.TradeMove;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;

@Service
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    @Autowired
    public PlayerService(@Qualifier("playerRepository") PlayerRepository playerRepository,
                         @Qualifier("userRepository") UserRepository userRepository) {
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create player from user player.
     * <p>
     * Will throw an exception if the user can not be found
     *
     * @param userId the userId to find the user
     * @return the player
     */
    public Player createPlayerFromUserId(@NotNull Long userId) {

        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new NullPointerException("No user with given Id found!");
        }
        Player player = new Player();
        player.setUsername(user.getUsername());
        player.setUserId(user.getId());

        return playerRepository.saveAndFlush(player);
    }

    public Player payForDevelopmentCard(TradeMove move) {

        // find player
        Player player = playerRepository.findByUserId(move.getUserId());

        if (player == null) {
            throw new NullPointerException(ErrorMsg.NO_PLAYER_FOUND_WITH_USER_ID);
        }

        // pay for developmentCard
        ResourceWallet price = move.getDevelopmentCard().getPrice();
        ResourceWallet funds = player.getWallet();

        for (ResourceType type: price.getAllTypes()) {
            funds.removeResource(type, price.getResourceAmount(type));
        }

        player.setWallet(funds);

        // save player
        return playerRepository.saveAndFlush(player);
    }

    public Player addDevelopmentCard(TradeMove move) {

        // find player
        Player player = playerRepository.findByUserId(move.getUserId());

        if (player == null) {
            throw new NullPointerException(ErrorMsg.NO_PLAYER_FOUND_WITH_USER_ID);
        }

        // add DevelopmentCard
        player.addDevelopmentCard(move.getDevelopmentCard());

        return playerRepository.saveAndFlush(player);

    }

    public Player payForResource(TradeMove move) {

        //Find Player
        Player player = playerRepository.findByUserId(move.getUserId());

        if (player == null) {
            throw new NullPointerException(ErrorMsg.NO_PLAYER_FOUND_WITH_USER_ID);
        }

        // pay for resource
        ResourceWallet funds = player.getWallet();
        ResourceType type = move.getNeededType();

        // TODO: maybe ratio initiate somewhere else (more general)
        int tradeRatio = 4;

        funds.removeResource(type, tradeRatio);

        player.setWallet(funds);

        return playerRepository.saveAndFlush(player);
    }

    public Player addResource(TradeMove move) {

        //Find Player
        Player player = playerRepository.findByUserId(move.getUserId());

        if (player == null) {
            throw new NullPointerException(ErrorMsg.NO_PLAYER_FOUND_WITH_USER_ID);
        }

        // get traded resourceType
        ResourceType type = move.getNeededType();
        ResourceWallet funds = player.getWallet();

        // TODO: maybe initialize somewhere else (more general)
        int tradeRatio = 1;

        // add resource to players wallet

        funds.addResource(type, tradeRatio);

        player.setWallet(funds);

        return playerRepository.saveAndFlush(player);
    }

    public Player payForBuilding(BuildMove move) {

        //Find Player
        Player player = playerRepository.findByUserId(move.getUserId());

        if (player == null) {
            throw new NullPointerException(ErrorMsg.NO_PLAYER_FOUND_WITH_USER_ID);
        }

        //Pay for the given building
        payForBuildingWorker(move.getBuilding(), player);

        //Save
        return playerRepository.saveAndFlush(player);
    }

    private void payForBuildingWorker(Building building, Player player) {

        //Get building price
        ResourceWallet price = building.getPrice();

        //Get players funds
        ResourceWallet funds = player.getWallet();

        //Remove all the resources required to complete payment
        for (ResourceType type : price.getAllTypes()) {
            funds.removeResource(type, price.getResourceAmount(type));
        }

        // TODO: necessary?
        player.setWallet(funds);
    }

    public void payDevCard(Long playerId, DevelopmentCard developmentCard) {

        // find player
        Player player = playerRepository.findByUserId(playerId);

        // get the currently owned development cards from the player
        List<DevelopmentCard> playerOwnedCards = player.getDevelopmentCards();

        // remove development card
        playerOwnedCards.remove(developmentCard);

        // set the new player owned development cards
        player.setDevelopmentCards(playerOwnedCards);
    }

    public void addVictoryPoint(Long playerId) {

        // find player
        Player player = playerRepository.findByUserId(playerId);

        // get current victoryPoints
        int victoryPoints = player.getVictoryPoints();

        // increase victoryPoints
        victoryPoints += 1;

        // set new victoryPoints
        player.setVictoryPoints(victoryPoints);
    }

    public int getPointsFromDevelopmentCards(Player player) {
        int victoryPoints = 0;

        // get all victory points earned from development cards
        for (DevelopmentCard card : player.getDevelopmentCards()) {
            if (card.getDevelopmentType().equals(DevelopmentType.VICTORYPOINT)) {
                victoryPoints += 1;
            }
        }

        return victoryPoints;
    }

    public Player findPlayerByUserId(Long id) {
        return playerRepository.findByUserId(id);
    }

    public void updateWallet(List<Long> playerIDs, List<ResourceType> resourceTypes) {

        for (Long playerID: playerIDs) {
            Player player = playerRepository.findByUserId(playerID);
            ResourceWallet funds = player.getWallet();

            for (ResourceType type: resourceTypes) {
                funds.addResource(type, 1);
            }

            player.setWallet(funds);
        }

    }
}
