package ch.uzh.ifi.seal.soprafs20.entity.game;

import ch.uzh.ifi.seal.soprafs20.entity.game.cards.DevelopmentCard;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PLAYER")
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private Long userId;

    @OneToOne(cascade = CascadeType.ALL)
    private ResourceWallet wallet;

    @Column(nullable = false)
    private String username;

    @OneToMany(cascade = CascadeType.ALL)
    private List<DevelopmentCard> developmentCards;

    @Column
    private int victoryPoints;


    public Player() {

        //Set up empty arrays
        wallet = new ResourceWallet();
        developmentCards = new ArrayList<>();
    }

    //Getter and setters

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public void setVictoryPoints(int victoryPoints) {
        this.victoryPoints = victoryPoints;
    }

    public List<DevelopmentCard> getDevelopmentCards() {
        return developmentCards;
    }

    public void setDevelopmentCards(List<DevelopmentCard> developmentCards) {
        this.developmentCards = developmentCards;
    }

    public void addDevelopmentCard(DevelopmentCard developmentCard) {
        developmentCards.add(developmentCard);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResourceWallet getWallet() {
        return wallet;
    }

    public void setWallet(ResourceWallet wallet) {
        this.wallet = wallet;
    }
}
