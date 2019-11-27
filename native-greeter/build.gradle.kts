plugins {
    `cpp-library`
}

library {
    linkage.set(listOf(Linkage.STATIC))
    binaries.configureEach {
        compileTask.get().compilerArgs.addAll(compileTask.get().toolChain.map {
            if (it is Gcc || it is Clang) listOf("--std=c++11")
            else emptyList()
        })
    }
}
