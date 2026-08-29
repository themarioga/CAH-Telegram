package org.themarioga.telegram.cah;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.themarioga.commons.engine.models.Room;
import org.themarioga.commons.engine.models.User;
import org.themarioga.commons.engine.services.intf.UserService;
import org.themarioga.engine.cah.config.GameConfig;
import org.themarioga.engine.cah.enums.CardTypeEnum;
import org.themarioga.engine.cah.models.dictionaries.Dictionary;
import org.themarioga.engine.cah.services.intf.dictionaries.CardService;
import org.themarioga.engine.cah.services.intf.dictionaries.DictionaryService;
import org.themarioga.engine.cah.services.intf.game.GameService;
import org.themarioga.telegram.cah.game.service.intf.CCLHTelegramService;
import org.themarioga.telegram.cah.services.intf.TelegramGameService;
import org.themarioga.telegram.cah.support.BotFlowTest;
import org.themarioga.telegram.cah.support.RecordingBotMessageService;

/**
 * Los comandos que borran partidas ajenas.
 * <p>
 * No los cubría nadie, y no funcionaban: la comprobación del creador del motor no dejaba pasar a
 * quien administra, así que {@code /deletegamebyusername} no borraba nada y {@code /deleteallgames}
 * fallaba en silencio, porque su bucle registra el error de cada partida y sigue con la siguiente.
 */
class AdminFlowTest extends BotFlowTest {

    private static final long GROUP_CHAT = -100700L;
    private static final long CREATOR = 700L;

    private static final long OTHER_GROUP_CHAT = -100710L;
    private static final long OTHER_CREATOR = 710L;

    @Autowired
    private CCLHTelegramService game;
    @Autowired
    private GameService gameService;
    @Autowired
    private TelegramGameService telegramGameService;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private CardService cardService;
    @Autowired
    private GameConfig gameConfig;
    @Autowired
    private UserService userService;

    private final RecordingBotMessageService messages = CCLH_MESSAGES;

    private User creator;

    @BeforeEach
    void setUp() {
        givenRegisteredUser(ADMIN_TELEGRAM_ID, "admin");
        creator = givenRegisteredUser(CREATOR, "creador");
        givenRegisteredUser(OTHER_CREATOR, "otrocreador");

        givenAPlayableDictionary();

        logInAs(CREATOR, GROUP_CHAT, "group");
        game.startCreatingGame(GROUP_CHAT, "Grupo de pruebas");

        messages.clear();
    }

    /** El alias llega tecleado por una persona: puede venir con arroba y en mayúsculas. */
    @Test
    void anAdminCanDeleteSomeoneElsesGameByUsername() {
        logInAs(ADMIN_TELEGRAM_ID, null, "private");

        game.deleteGameByCreatorUsername("@Creador");

        Assertions.assertNull(gameService.getByRoom(room()), "la partida del otro se borra");
        Assertions.assertTrue(messages.sentTo(ADMIN_TELEGRAM_ID).stream().anyMatch(sent -> sent.text().contains("@Creador")), "y se avisa a quien administra");
    }

    /**
     * Quien no administra tiene que enterarse de que no puede. Antes la excepción se escapaba a la
     * capa de arriba, que solo la apunta en el log, y el usuario no veía nada.
     */
    @Test
    void aPlainUserCannotDeleteSomeoneElsesGameAndIsToldSo() {
        logInAs(OTHER_CREATOR, null, "private");

        game.deleteGameByCreatorUsername("creador");

        Assertions.assertNotNull(gameService.getByRoom(room()), "la partida sigue ahí");
        Assertions.assertFalse(messages.sentTo(OTHER_CREATOR).isEmpty(), "hay que contestarle algo");
    }

    @Test
    void anAdminCanDeleteEveryGameAtOnce() {
        givenSecondGame();

        Assertions.assertEquals(2, telegramGameService.getAll().size());

        logInAs(ADMIN_TELEGRAM_ID, null, "private");

        game.deleteAllGames();

        Assertions.assertTrue(telegramGameService.getAll().isEmpty(), "no queda ninguna");
        Assertions.assertTrue(messages.sentTo(CREATOR).stream().anyMatch(sent -> sent.text().contains("administración")), "hay que avisar a cada creador");
        Assertions.assertTrue(messages.sentTo(OTHER_CREATOR).stream().anyMatch(sent -> sent.text().contains("administración")), "a los dos");
    }

