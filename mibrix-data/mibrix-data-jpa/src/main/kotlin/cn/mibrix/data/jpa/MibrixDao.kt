/*
 * Copyright (C) 2026 shareloke
 * All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * Project: mibrix
 */
package cn.mibrix.data.jpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

/**
 *
 *
 * @author shareloke
 * @date 2026-10-06
 */
@Suppress("unused")
@Component
class MibrixDao(
    @field:PersistenceContext
    private val entityManager: EntityManager,
    private val jdbcTemplate: JdbcTemplate,
)
