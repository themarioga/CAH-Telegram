package org.themarioga.telegram.cah.dao.impl;

import org.springframework.stereotype.Repository;
import org.themarioga.engine.cah.models.game.Game;
import org.themarioga.commons.engine.dao.AbstractHibernateDao;
import org.themarioga.commons.engine.models.User;
import org.themarioga.telegram.cah.dao.intf.TelegramGameDao;
import org.themarioga.telegram.cah.models.TelegramGame;

import java.util.List;

@Repository
public class TelegramGameDaoImpl extends AbstractHibernateDao<TelegramGame> implements TelegramGameDao {

    public TelegramGameDaoImpl() {
        setClazz(TelegramGame.class);
    }

    @Override
    public TelegramGame getByGame(Game game) {
        return getCurrentSession().createQuery("SELECT tg FROM TelegramGame tg WHERE tg.game = :game", TelegramGame.class).setParameter("game", game).getSingleResultOrNull();
    }

    @Override
    public TelegramGame getByCreator(User creator) {
        return getCurrentSession().createQuery("SELECT tg FROM TelegramGame tg JOIN FETCH tg.game g WHERE g.creator = :creator", TelegramGame.class).setParameter("creator", creator).getSingleResultOrNull();
    }

    @Override
    public List<TelegramGame> getAll() {
        return getCurrentSession().createQuery("SELECT tg FROM TelegramGame tg JOIN FETCH tg.game", TelegramGame.class).getResultList();
    }

}
