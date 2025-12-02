package com.ll.gateway.resopnse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ll.gateway.exception.GatewayErrorCode;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatewayBaseResponse<T> {

    private int status;
    private String message;
    private GatewayErrorCode errorCode;
    private T data;

    public static <T> GatewayBaseResponse<T> error(GatewayErrorCode errorCode) {
        return GatewayBaseResponse.<T>builder()
                .status(errorCode.getStatus().value())
                .message(errorCode.getMessage())
                .errorCode(errorCode)
                .build();
    }
}

