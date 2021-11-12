/*
 * Copyright (c) 2020-2021 CertifAI
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and itations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.classifai.core.util.datetime;

import ai.classifai.core.util.ParamConfig;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date Time
 *
 * @author codenamewei
 */
@Slf4j
@Getter
public class DateTime {

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern(ParamConfig.getDateTimeFormat());

    private String dateTime;

    private Integer year;
    private Integer month;
    private Integer day;

    private Integer hour;
    private Integer minute;
    private Integer second;

    public DateTime(@NonNull String inputDateTime)
    {
        dateTime = inputDateTime;
        formatDateTime();
    }

    public DateTime()
    {
        LocalDateTime now = LocalDateTime.now();
        String buffer = dtf.format(now);

        dateTime = buffer;

        formatDateTime();
    }

    private void formatDateTime()
    {
        this.year = Integer.parseInt(dateTime.substring(0, 4));
        this.month = Integer.parseInt(dateTime.substring(5, 7));
        this.day = Integer.parseInt(dateTime.substring(8, 10));

        this.hour = Integer.parseInt(dateTime.substring(11, 13));
        this.minute = Integer.parseInt(dateTime.substring(14, 16));
        this.second = Integer.parseInt(dateTime.substring(17, 19));
    }


    public String toString()
    {
        return dateTime;
    }

}
