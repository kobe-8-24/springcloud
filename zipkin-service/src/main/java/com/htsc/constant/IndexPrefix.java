package com.htsc.constant;

import java.util.stream.Stream;

public interface IndexPrefix {
    String SW_SEGMENT_SELF_FRONT = "sw_segment_self_front-";
    String SW_SEGMENT_PAGE_TRACE_RELATION = "sw_segment_page_trace_relation-";
    String SW_SEGMENT_FULL_LINK = "sw_segment_full_link-";

    static Stream<String> getAllIndexPrefix() {
        return Stream.of(SW_SEGMENT_SELF_FRONT, SW_SEGMENT_PAGE_TRACE_RELATION, SW_SEGMENT_FULL_LINK);
    }
}
