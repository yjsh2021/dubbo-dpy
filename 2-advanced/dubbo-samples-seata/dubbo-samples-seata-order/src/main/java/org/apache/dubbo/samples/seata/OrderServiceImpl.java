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

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.Order;
import org.apache.dubbo.samples.service.AccountService;
import org.apache.dubbo.samples.service.OrderService;

import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;

@DubboService(timeout = 10000)
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    @DubboReference
    private AccountService accountService;

    private JdbcTemplate jdbcTemplate;

    @Override
    public Order create(String userId, String commodityCode, int orderCount) {
        LOGGER.info("Order Service Begin ... xid: " + RootContext.getXID());

        // calculate payment
        int orderMoney = calculate(commodityCode, orderCount);

        // debit from the account
        accountService.debit(userId, orderMoney);

        final Order order = new Order();
        order.userId = userId;
        order.commodityCode = commodityCode;
        order.count = orderCount;
        order.money = orderMoney;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        LOGGER.info("Order Service SQL: insert into order_tbl (user_id, commodity_code, count, money) values ({}, {}, {}, {})", userId, commodityCode, orderCount, orderMoney);

        jdbcTemplate.update(con -> {
            PreparedStatement pst = con.prepareStatement(
                    "insert into order_tbl (user_id, commodity_code, count, money) values (?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setObject(1, order.userId);
            pst.setObject(2, order.commodityCode);
            pst.setObject(3, order.count);
            pst.setObject(4, order.money);
            return pst;
        }, keyHolder);

        order.id = keyHolder.getKey().longValue();

        LOGGER.info("Order Service End ... Created " + order);

        return order;
    }

    /**
     * Sets jdbc template.
     *
     * @param jdbcTemplate the jdbc template
     */
    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private int calculate(String commodityId, int orderCount) {
        return 200 * orderCount;
    }

}
