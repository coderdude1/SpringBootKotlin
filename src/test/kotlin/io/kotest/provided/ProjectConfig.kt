package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

/**
 * Global config options for all kotests
 *
 * https://kotest.io/docs/framework/project-config.html
 * https://kotest.io/docs/extensions/spring.html
 *
 */
class ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringExtension)
}

