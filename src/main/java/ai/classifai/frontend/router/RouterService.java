package ai.classifai.frontend.router;

import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.dto.TabularDTO;
import ai.classifai.core.dto.VideoDTO;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.properties.audio.AudioProperties;
import ai.classifai.core.properties.image.ImageDTO;
import ai.classifai.core.properties.tabular.TabularProperties;
import ai.classifai.core.properties.video.VideoProperties;
import ai.classifai.core.service.NativeUI;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.annotation.ImageAnnotationService;
import ai.classifai.core.service.project.ProjectDataService;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.core.utility.ParamConfig;
import ai.classifai.frontend.api.*;
import ai.classifai.frontend.request.ThumbnailProperties;
import com.zandero.rest.RestRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class RouterService extends AbstractVerticle {
    private final ProjectService projectService;
    private final ImageAnnotationService<ImageDTO, ThumbnailProperties> imageService;
    private final AnnotationService<VideoDTO, VideoProperties> videoService;
    private final AnnotationService<AudioDTO, AudioProperties> audioService;
    private final AnnotationService<TabularDTO, TabularProperties> tabularService;
    private final ProjectDataService projectDataService;
    private final ProjectHandler projectHandler;
    private final NativeUI ui;
    private ProjectController projectController;
    private ImageController imageController;
    private VideoController videoController;
    private AudioController audioController;
    private TabularController tabularController;

    public RouterService(
            ProjectService projectService,
            ImageAnnotationService<ImageDTO, ThumbnailProperties> imageService,
            AnnotationService<VideoDTO, VideoProperties> videoService,
            AnnotationService<AudioDTO, AudioProperties> audioService,
            AnnotationService<TabularDTO, TabularProperties> tabularService,
            ProjectDataService projectDataService,
            ProjectHandler projectHandler,
            NativeUI ui
    ) {
        this.projectService = projectService;
        this.imageService = imageService;
        this.videoService = videoService;
        this.audioService = audioService;
        this.tabularService = tabularService;
        this.projectDataService = projectDataService;
        this.projectHandler = projectHandler;
        this.ui = ui;
    }

    private void configureEndpoints() {
        this.projectController = new ProjectController(projectService, projectDataService, projectHandler, ui);
        this.imageController = new ImageController(imageService, projectHandler);
        this.videoController = new VideoController(videoService, projectHandler);
        this.audioController = new AudioController(audioService, projectHandler);
        this.tabularController = new TabularController(tabularService, projectHandler);
    }

    private void addNoCacheHeader(RoutingContext ctx)
    {
        ctx.response().headers().add("Cache-Control", "no-cache");
        ctx.next();
    }

    private void enableDevelopmentCORS(Router router) {
        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("Access-Control-Allow-Method");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("Cache-Control");
        allowedHeaders.add("Pragma");
        allowedHeaders.add("Content-Type");
        RestRouter.enableCors(router, "*", false, -1, allowedHeaders);
    }

    @Override
    public void start(Promise<Void> promise) {
        configureEndpoints();

        Router router = RestRouter.register(vertx, projectController, imageController, videoController, audioController, tabularController);

        // Only enable in development
        enableDevelopmentCORS(router);

        router.route().handler(this::addNoCacheHeader);
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer()
                .requestHandler(router)
                .exceptionHandler(Throwable::printStackTrace)
                .listen(ParamConfig.getHostingPort(), r -> {

                    if (r.succeeded())
                    {
                        promise.complete();
                    }
                    else {
                        log.debug("Failure in creating HTTPServer in ServerVerticle. " + r.cause().getMessage());
                        promise.fail(r.cause());
                    }
                });
    }

    @Override
    public void stop(Promise<Void> promise) {
        log.debug("Endpoint Router Verticle stopping...");

    }
}
