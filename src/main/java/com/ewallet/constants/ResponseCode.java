package com.ewallet.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {
    SUCCESS("00", "Success"),
    ACTION_FAIL("01", "Action fail"),
    INVALID_DATA("02", "Invalid data"),
    INVALID_INPUT("03", "Invalid input"),
    DO_NOT_HONOR("05", "Do not honor"),
    ERROR("06", "Failed to Process"),
    INVALID_TRANSACTION("12", "Invalid transaction"),
    INVALID_ACCOUNT_NUMBER("14", "Invalid account number"),
    CUSTOMER_CANCELLATION("17", "Customer cancellation"),
    CARD_NOT_SUPPORT("18", "Card not support"),
    INVALID_RESPONSE("20", "Invalid response"),
    SUSPECTED_MALFUNCTION("22", "Suspected malfunction"),
    DATA_EXISTS("26", "Data exists"),
    FORMAT_ERROR("30", "Format error"),
    RESTRICTED_CARD("36", "Restricted card"),
    FUNCTION_NOT_SUPPORTED("40", "Function not supported"),
    CLOSED_AUTH("41", "Closed authentication"),
    NOT_SUFFICIENT_FUNDS("51", "Not sufficient funds"),
    EXPIRED("54", "Expired"),
    VERIFY_FAIL("55", "Verify fail"),
    EXCEEDS_WITHDRAWAL_LIMIT("61", "Exceeds withdrawal limit"),
    CARD_ACCEPTOR_AMOUNT_LIMIT_EXCEEDED("61", "Card Acceptor Amount Limit Exceeded"),
    TIMEOUT("68", "Timeout"),
    CAN_NOT_SEND_NEXT_PROCESS("91", "Can not send next process"),
    DUPLICATE("94", "Duplicate"),
    SYSTEM_MALFUNCTION("96", "System malfunction"),
    PGS_SERVER_ERROR("99", "Pgs server error"),
    ACTION_NOT_MATCH("98", "Action not match");

    private final String code;
    private final String name;
}
