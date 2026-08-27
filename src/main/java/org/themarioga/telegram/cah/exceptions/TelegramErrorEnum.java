package org.themarioga.telegram.cah.exceptions;

import org.themarioga.commons.engine.enums.ErrorEnum;

/**
 * Errores propios de la capa de Telegram, los que no son de negocio y por tanto no tienen sitio en
 * los enums del motor.
 * <p>
 * Los códigos arrancan en 100 para no pisar los del motor.
 */
public enum TelegramErrorEnum implements ErrorEnum {

    SELECTION_INVALID(100L, "La opción elegida no está en la lista");

    private final Long errorCode;
    private final String errorDesc;

    TelegramErrorEnum(Long errorCode, String errorDesc) {
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    @Override
    public Long getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorDesc() {
        return errorDesc;
    }

}
