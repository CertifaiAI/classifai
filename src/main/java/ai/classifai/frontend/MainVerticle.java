package ai.classifai.frontend;

import ai.classifai.backend.application.*;
import ai.classifai.backend.data.DataService;
import ai.classifai.backend.repository.JDBCPoolHolder;
import ai.classifai.backend.repository.database.DbOps;
import ai.classifai.backend.repository.service.*;
import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.dto.TabularDTO;
import ai.classifai.core.dto.VideoDTO;
import ai.classifai.core.entity.annotation.AudioEntity;
import ai.classifai.core.entity.annotation.ImageEntity;
import ai.classifai.core.entity.annotation.TabularEntity;
import ai.classifai.core.entity.annotation.VideoEntity;
import ai.classifai.core.enumeration.RunningStatus;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.properties.audio.AudioProperties;
import ai.classifai.core.properties.image.ImageDTO;
import ai.classifai.core.properties.tabular.TabularProperties;
import ai.classifai.core.properties.video.VideoProperties;
import ai.classifai.core.service.NativeUI;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.annotation.ImageAnnotationService;
import ai.classifai.core.service.annotation.ImageDataRepository;
import ai.classifai.core.service.project.ProjectDataService;
import ai.classifai.core.service.project.ProjectLoadService;
import ai.classifai.core.service.project.ProjectRepository;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.core.utility.DbConfig;
import ai.classifai.core.utility.ParamConfig;
import ai.classifai.core.utility.ProjectImport;
import ai.classifai.frontend.request.ThumbnailProperties;
import ai.classifai.frontend.router.RouterService;
import ai.classifai.frontend.ui.ContainerUI;
import ai.classifai.frontend.ui.DesktopUI;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class MainVerticle extends AbstractVerticle {
    private final ProjectRepository projectRepoService;
    private final ImageDataRepository<ImageEntity, ImageDTO> imageRepoService;
    private final AnnotationRepository<VideoEntity, VideoDTO> videoRepoService;
    private final AnnotationRepository<AudioEntity, AudioDTO> audioRepoService;
    private final AnnotationRepository<TabularEntity, TabularDTO> tabularRepoService;
    private final ProjectService projectService;
    private final ProjectDataService projectDataService;
    private final ImageAnnotationService<ImageDTO, ThumbnailProperties> imageService;
    private final AnnotationService<VideoDTO, VideoProperties> videoService;
    private final AnnotationService<AudioDTO, AudioProperties> audioService;
    private final AnnotationService<TabularDTO, TabularProperties> tabularService;
    private final RouterService routerService;
    private final JDBCPoolHolder jdbcPoolHolder;
    private final NativeUI ui;
    private final ProjectLoadService projectLoadService;

    public MainVerticle(Vertx vertx) {
        jdbcPoolHolder = new JDBCPoolHolder(vertx, DbConfig.getH2());
        final ProjectImport projectImport = new ProjectImport(null);

        if(ParamConfig.isDockerEnv()) {
            ui = new ContainerUI();
        } else {
            ui = new DesktopUI(this::closeVerticles, projectImport);
        }
        final ProjectHandler projectHandler = new ProjectHandler(ui);
        this.projectRepoService = new ProjectRepoService(jdbcPoolHolder, projectHandler);
        this.projectService = new ProjectServiceImpl(projectRepoService, projectHandler);
        this.projectLoadService = new ProjectLoadServiceImpl(projectService, projectHandler);
        this.imageRepoService = new ImageRepoRepository(jdbcPoolHolder);
        this.videoRepoService = new VideoRepoService(jdbcPoolHolder);
        this.audioRepoService = new AudioRepoService(jdbcPoolHolder);
        this.tabularRepoService = new TabularRepoService(jdbcPoolHolder);
        this.imageService = new ImageService(imageRepoService, projectService, projectLoadService, projectHandler);
        this.videoService = new VideoService(videoRepoService, projectService);
        this.audioService = new AudioService(audioRepoService, projectService);
        this.tabularService = new TabularService(tabularRepoService, projectService);
        this.projectDataService = new DataService(imageService, videoService, audioService, tabularService);
        this.routerService = new RouterService(projectService, imageService, videoService, audioService,
                tabularService, projectDataService, projectHandler, ui);
    }

    @Override
    public void start(Promise<Void> promise)
    {
        ui.start();
        new DbOps(ui).configureDatabase();

        Promise<String> serverDeployment = Promise.promise();
        vertx.deployVerticle(routerService, serverDeployment);

        serverDeployment.future().onComplete(ar -> {
            if (ar.succeeded())
            {
                configProjectLoaderFromDB();

                printLogo();

                log.info("Classifai started successfully");
                log.info("Go on and open http://localhost:" + ParamConfig.getHostingPort());

                //docker environment not enabling welcome launcher
                if (!ParamConfig.isDockerEnv())
                {
                    try
                    {
                        ui.setRunningStatus(RunningStatus.RUNNING);
                    }
                    catch (Exception e)
                    {
                        log.info("Welcome Launcher failed to launch: ", e);
                    }
                }

                promise.complete();

            }
            else
            {
                promise.fail(ar.cause());
            }
        });

    }

    private void configProjectLoaderFromDB() {
        projectRepoService.configProjectLoaderFromDb()
                .onComplete(res -> {
                    if (res.succeeded()) {
                        loadAllDataPoint(res.result());
                    }
                });
    }

    private void loadAllDataPoint(Map<Integer, List<ProjectLoader>> annotationTypeProjectLoaderMap) {
        List<ProjectLoader> imageLoader = annotationTypeProjectLoaderMap.get(0);
        
        for (ProjectLoader loader : imageLoader) {
            imageRepoService.configProjectLoaderFromDb(loader);
        }
    }

    public void closeVerticles()
    {
        try
        {
            routerService.stop(Promise.promise());
        }
        catch (Exception e)
        {
            log.info("Error when stopping verticles: ", e);
        }
    }

    @Override
    public void stop(Promise<Void> promise) {
        jdbcPoolHolder.stop();
        vertx.close( r -> {if(r.succeeded()){
            log.info("Classifai close successfully");
        }});
    }

    public static void printLogo()
    {
        log.info("\n");
        log.info("   *********  ***          *****     *********  *********  *********  *********    *****    *********  ");
        log.info("   *********  ***        *********   ***        ***        *********  *********  *********  *********  ");
        log.info("   ***        ***        ***    ***  ***        ***           ***     ***        ***   ***     ***     ");
        log.info("   ***        ***        ***    ***  *********  *********     ***     *********  ***   ***     ***     ");
        log.info("   ***        ***        **********        ***        ***     ***     *********  *********     ***     ");
        log.info("   *********  *********  ***    ***        ***        ***  *********  ***        ***   ***  *********  ");
        log.info("   *********  *********  ***    ***  *********  *********  *********  ***        ***   ***  *********  ");
        log.info("\n");
    }
}
