package org.themarioga.telegram.cah.dictionaries.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.themarioga.telegram.cah.dictionaries.service.intf.DictionariesTelegramService;
import org.themarioga.commons.telegram.models.CallbackQueryHandler;
import org.themarioga.commons.telegram.models.CommandHandler;
import org.themarioga.commons.telegram.services.intf.ApplicationService;
import org.themarioga.commons.telegram.services.intf.BotMessageService;
import org.themarioga.commons.engine.services.intf.I18NService;
import org.themarioga.commons.telegram.util.BotMessageUtils;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

@Service("dictionariesBotApplicationService")
@ConditionalOnProperty(prefix = "dictionaries.bot", name = "enabled", havingValue = "true")
public class DictionariesApplicationServiceImpl implements ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(DictionariesApplicationServiceImpl.class);

    private final BotMessageService botMessageService;
    private final DictionariesTelegramService dictionariesTelegramService;
    private final I18NService i18NService;

    @Autowired
    public DictionariesApplicationServiceImpl(@Qualifier("dictionariesBotMessageService") BotMessageService botMessageService, DictionariesTelegramService dictionariesTelegramService, I18NService i18NService) {
        this.dictionariesTelegramService = dictionariesTelegramService;
        this.i18NService = i18NService;
        this.botMessageService = botMessageService;
    }

    @Override
    public Map<String, CommandHandler> getBotCommands() {
        Map<String, CommandHandler> commands = new HashMap<>();

        commands.put("/start", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /start enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.registerUser(message.getFrom());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/lang", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /lang enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.changeUserLanguageMessage();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/menu", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /menu enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.mainMenu();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/create", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /create enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.createDictionary(message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/rename_select", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /rename_select enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.selectDictionaryToRename(message.getMessageId(), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/rename", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /rename enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.renameDictionary(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/change_lang_select", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /change_lang_select enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.selectDictionaryToChangeLang(message.getMessageId(), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/change_lang", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /change_lang enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.changeDictionaryLang(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/delete_select", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /delete_select enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.selectDictionaryToDelete(message.getMessageId(), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/delete", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /delete enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.deleteDictionary(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/toggle_select", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /delete_select enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.toggleDictionary(message.getMessageId(), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/share_select", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /delete_select enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.requestShareDictionary(message.getMessageId(), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/manage_cards_select", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /manage_cards_select enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.selectDictionaryToManageCards(null, message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/add_white_card", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /add_white_card enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.addWhiteCard(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/edit_white_card_sel", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /edit_white_card_sel enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.editWhiteCardSelect(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/edit_white_card", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /edit_white_card enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.editWhiteCard(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/delete_white_card", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /delete_white_card enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.deleteWhiteCard(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/add_black_card", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /add_black_card enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.addBlackCard(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/edit_black_card_sel", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /edit_black_card_sel enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.editBlackCardSelect(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/edit_black_card", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /edit_black_card enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.editBlackCard(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/delete_black_card", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /delete_black_card enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.deleteBlackCard(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/manage_collabs_select", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /manage_collabs_select enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.selectDictionaryToManageCollaborators(null, message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/add_collab", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /add_collab enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.addCollaborator(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/delete_collab", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /delete_collab enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.deleteCollaborator(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/toggle_collab", (message, data) -> {
            if (!BotMessageUtils.isMessagePrivate(message)) {
                logger.error("Comando /toggle_collab enviado en lugar incorrecto por {}", BotMessageUtils.getUserInfo(message.getFrom()));

                botMessageService.sendMessage(message.getChat().getId(), i18NService.get("ERROR_COMMAND_SHOULD_BE_ON_PRIVATE", message.getFrom().getLanguageCode()));

                return;
            }

            try {
                dictionariesTelegramService.loginUser(message.getFrom().getId());

                dictionariesTelegramService.toggleCollaborator(UUID.fromString(data), message.getText().trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });

        commands.put("/getmyid", (message, data) -> botMessageService.sendMessage(message.getFrom().getId(), "ID: " + message.getFrom().getId()));

        commands.put("/help", (message, data) -> dictionariesTelegramService.sendHelpMessage(message.getChatId()));

        return commands;
    }

    @Override
    public Map<String, CallbackQueryHandler> getCallbackQueries() {
        Map<String, CallbackQueryHandler> callbackQueryHandlerMap = new HashMap<>();

        callbackQueryHandlerMap.put("change_user_lang", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.changeUserLanguage(callbackQuery.getMessage().getMessageId(), data != null && !data.isBlank() ? data : callbackQuery.getFrom().getLanguageCode());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("menu", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.mainMenu(callbackQuery.getMessage().getMessageId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("dictionary_list", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.listDictionaries(callbackQuery.getMessage().getMessageId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("dictionary_create", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.createDictionaryMessage(callbackQuery.getMessage().getMessageId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("dictionary_rename", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.renameDictionaryMessage(callbackQuery.getMessage().getMessageId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("dictionary_lang", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.changeDictionaryLangMessage(callbackQuery.getMessage().getMessageId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("dictionary_delete", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.deleteDictionaryMessage(callbackQuery.getMessage().getMessageId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("dictionary_toggle", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.toggleDictionaryMessage(callbackQuery.getMessage().getMessageId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("dictionary_share", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.shareDictionaryMessage(callbackQuery.getMessage().getMessageId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("share_accept", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.acceptShareDictionary(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("share_decline", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.rejectShareDictionary(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("dictionary_manage_cards", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.manageCardsMessage(callbackQuery.getMessage().getMessageId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("manage_cards_select", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.selectDictionaryToManageCards(callbackQuery.getMessage().getMessageId(), data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("list_white_cards", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.listWhiteCardsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("add_white_cards", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.addWhiteCardsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("edit_white_cards", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.editWhiteCardsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("delete_white_cards", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.deleteWhiteCardsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("list_black_cards", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.listBlackCardsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("add_black_cards", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.addBlackCardsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("edit_black_cards", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.editBlackCardsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("delete_black_cards", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.deleteBlackCardsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("dictionary_manage_collabs", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.manageCollaboratorsMessage(callbackQuery.getMessage().getMessageId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("manage_collabs_select", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.selectDictionaryToManageCollaborators(callbackQuery.getMessage().getMessageId(), data);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("list_collabs", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.listCollaboratorsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("add_collabs", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.addCollaboratorsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("delete_collabs", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.removeCollaboratorsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("toggle_collabs", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.toggleCollaboratorsMessage(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("collaborator_accept", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.acceptCollaborator(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        callbackQueryHandlerMap.put("collaborator_decline", (callbackQuery, data) -> {
            try {
                dictionariesTelegramService.loginUser(callbackQuery.getFrom().getId());

                dictionariesTelegramService.rejectCollaborator(callbackQuery.getMessage().getMessageId(), UUID.fromString(data));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            botMessageService.answerCallbackQuery(callbackQuery.getId());
        });

        return callbackQueryHandlerMap;
    }

}
