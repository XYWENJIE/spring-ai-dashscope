package org.springframework.ai.dashscope.image;

import org.springframework.ai.model.ModelOptions;
import org.springframework.ai.model.ModelRequest;

public class SynthesisImagePrompt implements ModelRequest<Object> {
    @Override
    public Object getInstructions() {
        return null;
    }

    @Override
    public ModelOptions getOptions() {
        return null;
    }
}
