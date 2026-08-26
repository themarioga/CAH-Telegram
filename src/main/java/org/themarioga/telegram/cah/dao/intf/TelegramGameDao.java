package org.themarioga.telegram.cah.dao.intf;

import org.themarioga.engine.cah.models.game.Game;
import org.themarioga.commons.engine.dao.InterfaceHibernateDao;
import org.themarioga.commons.engine.models.User;
import org.themarioga.telegram.cah.models.TelegramGame;

import java.util.List;

public interface TelegramGameDao extends InterfaceHibernateDao<TelegramGame> {

    TelegramGame getByGame(Game game);

    /**
     * Partida creada por un usuario. Sirve a /deletemygames y a /deletegamebyusername.
     */
    TelegramGame getByCreator(User creator);

    /**
     * Todas las partidas vivas. Esta tabla hace de índice de partidas para /deleteallgames: el
     * motor no expone un "listar todas".
     */
    List<TelegramGame> getAll();

}
