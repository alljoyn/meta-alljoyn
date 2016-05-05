SUMMARY = "Alljoyn framework services."
DESCRIPTION = "Alljoyn is an Open Source framework that makes it easy for devices and apps to discover and securely communicate with each other."
AUTHOR = "Herve Jourdain <herve.jourdain@beechwoods.com>"
HOMEPAGE = "https://www.allseenalliance.org/"
SECTION = "libs"
LICENSE = "ISC"
DEPENDS = "openssl libxml2 libcap"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/ISC;md5=f3b90e78ea0cffb20bf5cca7947a896d"

S = "${WORKDIR}/alljoyn"
SRC_URI = " \
            git://git.allseenalliance.org/gerrit/core/alljoyn.git;protocol=https;branch=RB${PV};destsuffix=alljoyn/core/alljoyn \
          "
SRCREV = "${AUTOREV}"

ALLJOYN_BINDINGS ?= "cpp"
ALLJOYN_SERVICES ?= "config,controlpanel,notification,onboarding,time,audio"
ALLJOYN_SERVICES_SAMPLES ?= " \
                              AboutClient \
                              AboutClient_legacy \
                              AboutService \
                              AboutService_legacy \
                              ACServerSample \
                              ACServerSample.conf \
                              ConfigClient \
                              ConfigService \
                              ConfigService.conf \
                              ConsumerService \
                              ControlPanelController \
                              ControlPanelProducer \
                              ControlPanelSample \
                              FactoryACServerSample.conf \
                              FactoryConfigService.conf \
                              FactoryOnboardingService.conf \
                              OnboardingClient \
                              onboarding-daemon \
                              OnboardingService.conf \
                              ProducerBasic \
                              ProducerService \
                              TestService \
                              wifi_scan_results \
                            "
ALLJOYN_BINDIR ?= "/opt/alljoyn/bin"

ALLJOYN_BUILD_OPTIONS += "SERVICES=${ALLJOYN_SERVICES}"
ALLJOYN_BUILD_OPTIONS_NATIVE += "SERVICES=${ALLJOYN_SERVICES}"

inherit scons

PACKAGES = " \
             ${PN}-dbg \
             ${PN} \
             ${PN}-staticdev \
             ${PN}-dev \
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
    cd ${S}/core/alljoyn
    scons OS=openwrt CPU=openwrt DOCS=html CRYPTO=openssl BINDINGS=${ALLJOYN_BINDINGS} ${ALLJOYN_BUILD_OPTIONS} OE_BASE=/usr WS=off VARIANT=debug
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
    cd ${S}/core/alljoyn
    scons DOCS=html CRYPTO=openssl BINDINGS=${ALLJOYN_BINDINGS} ${ALLJOYN_BUILD_OPTIONS_NATIVE} OE_BASE=/usr WS=off VARIANT=debug
}

install_alljoyn_services() {
# ABOUT and CONFIG services are always present
    install -d ${D}/${libdir} ${D}/${includedir}/alljoyn/about ${D}/${includedir}/alljoyn/config
    install ${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/cpp/lib/liballjoyn_about.* ${D}/${libdir}
    install ${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/cpp/lib/liballjoyn_config.* ${D}/${libdir}
    install ${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/cpp/inc/alljoyn/about/*.h ${D}/${includedir}/alljoyn/about
    install ${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/cpp/inc/alljoyn/config/*.h ${D}/${includedir}/alljoyn/config
# Install other services
    for i in `find ${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/ -maxdepth 1 -type d`; do
        if [ "${i}" != "${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/cpp" -a \
             "${i}" != "${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/java" -a \
             "${i}" != "${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/c" -a \
             "${i}" != "${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/js" ]; then
            if [ -d ${i}/inc ]; then
                install -d ${D}/${includedir}/alljoyn/
                cp -r ${i}/inc/alljoyn/* ${D}/${includedir}/alljoyn/
            fi
            if [ -d ${i}/lib ]; then
                install -d ${D}/${libdir}
                install ${i}/lib/* ${D}/${libdir}
            fi
        fi
    done
}

install_alljoyn_services_samples() {
    for i in ${ALLJOYN_SERVICES_SAMPLES}
    do
        if [ -f "${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/cpp/bin/samples/${i}" ]; then
            install -d ${D}/${ALLJOYN_BINDIR}
            install ${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/cpp/bin/samples/${i} ${D}/${ALLJOYN_BINDIR}
        else
            for j in `find ${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/ -maxdepth 1 -type d`;
            do
                if [ "${j}" != "${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/cpp" -a \
                     "${j}" != "${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/java" -a \
                     "${j}" != "${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/c" -a \
                     "${j}" != "${S}/core/alljoyn/build/openwrt/openwrt/debug/dist/js" ]; then
                    if [ -f ${j}/bin/${i} ]; then
                        install -d ${D}/${ALLJOYN_BINDIR}
                        install ${j}/bin/${i} ${D}/${ALLJOYN_BINDIR}
                    fi
                fi
            done
        fi
    done
}

do_install() {
    install_alljoyn_services
    install_alljoyn_services_samples
}

FILES_${PN} = " \
                ${libdir}/liballjoyn_*.so \
                ${ALLJOYN_BINDIR}/* \
              "

RDEPENDS_${PN} += "alljoyn"

BBCLASSEXTEND = "native nativesdk"
