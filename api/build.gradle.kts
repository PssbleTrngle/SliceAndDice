plugins {
    id("com.possible-triangle.common")
}

dependencies {
    val neoforge_version: String by project.extra
    implementation("net.neoforged:neoforge:$neoforge_version")
}
