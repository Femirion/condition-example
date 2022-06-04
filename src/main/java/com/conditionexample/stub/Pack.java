package com.conditionexample.stub;

import java.util.Collections;
import java.util.List;

public class Pack {

    public List<StampLink> getStampLinks() {
        return Collections.emptyList();
    }

    public PackState getState() {
        return PackState.GOOD;
    }
}
