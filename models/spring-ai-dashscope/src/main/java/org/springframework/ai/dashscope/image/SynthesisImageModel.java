package org.springframework.ai.dashscope.image;

import org.springframework.ai.model.Model;

public interface SynthesisImageModel extends Model<SynthesisImagePrompt,SynthesisResponse> {


    void queryTask(String taskId);

}
