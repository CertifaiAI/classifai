package ai.classifai.backend.data;

import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.dto.TabularDTO;
import ai.classifai.core.dto.VideoDTO;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.properties.audio.AudioProperties;
import ai.classifai.core.properties.image.ImageDTO;
import ai.classifai.core.properties.tabular.TabularProperties;
import ai.classifai.core.properties.video.VideoProperties;
import ai.classifai.core.service.annotation.*;
import ai.classifai.core.service.project.ProjectDataService;
import ai.classifai.frontend.request.ThumbnailProperties;
import io.vertx.core.Future;
import lombok.NonNull;

public class DataService implements ProjectDataService {

    private final ImageAnnotationService<ImageDTO, ThumbnailProperties> imageService;
    private final VideoAnnotationService<VideoDTO, VideoProperties> videoService;
    private final AudioAnnotationService<AudioDTO, AudioProperties> audioService;
    private final TabularAnnotationService<TabularDTO, TabularProperties> tabularService;

    public DataService(ImageAnnotationService<ImageDTO, ThumbnailProperties> imageService,
                       VideoAnnotationService<VideoDTO, VideoProperties> videoService,
                       AudioAnnotationService<AudioDTO, AudioProperties> audioService,
                       TabularAnnotationService<TabularDTO, TabularProperties> tabularService)
    {
        this.imageService = imageService;
        this.videoService = videoService;
        this.audioService = audioService;
        this.tabularService = tabularService;
    }

    @Override
    public Future<ProjectLoader> parseFileData(@NonNull ProjectDTO projectDTO) throws Exception {
        AnnotationType type = AnnotationType.get(projectDTO.getAnnotationType());

        if (type.equals(AnnotationType.IMAGEBOUNDINGBOX) || type.equals(AnnotationType.IMAGESEGMENTATION)) {
            return parseImageData(projectDTO);
        }
        else if (type.equals(AnnotationType.VIDEOBOUNDINGBOX) || type.equals(AnnotationType.VIDEOSEGMENTATION)) {
            return parseVideoData(projectDTO);
        }
        else if (type.equals(AnnotationType.AUDIO)) {
            return parseAudioData(projectDTO);
        }
        else if (type.equals(AnnotationType.TABULAR)) {
            return parseTabularData(projectDTO);
        }
        else {
            throw new IllegalArgumentException("AnnotationType not found.");
        }
    }

    @Override
    public Future<Void> deleteProject(@NonNull ProjectDTO projectDTO) {
        AnnotationType type = AnnotationType.get(projectDTO.getAnnotationType());

        if (type.equals(AnnotationType.IMAGEBOUNDINGBOX) || type.equals(AnnotationType.IMAGESEGMENTATION)) {
            return deleteImageProject(projectDTO);
        }
        else if (type.equals(AnnotationType.VIDEOBOUNDINGBOX) || type.equals(AnnotationType.VIDEOSEGMENTATION)) {
            return deleteVideoProject(projectDTO);
        }
        else if (type.equals(AnnotationType.AUDIO)) {
            return deleteAudioProject(projectDTO);
        }
        else if (type.equals(AnnotationType.TABULAR)) {
            return deleteTabularProject(projectDTO);
        }
        else {
            throw new IllegalArgumentException("AnnotationType not found.");
        }
    }

    private Future<ProjectLoader> parseImageData(ProjectDTO projectDTO) throws Exception {
        return imageService.createAnnotationProject(projectDTO);
    }

    private Future<ProjectLoader> parseVideoData(ProjectDTO projectDTO) throws Exception {
        return videoService.createAnnotationProject(projectDTO);
    }

    private Future<ProjectLoader> parseAudioData(ProjectDTO projectDTO) throws Exception {
        return audioService.createAnnotationProject(projectDTO);
    }

    private Future<ProjectLoader> parseTabularData(ProjectDTO projectDTO) throws Exception {
        return tabularService.createAnnotationProject(projectDTO);
    }

    private Future<Void> deleteImageProject(ProjectDTO projectDTO) {
        return imageService.deleteProjectById(projectDTO);
    }

    private Future<Void> deleteVideoProject(ProjectDTO projectDTO) {
        return null;
    }

    private Future<Void> deleteAudioProject(ProjectDTO projectDTO) {
        return audioService.deleteProjectById(projectDTO);
    }

    private Future<Void> deleteTabularProject(ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public Future<ProjectLoaderStatus> loadProject(@NonNull ProjectLoader projectLoader) {
        AnnotationType annotationType = AnnotationType.get(projectLoader.getAnnotationType());

        if (annotationType.equals(AnnotationType.IMAGEBOUNDINGBOX) || annotationType.equals(AnnotationType.IMAGESEGMENTATION)) {
            return loadImageProject(projectLoader);
        }
        else if (annotationType.equals(AnnotationType.VIDEOBOUNDINGBOX) || annotationType.equals(AnnotationType.VIDEOSEGMENTATION)) {
            return loadVideoProject(projectLoader);
        }
        else if (annotationType.equals(AnnotationType.AUDIO)) {
            return loadAudioProject(projectLoader);
        }
        else if (annotationType.equals(AnnotationType.TABULAR)) {
            return loadTabularProject(projectLoader);
        }
        else {
            throw new IllegalArgumentException("AnnotationType not found.");
        }
    }

    private Future<ProjectLoaderStatus> loadImageProject(ProjectLoader projectLoader) {
        return imageService.loadProject(projectLoader);
    }

    private Future<ProjectLoaderStatus> loadVideoProject(@NonNull ProjectLoader projectLoader) {
        return videoService.loadProject(projectLoader);
    }

    private Future<ProjectLoaderStatus> loadTabularProject(@NonNull ProjectLoader projectLoader) {
        return tabularService.loadProject(projectLoader);
    }

    private Future<ProjectLoaderStatus> loadAudioProject(@NonNull ProjectLoader projectLoader) {
        return audioService.loadProject(projectLoader);
    }

}
