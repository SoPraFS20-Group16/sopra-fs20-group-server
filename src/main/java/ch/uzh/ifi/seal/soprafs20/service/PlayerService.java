package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.DevelopmentType;
import ch.uzh.ifi.seal.soprafs20.constant.ErrorMsg;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.entity.game.Player;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.Building;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.City;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.Road;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.Settlement;
import ch.uzh.ifi.seal.soprafs20.entity.game.cards.DevelopmentCard;
import ch.uzh.ifi.seal.soprafs20.entity.game.cards.ResourceCard;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
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


    public Player buildAndPay(Long userId, Building building) {

        //Find Player
        Player player = playerRepository.findByUserId(userId);

        if (player == null) {
            throw new NullPointerException(ErrorMsg.NO_PLAYER_FOUND_WITH_USER_ID);
        }

        //Pay for the given building
        payForBuilding(building, player);

        //Add building
        addBuilding(player, building);

        //Save
        return playerRepository.saveAndFlush(player);
    }

    private void addBuilding(Player player, Building building) {

        switch (building.getType()) {
            case CITY:
                player.addCity((City) building);
                break;
            case ROAD:
                player.addRoad((Road) building);
                break;
            case SETTLEMENT:
                player.addSettlement((Settlement) building);
                break;
            default:
                throw new IllegalStateException(ErrorMsg.UNDEFINED_BUILDING_TYPE);
        }
    }

    private void payForBuilding(Building building, Player player) {
        //Remove resources
        List<ResourceCard> price = building.getPrice();

        //Collect the cards that will be taken from the player
        List<ResourceCard> cardsToRemove = new ArrayList<>();

        //The cards the player currently owns
        List<ResourceCard> playerOwnedCards = player.getResourceCards();

        //Find the cards to remove from the players array
        for (ResourceCard card : price) {
            for (ResourceCard playerOwnedCard : playerOwnedCards) {

                if (card.getResourceType().equals(playerOwnedCard.getResourceType())) {
                    cardsToRemove.add(playerOwnedCard);
                }
            }
        }

        //remove the cards from the players array
        playerOwnedCards.removeAll(cardsToRemove);

        //set the new player owned resource cards
        player.setResourceCards(playerOwnedCards);
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

    public void recalculateVictoryPoints(Game game) {

        for (Player player: game.getPlayers()) {

            int victoryPoints = 0;

            // get all victory points earned from settlements
            for (Building settlement: player.getSettlements()) {
                int tmp = settlement.getVictoryPoints();
                victoryPoints += tmp;
            }

            // get all victory points earned from cities
            for (Building city: player.getCities()) {
                int tmp = city.getVictoryPoints();
                victoryPoints += tmp;
            }

            // get all victory points earned from development cards
            for (DevelopmentCard card: player.getDevelopmentCards()) {
                if (card.getDevelopmentType().equals(DevelopmentType.VICTORYPOINT)) {
                    victoryPoints += 1;
                }
            }

            player.setVictoryPoints(victoryPoints);

        }

    }

}
