package ai.classifai.backend.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class JsonUtility<T> {
    private List<T> parseJsonToList(String jsonList) throws JsonProcessingException {
        return new ObjectMapper().readValue(jsonList, new TypeReference<>() {});
    }

    private String writeJsonString(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }
}
