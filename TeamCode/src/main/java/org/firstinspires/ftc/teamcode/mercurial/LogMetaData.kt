package dev.frozenmilk.dairy.mercurial.ftc

import dev.frozenmilk.dairy.MercurialBuildMetaData
import dev.frozenmilk.sinister.loading.Preload
import dev.frozenmilk.sinister.util.log.Logger

@Preload
object LogMetaData {
    init {
        Logger.d("Meta", """
name: ${MercurialBuildMetaData.name}
version: ${MercurialBuildMetaData.version}
ref: ${MercurialBuildMetaData.gitRef}
""")
    }
}
