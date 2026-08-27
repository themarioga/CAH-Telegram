package org.themarioga.telegram.cah.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;
import org.themarioga.commons.telegram.models.UpdateInterceptor;
import org.themarioga.commons.telegram.services.impl.BotMessageServiceImpl;
import org.themarioga.commons.telegram.services.impl.LongPollingBotServiceImpl;
import org.themarioga.commons.telegram.services.impl.PendingReplyRegistry;
import org.themarioga.commons.telegram.services.impl.WebhookBotServiceImpl;
import org.themarioga.commons.telegram.services.intf.ApplicationService;
import org.themarioga.commons.telegram.services.intf.BotMessageService;
import org.themarioga.commons.telegram.services.intf.BotService;

import java.util.List;

/**
 * Da de alta los dos bots de CAH: el de juego y el de diccionarios.
 * <p>
 * El orden de dependencias es {@code TelegramClient → BotMessageService → ApplicationService → bot},
 * en un solo sentido. Antes había un ciclo (el {@code ApplicationService} pedía el {@code BotService}
 * y el {@code BotService} pedía el {@code ApplicationService}) que se intentaba tapar con
 * {@code spring.main.allow-circular-references}, opción que ni siquiera sirve para ciclos por
 * constructor.
 * <p>
 * El modo (long-polling o webhook) es común a los dos bots y lo decide {@code telegram.bots.type},
 * el mismo que elige qué objeto de aplicación crea
 * {@link org.themarioga.commons.telegram.config.TelegramBotsRegistrarConfig}. La versión anterior
 * declaraba <b>dos {@code @Bean} con el mismo nombre</b> en la misma clase distinguidos por
 * {@code @Conditional}, algo que Spring Boot 4 ya no permite.
 */
@Configuration
public class CAHTelegramBotsConfig {

    public static final String GAME_BOT = "cclh";
    public static final String DICTIONARIES_BOT = "dictionaries";

    @Bean("cclhTelegramClient")
    @ConditionalOnProperty(prefix = "cclh.bot", name = "enabled", havingValue = "true")
    public TelegramClient cclhTelegramClient(@Value("${cclh.bot.token}") String token) {
        return new OkHttpTelegramClient(token);
    }

    @Bean("dictionariesTelegramClient")
    @ConditionalOnProperty(prefix = "dictionaries.bot", name = "enabled", havingValue = "true")
    public TelegramClient dictionariesTelegramClient(@Value("${dictionaries.bot.token}") String token) {
        return new OkHttpTelegramClient(token);
    }

    @Bean("cclhBotMessageService")
    @ConditionalOnProperty(prefix = "cclh.bot", name = "enabled", havingValue = "true")
    public BotMessageService cclhBotMessageService(@Qualifier("cclhTelegramClient") TelegramClient client,
                                                   PendingReplyRegistry pendingReplies,
                                                   @Value("${cclh.bot.name}") String name) {
        return new BotMessageServiceImpl(client, name, pendingReplies);
    }

    @Bean("dictionariesBotMessageService")
    @ConditionalOnProperty(prefix = "dictionaries.bot", name = "enabled", havingValue = "true")
    public BotMessageService dictionariesBotMessageService(@Qualifier("dictionariesTelegramClient") TelegramClient client,
                                                   PendingReplyRegistry pendingReplies,
                                                           @Value("${dictionaries.bot.name}") String name) {
        return new BotMessageServiceImpl(client, name, pendingReplies);
    }

    @Configuration
    @ConditionalOnProperty(prefix = "telegram.bots", name = "type", havingValue = "longpolling", matchIfMissing = true)
    public static class LongPollingBots {

        @Bean("cclhBot")
        @ConditionalOnProperty(prefix = "cclh.bot", name = "enabled", havingValue = "true")
        public SpringLongPollingBot cclhBot(@Value("${cclh.bot.token}") String token,
                                            @Value("${cclh.bot.name}") String name,
                                            @Qualifier("cclhTelegramClient") TelegramClient client,
                                            @Qualifier("cclhBotApplicationService") ApplicationService applicationService,
                                            PendingReplyRegistry pendingReplies,
                                            List<UpdateInterceptor> interceptors) {
            return new LongPollingBotServiceImpl(token, name, client, applicationService, pendingReplies, interceptors);
        }

        @Bean("dictionariesBot")
        @ConditionalOnProperty(prefix = "dictionaries.bot", name = "enabled", havingValue = "true")
        public SpringLongPollingBot dictionariesBot(@Value("${dictionaries.bot.token}") String token,
                                                    @Value("${dictionaries.bot.name}") String name,
                                                    @Qualifier("dictionariesTelegramClient") TelegramClient client,
                                                    @Qualifier("dictionariesBotApplicationService") ApplicationService applicationService,
                                                    PendingReplyRegistry pendingReplies,
                                                    List<UpdateInterceptor> interceptors) {
            return new LongPollingBotServiceImpl(token, name, client, applicationService, pendingReplies, interceptors);
        }

    }

    @Configuration
    @ConditionalOnProperty(prefix = "telegram.bots", name = "type", havingValue = "webhook")
    public static class WebhookBots {

        @Bean("cclhBot")
        @ConditionalOnProperty(prefix = "cclh.bot", name = "enabled", havingValue = "true")
        public SpringTelegramWebhookBot cclhBot(@Value("${cclh.bot.token}") String token,
                                                @Value("${cclh.bot.name}") String name,
                                                @Value("${cclh.bot.webhook.url}") String webhookUrl,
                                                @Value("${cclh.bot.webhook.cert.path:}") String certPath,
                                                @Qualifier("cclhTelegramClient") TelegramClient client,
                                                @Qualifier("cclhBotApplicationService") ApplicationService applicationService,
                                                PendingReplyRegistry pendingReplies,
                                                List<UpdateInterceptor> interceptors) {
            BotService botService = new WebhookBotServiceImpl(token, name, webhookUrl, certPath, client,
                    applicationService, pendingReplies, interceptors);

            return (SpringTelegramWebhookBot) botService.getBean();
        }

        @Bean("dictionariesBot")
        @ConditionalOnProperty(prefix = "dictionaries.bot", name = "enabled", havingValue = "true")
        public SpringTelegramWebhookBot dictionariesBot(@Value("${dictionaries.bot.token}") String token,
                                                        @Value("${dictionaries.bot.name}") String name,
                                                        @Value("${dictionaries.bot.webhook.url}") String webhookUrl,
                                                        @Value("${dictionaries.bot.webhook.cert.path:}") String certPath,
                                                        @Qualifier("dictionariesTelegramClient") TelegramClient client,
                                                        @Qualifier("dictionariesBotApplicationService") ApplicationService applicationService,
                                                        PendingReplyRegistry pendingReplies,
                                                        List<UpdateInterceptor> interceptors) {
            BotService botService = new WebhookBotServiceImpl(token, name, webhookUrl, certPath, client,
                    applicationService, pendingReplies, interceptors);

            return (SpringTelegramWebhookBot) botService.getBean();
        }

    }

}