    @Test
    void aPlainUserCannotDeleteEveryGameAndIsToldSo() {
        logInAs(OTHER_CREATOR, null, "private");

        game.deleteAllGames();

        Assertions.assertEquals(1, telegramGameService.getAll().size(), "la partida sigue ahí");
        Assertions.assertFalse(messages.sentTo(OTHER_CREATOR).isEmpty(), "hay que contestarle algo");
    }

    // ///////////// Difusión //////////////////

    @Test
    void anAdminReachesEveryUserAndEveryRoom() {
        logInAs(ADMIN_TELEGRAM_ID, null, "private");

        game.sendMessageToEveryone("Mañana hay mantenimiento");

        Assertions.assertTrue(messages.sentTo(CREATOR).stream().anyMatch(sent -> sent.text().equals("Mañana hay mantenimiento")), "le tiene que llegar a cada usuario");
        Assertions.assertTrue(messages.sentTo(GROUP_CHAT).stream().anyMatch(sent -> sent.text().equals("Mañana hay mantenimiento")), "y a cada sala");
        Assertions.assertTrue(messages.sentTo(ADMIN_TELEGRAM_ID).stream().anyMatch(sent -> sent.text().contains("Se han enviado todos los mensajes")), "y hay que confirmarlo");
    }

    /**
     * El borde caro: un mensaje que Telegram rechazaría se para antes de salir, porque cada rechazo
     * marca ese chat como inactivo y la difusión los recorre todos. Un {@code /sendmessagetoeveryone}
     * sin texto daba de baja a toda la base de datos.
     */
    @Test
    void anEmptyOrOverlongBroadcastIsNotSentToAnyone() {
        logInAs(ADMIN_TELEGRAM_ID, null, "private");

        game.sendMessageToEveryone("   ");
        game.sendMessageToEveryone("x".repeat(4097));

        Assertions.assertTrue(messages.sentTo(CREATOR).isEmpty(), "no le tiene que llegar nada a nadie");
        Assertions.assertTrue(messages.sentTo(GROUP_CHAT).isEmpty(), "ni a las salas");
        Assertions.assertEquals(2, messages.sentTo(ADMIN_TELEGRAM_ID).size(), "pero a quien administra se le dice las dos veces");

        for (User user : userService.getAllUsers()) {
            Assertions.assertTrue(user.getActive(), () -> "nadie puede quedarse desactivado: " + user.getUsername());
        }
    }

    @Test
    void aPlainUserCannotBroadcastAndIsToldSo() {
        logInAs(OTHER_CREATOR, null, "private");

        game.sendMessageToEveryone("hola a todos");

        Assertions.assertTrue(messages.sent().stream().noneMatch(sent -> sent.text().equals("hola a todos")), "no sale de aquí");
        Assertions.assertFalse(messages.sentTo(OTHER_CREATOR).isEmpty(), "hay que contestarle algo");
    }

    // ///////////// Apoyo //////////////////

    private Room room() {
        return roomResolver.resolveRoom(GROUP_CHAT, "Grupo de pruebas");
    }

    private void givenSecondGame() {
        logInAs(OTHER_CREATOR, OTHER_GROUP_CHAT, "group");
        game.startCreatingGame(OTHER_GROUP_CHAT, "Otro grupo");

        messages.clear();
    }

    private void givenAPlayableDictionary() {
        Dictionary dictionary = dictionaryService.create("Diccionario de pruebas", creator);

        for (int i = 0; i < 60; i++) {
            cardService.create(dictionary, CardTypeEnum.WHITE, "Carta blanca " + i);
        }
        for (int i = 0; i < 15; i++) {
            cardService.create(dictionary, CardTypeEnum.BLACK, "Carta negra " + i);
        }

        dictionaryService.togglePublished(dictionary);

        // Es con el que arrancan las partidas que no eligen otro
        gameConfig.setDefaultDictionaryId(dictionary.getId());
    }

}
