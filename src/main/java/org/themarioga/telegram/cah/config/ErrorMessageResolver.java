package org.themarioga.telegram.cah.config;

import org.springframework.stereotype.Component;
import org.themarioga.commons.engine.enums.CommonErrorEnum;
import org.themarioga.commons.engine.enums.ErrorEnum;
import org.themarioga.commons.engine.exceptions.ApplicationException;
import org.themarioga.commons.engine.services.intf.I18NService;
import org.themarioga.engine.cah.enums.CAHErrorEnum;

import java.util.Map;

/**
 * Traduce los errores del motor al texto que ve el usuario.
 * <p>
 * Todas las excepciones del motor llevan su {@link ErrorEnum}, así que basta una tabla. El código
 * anterior repetía en cada uno de sus ~57 métodos una escalera de {@code catch} por excepción
 * concreta que mapeaba a mano a su tag, con el resultado previsible: unos métodos capturaban unas
 * excepciones y otros no, y lo que se escapaba llegaba al usuario como el mensaje interno en
 * castellano del enum ("Diccionario no encontrado"), sin traducir.
 */
@Component
public class ErrorMessageResolver {

    private static final String FALLBACK_TAG = "UNKNOWN_ERROR";

    private final I18NService i18NService;

    /**
     * Solo hacen falta los errores cuyo tag no se deduce del nombre; el resto se resuelve por
     * convención (ver {@link #tagOf}).
     */
    private static final Map<ErrorEnum, String> EXCEPTIONS_TO_CONVENTION = Map.of(
            CAHErrorEnum.DICTIONARY_COLLAB_ALREADY_EXISTS, "COLLABORATOR_ADD_ALREADY_EXISTS",
            CAHErrorEnum.DICTIONARY_COLLAB_NOT_FOUND, "ERROR_COLLABORATOR_DOESNT_EXISTS",
            CAHErrorEnum.DICTIONARY_MAX_COLLABORATORS_REACHED, "COLLABORATOR_ADD_MAX_REACHED",
            CAHErrorEnum.CARD_TEXT_TOO_LONG, "ERROR_CARD_EXCEEDED_LENGTH",
            CAHErrorEnum.CARD_NOT_FOUND, "ERROR_CARD_DOESNT_EXISTS",
            CAHErrorEnum.CARD_ALREADY_EXISTS, "ERROR_CARD_ALREADY_EXISTS",
            CommonErrorEnum.USER_NOT_FOUND, "ERROR_GAME_USER_DOESNT_EXISTS",
            CommonErrorEnum.ROOM_NOT_FOUND, "ERROR_GAME_ROOM_DOESNT_EXISTS");

    public ErrorMessageResolver(I18NService i18NService) {
        this.i18NService = i18NService;
    }

    /**
     * Texto traducido para una excepción, en el idioma del usuario de la sesión.
     * <p>
     * Si el error no tiene texto, se devuelve el genérico. {@code I18NService} devuelve el propio
     * nombre del tag cuando no lo encuentra, y enseñarle "ERROR_ROUND_NOT_FOUND" a un usuario es
     * peor que decirle que algo ha fallado: hoy 37 de los errores del motor no tienen texto, casi
     * todos validaciones internas que no deberían llegar hasta aquí.
     */
    public String resolve(Throwable e) {
        if (e instanceof ApplicationException applicationException) {
            String tag = tagOf(applicationException.getErrorEnum());
            String text = i18NService.get(tag);

            if (!tag.equals(text)) return text;
        }

        return i18NService.get(FALLBACK_TAG);
    }

    /**
     * Por convención, {@code DICTIONARY_NOT_FOUND} se traduce con el tag
     * {@code ERROR_DICTIONARY_NOT_FOUND}. Los que no siguen esa regla están en la tabla de arriba.
     */
    public String tagOf(ErrorEnum error) {
        if (error == null) return FALLBACK_TAG;

        String mapped = EXCEPTIONS_TO_CONVENTION.get(error);
        if (mapped != null) return mapped;

        return "ERROR_" + ((Enum<?>) error).name();
    }

}
