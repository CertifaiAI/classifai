package ai.classifai.service.generic;

import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.service.generic.FileService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.commons.io.IOUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LabelService extends FileService
{
    public LabelService(Vertx vertx)
    {
        super(vertx);
    }

    public List<String> getLabelStringListFromString(String newLabelListStr)
    {
        return Arrays.stream(newLabelListStr.substring(1, newLabelListStr.length() - 1)
                .replaceAll("\\s", "")
                .split(","))
                .filter(string -> string.length() > 0)
                .collect(Collectors.toList());
    }

    public Future<List<Label>> getToDeleteLabelListFuture(List<Label> currentLabelList, List<String> newLabelList)
    {
        return Future.succeededFuture(currentLabelList.stream()
                    .filter(label -> !newLabelList.contains(label.getName()))
                    .collect(Collectors.toList()));
    }

    public Future<List<LabelDTO>> getToAddLabelDTOListFuture(List<Label> currentLabelList, List<String> newLabelStrList)
    {
        return vertx.executeBlocking(promise ->
        {
            List<String> currentLabelStringList = currentLabelList.stream()
                    .map(Label::getName)
                    .collect(Collectors.toList());

            promise.complete(newLabelStrList.stream()
                    .filter(labelString -> !currentLabelStringList.contains(labelString))
                    .map(labelString -> LabelDTO.builder()
                            .name(labelString)
                            .build())
                    .collect(Collectors.toList()));
        });
    }

    public Future<List<LabelDTO>> getLabelDtoList(String labelFilePath)
    {
        return vertx.executeBlocking(promise ->
        {
            try
            {
                if (labelFilePath.length() == 0)
                {
                    promise.complete(new ArrayList<>());
                    return;
                }

                List<String> labelStringList = getLabelStringList(labelFilePath);

                promise.complete(labelStringList.stream()
                        .map(labelString -> LabelDTO.builder()
                                .name(labelString)
                                .build())
                        .collect(Collectors.toList())
                );
            }
            catch (IOException e)
            {
                promise.fail(String.format("Failed to import label file : %s.%n%s", labelFilePath, e.getMessage()));
            }
        });
    }

    private List<String> getLabelStringList(String labelFilePath) throws IOException
    {
        String[] labelStringArr = IOUtils.toString(new FileReader(labelFilePath))
                .split("\n");

        return Arrays.stream(labelStringArr)
                .map(labelString -> labelString.replaceAll("\\s", ""))
                .filter(labelString -> labelString.length() > 0)
                .collect(Collectors.toList());
    }


}
