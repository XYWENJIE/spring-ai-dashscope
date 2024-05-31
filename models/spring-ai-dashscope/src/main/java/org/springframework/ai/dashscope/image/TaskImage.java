package org.springframework.ai.dashscope.image;

import org.springframework.ai.model.ModelResult;
import org.springframework.ai.model.ResultMetadata;

public class TaskImage implements ModelResult<String> {
    @Override
    public String getOutput() {
        return "";
    }

    @Override
    public ResultMetadata getMetadata() {
        return null;
    }
}
