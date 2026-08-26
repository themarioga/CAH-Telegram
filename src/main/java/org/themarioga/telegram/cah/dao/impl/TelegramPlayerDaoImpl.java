package org.themarioga.telegram.cah.dao.impl;

import org.springframework.stereotype.Repository;
import org.themarioga.engine.cah.models.game.Game;
import org.themarioga.engine.cah.models.game.Player;
import org.themarioga.engine.commons.dao.AbstractHibernateDao;
import org.themarioga.telegram.cah.dao.intf.TelegramPlayerDao;
import org.themarioga.telegram.cah.models.TelegramPlayer;

import java.util.List;

@Repository
public class TelegramPlayerDaoImpl extends AbstractHibernateDao<TelegramPlayer> implements TelegramPlayerDao {

    public TelegramPlayerDaoImpl() {
        setClazz(TelegramPlayer.class);
    }

    @Override
    public TelegramPlayer getByPlayer(Player player) {
        return getCurrentSession()
                .createQuery("SELECT tp FROM TelegramPlayer tp WHERE tp.player = :player", TelegramPlayer.class)
                .setParameter("player", player)
                .getSingleResultOrNull();
    }

    @Override
    public List<TelegramPlayer> getByGame(Game game) {
        return getCurrentSession()
                .createQuery("SELECT tp FROM TelegramPlayer tp JOIN FETCH tp.player p WHERE p.game = :game", TelegramPlayer.class)
                .setParameter("game", game)
                .getResultList();
    }

}
