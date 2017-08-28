package com.fx.sharingbikes.common.exception;

import com.fx.sharingbikes.common.constants.Constants;

public class SharingBikesException extends Exception {

    public SharingBikesException(String message) {
        super(message);
    }

    public int getStatusCode() {
        return Constants.RESP_STATUS_INTERNAL_ERROR;
    }
}
