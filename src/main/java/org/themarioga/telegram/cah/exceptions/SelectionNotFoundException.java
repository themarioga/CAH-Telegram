package org.themarioga.telegram.cah.exceptions;

import org.themarioga.commons.engine.exceptions.ApplicationException;

/**
 * El usuario ha contestado algo que no corresponde a ninguna opción de la última lista que se le
 * enseñó.
 */
public class SelectionNotFoundException extends ApplicationException {

    public SelectionNotFoundException() {
        super(TelegramErrorEnum.SELECTION_INVALID);
    }

}
