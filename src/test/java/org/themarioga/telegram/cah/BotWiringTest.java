package org.themarioga.telegram.cah;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.themarioga.commons.telegram.models.CallbackQueryHandler;
import org.themarioga.commons.telegram.models.CommandHandler;
import org.themarioga.commons.telegram.services.impl.AuthUpdateInterceptor;
import org.themarioga.commons.telegram.services.intf.ApplicationService;

import java.util.Map;

/**
 * Comprueba que los dos bots se cablean y arrancan de verdad.
 * <p>
 * Es el test que cubre los dos problemas que impedían arrancar la aplicación:
 * <ul>
 *   <li>los starters de long-polling y webhook, que registraban los dos un bean
 *       {@code telegramBotsApplication} y se pisaban;</li>
 *   <li>el ciclo de dependencias entre {@code ApplicationService} y {@code BotService}.</li>
 * </ul>
 * Aquí no se prueban comandos: los {@code ApplicationService} reales llegan en F5 y F6, así que se
 * sustituyen por uno vacío. Lo que se prueba es el arranque.
 */
@SpringBootTest(properties = {
        "telegram.bots.type=longpolling",
        "cclh.bot.enabled=true",
        "cclh.bot.token=111:fake-token-de-pruebas",
        "cclh.bot.name=cclhtestbot",
        "dictionaries.bot.enabled=true",
        "dictionaries.bot.token=222:fake-token-de-pruebas",
        "dictionaries.bot.name=dictionariestestbot"})
class BotWiringTest {

    @Autowired
    private ApplicationContext context;

    @TestConfiguration
    static class StubApplicationServices {

        private static ApplicationService empty() {
            return new ApplicationService() {

                @Override
                public Map<String, CommandHandler> getBotCommands() {
                    return Map.of();
                }

                @Override
                public Map<String, CallbackQueryHandler> getCallbackQueries() {
                    return Map.of();
                }

            };
        }

        @Bean("cclhBotApplicationService")
        ApplicationService cclhBotApplicationService() {
            return empty();
        }

        @Bean("dictionariesBotApplicationService")
        ApplicationService dictionariesBotApplicationService() {
            return empty();
        }

    }

    @Test
    void bothBotsAreRegisteredInLongPolling() {
        Assertions.assertNotNull(context.getBean("cclhBot", SpringLongPollingBot.class));
        Assertions.assertNotNull(context.getBean("dictionariesBot", SpringLongPollingBot.class));

        Assertions.assertEquals(2, context.getBeansOfType(SpringLongPollingBot.class).size());
    }

    /**
     * Solo debe existir un objeto de aplicación, el que crea nuestro registrador. Si reaparecieran
     * las autoconfiguraciones de los starters, el contexto ni siquiera llegaría hasta aquí.
     */
    @Test
    void thereIsExactlyOneBotApplication() {
        Assertions.assertEquals(1, context.getBeansOfType(TelegramBotsLongPollingApplication.class).size());
        Assertions.assertFalse(context.containsBean("telegramBotsApplication"));
    }

    /**
     * El interceptor de sesión tiene que llegar a los bots: sin él no habría usuario en el contexto
     * de seguridad y el motor rechazaría cualquier acción.
     */
    @Test
    void authInterceptorIsAvailableForTheBots() {
        Assertions.assertEquals(1, context.getBeansOfType(AuthUpdateInterceptor.class).size());
    }

    @Test
    void eachBotHasItsOwnClientAndMessageService() {
        Assertions.assertNotNull(context.getBean("cclhTelegramClient"));
        Assertions.assertNotNull(context.getBean("dictionariesTelegramClient"));
        Assertions.assertNotSame(context.getBean("cclhBotMessageService"),
                context.getBean("dictionariesBotMessageService"));
    }

}
