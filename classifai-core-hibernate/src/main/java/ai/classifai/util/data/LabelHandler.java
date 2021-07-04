package ai.classifai.util.data;

import ai.classifai.database.model.Label;
import ai.classifai.database.model.Project;

import java.util.List;
import java.util.stream.Collectors;

public class LabelHandler {
    public List<Label> getLabelList(Project project, List<String> strLabelList)
    {
        List<Label> currentLabelList = project.getCurrentVersion().getLabelList();

        List<Label> newLabelList = Label.labelListFromStringList(strLabelList, project.getCurrentVersion());

        return newLabelList.stream()
                .map(label -> mergeLabelList(currentLabelList, label))
                .collect(Collectors.toList());
    }

    private Label mergeLabelList(List<Label> currentLabelList, Label label)
    {
        int idx = currentLabelList.indexOf(label);

        return idx == -1 ? label : currentLabelList.get(idx);
    }
}
