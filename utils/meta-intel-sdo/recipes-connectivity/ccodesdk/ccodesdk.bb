#sdo yocto project 

DESCRIPTION = "Recipe for sdo (c-code-sdk) on linux"
LICENSE = "Apache-2.0"
BB_STRICT_CHECKSUM = "0"

LIC_FILES_CHKSUM = "file://LICENSE;md5=fa818a259cbed7ce8bc2a22d35a464fc"

SRCREV = "7e882c2f5de8f94efc2b4212616933c7d94b272b"
SRC_URI = "git://github.com/secure-device-onboard/client-sdk.git"
#SRC_URI[sha256sum] = "c821a9afa9f987ac829fb3a8dd72122c3c612b0c25c9c0fe03201f7e1081f183"
#SRC_URI[sha256sum] = "25aaa5ea90c023484f09027ae4e17d14dd5e927992a6d9717d0e954208a57f8f"

S = "${WORKDIR}/git"

TOOLCHAIN = "POKY-GLIBC"

APP_NAME = "c_code_sdk"

DEPENDS += "openssl"

inherit pkgconfig cmake

FILES_${PN} += "/opt \
                /opt/sdo \
                /opt/sdo/linux-client" 

do_configure(){
}

do_compile(){
CUR_DIR=$(pwd)
cd "${WORKDIR}/git"

cd ${CUR_DIR}/../

if [ ! -d "safestringlib" ] ; then
	git clone git://github.com/intel/safestringlib.git
fi

export SAFESTRING_ROOT=${CUR_DIR}/../safestringlib
cd ${SAFESTRING_ROOT}
make 

cd ${S}
cmake -DPK_ENC=ecdsa -DDA=ecdsa256 -DMANUFACTURER_TOOLKIT=true -DKEX=ecdh . ;
CFLAGS="${CFLAGS}" make -j$(nproc)

}

do_install() {
    install -d "${D}/opt/sdo"
    install "${WORKDIR}/git/build/linux-client" "${D}/opt/sdo"
    install -d "${D}/opt/sdo/data"
    cp -r "${WORKDIR}/git/data/" "${D}/opt/sdo/"
    install -d "${D}/opt/sdo/data_bkp"
    cp -r "${WORKDIR}/git/data/" "${D}/opt/sdo/data_bkp"
}

do_package_qa[noexec] = "1"

INITSCRIPT_PACKAGES = "${PN}"
