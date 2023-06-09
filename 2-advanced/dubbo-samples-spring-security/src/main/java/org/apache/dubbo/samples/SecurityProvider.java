/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dubbo.samples;

import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.dubbo.samples.zookeeper.EmbeddedZooKeeper;
import org.apache.dubbo.samples.zookeeper.ZKTools;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.concurrent.CountDownLatch;

public class SecurityProvider {

    public static void main(String[] args) throws Exception {
        new EmbeddedZooKeeper(2181, false).start();
        ZKTools.generateDubboProperties();

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                ProviderConfiguration.class);

        context.start();

        System.out.println("dubbo service started");
        new CountDownLatch(1).await();
    }

    @Configuration
    @EnableDubbo(scanBasePackages = "org.apache.dubbo.samples.service")
    @PropertySource("classpath:/spring/dubbo-provider.properties")
    @EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true)
    static public class ProviderConfiguration {
        @Bean
        public ProviderConfig providerConfig() {
            ProviderConfig providerConfig = new ProviderConfig();
            providerConfig.setTimeout(1000);
            return providerConfig;
        }
    }
}
