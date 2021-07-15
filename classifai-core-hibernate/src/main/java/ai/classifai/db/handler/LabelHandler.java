package ai.classifai.db.handler;

import ai.classifai.db.entities.LabelEntity;
import ai.classifai.db.entities.ProjectEntity;
import ai.classifai.db.entities.VersionEntity;

import java.util.List;
import java.util.stream.Collectors;

public class LabelHandler {
    public List<LabelEntity> getLabelList(ProjectEntity project, List<String> strLabelList)
    {
        return null;
//        List<LabelEntity> currentLabelEntityList = project.getCurrentVersionEntity().getLabelList();
//
//        List<LabelEntity> newLabelEntityList = labelListFromStringList(strLabelList, project.getCurrentVersionEntity());
//
//        return newLabelEntityList.stream()
//                .map(label -> mergeLabelIntoList(currentLabelEntityList, label))
//                .collect(Collectors.toList());
    }

    //
    private LabelEntity mergeLabelIntoList(List<LabelEntity> currentLabelEntityList, LabelEntity labelEntity)
    {
        int idx = currentLabelEntityList.indexOf(labelEntity);

        return idx == -1 ? labelEntity : currentLabelEntityList.get(idx);
    }

    public List<LabelEntity> getDeleteList(ProjectEntity project, List<String> strLabelList)
    {
        return null;
//        List<LabelEntity> currentLabelEntityList = project.getCurrentVersionEntity().getLabelList();
//
//        List<LabelEntity> newLabelEntityList = labelListFromStringList(strLabelList, project.getCurrentVersionEntity());
//
//        return currentLabelEntityList.stream()
//                .filter(label -> !newLabelEntityList.contains(label))
//                .collect(Collectors.toList());
    }


    public static List<LabelEntity> labelListFromStringList(List<String> list, VersionEntity versionEntity)
    {
        return null;
//        return list.stream()
//                .map(string -> new LabelEntity(string, versionEntity))
//                .collect(Collectors.toList());
    }

    public static List<LabelEntity> getLabelListFromVersionList(List<VersionEntity> versionEntityList)
    {
        return null;
//        return versionEntityList.stream()
//                .map(VersionEntity::getLabelEntityList)
//                .flatMap(List::stream)
//                .collect(Collectors.toList());
    }

    public static List<String> getStringListFromLabelList(List<LabelEntity> labelEntityList)
    {
        return labelEntityList.stream()
                .map(LabelEntity::toString)
                .collect(Collectors.toList());
    }

}
