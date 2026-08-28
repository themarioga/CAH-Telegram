package org.themarioga.telegram.cah;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.themarioga.commons.telegram.services.intf.ApplicationService;

import java.util.List;
import java.util.Set;

/**
 * La interfaz de comandos del bot es contrato con quien ya lo usa: los nombres de comando y las
 * claves de callback tienen que seguir siendo exactamente los mismos que antes del refactor, o los
 * botones de los mensajes que ya están enviados dejan de funcionar.
 */
@SpringBootTest(properties = {"dictionaries.bot.enabled=true", "dictionaries.bot.token=222:fake-token-de-pruebas", "dictionaries.bot.name=dictionariestestbot"})
class DictionariesApplicationServiceTest {

    @Autowired
    @Qualifier("dictionariesBotApplicationService")
    private ApplicationService applicationService;

    private static final Set<String> COMMANDS = Set.of(
            "/start", "/lang", "/menu", "/create", "/rename_select", "/rename", "/change_lang_select", "/change_lang", "/delete_select", "/delete", "/toggle_select", "/share_select", "/manage_cards_select", "/add_white_card", "/edit_white_card_sel", "/edit_white_card", "/delete_white_card", "/add_black_card", "/edit_black_card_sel", "/edit_black_card", "/delete_black_card", "/manage_collabs_select", "/add_collab", "/delete_collab", "/toggle_collab", "/getmyid", "/help");

    private static final Set<String> CALLBACKS = Set.of(
            "change_user_lang", "menu", "dictionary_list", "dictionary_create", "dictionary_rename", "dictionary_lang", "dictionary_delete", "dictionary_toggle", "dictionary_share", "share_accept", "share_decline", "dictionary_manage_cards", "manage_cards_select", "list_white_cards", "add_white_cards", "edit_white_cards", "delete_white_cards", "list_black_cards", "add_black_cards", "edit_black_cards", "delete_black_cards", "dictionary_manage_collabs", "manage_collabs_select", "list_collabs", "add_collabs", "delete_collabs", "toggle_collabs", "collaborator_accept", "collaborator_decline");

    @Test
    void theCommandInterfaceIsUnchanged() {
        Assertions.assertEquals(COMMANDS, applicationService.getBotCommands().keySet());
    }

    @Test
    void theCallbackInterfaceIsUnchanged() {
        Assertions.assertEquals(CALLBACKS, applicationService.getCallbackQueries().keySet());
    }

    /**
     * Ningún handler puede quedarse sin registrar por un despiste al portar.
     */
    @Test
    void everyHandlerIsWired() {
        for (String command : COMMANDS) {
            Assertions.assertNotNull(applicationService.getBotCommands().get(command), () -> "falta " + command);
        }
        for (String callback : CALLBACKS) {
            Assertions.assertNotNull(applicationService.getCallbackQueries().get(callback), () -> "falta " + callback);
        }
    }

    @Test
    void theBotStartsWithItsRealApplicationService() {
        Assertions.assertFalse(applicationService.getBotCommands().isEmpty());
        Assertions.assertFalse(applicationService.getCallbackQueries().isEmpty());
        Assertions.assertEquals(List.of(), List.of());
    }

}
