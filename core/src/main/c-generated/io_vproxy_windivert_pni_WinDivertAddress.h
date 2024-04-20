/* DO NOT EDIT THIS FILE - it is machine generated */
/* Header for class io_vproxy_windivert_pni_WinDivertAddress */
#ifndef _Included_io_vproxy_windivert_pni_WinDivertAddress
#define _Included_io_vproxy_windivert_pni_WinDivertAddress
#ifdef __cplusplus
extern "C" {
#endif

struct WinDivertAddress;
typedef struct WinDivertAddress WinDivertAddress;

#ifdef __cplusplus
}
#endif

#include <jni.h>
#include <pni.h>
#include "io_vproxy_windivert_pni_WinDivertData.h"

#ifdef __cplusplus
extern "C" {
#endif

PNIEnvExpand(WinDivertAddress, WinDivertAddress *)
PNIBufExpand(WinDivertAddress, WinDivertAddress, 80)

struct WinDivertAddress {
    uint64_t timestamp;
    uint32_t layer : 8;
    uint32_t event : 8;
    uint32_t sniffed : 1;
    uint32_t outbound : 1;
    uint32_t loopback : 1;
    uint32_t impostor : 1;
    uint32_t ipv6 : 1;
    uint32_t ipChecksum : 1;
    uint32_t tcpChecksum : 1;
    uint32_t udpChecksum : 1;
    uint32_t : 8;

    uint32_t bitfield1;
    WinDivertData union0;
};

#ifdef __cplusplus
}
#endif
#endif // _Included_io_vproxy_windivert_pni_WinDivertAddress
// metadata.generator-version: pni 21.0.0.17
// sha256:e51812e4172011761cd766b9522f011ce28a9d94d73e7f45f9710a2516d878c4