package com.marketboro.Premission.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorResult {

    DUPLICATED_MEMBER_REGISTER(HttpStatus.BAD_REQUEST, "Duplicated Member Register Request"),
    NOT_MEMBER(HttpStatus.BAD_REQUEST, "Not a member"),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
