package ai.classifai.util.data;


import ai.classifai.data.type.image.ImageFileType;
import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.database.wasabis3.WasabiProject;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.util.type.AnnotationHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Wasabi S3 Handler
 *
 * @author codenamewei
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WasabiHandler
{
    public static void retrieveObjectsInBucket(@NonNull ProjectLoader loader)
    {
        loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_LOADING_FILES);

        WasabiProject project = loader.getWasabiProject();

        ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(project.getWasabiBucket())
                .build();

        ListObjectsV2Iterable response = project.getWasabiS3Client().listObjectsV2Paginator(req);

        List<Object> dataPaths = new ArrayList<>();

        for (ListObjectsV2Response page : response)
        {
            page.contents().forEach((S3Object object) ->
            {
                String inputObject = object.key();

                if(FileHandler.isfileSupported(inputObject, ImageFileType.getImageFileTypes()))
                {
                    System.out.println("Path: " + inputObject);
                    dataPaths.add(inputObject);
                }
            });
        }

        ImageHandler.saveToProjectTable(loader, dataPaths);
    }
}
