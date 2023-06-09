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

package org.apache.dubbo.samples.seata;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.service.StorageService;

import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

@DubboService(timeout = 10000)
public class StorageServiceImpl implements StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private JdbcTemplate jdbcTemplate;

    /**
     * Set jdbc template.
     *
     * @param jdbcTemplate the jdbc template
     */
    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void deduct(String commodityCode, int count) {
        LOGGER.info("Storage Service Begin ... xid: " + RootContext.getXID());
        LOGGER.info("Deducting inventory SQL: update storage_tbl set count = count - {} where commodity_code = {}",
                count, commodityCode);

        jdbcTemplate.update("update storage_tbl set count = count - ? where commodity_code = ?",
                new Object[]{count, commodityCode});
        LOGGER.info("Storage Service End ... ");

    }

    @Override
    public int queryCount(String commodityCode) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet("select count from storage_tbl where commodity_code = ?",
                commodityCode);
        if (rowSet.next()) {
            int count = rowSet.getInt("count");
            LOGGER.info("Storage has " + count + " for " + commodityCode);
            return count;
        }
        return -1;
    }
}
