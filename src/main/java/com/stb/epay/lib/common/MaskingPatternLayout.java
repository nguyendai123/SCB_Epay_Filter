package com.stb.epay.lib.common;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class MaskingPatternLayout extends PatternLayout {

    private Pattern maskMultilinePattern;
    private Pattern hideMultilinePattern;
    private final List<String> maskPatterns = new ArrayList<>();
    private final List<String> hidePatterns = new ArrayList<>();

    public void addMaskPattern(String maskPattern) {
        maskPatterns.add(maskPattern);
        maskMultilinePattern = Pattern.compile(String.join("|", maskPatterns), Pattern.MULTILINE);
    }

    public void addHidePattern(String hidePattern) {
        hidePatterns.add(hidePattern);
        hideMultilinePattern = Pattern.compile(String.join("|", hidePatterns), Pattern.MULTILINE);
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        return maskMessage(super.doLayout(event));
    }

    private String maskMessage(String message) {
        if (maskMultilinePattern == null && hideMultilinePattern == null) {
            return message;
        }

        message = message.replaceAll("\\\\","");

        StringBuilder sb = new StringBuilder(message);
        if (maskMultilinePattern != null) {
            Matcher matcher = maskMultilinePattern.matcher(sb);
            while (matcher.find()) {
                IntStream.rangeClosed(1, matcher.groupCount()).forEach(group -> {
                    if (matcher.group(group) != null) {
                        IntStream.range(matcher.start(group), matcher.end(group)).forEach(i -> sb.setCharAt(i, '*'));
                    }
                });
            }
        }
        return handleHide(sb);
    }
    private String handleHide(StringBuilder sb) {
        if (hideMultilinePattern != null) {
            Matcher matcher = hideMultilinePattern.matcher(sb);
            List<String> list = new ArrayList<>();
            while (matcher.find()) {
                IntStream.rangeClosed(1, matcher.groupCount()).forEach(group -> {
                    if (StringUtils.isNotBlank(matcher.group(group))) {
                        list.add(matcher.group(group));
                    }
                });
            }
            String message = sb.toString();
            for (String data : list){
                message = message.replace(data, "DONT SHOW");
            }
            return message;
        }
        return sb.toString();
    }
}
