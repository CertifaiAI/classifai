package ai.classifai.backend.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class JsonUtility<T> {
    public static <T> List<T> parseJsonToList(String jsonList) throws JsonProcessingException {
        return new ObjectMapper().readValue(jsonList, new TypeReference<>() {});
    }

    public String writeJsonString(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }
}
