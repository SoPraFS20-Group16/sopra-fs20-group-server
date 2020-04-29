package ch.uzh.ifi.seal.soprafs20.entity;

import ch.uzh.ifi.seal.soprafs20.constant.GameConstants;
import ch.uzh.ifi.seal.soprafs20.entity.game.Board;
import ch.uzh.ifi.seal.soprafs20.entity.game.Player;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "GAME")
public class Game implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private int playerMinimum = GameConstants.DEFAULT_PLAYER_MINIMUM;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Player> players;

    @ManyToOne(cascade = CascadeType.ALL)
    private Player currentPlayer;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Board board;

    @Column
    private boolean withBots;

    @Column(nullable = false, updatable = false)
    private Long creatorId;     //Id of the user that created the game

<<<<<<< Updated upstream
=======
    @Column
    private boolean started;


>>>>>>> Stashed changes
    public Game() {
        players = new ArrayList<>();
    }

    public boolean getStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWithBots() {
        return withBots;
    }

    public void setWithBots(boolean withBots) {
        this.withBots = withBots;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long userId) {
        this.creatorId = userId;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public int getPlayerMinimum() {
        return playerMinimum;
    }

    public void setPlayerMinimum(int playerMinimum) {
        this.playerMinimum = playerMinimum;
    }

    /**
     * Adds a player to the game.
     * Players must already be saved and not null
     *
     * @param player the player
     */
    public void addPlayer(Player player) {

        if (player == null) {
            throw new NullPointerException("Player to be added should not be null!");
        }

        players.add(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    /**
     * Returns true if the passed user is a player of the game, else returns false
     *
     * @param player the player
     * @return the boolean
     */
    public boolean isPlayer(Player player) {
        if (players.isEmpty()) {
            return false;
        }
        for (Player candidatePlayer : this.players) {
            if (player.getId().equals(candidatePlayer.getId())) {
                return true;
            }
        }
        return false;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
