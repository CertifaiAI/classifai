package ai.classifai.database.handler;

import ai.classifai.database.model.Label;
import ai.classifai.database.model.Project;
import ai.classifai.database.model.Version;

import java.util.List;
import java.util.stream.Collectors;

public class LabelHandler {
    public List<Label> getLabelList(Project project, List<String> strLabelList)
    {
        List<Label> currentLabelList = project.getCurrentVersion().getLabelList();

        List<Label> newLabelList = labelListFromStringList(strLabelList, project.getCurrentVersion());

        return newLabelList.stream()
                .map(label -> mergeLabelIntoList(currentLabelList, label))
                .collect(Collectors.toList());
    }

    //
    private Label mergeLabelIntoList(List<Label> currentLabelList, Label label)
    {
        int idx = currentLabelList.indexOf(label);

        return idx == -1 ? label : currentLabelList.get(idx);
    }

    public List<Label> getDeleteList(Project project, List<String> strLabelList)
    {
        List<Label> currentLabelList = project.getCurrentVersion().getLabelList();

        List<Label> newLabelList = labelListFromStringList(strLabelList, project.getCurrentVersion());

        return currentLabelList.stream()
                .filter(label -> !newLabelList.contains(label))
                .collect(Collectors.toList());
    }


    public static List<Label> labelListFromStringList(List<String> list, Version version)
    {
        return list.stream()
                .map(string -> new Label(string, version))
                .collect(Collectors.toList());
    }

    public static List<Label> getLabelListFromVersionList(List<Version> versionList)
    {
        return versionList.stream()
                .map(Version::getLabelList)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public static List<String> getStringListFromLabelList(List<Label> labelList)
    {
        return labelList.stream()
                .map(Label::toString)
                .collect(Collectors.toList());
    }

}
