package org.themarioga.telegram.cah;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.themarioga.commons.engine.enums.CommonErrorEnum;
import org.themarioga.commons.engine.enums.ErrorEnum;
import org.themarioga.commons.engine.exceptions.ApplicationException;
import org.themarioga.commons.engine.exceptions.room.RoomDoesntExistsException;
import org.themarioga.commons.engine.exceptions.user.UserDoesntExistsException;
import org.themarioga.engine.cah.enums.CAHErrorEnum;
import org.themarioga.engine.cah.exceptions.card.CardTextExcededLength;
import org.themarioga.engine.cah.exceptions.dictionary.DictionaryAlreadySharedException;
import org.themarioga.engine.cah.exceptions.dictionary.DictionaryDoesntExistsException;
import org.themarioga.engine.cah.exceptions.dictionary.DictionaryMaxCollaboratorsReached;
import org.themarioga.telegram.cah.config.ErrorMessageResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Sustituye a la escalera de {@code catch} por excepción que el código anterior repetía en cada
 * método.
 */
@SpringBootTest
class ErrorMessageResolverTest {

    @Autowired
    private ErrorMessageResolver errorMessageResolver;

    @Test
    void resolvesByConvention() {
        Assertions.assertEquals("ERROR_DICTIONARY_NOT_FOUND",
                errorMessageResolver.tagOf(CAHErrorEnum.DICTIONARY_NOT_FOUND));
        Assertions.assertEquals("ERROR_ROOM_NOT_ACTIVE",
                errorMessageResolver.tagOf(CommonErrorEnum.ROOM_NOT_ACTIVE));
    }

    @Test
    void resolvesTheOnesThatDoNotFollowTheConvention() {
        Assertions.assertEquals("COLLABORATOR_ADD_MAX_REACHED",
                errorMessageResolver.tagOf(CAHErrorEnum.DICTIONARY_MAX_COLLABORATORS_REACHED));
        Assertions.assertEquals("ERROR_CARD_EXCEEDED_LENGTH",
                errorMessageResolver.tagOf(CAHErrorEnum.CARD_TEXT_TOO_LONG));
    }

    @Test
    void translatesTheExceptionsOfTheDictionaryBot() {
        for (ApplicationException e : List.<ApplicationException>of(new DictionaryDoesntExistsException(),
                new DictionaryAlreadySharedException(), new DictionaryMaxCollaboratorsReached(),
                new CardTextExcededLength(), new UserDoesntExistsException(), new RoomDoesntExistsException())) {
            String message = errorMessageResolver.resolve(e);

            Assertions.assertFalse(message.startsWith("ERROR_"), () -> "sin traducir: " + message);
            Assertions.assertNotEquals(e.getMessage(), message,
                    "debe traducirse, no devolver la descripción interna del enum");
        }
    }

    /**
     * La invariante que importa: pase lo que pase, al usuario nunca se le enseña el nombre de un tag.
     * Hoy 37 errores del motor no tienen texto (casi todos validaciones internas), y sin esta red
     * cualquiera de ellos aparecería en pantalla como "ERROR_ROUND_NOT_FOUND".
     */
    @Test
    void neverLeaksATagName() {
        List<ErrorEnum> all = new ArrayList<>();
        all.addAll(List.of(CAHErrorEnum.values()));
        all.addAll(List.of(CommonErrorEnum.values()));

        for (ErrorEnum error : all) {
            String message = errorMessageResolver.resolve(new ApplicationException(error));

            Assertions.assertFalse(message.startsWith("ERROR_") || message.equals(errorMessageResolver.tagOf(error)),
                    () -> "el error " + error + " se filtra como nombre de tag: " + message);
        }
    }

    @Test
    void unknownThrowableFallsBackToTheGenericMessage() {
        Assertions.assertEquals("Ha ocurrido un error inesperado.",
                errorMessageResolver.resolve(new IllegalStateException("boom")));
    }

}
