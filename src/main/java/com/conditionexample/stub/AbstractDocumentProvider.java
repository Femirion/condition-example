package com.conditionexample.stub;

import java.util.Collections;
import java.util.List;

public class AbstractDocumentProvider {

    // не стал делать дженерик тк не знаю почему автор их не использует, может из библиотеки приходит не дженери лист
    public List getStampCodes(Pack pack, StampLinkState stampLinkState) {
        return Collections.emptyList();
    }

    public List getAggregationCodes(Pack pack, StampLinkState stampLinkState) {
        return Collections.emptyList();
    }

    public boolean containsCode(List codes, String imageCode, boolean isSingleMode) {
        return false;
    }

}
