package com.dailytable.dailytable.domain.gacha;

import com.dailytable.dailytable.global.common.ErrorCode;
import com.dailytable.dailytable.global.exception.BaseException;

public class GachaException extends BaseException {

    public GachaException(ErrorCode errorCode) {
        super(errorCode);
    }
}
