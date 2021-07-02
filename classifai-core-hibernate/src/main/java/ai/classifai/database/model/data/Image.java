package ai.classifai.database.model.data;

import ai.classifai.database.model.Project;
import ai.classifai.database.model.dataVersion.ImageDataVersion;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "IMAGE")
public class Image extends Data
{
    @Column(name = "img_depth")
    private int depth;

    @Column(name = "img_width")
    private int width;

    @Column(name = "img_height")
    private int height;

    public Image(Project project, String dataPath, String checksum, long fileSize, int depth, int width, int height)
    {
        super(dataPath, checksum, fileSize, project);
        this.depth = depth;
        this.width = width;
        this.height = height;
        addNewDataVersion();
    }

    public Image() {}

    @Override
    public void addNewDataVersion()
    {
        ImageDataVersion dataVersion = new ImageDataVersion(this, this.getProject().getCurrentVersion());
        dataVersions.add(dataVersion);
    }


    // FIXME: hardcoded value
    public JsonObject loadImage(String thumbnail)
    {
        JsonObject jsonObj = new JsonObject();

        jsonObj.put("uuid", getDataId().toString());
        jsonObj.put("project_name", getProject().getProjectName());
        jsonObj.put("img_path", getFullPath());
        jsonObj.put("img_depth", depth);
        jsonObj.put("file_size", getFileSize());
        jsonObj.put("img_ori_w", width);
        jsonObj.put("img_ori_h", height);
        jsonObj.put("img_thumbnail", thumbnail);

        ImageDataVersion dataVersion = (ImageDataVersion) getCurrentDataVersion();

        jsonObj.put("bnd_box", dataVersion.getAnnotations()); //FIXME: key needed to be change
        jsonObj.put("img_x", dataVersion.getImgX());
        jsonObj.put("img_y", dataVersion.getImgY());
        jsonObj.put("img_w", dataVersion.getImgW());
        jsonObj.put("img_h", dataVersion.getImgH());

        return jsonObj;
    }
}
