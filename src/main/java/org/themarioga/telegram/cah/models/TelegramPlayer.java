package org.themarioga.telegram.cah.models;

import jakarta.persistence.*;
import org.themarioga.engine.cah.models.game.Player;

import java.io.Serializable;
import java.util.Objects;

/**
 * Mensaje privado en el que un jugador ve su mano de cartas.
 * <p>
 * Efímera como {@link TelegramGame}: se borra al terminar la partida.
 */
@Entity
@Table(name = "telegram_player")
public class TelegramPlayer implements Serializable {

    @Id
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "hand_message_id", nullable = false)
    private Integer handMessageId;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Integer getHandMessageId() {
        return handMessageId;
    }

    public void setHandMessageId(Integer handMessageId) {
        this.handMessageId = handMessageId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TelegramPlayer that = (TelegramPlayer) o;
        return Objects.equals(getPlayer(), that.getPlayer());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPlayer());
    }

    @Override
    public String toString() {
        return "TelegramPlayer{player=" + player + ", handMessageId=" + handMessageId + '}';
    }

}
