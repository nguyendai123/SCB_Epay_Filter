package com.stb.epay.lib.common;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommonUtil {

    public static String maskCardNo(String content) {
        if (Strings.isNullOrEmpty(content)) return "";
        Pattern pattern = Pattern.compile("(\\d[\\s|-]?){12,15}\\d");
        Matcher matcher = pattern.matcher(content);
        return matcher.replaceAll(
                ms -> ms.group().substring(0, 6) + "X".repeat(ms.group().length() - 10)
                        + ms.group().substring(ms.group().length() - 4));
    }

    public static boolean checkPrefixVa(String lIMD, String accountNumber, String accountVAPrefixNotExists) {
        String[] existPrefix = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
                "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "7"};
        String[] notExistPrefix = accountVAPrefixNotExists.split("#");
        try {
            if (Arrays.stream(notExistPrefix).anyMatch(x -> accountNumber.toUpperCase().startsWith(x))
                    || Arrays.stream(existPrefix).noneMatch(x -> accountNumber.toUpperCase().startsWith(x))) {
                return false;
            }
        } catch (Exception ex) {
            log.error(">>> {}:{}", lIMD, ex);
            return false;
        }
        return true;
    }

    public static String convertRenMethodNameT24(String code) {
        String result = "";
        if ("TTVL".equals(code)) {
            result = "Tự động tái tục vốn & lãi";
        } else if ("TTV".equals(code)) {
            result = "Tự động tái tục vốn";
        } else if ("KTT".equals(code)) {
            result = "Không tái tục";
        }
        return result;
    }

    public static String convertPmtMethodT24(String renMethod, String intPmtMethod, String depositTermType) {
        String[] arrDepositTermType = depositTermType.split("#");
        String result = "";
        if (arrDepositTermType[0].contains(";" + intPmtMethod + ";")) // hang thang
        {
            result = "18";
        } else if (arrDepositTermType[1].contains(";" + intPmtMethod + ";")) // hang quy
        {
            result = "19";
        } else if (arrDepositTermType[2].contains(";" + intPmtMethod + ";")) // hang nam
        {
            result = "20";
        } else if (arrDepositTermType[2].contains(";" + intPmtMethod + ";")) // cuoi thang
        {
            result = "21";
        } else if (arrDepositTermType[2].contains(";" + intPmtMethod + ";")) // hang 6 thang
        {
            result = "22";
        } else if (arrDepositTermType[2].contains(";" + intPmtMethod + ";")) // tra truoc
        {
            result = "23";
        } else //cuoi ky
        {
            result = "15";
        }
        return result;
    }

    public static int convertTempToValue(String term) {
        int result = 0;
        if ("Y".contains(term)) {
            result = Integer.parseInt(term.replace("Y", ""));
        } else if ("M".contains(term)) {
            result = Integer.parseInt(term.replace("M", ""));
        } else if ("D".contains(term)) {
            result = Integer.parseInt(term.replace("D", ""));
        }
        return result;
    }

    public static String convertPmtMethodNameT24(String code) {
        String result = "";
        if ("15".equals(code)) {
            result = "Cuối kỳ";
        } else if ("23".equals(code)) {
            result = "Trả trước";
        } else if ("22".equals(code)) {
            result = "Hàng 6 tháng";
        } else if ("21".equals(code)) {
            result = "Hàng tháng";
        } else if ("20".equals(code)) {
            result = "Hàng năm";
        } else if ("19".equals(code)) {
            result = "Hàng quý";
        } else if ("18".equals(code)) {
            result = "Hàng tháng";
        }
        return result;
    }



    public static String convertResponseCoreBankingToCard(String value) {
        String result = "";
        try {
            switch (value) {
                case "000" -> result = "00";
                case "111", "906" -> result = "26";
                case "114", "908", "189" -> result = "14";
                case "115" -> result = "40";
                case "116" -> result = "51";
                case "119" -> result = "57";
                case "121" -> result = "61";
                case "180", "184", "188" -> result = "";
                case "185" -> result = "13";
                case "190" -> result = "03";
                case "902" -> result = "12";
                case "904" -> result = "30";
                case "907" -> result = "91";
                case "909" -> result = "96";
                case "911" -> result = "68";
                case "913" -> result = "94";
                case "914", "915", "916", "917", "918", "919", "920", "921", "922", "923" -> result = "36";
                case "021" -> result = "V1";
                case "022" -> result = "V2";
                default -> result = "";
            }
        } catch (Exception ex) {
            log.error("", ex);
            return "";
        }
        return result;
    }

    public static String convertResponseCoreEwalletToCard(String value) {
        String result = "";
        try {
            if (StringUtils.hasText(value)) {
                switch (value) {
                    case "0000" -> result = "00";
                    case "0001", "0100" -> result = "03";
                    case "0002", "0010", "0101" -> result = "14";
                    case "0003", "0004", "0005", "0006", "9997" -> result = "06";
                    case "0007" -> result = "94";
                    case "0011" -> result = "36";
                    case "0012", "0013", "0014", "0015", "0016", "0017", "0018", "9998", "9999" -> result = "05";
                    case "0019" -> result = "61";
                    case "9996" -> result = "68";
                    case "0102" -> result = "26";
                    case "0103" -> result = "98";
                    default -> result = "";
                }

            }
        } catch (Exception ex) {
            log.error(">>> ", ex);
            return "";
        }
        return result;
    }

    public static String convertResponseAccountAtomicToCard(String value) {
        return convertFromAtomicCode(value);
    }

    public static String convertFromAtomicCode(String value) {
        String result = "";
        if (StringUtils.hasText(value)) {
            result = switch (value) {
                case "0000" -> "00";
                case "0100" -> "01";
                case "0200" -> "02";
                case "0300", "0302", "0303", "0304", "0310", "0311", "0312", "0313", "0314", "0315", "0316" -> "03";
                case "0317" -> "417";
                case "0318" -> "415";
                case "0400" -> "04";
                case "0500" -> "05";
                case "0600", "0601", "0602", "0603", "0604", "0605", "0606", "0650", "0651" -> "06";
                case "0607" -> "401";
                case "0700" -> "07";
                case "0800" -> "08";
                case "0900" -> "09";
                case "1000" -> "10";
                case "1100" -> "11";
                case "1140" -> "114";
                case "1200", "1202" -> "12";
                case "1300" -> "13";
                case "1400", "1401", "1402", "1403", "1404", "1405", "1406", "1407", "1408", "1409", "1410", "1413", "0301" -> "14";
                case "1414" -> "04";
                case "1415" -> "410";
                case "1458" -> "58";
                case "1500" -> "15";
                case "1600" -> "16";
                case "1700" -> "17";
                case "1800", "1801", "1802" -> "18";
                case "1900" -> "19";
                case "2000" -> "20";
                case "2100" -> "21";
                case "2200" -> "22";
                case "2300" -> "23";
                case "2400", "2402", "2404", "2410", "2414", "2415", "2418", "2423", "2424", "2425", "2426", "2427", "2428", "2430", "2431" -> "24";
                case "2434" -> "34";
                case "2450" -> "50";
                case "2500" -> "25";
                case "2601", "2602", "2626" -> "26";
                case "2700" -> "27";
                case "2800" -> "28";
                case "2900" -> "29";
                case "3000" -> "30";
                case "3001" -> "01";
                case "3040" -> "400";
                case "3100" -> "31";
                case "3200" -> "32";
                case "3300" -> "33";
                case "3400" -> "34";
                case "3500" -> "35";
                case "3600", "3601" -> "36";
                case "3700" -> "37";
                case "3800" -> "38";
                case "3900" -> "39";
                case "4000" -> "40";
                case "4003" -> "403";
                case "4005" -> "205";
                case "4089" -> "89";
                case "4100" -> "41";
                case "4200" -> "42";
                case "4300" -> "43";
                case "4400" -> "44";
                case "5100", "5101" -> "51";
                case "5200" -> "52";
                case "5300" -> "53";
                case "5400" -> "54";
                case "5500", "5501", "5502", "5503" -> "55";
                case "5600" -> "56";
                case "5700" -> "57";
                case "5800" -> "58";
                case "5900" -> "59";
                case "6000" -> "60";
                case "6100" -> "61";
                case "6200" -> "62";
                case "6300" -> "63";
                case "6400" -> "64";
                case "6500" -> "65";
                case "6600" -> "66";
                case "6700" -> "67";
                case "6800" -> "68";
                case "6801" -> "416";
                case "7500", "7501" -> "75";
                case "7600" -> "76";
                case "7700" -> "77";
                case "8000" -> "80";
                case "8100" -> "81";
                case "8200" -> "82";
                case "8300" -> "83";
                case "8400" -> "84";
                case "8500" -> "85";
                case "8600" -> "86";
                case "8800" -> "88";
                case "8900" -> "89";
                case "9000" -> "90";
                case "9060" -> "906";
                case "9070" -> "907";
                case "9100", "9103" -> "91";
                case "9110" -> "911";
                case "9200" -> "92";
                case "9300", "9301", "9302" -> "93";
                case "9400" -> "94";
                case "9500" -> "95";
                case "9600" -> "96";
                default -> // 0600
                        "06";
            };
        }
        return result;
    }


    public static String convertAcctProductNameT24(String value) {
        String result = "";
        if (StringUtils.hasText(value)) {
            result = switch (value) {
                case "8100", "8150", "21010" -> "Tiết kiệm có kỳ hạn";
                case "8102" -> "TG CKH trực tuyến";
                case "8606" -> "Tiết kiệm Tích góp";
                case "8609" -> "Tiết kiệm Tích góp";
                case "8612" -> "Tiết kiệm Tích góp";
                case "8624" -> "Tiết kiệm Tích góp";
                case "8406", "8409", "8412", "8424" -> "Tiết kiệm tích tài tại quầy";
                default -> "Tiền gửi/Tiết kiệm có kỳ hạn";
            };
        }
        return result;
    }

    public static String convertResponseGiftsToCard(String value) {
        String result = EMPTY;
        try {
            if (StringUtils.hasText(value)) {
                switch (value) {
                    case "000" -> result = "00";
                    case "100" -> result = "26";
                    case "200" -> result = "14";
                    case "201" -> result = "V1";
                    case "202" -> result = "V2";
                    case "203" -> result = "V3";
                    case "2011" -> result = "V4";
                    case "2012" -> result = "V5";
                    case "900" -> result = "68";
                    case "901" -> result = "94";
                    case "999" -> result = "06";
                }

            }
        } catch (Exception ex) {
            log.error(ex.toString());
            return "";
        }
        return result;
    }

    public String buildKey(String cifNo, String country, String customerIdNo, String customerIdType) {
        if (!isBlank(cifNo) && !isBlank(country)) {
            return cifNo.trim() + "." + country.trim();
        }

        if (!isBlank(customerIdNo) && !isBlank(customerIdType)) {
            return customerIdNo.trim() + "." + customerIdType.trim();
        }

        return "";
    }

    public static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    public static String toStringEmpty(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    public static String toStringTrimEmpty(Object obj) {
        return obj != null ? obj.toString().trim() : "";
    }

    public static String toStringNull(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    public static String trim(String obj) {
        return obj != null ? obj.trim() : "";
    }

    public static String convertResponeCoreEwalletToCard(String value) {
        String result = "";
        try {
            if (StringUtils.hasText(value)) {
                switch (value) {
                    case "0000" -> result = "00";
                    case "0001", "0100" -> result = "03";
                    case "0002", "0010", "0101" -> result = "14";
                    case "0003", "0004", "0005", "0006", "9997" -> result = "06";
                    case "0007" -> result = "94";
                    case "0011" -> result = "36";
                    case "0012", "0013", "0014", "0015", "0016", "0017", "0018", "9998", "9999" -> result = "05";
                    case "0019" -> result = "61";
                    case "9996" -> result = "68";
                    case "0102" -> result = "26";
                    case "0103" -> result = "98";
                }

            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return "";
        }
        return result;
    }

    public static long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return -1;
        }
    }

    public static String convertResponseEnSecuredToCard(String value) {
        if (StringUtils.hasText(value)) {
            switch (value) {
                case "000" -> {
                    return "00";
                }
                case "111", "906" -> {
                    return "26";
                }
                case "114", "189", "908" -> {
                    return "14";
                }
                case "115" -> {
                    return "40";
                }
                case "116" -> {
                    return "51";
                }
                case "119" -> {
                    return "57";
                }
                case "121" -> {
                    return "61";
                }
                case "185" -> {
                    return "13";
                }
                case "190" -> {
                    return "03";
                }
                case "902" -> {
                    return "12";
                }
                case "904" -> {
                    return "30";
                }
                case "907" -> {
                    return "91";
                }
                case "909" -> {
                    return "96";
                }
                case "911" -> {
                    return "68";
                }
                case "913" -> {
                    return "94";
                }
                case "914", "916", "915", "917", "918", "919", "920", "921", "922", "923" -> {
                    return "36";
                }
                case "925" -> { //has change fee & interest
                    return "V0";
                }
                case "926" -> { //has change debt
                    return "V1";
                }
                default -> {
                    return "";
                }
            }
        }
        return "";
    }

    /**
     * @param format
     * @return String Time by format
     * example : "yyyy-MM-dd'T'HH:mm:ss"
     */
    public static String getTime(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(System.currentTimeMillis());
    }

    /**
     * @param data
     * @param defaultValue
     * @return if data != null then return data else return  defaultValue
     */
    public static String getDefaultValue(String data, String defaultValue) {
        return data != null ? data : defaultValue;
    }

}
