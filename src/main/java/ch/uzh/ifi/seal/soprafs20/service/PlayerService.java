package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.constant.DevelopmentType;
import ch.uzh.ifi.seal.soprafs20.constant.ErrorMsg;
import ch.uzh.ifi.seal.soprafs20.constant.GameConstants;
import ch.uzh.ifi.seal.soprafs20.constant.ResourceType;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.entity.game.Player;
import ch.uzh.ifi.seal.soprafs20.entity.game.ResourceWallet;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.Building;
import ch.uzh.ifi.seal.soprafs20.entity.game.cards.DevelopmentCard;
import ch.uzh.ifi.seal.soprafs20.entity.moves.BuildMove;
import ch.uzh.ifi.seal.soprafs20.entity.moves.CardMove;
import ch.uzh.ifi.seal.soprafs20.entity.moves.PurchaseMove;
import ch.uzh.ifi.seal.soprafs20.entity.moves.TradeMove;
import ch.uzh.ifi.seal.soprafs20.entity.moves.development.MonopolyMove;
import ch.uzh.ifi.seal.soprafs20.entity.moves.development.PlentyMove;
import ch.uzh.ifi.seal.soprafs20.entity.moves.development.StealMove;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
import ch.uzh.ifi.seal.soprafs20.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    public Player payForDevelopmentCard(PurchaseMove move) {

        // find player
        Player player = playerRepository.findByUserId(move.getUserId());

        if (player == null) {
            throw new NullPointerException(ErrorMsg.NO_PLAYER_FOUND_WITH_USER_ID);
        }

        // pay for developmentCard
        ResourceWallet price = new DevelopmentCard().getPrice();
        ResourceWallet funds = player.getWallet();

        for (ResourceType type: price.getAllTypes()) {
            funds.removeResource(type, price.getResourceAmount(type));
        }

        player.setWallet(funds);

        // save player
        return playerRepository.saveAndFlush(player);
    }

    public Player addDevelopmentCard(PurchaseMove move) {

        // find player
        Player player = playerRepository.findByUserId(move.getUserId());

        if (player == null) {
            throw new NullPointerException(ErrorMsg.NO_PLAYER_FOUND_WITH_USER_ID);
        }

        // generate and set random development Card
        DevelopmentCard developmentCard = new DevelopmentCard();

        DevelopmentType devType;
        int randomCard = ThreadLocalRandom.current().nextInt(1, 100 + 1);

        if (randomCard >= 1 && randomCard <= 56) {
            devType = DevelopmentType.KNIGHT;
        }
        else if (randomCard >= 57 && randomCard <= 76) {
            devType = DevelopmentType.VICTORYPOINT;
        }
        else if (randomCard >= 77 && randomCard <= 84) {
            devType = DevelopmentType.ROADPROGRESS;
        }
        else if (randomCard >= 85 && randomCard <= 92) {
            devType = DevelopmentType.PLENTYPROGRESS;
        }
        else {
            devType = DevelopmentType.MONOPOLYPROGRESS;
        }

        developmentCard.setDevelopmentType(devType);

        // add DevelopmentCard
        player.addDevelopmentCard(developmentCard);

        return playerRepository.saveAndFlush(player);
    }

    public Player payForTrade(TradeMove move) {

        //Find Player
        Player player = playerRepository.findByUserId(move.getUserId());

        if (player == null) {
            throw new NullPointerException(ErrorMsg.NO_PLAYER_FOUND_WITH_USER_ID);
        }

        // pay for resource
        ResourceWallet funds = player.getWallet();
        ResourceType type = move.getOfferedType();

        int tradeRatio = GameConstants.TRADE_WITH_BANK_RATIO;

        funds.removeResource(type, tradeRatio);

        return playerRepository.saveAndFlush(player);
    }

    public Player receiveFromTrade(TradeMove move) {

        //Find Player
        Player player = playerRepository.findByUserId(move.getUserId());

        if (player == null) {
            throw new NullPointerException(ErrorMsg.NO_PLAYER_FOUND_WITH_USER_ID);
        }

        // get traded resourceType and funds from player
        ResourceType type = move.getNeededType();
        ResourceWallet funds = player.getWallet();

        // add resource to players' wallet
        funds.addResource(type, 1);

        return playerRepository.saveAndFlush(player);
    }

    public void monopolizeResources(MonopolyMove move, Player player, List<Player> opponents) {

        // get monopolized resource
        ResourceType monopolyType = move.getMonopolyType();

        // get funds of player
        ResourceWallet funds = player.getWallet();

        // get all resources of monopoly type from all opponents and add them to player funds
        for (Player opponent : opponents) {

            ResourceWallet wallet = opponent.getWallet();
            int transfer = wallet.getResourceAmount(monopolyType);

            wallet.removeResource(monopolyType, transfer);
            funds.addResource(monopolyType, transfer);

            save(opponent);
        }

        save(player);
    }

    public void plentyResources(PlentyMove move) {

        // find player
        Player player = playerRepository.findByUserId(move.getUserId());

        if (player == null) {
            throw new NullPointerException(ErrorMsg.NO_PLAYER_FOUND_WITH_USER_ID);
        }

        // get requested resource type(s) and funds from player
        ResourceType type1 = move.getPlentyType1();
        ResourceType type2 = move.getPlentyType2();

        ResourceWallet funds = player.getWallet();

        // add resources to wallet
        funds.addResource(type1, 1);
        funds.addResource(type2, 1);

        // save
        player.setWallet(funds);
        playerRepository.saveAndFlush(player);
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
    }

    public void removeDevelopmentCard(CardMove move) {

        // find player
        Player player = playerRepository.findByUserId(move.getUserId());

        // get owed development cardType
        DevelopmentType type = move.getDevelopmentCard().getDevelopmentType();

        // get currently owned development cards from player
        List<DevelopmentCard> ownedCards = player.getDevelopmentCards();

        // remove development card from player
        for (DevelopmentCard card: ownedCards) {
            if (card.getDevelopmentType() == type) {
                ownedCards.remove(card);
                break;
            }
        }

        // set the new owned cards
        player.setDevelopmentCards(ownedCards);
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

    public Player save(Player player) {
        return playerRepository.saveAndFlush(player);
    }

    public void stealResource(StealMove move) {

        // find player & corresponding funds
        Player player = playerRepository.findByUserId(move.getUserId());
        ResourceWallet fundsPlayer = player.getWallet();

        // find victim & corresponding funds
        Player victim = playerRepository.findByUserId(move.getVictim().getUserId());
        ResourceWallet fundsVictim = victim.getWallet();

        // deduct random card from opponent and add to player
        // TODO: implement fuctionality

        // save players
        player.setWallet(fundsPlayer);
        victim.setWallet(fundsVictim);
        playerRepository.saveAndFlush(player);
        playerRepository.saveAndFlush(victim);
    }

    public void updateResources(ResourceType type, List<Building> buildings, Player player) {

        ResourceWallet funds = player.getWallet();
        for (Building building : buildings) {
            int amount = building.getResourceDistributingAmount();
            funds.addResource(type, amount);
        }
        save(player);
    }
}
