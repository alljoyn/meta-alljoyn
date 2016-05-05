SUMMARY = "Alljoyn framework core."
DESCRIPTION = "Alljoyn is an Open Source framework that makes it easy for devices and apps to discover and securely communicate with each other."
AUTHOR = "Herve Jourdain <herve.jourdain@beechwoods.com>"
HOMEPAGE = "https://www.allseenalliance.org/"
SECTION = "libs"
LICENSE = "ISC"
DEPENDS = "openssl libxml2 libcap"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/ISC;md5=f3b90e78ea0cffb20bf5cca7947a896d"

S = "${WORKDIR}/${PN}"
SRC_URI = "git://git.allseenalliance.org/gerrit/core/alljoyn.git;protocol=https;branch=RB${PV};destsuffix=alljoyn/core/alljoyn"
SRCREV = "${AUTOREV}"

ALLJOYN_BINDINGS ?= "cpp"
ALLJOYN_CORE_BIN ?= " \
                      aclient \
                      advtunnel \
                      aes_ccm \
                      ajxmlcop \
                      alljoyn-daemon \
                      aservice \
                      bastress \
                      bastress2 \
                      bbclient \
                      bbjitter \
                      bbjoin \
                      bbservice \
                      bbsig \
                      bbsigtest \
                      bignum \
                      eventsactionservice \
                      init \
                      marshal \
                      names \
                      ns \
                      propstresstest \
                      proptester \
                      rawclient \
                      rawservice \
                      remarshal \
                      sessions \
                      socktest \
                      srp \
                      unpack \
                    "
ALLJOYN_CORE_SAMPLES ?= " \
                          AboutListener \
                          DeskTopSharedKSClient1 \
                          DeskTopSharedKSClient2 \
                          DeskTopSharedKSService \
                          SampleCertificateUtility \
                          SampleClientECDHE \
                          sample_rule_app \
                          SampleServiceECDHE \
                          SecureDoorConsumer \
                          SecureDoorProvider \
                        "
ALLJOYN_BINDIR ?= "/opt/${PN}/bin"

inherit scons

PACKAGES = " \
             ${PN}-dbg \
             ${PN} \
             ${PN}-staticdev \
             ${PN}-dev \
             ${PN}-doc \
           "

do_compile() {
# For _class-target and _class-nativesdk
    export TARGET_CC="${CC}"
    export TARGET_CXX="${CXX}"
    export TARGET_CFLAGS="${CFLAGS}"
    export TARGET_CPPFLAGS="${CPPFLAGS}"
# Path to toolchain already in the PATH variable!
    export TARGET_PATH="${PATH}"
    export TARGET_LINKFLAGS="${LDFLAGS}"
    export TARGET_LINK="${CCLD}"
    export TARGET_AR="${AR}"
    export TARGET_RANLIB="${RANLIB}"
    export STAGING_DIR="${STAGING_DIR_TARGET}"
    cd ${S}/core/${PN}
    scons OS=openwrt CPU=openwrt DOCS=html CRYPTO=openssl BINDINGS=${ALLJOYN_BINDINGS} OE_BASE=/usr WS=off VARIANT=debug
    unset TARGET_CC
    unset TARGET_CXX
    unset TARGET_CFLAGS
    unset TARGET_CPPFLAGS
    unset TARGET_PATH
    unset TARGET_LINKFLAGS
    unset TARGET_LINK
    unset TARGET_AR
    unset TARGET_RANLIB
    unset STAGING_DIR
}

do_compile_class-native() {
    cd ${S}/core/${PN}
    scons DOCS=html CRYPTO=openssl BINDINGS=${ALLJOYN_BINDINGS} OE_BASE=/usr WS=off VARIANT=debug
}

install_alljoyn_core() {
    install -d ${D}/${libdir}
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/lib/lib${PN}.* ${D}/${libdir}
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/lib/libajrouter.* ${D}/${libdir}
}

install_alljoyn_core_dev() {
    install -d ${D}/${includedir}/${PN} ${D}/${includedir}/qcc ${D}/${includedir}/qcc/posix
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/inc/${PN}/*.h ${D}/${includedir}/${PN}
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/inc/qcc/*.h ${D}/${includedir}/qcc
    install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/inc/qcc/posix/*.h ${D}/${includedir}/qcc/posix
}

install_alljoyn_core_docs() {
    install -d ${D}/${docdir}/${PN}
    cp -r ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/docs/* ${D}/${docdir}/${PN}
}

install_alljoyn_core_bin() {
    for i in ${ALLJOYN_CORE_BIN}
    do
        if [ -f "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/${i}" ]; then
            install -d ${D}/${ALLJOYN_BINDIR}
            install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/${i} ${D}/${ALLJOYN_BINDIR}
        fi
    done
}

install_alljoyn_core_samples() {
    for i in ${ALLJOYN_CORE_SAMPLES}
    do
        if [ -f "${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/samples/${i}" ]; then
            install -d ${D}/${ALLJOYN_BINDIR}
            install ${S}/core/${PN}/build/openwrt/openwrt/debug/dist/cpp/bin/samples/${i} ${D}/${ALLJOYN_BINDIR}
        fi
    done
}

do_install() {
    install_alljoyn_core
    install_alljoyn_core_dev
    install_alljoyn_core_docs
    install_alljoyn_core_bin
    install_alljoyn_core_samples
}

FILES_${PN} = " \
                ${libdir}/lib${PN}.so \
                ${ALLJOYN_BINDIR}/* \
              "

BBCLASSEXTEND = "native nativesdk"
