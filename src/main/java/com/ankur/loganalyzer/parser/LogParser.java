package com.ankur.loganalyzer.parser;

import com.ankur.loganalyzer.model.ParsedLogEvent;

public interface LogParser {

    boolean supports(String rawLog);

    ParsedLogEvent.ParsedLogEventBuilder parse(String rawLog);
}
