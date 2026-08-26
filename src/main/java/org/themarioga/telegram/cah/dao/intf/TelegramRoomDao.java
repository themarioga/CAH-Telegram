package org.themarioga.telegram.cah.dao.intf;

import org.themarioga.engine.commons.dao.InterfaceHibernateDao;
import org.themarioga.engine.commons.models.Room;
import org.themarioga.telegram.cah.models.TelegramRoom;

public interface TelegramRoomDao extends InterfaceHibernateDao<TelegramRoom> {

    TelegramRoom getByChatId(Long chatId);

    /**
     * Camino inverso, necesario para escribir al grupo desde un evento que llega por privado
     * (jugar una carta, votar).
     */
    TelegramRoom getByRoom(Room room);

}
