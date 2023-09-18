package com.marketboro.Premission.exception;


import com.marketboro.Premission.enums.MemberErrorResult;
import lombok.Getter;

@Getter
public class MemberException extends RuntimeException {

    private final MemberErrorResult errorResult;

    public MemberException(MemberErrorResult errorResult) {
        super(errorResult.getMessage());
        this.errorResult = errorResult;
    }

}

