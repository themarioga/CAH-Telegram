package org.themarioga.telegram.cah.dictionaries.service.intf;

import java.util.UUID;

/**
 * Comportamiento del bot de diccionarios: orquesta el motor y compone lo que ve el usuario.
 * <p>
 * Sustituye a {@code DictionariesBotService}. Los identificadores son {@link UUID} porque es lo que
 * usa el motor desde el refactor; antes eran {@code long}.
 * <p>
 * Los parámetros llamados {@code selection} son lo que ha tecleado el usuario: el número corto que
 * el bot le enseñó en la lista, o un identificador completo (que es lo que llevan los botones).
 */
public interface DictionariesTelegramService {

    // ///////////// Usuario //////////////////

    void registerUser(org.telegram.telegrambots.meta.api.objects.User from);

    /**
     * Comprueba que la petición trae sesión. Ya no la monta: de eso se encarga el interceptor de
     * updates en cada petición. Se conserva para que la tabla de comandos siga igual.
     *
     * @param telegramId ignorado; la sesión sale del contexto de seguridad
     */
    void loginUser(long telegramId);

    void changeUserLanguageMessage();

    void changeUserLanguage(int messageId, String lang);

    // ///////////// Menú y diccionarios //////////////////

    void mainMenu();

    void mainMenu(int messageId);

    void listDictionaries(int messageId);

    void createDictionaryMessage(int messageId);

    void createDictionary(String name);

    void renameDictionaryMessage(int messageId);

    void selectDictionaryToRename(int messageId, String selection);

    void renameDictionary(UUID dictionaryId, String newName);

    void changeDictionaryLangMessage(int messageId);

    void selectDictionaryToChangeLang(int messageId, String selection);

    void changeDictionaryLang(UUID dictionaryId, String language);

    void deleteDictionaryMessage(int messageId);

    void selectDictionaryToDelete(int messageId, String selection);

    void deleteDictionary(UUID dictionaryId, String text);

    void toggleDictionaryMessage(int messageId);

    void toggleDictionary(int messageId, String selection);

    void shareDictionaryMessage(int messageId);

    void requestShareDictionary(int messageId, String selection);

    void acceptShareDictionary(int messageId, UUID dictionaryId);

    void rejectShareDictionary(int messageId, UUID dictionaryId);

    // ///////////// Cartas //////////////////

    void manageCardsMessage(int messageId);

    void selectDictionaryToManageCards(Integer messageId, String selection);

    void listWhiteCardsMessage(int messageId, UUID dictionaryId);

    void addWhiteCardsMessage(int messageId, UUID dictionaryId);

    void addWhiteCard(UUID dictionaryId, String text);

    void editWhiteCardsMessage(int messageId, UUID dictionaryId);

    void editWhiteCardSelect(UUID dictionaryId, String cardSelection);

    void editWhiteCard(UUID cardId, String newText);

    void deleteWhiteCardsMessage(int messageId, UUID dictionaryId);

    void deleteWhiteCard(UUID dictionaryId, String cardSelection);

    void listBlackCardsMessage(int messageId, UUID dictionaryId);

    void addBlackCardsMessage(int messageId, UUID dictionaryId);

    void addBlackCard(UUID dictionaryId, String text);

    void editBlackCardsMessage(int messageId, UUID dictionaryId);

    void editBlackCardSelect(UUID dictionaryId, String cardSelection);

    void editBlackCard(UUID cardId, String newText);

    void deleteBlackCardsMessage(int messageId, UUID dictionaryId);

    void deleteBlackCard(UUID dictionaryId, String cardSelection);

    // ///////////// Colaboradores //////////////////

    void manageCollaboratorsMessage(int messageId);

    void selectDictionaryToManageCollaborators(Integer messageId, String selection);

    void listCollaboratorsMessage(int messageId, UUID dictionaryId);

    void addCollaboratorsMessage(int messageId, UUID dictionaryId);

    void addCollaborator(UUID dictionaryId, String nameOrId);

    void acceptCollaborator(int messageId, UUID dictionaryId);

    void rejectCollaborator(int messageId, UUID dictionaryId);

    void removeCollaboratorsMessage(int messageId, UUID dictionaryId);

    void deleteCollaborator(UUID dictionaryId, String nameOrId);

    void toggleCollaboratorsMessage(int messageId, UUID dictionaryId);

    void toggleCollaborator(UUID dictionaryId, String nameOrId);

    // ///////////// Ayuda //////////////////

    void sendHelpMessage(long chatId);

}
