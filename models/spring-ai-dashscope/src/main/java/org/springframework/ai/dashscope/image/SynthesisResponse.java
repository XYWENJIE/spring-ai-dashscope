package org.springframework.ai.dashscope.image;

import org.springframework.ai.model.ModelResponse;
import org.springframework.ai.model.ResponseMetadata;

import java.util.List;

public class SynthesisResponse implements ModelResponse<TaskImage> {
    @Override
    public TaskImage getResult() {
        return null;
    }

    @Override
    public List<TaskImage> getResults() {
        return List.of();
    }

    @Override
    public ResponseMetadata getMetadata() {
        return null;
    }
}
