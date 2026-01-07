package com.qnit18.main_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    ASSET_NOT_FOUND(2001, "Asset not found", HttpStatus.NOT_FOUND),
    CHART_DATA_NOT_FOUND(2002, "Chart data not found", HttpStatus.NOT_FOUND),
    SERIES_ITEM_NOT_FOUND(2003, "Series item not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(2004, "User not found", HttpStatus.NOT_FOUND),
    USER_EXISTED(2005, "User already exists", HttpStatus.BAD_REQUEST),
    BUDGET_RULE_NOT_FOUND(2006, "Budget rule not found", HttpStatus.NOT_FOUND),
    BUDGET_ENTRY_NOT_FOUND(2007, "Budget entry not found", HttpStatus.NOT_FOUND),
    PORTFOLIO_NOT_FOUND(2008, "Portfolio not found", HttpStatus.NOT_FOUND),
    ASSET_USER_NOT_FOUND(2009, "Asset user not found", HttpStatus.NOT_FOUND),
    INVALID_BUDGET_PERCENTAGE(2010, "Budget percentages must sum to 100", HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}

