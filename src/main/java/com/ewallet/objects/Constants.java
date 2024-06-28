package com.ewallet.objects;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public class Constants {
    private Constants() {

    }

    public static final String PREFIX_DEFAULT_CACHE = "default:";
    public static final String PREFIX_MERCHANT_CACHE = "MerchantCache:";
    public static final String PREFIX_REQUEST_OBJECT_CACHE = "RequestObjectCache:";
    public static final String PREFIX_CARD_CACHE = "CardCache:";
    public static final String PREFIX_CARD_BALANCE_INQUIRY_CACHE = "CardBalanceInquiryCache:";
    public static final String PREFIX_CARD_LIST_INQUIRY_CACHE = "EWalletCardListCache:";
    public static final String PREFIX_BIN_CACHE = "BinCache:";
    public static final String PREFIX_ANTI_DDOS_CACHE = "AntiDdosCache:";
    public static final String PREFIX_ACCOUNT_SCP_CACHE = "AccountSCPCache:";
    public static final int MIN_LENGTH_CARD_NUMBER = 15;
    public static final String PREFIX_CARD_ESSENTIAL_DESCRIPTION = "BI ";
    public static final String PREFIX_T24_DEPOSIT_PRODUCT_NAME = "T24DepositProductNameCache:";
    public static final String PREFIX_APPLE_PILOT_CACHE = "ApplePilotCache:";
    public static final String PREFIX_TRANSACTION_GET_FX_CACHE = "TransactionGetFXCache:";
    public static final String PREFIX_TOPUP_MUTI_CACHE = "TopupMutiCache:";
    public static final int DURATION_APPLE_PILOT = 1800;

    public static final String PREFIX_ACCOUNT_CACHE = "EWalletAccountCache:";

    public static class EWalletResponseCode {
        private EWalletResponseCode() {

        }

        public static final String SUCCESS = "00";
        public static final String ACTION_FAIL = "01";
        public static final String INVALID_DATA = "02";
        public static final String INVALID_INPUT = "03";
        public static final String DO_NOT_HONOR = "05";
        public static final String ERROR = "06";
        public static final String HONOR_IDENTIFICATION = "08";
        public static final String INVALID_TRANSACTION = "12";
        public static final String INVALID_ACCOUNT_NUMBER = "14"; //Account, CIF,CARD,eWalletId
        public static final String CUSTOMER_CANCELLATION = "17";
        public static final String CARD_NOT_SUPPORT = "18";
        public static final String INVALID_RESPONSE = "20";
        public static final String SUSPECTED_MALFUNCTION = "22";


        public static final String DATA_EXISTS = "26";
        public static final String FORMAT_ERROR = "30";
        public static final String RESTRICTED_CARD = "36";
        public static final String FUNCTION_NOT_SUPPORTED = "40";
        public static final String CLOSED_AUTHEN = "41";
        public static final String NOT_SUFFICIENT_FUNDS = "51";
        public static final String EXPIRED = "54";
        public static final String VERIFY_FAIL = "55"; //verify fail PIN/mPass/OTP/CVV
        public static final String EXCEEDS_WITHDRAWAL_LIMIT = "61";
        public static final String CARD_ACCEPTOR_AMOUNT_LIMIT_EXCEEDED = "66";
        public static final String TIMEOUT = "68";
        public static final String CAN_NOT_SEND_NEXT_PROCESS = "91";
        public static final String DUPLICATE = "94";
        public static final String SYSTEM_MALFUNCTION = "96";
        public static final String PGS_SERVER_ERROR = "99";
        public static final String ACTION_NOT_MATCH = "98";

        public static final String VALIDATE_0 = "V0";
        public static final String VALIDATE_1 = "V1";
        public static final String VALIDATE_2 = "V2";
        public static final String VALIDATE_3 = "V3";
        public static final String VALIDATE_4 = "V4";
        public static final String VALIDATE_5 = "V5";
        public static final String VALIDATE_6 = "V6";
        public static final String VALIDATE_7 = "V7";
        public static final String VALIDATE_8 = "V8";
        public static final String VALIDATE_9 = "V9";
    }

    public class CurrencyCode {
        private CurrencyCode() {

        }

        public static final String VN = "704";
        public static final String LA = "840";
        public static final String KH = "840";

    }

    public static class EWalletFTNameInquiryResponseCode {
        private EWalletFTNameInquiryResponseCode() {

        }

        public final String NOT_ACTIVE = "03";
    }


    public class IVRResponseCode {
        private IVRResponseCode() {

        }

        public final String SERVER_ERROR = "05";
        public final String INVALID_CARDNUMBER_CARDCODE = "14";
        public final String SUCCESS = "00";
        public final String NO_DATA = "01";
    }


    public static class AccountType {
        private AccountType() {

        }

        public static final String SacombankPAN = "01";
        public static final String SacombankACCVN = "02";
        public static final String VisaPAN = "03";
        public static final String MastercardPan = "04";
        public static final String NapasPAN = "05";
        public static final String NapasACC = "06";
    }


    public static class Request {
        private Request() {

        }

        public static final String NO_CRYPT = "nocrypt";
        public static final String APPLICATION_JSON = "application/json";
        public static final String APPLICATION_XML = "application/xml";
        public static final int BASIC_LENGTH = 5; // "Basic".length()
        public static final String AUTHORIZATION = "Authorization";
        public static final String BASIC_AUTH = "basic";
        public static final String INVALID_BASIC_AUTH = "Invalid BasicAuth credentials";
        public static final String INVALID_MERCHANT = "Invalid merchant";
        public static final String DUPLICATE_REQUEST = "Duplicate request";
        public static final String DATA_NOT_MATCH = "Data not match";
        public static final String SCP = "SCP";
        public static final String USER_NAME = "UserName";
        public static final String PASSWORD = "Password";
        public static final String USER = "user";
        public static final String PASS = "pass";
        public static final String SESSION_KEY = "SessionKey";
        public static final String YYYY_MM_DD_HH_MM_SS_X = "yyyy-MM-dd'T'HH:mm:ssX";
        public static final String YYYY_MM_DD_HH_MM_SS_Z = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        public static final String YYYY_MM_DD_HH_MM_SS = "yyyyMMddHHmmss";
        public static final String DD_MM_YYYY_HH_MM = "dd/MM/yyyy HH:mm";
        public static final String YYYY_MM_DD = "yyyyMMdd";
        public static final String M_DD_YYYY_HH_MM_SS_A = "M/dd/yyyy HH:mm:ss a";

    }

    public static class LogFormat {
        private LogFormat() {

        }

        public static final String DEBUG_FORMAT = "{}: Elapsed time: {}. To EWallet - FunctionName: {} - RequestID: {} - Data: {}";
        public static final String REQUEST_FORMAT = "{}: From EWallet - FunctionName: {} - RequestID: {} - Data: {}";
        public static final String ERROR_FORMAT = "{}: >>> ";
        public static final String TIME_OVER_FORMAT = "{}: API over MaxTimeLog: {}. To {}. FunctionName: {}. RequestID: {}";
        public static final String FUNCTION_INVALID_FORMAT = "{}: Cannot find function {}";
        public static final String CONVERT_RES_CODE_FORMAT = "{}: Convert ResponseCode {}->68";
        public static final String INVALID_REQUEST_ID_FORMAT = "From {} - {}: Invalid request ID";
        public static final String INVALID_REQUEST_DATE_TIME_FORMAT = "From {} - {}: Invalid request date and time";
        public static final String CANNOT_STORE_REQUEST_FORMAT = "From {} - {}: Cannot store request";
        public static final String BODY_NOT_MATCH_FORMAT = "Body not match EWalletRequest";
        public static final String ERROR_DATA_SERVICE_NULL_FORMAT = "{}: >> response data from service is null";
        public static final String ERROR_EXCEPTION_FORMAT = ">>> {}";
        public static final String INVALID_DATA_FUNCTION_FORMAT = "{}: FunctionName : {} - Invalid Data";
        public static final String CANNOT_DESERIALIZE_FORMAT = "%s: Cannot deserialize";
        public static final String M_PASS_VERIFICATION_CONVERT = "{}: mPassVerification convert {} -> {}";
        public static final String LMID_FUNC_DATA = "{}: {} - {}";
        public static final String LMID_LOG_FORM_2 = "{}: {}";
        public static final String LMID_LOG_FORM_3 = "{}: {} - {}";
        public static final String LMID_LOG_FORM_4 = "{}: {} - {} - {}";
        public static final String LMID_LOG_FORM_5 = "{}: {} - {} - {} - {}";

    }

    public static class Language {
        private Language() {

        }

        public static final String VI = "VI";
        public static final String EN = "EN";
    }

    public static class CountryCode {
        private CountryCode() {

        }

        public static final String VN = "VN";
        public static final String EN = "EN";
    }

    public static class AuthenType {
        private AuthenType() {

        }

        public static final String M_PASS = "0";
        public static final String SMS_OTP = "1";
        public static final String HARD_TOKEN = "2";
        public static final String M_CODE = "3";
        public static final String M_CONNECTED = "4";
        public static final String ADV_TOKEN = "5";
        public static final String SOFT_OTP = "6";
        public static final String SMART_OTP = "8";
        public static final String INTER_APP = "41";
    }

    public static class InquiryType {
        private InquiryType() {

        }

        public static final String IDENTIFIED = "I";
        public static final String ANONYMOUS = "A";
    }

    public static class CardInfoType {
        private CardInfoType() {

        }

        public static final String INFO = "I";
        public static final String STATUS = "S";
        public static final String CARD_TOKEN_ANONYMOUS = "IC";
        public static final String SRC = "SRC";
        public static final String DST = "DST";
        public static final String ZERO = "0";
        public static final String EMPTY = "";
    }

    public static class CardCategory {
        private CardCategory() {

        }

        public static final String C_CATEGORY = "C";
    }

    public static class SecurityProps {
        private SecurityProps() {

        }

        public static final String NOOP = "{noop}";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ActivityFilter {
        public static final String GET_ACOUNT_VERIFY_PASSWORD = "GetAcountVerifyPassword";
        public static final String UPDATE_PASSWORD_OFFSET = "UpdatePasswordOffset";
        public static final String INSERT = "Insert";
        public static final String UPDATE = "Update";
    }

    public static class Monitor {
        private Monitor() {

        }

        public static final String LABEL = "Label";
        public static final String VALUE_MSG = "1000#1003#1000#1000#";
        public static final String LABEL_VALUE = "1000#1003#1000#1000#%s";
        public static final String TIME_FORMAT_MONITOR = "yyyyMM:HHmmss:sss";
        public static final String APP_NAME = "AppName";
        public static final String CPU = "CPU";
        public static final String RAM = "RAM";
        public static final String THREADS = "Threads";
        public static final String HANDLES = "Handles";
        public static final String HH_MM_SS = "HHmmss";
        public static final String IS_BODY = "IsBody";

    }

    public static class GetBranchName {
        public GetBranchName() {
        }

        public static final String GET_BRANCH_NAME = "GetBranchName";
    }


    //constant OmniCard
    public static class ConstantOmniCard {
        private ConstantOmniCard() {
        }

        public static final String CARD_NUMBER = "cardNumber";
        public static final String TOKENIZATION_ID = "tokenizationID";
        public static final String EXPIRY_DATE = "expiryDate";
        public static final String NEW_PIN = "newPIN";
        public static final String CLIENT_USER_ID = "clientUserId";
        public static final String PIN_SELECTION = "pinSelection";

        public static final String MSG_ID = "msgId";
        public static final String SERVICE_ID = "serviceId";
        public static final String CONNECTOR_USER_ID = "connectorUserId";
        public static final String CLIENT_SESSION_ID = "clientSessionId";
        public static final String APPLICATION = "application";
        public static final String WORKSTATION = "workstation";
        public static final String HOST = "host";
        public static final String ROWS_PER_PAGE = "rowsPerPage";
        public static final String PAGE_NO = "pageNo";
        public static final String PAGE_IN = "pageIn";
        public static final String HEADER_IN = "headerIn";
        public static final String DET_REQ = "DetReq";
        public static final String DATA_REQ = "DataReq";
        public static final String TIMEOUT_MSG = "Timeout";
        public static final String RESULT_CODE = "resultCode";
        public static final String RESULT_CODE_1 = "1";
        public static final String HEADER_OUT = "headerOut";
        public static final String OMNI_WS_TIMEOUT = "OmniWS Timeout";
        public static final String DATA_RESP = "DataResp";
        public static final String DET_RESP = "DetResp";
        public static final String ERROR_OUT_LIST = "errorOutList";
        public static final String ERROR_FIELD = "error";
        public static final String ERROR_NO = "errorNo";

        public static final String INSERT = "Insert";

        public static final String UPDATE = "Update";

        public static final String DIRECTORY_TEMPLATE = "classpath:template/";

    }


}

