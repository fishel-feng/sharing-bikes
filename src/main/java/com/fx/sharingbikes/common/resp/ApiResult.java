package com.fx.sharingbikes.common.resp;

import com.fx.sharingbikes.common.constants.Constants;
import lombok.Data;

@Data
public class ApiResult<T> {

    private int code = Constants.RESP_STATUS_OK;

    private String message;

    private T data;
}
