pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = "https://artifactory.img.ly/artifactory/imgly")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://maven.aliyun.com/repository/jcenter")
        maven(url = "https://jcenter.bintray.com")
        maven { setUrl("https://jitpack.io") }
        maven(url = "https://artifactory.img.ly/artifactory/imgly")
        mavenLocal()
    }
}

rootProject.name = "Simple-Gallery"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")