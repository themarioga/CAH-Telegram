package org.themarioga.telegram.cah;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.themarioga.commons.engine.models.Lang;
import org.themarioga.commons.engine.services.intf.I18NService;
import org.themarioga.commons.engine.services.intf.LanguageService;

import java.util.List;

/**
 * Comprueba que el baseline de Flyway y el modelo de entidades dicen lo mismo.
 * <p>
 * El trabajo lo hace {@code spring.jpa.hibernate.ddl-auto=validate}: si al esquema le falta una
 * columna, le sobra, o un tipo no cuadra, el contexto no arranca y el test falla. Es la red que
 * evita que el baseline se quede atrás cuando alguien toque una entidad.
 */
@SpringBootTest
class SchemaBaselineTest {

    @Autowired
    private LanguageService languageService;
    @Autowired
    private I18NService i18NService;

    @Test
    void schemaMatchesTheEntityModel() {
        // Si hemos llegado aquí, Flyway migró y Hibernate validó el esquema resultante.
        Assertions.assertNotNull(languageService);
    }

    @Test
    void languagesAreSeeded() {
        List<Lang> langs = languageService.getLangs();

        Assertions.assertEquals(2, langs.size());
        Assertions.assertTrue(langs.stream().anyMatch(l -> "es".equals(l.getId())));
        Assertions.assertTrue(langs.stream().anyMatch(l -> "en".equals(l.getId())));
        Assertions.assertEquals("es", languageService.getDefaultLanguage().getId());
    }

    /**
     * Los textos i18n se recuperaron de las migraciones antiguas: comprobamos que están los dos
     * idiomas y que un tag conocido devuelve su texto y no su propio nombre (que es lo que devuelve
     * {@code I18NService} cuando no encuentra el tag).
     */
    @Test
    void tagsAreSeededInBothLanguages() {
        Assertions.assertEquals("← Volver", i18NService.get("GO_BACK", "es"));
        Assertions.assertEquals("← Go back", i18NService.get("GO_BACK", "en"));

        String welcome = i18NService.get("PLAYER_WELCOME", "es");
        Assertions.assertNotEquals("PLAYER_WELCOME", welcome, "el tag no está en la tabla");
        Assertions.assertTrue(welcome.contains("\n"), "los \\n del SQL deben llegar como saltos de línea");
    }

}
