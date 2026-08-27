package org.themarioga.telegram.cah.game.service.intf;

/**
 * Comportamiento del bot de juego.
 * <p>
 * Sustituye a {@code CCLHBotService}. Los {@code chatId} son identificadores de chat de Telegram,
 * no del motor: la sala se resuelve por la tabla de equivalencias.
 */
public interface CCLHTelegramService {

    // ///////////// Usuario //////////////////

    void registerUser(org.telegram.telegrambots.meta.api.objects.User from);

    /**
     * Comprueba que la petición trae sesión. Ya no la monta: de eso se encarga el interceptor de
     * updates. Se conserva para que la tabla de comandos siga igual.
     *
     * @param telegramId ignorado; la sesión sale del contexto de seguridad
     */
    void loginUser(long telegramId);

    void changeUserLanguageMessage();

    void changeUserLanguage(int messageId, String lang);

    // ///////////// Creación y configuración //////////////////

    void startCreatingGame(long chatId, String chatTitle);

    void gameMenuQuery(long chatId, String callbackQueryId);

    void gameConfigureQuery(long chatId, String callbackQueryId);

    void gameSelectModeQuery(long chatId, String callbackQueryId);

    void gameSelectPunctuationModeQuery(long chatId, String callbackQueryId);

    void gameSelectNRoundsToEndQuery(long chatId, String callbackQueryId);

    void gameSelectNPointsToWinQuery(long chatId, String callbackQueryId);

    void gameSelectDictionaryQuery(long chatId, String callbackQueryId, String page);

    void gameSelectMaxPlayersQuery(long chatId, String callbackQueryId);

    void gameChangeMode(long chatId, String callbackQueryId, String data);

    void gameChangeDictionary(long chatId, String callbackQueryId, String data);

    void gameChangeMaxPlayers(long chatId, String callbackQueryId, String data);

    void gameChangeNRoundsToEnd(long chatId, String callbackQueryId, String data);

    void gameChangeNCardsToWin(long chatId, String callbackQueryId, String data);

    // ///////////// Ayuda //////////////////

    void sendHelpMessage(long chatId);

}
