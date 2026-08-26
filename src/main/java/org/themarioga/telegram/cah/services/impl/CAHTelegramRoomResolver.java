package org.themarioga.telegram.cah.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.themarioga.engine.commons.exceptions.ApplicationException;
import org.themarioga.engine.commons.models.Room;
import org.themarioga.engine.commons.services.intf.RoomService;
import org.themarioga.telegram.cah.dao.intf.TelegramRoomDao;
import org.themarioga.telegram.cah.models.TelegramRoom;
import org.themarioga.telegram.commons.services.intf.TelegramRoomResolver;

/**
 * Traduce un chat de grupo de Telegram a una sala del motor.
 * <p>
 * La sala se identifica por "tg:&lt;chatId&gt;" y no por el título del grupo: dos grupos distintos
 * pueden llamarse igual, y con el título como identidad acabarían compartiendo sala y, por tanto,
 * partida.
 */
@Service
public class CAHTelegramRoomResolver implements TelegramRoomResolver {

    private static final Logger logger = LoggerFactory.getLogger(CAHTelegramRoomResolver.class);

    private final TelegramRoomDao telegramRoomDao;
    private final RoomService roomService;

    @Autowired
    public CAHTelegramRoomResolver(TelegramRoomDao telegramRoomDao, RoomService roomService) {
        this.telegramRoomDao = telegramRoomDao;
        this.roomService = roomService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = ApplicationException.class)
    public Room resolveRoom(long chatId, String title) {
        TelegramRoom telegramRoom = telegramRoomDao.getByChatId(chatId);

        if (telegramRoom == null) {
            logger.debug("Primera partida en el chat {}: creando sala", chatId);

            Room room = roomService.createOrReactivate(roomnameOf(chatId), title != null ? title : roomnameOf(chatId));

            telegramRoom = new TelegramRoom();
            telegramRoom.setId(chatId);
            telegramRoom.setRoom(room);

            return telegramRoomDao.createOrUpdate(telegramRoom).getRoom();
        }

        Room room = telegramRoom.getRoom();
        if (title != null && !title.equals(room.getName())) {
            logger.debug("El grupo {} se ha renombrado a {}", chatId, title);

            roomService.rename(room, title);
        }

        return room;
    }

    public static String roomnameOf(long chatId) {
        return "tg:" + chatId;
    }

}
