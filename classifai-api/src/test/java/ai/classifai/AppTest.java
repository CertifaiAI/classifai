/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
 */

package ai.classifai;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Unit test for IntellibelApp
 */
@Slf4j
@RunWith(VertxUnitRunner.class)
public class AppTest
{
    protected static Vertx vertx;
    protected static Integer port;
    protected static WebClient webClient;

    @BeforeClass
    public static void setUp(TestContext tc) {


    }

    @AfterClass
    public static void testDown(TestContext tc)
    {
        //Stop the the Vertx instance and release any resources held by it.
        vertx.close( r -> {if(r.succeeded()){tc.asyncAssertSuccess();}});
    }

    @Test
    public void checkStatusCodeSuccess(TestContext context)
    {

    }
}
