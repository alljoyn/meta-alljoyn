SUMMARY = "Alljoyn thin framework services."
DESCRIPTION = "Alljoyn is an Open Source framework that makes it easy for devices and apps to discover and securely communicate with each other."
AUTHOR = "Herve Jourdain <herve.jourdain@beechwoods.com>"
HOMEPAGE = "https://www.allseenalliance.org/"
SECTION = "libs"
LICENSE = "ISC"
DEPENDS = "openssl libxml2"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/ISC;md5=f3b90e78ea0cffb20bf5cca7947a896d"

S = "${WORKDIR}/git"
SRCREV_FORMAT = "basetcl"
SRCREV_ajtclcore = "4361899010c24a549aa94e837588a96afe0e6f89"
SRCREV_basetcl = "820016899146caaf7dd3e7d4a062ef22aaa02aa3"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI = " \
            git://git.allseenalliance.org/gerrit/services/base_tcl.git;protocol=https;destsuffix=git/services/base_tcl;name=basetcl \
            git://git.allseenalliance.org/gerrit/core/ajtcl.git;protocol=https;destsuffix=git/core/ajtcl;name=ajtclcore \
            file://001_ajtcl_services_git_basetcl_add_services_config_lib.patch \
          "

PV = "master+git${SRCPV}"

AJTCL_SERVICES_SAMPLES ?= "ConfigSample ControlleeSample NotifConfigSample NotificationConsumerSample NotificationProducerSample"
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
    cd ${S}/core/ajtcl
# GTEST_DIR is required because if gtest framework is present, but GTEST_DIR is not, then it triggers a compilation error!
    scons TARG=linux WS=off GTEST_DIR=${STAGING_DIR_HOST}/${prefix}
    cd ${S}/services/base_tcl
    scons TARG=linux WS=off AJ_TCL_ROOT=../../core/ajtcl
    unset CROSS_PREFIX
    unset CROSS_PATH
    unset CROSS_CFLAGS
    unset CROSS_LINKFLAGS
}

do_compile_class-native() {
    cd ${S}/core/ajtcl
    scons TARG=linux WS=off
    cd ${S}/services/base_tcl
    scons WS=off AJ_TCL_ROOT=../../core/ajtcl
}

do_install() {
# Install service libraries from core
    install -d ${D}/${libdir} ${D}/${includedir}/ajtcl
    install ${S}/core/ajtcl/dist/lib/libajtcl_services* ${D}/${libdir}
    cp -r ${S}/core/ajtcl/dist/include/ajtcl/services ${D}/${includedir}/ajtcl
# Install base_tcl
    install -d ${D}/${libdir} ${D}/${includedir}/ajtcl
    install ${S}/services/base_tcl/dist/lib/* ${D}/${libdir}
    cp -r ${S}/services/base_tcl/dist/include/ajtcl/services ${D}/${includedir}/ajtcl
# Install service samples from core
    for i in ${AJTCL_SERVICES_SAMPLES}
    do
        if [ -f "${S}/core/ajtcl/dist/bin/services/${i}" ]; then
            install -d ${D}/${AJTCL_BINDIR}
            install ${S}/core/ajtcl/dist/bin/services/${i} ${D}/${AJTCL_BINDIR}
        fi
    done
# Install base_tcl samples
    for i in ${AJTCL_SERVICES_SAMPLES}
    do
        if [ -f "${S}/services/base_tcl/dist/bin/${i}" ]; then
            install -d ${D}/${AJTCL_BINDIR}
            install ${S}/services/base_tcl/dist/bin/${i} ${D}/${AJTCL_BINDIR}
        fi
    done
}

FILES_${PN} = " \
                ${libdir}/libajtcl_*.so \
                ${AJTCL_BINDIR}/* \
              "

RDEPENDS_${PN} += "ajtcl"

BBCLASSEXTEND = "native nativesdk"
