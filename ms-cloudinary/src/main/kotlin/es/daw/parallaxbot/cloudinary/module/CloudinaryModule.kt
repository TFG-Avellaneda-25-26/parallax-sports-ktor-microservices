package es.daw.parallaxbot.cloudinary.module

import es.daw.parallaxbot.cloudinary.config.configureCloudinary
import es.daw.parallaxbot.cloudinary.service.CloudinaryService
import es.daw.parallaxbot.common.config.cloudinaryConfigModule
import es.daw.parallaxbot.common.config.networkModule
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val cloudinaryModule = module {
    singleOf(::configureCloudinary)
    singleOf(::CloudinaryService)
    includes(cloudinaryConfigModule, networkModule)
}