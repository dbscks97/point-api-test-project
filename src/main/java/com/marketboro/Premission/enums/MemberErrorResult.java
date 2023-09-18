package com.marketboro.Premission.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorResult {

    DUPLICATED_MEMBER_REGISTER(HttpStatus.BAD_REQUEST, "Duplicated Member Register Request"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member Not Found"),
    NOT_MEMBER_OWNER(HttpStatus.BAD_REQUEST,"Member Is Not Owner"),
    UNKNOWN_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,"Unknown Exception"),
    NEGATIVE_POINTS(HttpStatus.BAD_REQUEST, "Negative Points"),

    ;

    private final HttpStatus httpStatus;
    private final String message;
}
