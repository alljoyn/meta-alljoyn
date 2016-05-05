SUMMARY = "Alljoyn thin framework core."
DESCRIPTION = "Alljoyn is an Open Source framework that makes it easy for devices and apps to discover and securely communicate with each other."
AUTHOR = "Herve Jourdain <herve.jourdain@beechwoods.com>"
HOMEPAGE = "https://www.allseenalliance.org/"
SECTION = "libs"
LICENSE = "ISC"
DEPENDS = "openssl libxml2"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/ISC;md5=f3b90e78ea0cffb20bf5cca7947a896d"

S = "${WORKDIR}/alljoyn"
SRC_URI = "git://git.allseenalliance.org/gerrit/core/${PN}.git;protocol=https;branch=RB${PV};destsuffix=alljoyn/core/ajtcl"
SRCREV = "${AUTOREV}"

AJTCL_CORE_SAMPLES ?= " \
                        basic_client \
                        basic_service \
                        eventaction_service \
                        nameChange_client \
                        net_bus \
                        SecureClient \
                        SecureClientECDHE \
                        SecureService \
                        SecureServiceECDHE \
                        signalConsumer_client \
                        signal_service \
                      "

AJTCL_BINDIR ?= "/opt/ajtcl/bin"

inherit scons

PACKAGES = " \
             ${PN}-dbg \
             ${PN} \
             ${PN}-staticdev \
             ${PN}-dev \
           "

do_compile() {
# For _class-target and _class-nativesdk
    export CROSS_PREFIX="${TARGET_PREFIX}"
# Path to the cross-compiler is included in the PATH variable set by the OE environment
    export CROSS_PATH="${PATH}"
    export CROSS_CFLAGS="${TARGET_CC_ARCH} ${TOOLCHAIN_OPTIONS} ${CFLAGS}"
    export CROSS_LINKFLAGS="${TARGET_LD_ARCH} ${TOOLCHAIN_OPTIONS} ${LDFLAGS}"
    cd ${S}/core/${PN}
# GTEST_DIR is required because if gtest framework is present, but GTEST_DIR is not, then it triggers a compilation error!
    scons TARG=linux WS=off GTEST_DIR=${STAGING_DIR_HOST}/${prefix}
    unset CROSS_PREFIX
    unset CROSS_PATH
    unset CROSS_CFLAGS
    unset CROSS_LINKFLAGS
}

do_compile_class-native() {
    cd ${S}/core/${PN}
    scons TARG=linux WS=off
}

do_install() {
# Install ajtcl core
    install -d ${D}/${libdir} ${D}/${includedir}/${PN}
    install ${S}/core/${PN}/dist/lib/lib${PN}.* ${D}/${libdir}
    install ${S}/core/${PN}/dist/include/${PN}/*.h ${D}/${includedir}/${PN}
# Install ajtcl samples
    for i in ${AJTCL_CORE_SAMPLES}
    do
        if [ -f "${S}/core/${PN}/dist/bin/${i}" ]; then
            install -d ${D}/${AJTCL_BINDIR}
            install ${S}/core/${PN}/dist/bin/${i} ${D}/${AJTCL_BINDIR}
        fi
    done
}

FILES_${PN} = " \
                ${libdir}/lib${PN}.so \
                ${AJTCL_BINDIR}/* \
              "

BBCLASSEXTEND = "native nativesdk"
