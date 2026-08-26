package org.themarioga.telegram.cah.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Datos de presentación de los bots.
 * <p>
 * Antes vivían en la tabla {@code t_configuration}, que desapareció con el refactor de
 * Engine-Commons junto a su {@code ConfigurationService}. Son configuración de despliegue, no datos:
 * su sitio es el fichero de propiedades.
 */
@Configuration
@ConfigurationProperties(prefix = "cah.telegram")
public class BotProperties {

    private final Bot game = new Bot();
    private final Bot dictionaries = new Bot();

    /**
     * Diccionarios que caben en una página del menú de selección.
     */
    private Integer dictionariesPerPage = 10;

    public Bot getGame() {
        return game;
    }

    public Bot getDictionaries() {
        return dictionaries;
    }

    public Integer getDictionariesPerPage() {
        return dictionariesPerPage;
    }

    public void setDictionariesPerPage(Integer dictionariesPerPage) {
        this.dictionariesPerPage = dictionariesPerPage;
    }

    public static class Bot {

        private String displayName;
        private String alias;
        private String version;
        private String ownerAlias;
        private String helpUrl;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getOwnerAlias() {
            return ownerAlias;
        }

        public void setOwnerAlias(String ownerAlias) {
            this.ownerAlias = ownerAlias;
        }

        public String getHelpUrl() {
            return helpUrl;
        }

        public void setHelpUrl(String helpUrl) {
            this.helpUrl = helpUrl;
        }

    }

}
