package ch.uzh.ifi.seal.soprafs20.rest.dto.game;

public class GamePostDTO {

    private String name;
    private boolean withBots;
    private  boolean started;

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
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
}
