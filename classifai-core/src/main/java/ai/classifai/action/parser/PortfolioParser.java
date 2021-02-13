package ai.classifai.action.parser;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class PortfolioParser
{
    private Integer projectID;
    private String projectName;
    private String annotationType;
    private String labelList;
    private Integer uuidGeneratorSeed;

    private String uuidList;

    private boolean isNew;
    private boolean isStar;

    private String createdDate;
}
