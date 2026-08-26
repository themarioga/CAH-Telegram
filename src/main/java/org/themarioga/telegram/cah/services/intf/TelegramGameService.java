package org.themarioga.telegram.cah.services.intf;

import org.themarioga.engine.cah.models.game.Game;
import org.themarioga.engine.cah.models.game.Player;
import org.themarioga.engine.commons.models.Room;
import org.themarioga.engine.commons.models.User;
import org.themarioga.telegram.cah.models.TelegramGame;
import org.themarioga.telegram.cah.models.TelegramPlayer;

import java.util.List;

/**
 * Guarda la correspondencia entre lo que el motor entiende (partidas y jugadores) y lo que Telegram
 * necesita para pintarlo (identificadores de mensaje y de chat).
 * <p>
 * Aquí no hay reglas de juego: eso vive en {@code CAHService}.
 */
public interface TelegramGameService {

    TelegramGame create(Game game, int firstMessageId, int creatorMessageId);

    TelegramGame getByGame(Game game);

    TelegramGame getByCreator(User creator);

    List<TelegramGame> getAll();

    void setCurrentRoundMessageId(TelegramGame telegramGame, int messageId);

    /**
     * Id del chat de Telegram en el que se juega una partida. Es el camino que permite escribir al
     * grupo cuando la acción llega por el chat privado de un jugador.
     */
    Long getChatId(Room room);

    TelegramPlayer createPlayer(Player player, int handMessageId);

    TelegramPlayer getByPlayer(Player player);

    List<TelegramPlayer> getPlayers(Game game);

    /**
     * Borra las filas de Telegram de una partida terminada. No toca la partida en el motor.
     */
    void deleteGameData(Game game);

}
