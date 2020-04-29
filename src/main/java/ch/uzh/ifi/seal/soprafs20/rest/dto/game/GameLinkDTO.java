package ch.uzh.ifi.seal.soprafs20.rest.dto.game;

import ch.uzh.ifi.seal.soprafs20.entity.game.Player;

import java.util.List;

/**
 * The GameLinkDTO represents an element in the array when a GET request is sent to /games
 * <p>
 */
public class GameLinkDTO {

    private Long gameId;

    private String url;

    private String name;

    private int minPlayers;

    private int joinedPlayers;

    private boolean withBots;

    private boolean started;

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(Long gameId) {
        this.url = String.format("/games/%d", gameId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getJoinedPlayers() {
        return joinedPlayers;
    }

    public boolean isWithBots() {
        return withBots;
    }

    public void setWithBots(boolean withBots) {
        this.withBots = withBots;
    }

    public void setJoinedPlayers(List<Player> players) {
        joinedPlayers = players.size();
    }
}
