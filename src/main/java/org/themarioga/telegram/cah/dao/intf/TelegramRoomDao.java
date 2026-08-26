package org.themarioga.telegram.cah.dao.intf;

import org.themarioga.commons.engine.dao.InterfaceHibernateDao;
import org.themarioga.commons.engine.models.Room;
import org.themarioga.telegram.cah.models.TelegramRoom;

public interface TelegramRoomDao extends InterfaceHibernateDao<TelegramRoom> {

    TelegramRoom getByChatId(Long chatId);

    /**
     * Camino inverso, necesario para escribir al grupo desde un evento que llega por privado
     * (jugar una carta, votar).
     */
    TelegramRoom getByRoom(Room room);

}
