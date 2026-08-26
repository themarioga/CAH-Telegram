package org.themarioga.telegram.cah.dao.intf;

import org.themarioga.engine.cah.models.game.Game;
import org.themarioga.engine.cah.models.game.Player;
import org.themarioga.commons.engine.dao.InterfaceHibernateDao;
import org.themarioga.telegram.cah.models.TelegramPlayer;

import java.util.List;

public interface TelegramPlayerDao extends InterfaceHibernateDao<TelegramPlayer> {

    TelegramPlayer getByPlayer(Player player);

    /**
     * Jugadores de una partida, para repartir las manos y para limpiar al terminar.
     */
    List<TelegramPlayer> getByGame(Game game);

}
