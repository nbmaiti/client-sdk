#sdotpm yocto project 

DESCRIPTION = "Recipe for sdo (c-code-sdk) on linux with tpm"
LICENSE = "Apache-2.0"

BB_STRICT_CHECKSUM = "0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=fa818a259cbed7ce8bc2a22d35a464fc"

SRCREV = "d5cbe0a5c060bbedfa75c42fb802cc3abe6b5d68"
SRC_URI = "git://github.com/secure-device-onboard/client-sdk.git"
###SRC_URI[sha256sum] = "f21ab4d2f2ddf83feac2e6d98f79ae1ccf8fdff5ec03661d0a5240928c0d3d7f"


S = "${WORKDIR}/git"

TOOLCHAIN = "POKY-GLIBC"

APP_NAME = "c_code_sdk"

inherit pkgconfig cmake

DEPENDS += "openssl"
DEPENDS += "tpm2-tss"
DEPENDS += "tpm2-abrmd"
DEPENDS += "tpm2-tools"
DEPENDS += "tpm2-tss-engine"

FILES_${PN} += "/opt \
                /opt/sdotpm \
                /opt/sdotpm/linux-client" 

do_configure(){
}

do_compile(){
CUR_DIR=$(pwd)
cd "${WORKDIR}/git"

#export MODULES=true
#export DA=tpm20_ecdsa256
#export ARCH=x86

cd ${CUR_DIR}/../
if [ ! -d "safestringlib" ] ; then
	git clone git://github.com/intel/safestringlib.git
fi
export SAFESTRING_ROOT=${CUR_DIR}/../safestringlib
cd ${SAFESTRING_ROOT}
make 

cd ${S}

#patching for yocto
sed -i '1s/^/ln -s \/usr\/lib64\/engines-1.1\/libtpm2tss.so  \/usr\/lib64\/engines-1.1\/tpm2tss.so\n/' ./utils/tpm_make_ready_ecdsa.sh
sed -i 's/usr\/local\/lib\/engines-1.1/usr\/lib64\/engines-1.1/g' ./utils/tpm_make_ready_ecdsa.sh
sed -i 's/usr\/local\/lib\/engines-1.1/usr\/lib64\/engines-1.1/g' cmake/blob_path.cmake

cmake -DHTTPPROXY=true -DMANUFACTURER_TOOLKIT=true -DBUILD=debug -DKEX=ecdh -DAES_MODE=ctr -DDA=tpm20_ecdsa256 -DPK_ENC=ecdsa .
make -j$(nproc)

}

do_install() {
    install -d "${D}/opt/sdotpm"
    install "${WORKDIR}/git/build//linux-client" "${D}/opt/sdotpm"
    cp -r "${WORKDIR}/git/utils/tpm_make_ready_ecdsa.sh" "${D}/opt/sdotpm"
    install -d "${D}/opt/sdotpm/data"
    cp -r "${WORKDIR}/git/data/" "${D}/opt/sdotpm/"
    install -d "${D}/opt/sdotpm/data_bkp"
    cp -r "${WORKDIR}/git/data/" "${D}/opt/sdotpm/data_bkp"
}

do_package_qa[noexec] = "1"

INITSCRIPT_PACKAGES = "${PN}"
