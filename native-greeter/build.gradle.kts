plugins {
    `cpp-library`
}

library {
    linkage.set(listOf(Linkage.STATIC))
}
