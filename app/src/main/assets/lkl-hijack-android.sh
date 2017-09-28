
script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

export PATH=${PATH}:${script_dir}


# rmnet changes often, so just grep it
RMNET_IP=$(ifconfig rmnet0 | grep "inet addr" | cut -d ":" -f 2 | cut -d " " -f 1)
RMNET_MASK=$(ip route | grep rmnet0 | grep '/' | head -n 1 | cut -d " " -f 1 | cut -d "/" -f 2)
RMNET_GATEWAY=$(ip route | grep "via" | grep rmnet0 | head -n 1 | cut -d " " -f 3)

# wifi configs
WIFI_MAC=$(ip ad show dev wlan0 | grep link/ether | cut -d " " -f6)
# rmnet changes often, so just grep it
WIFI_IP=$(ifconfig wlan0 | grep "inet addr" | cut -d ":" -f 2 | cut -d " " -f 1)
WIFI_MASK=$(ip route | grep wlan0 | grep '/' | head -n 1 | cut -d " " -f 1 | cut -d "/" -f 2)
WIFI_GATEWAY=$(ip route show table all | grep "via" | grep wlan0 | head -n 1 | cut -d " " -f 3)

#LKL_HIJACK_DEBUG=1
# LKL_HIJACK_BOOT_CMDLINE="ip=dhcp"

DEF_GATEWAY=$WIFI_GATEWAY
if [ -z $WIFI_GATEWAY ] ; then
    DEF_GATEWAY=$RMNET_GATEWAY
fi

if [ -n ${LKL_HIJACK_DEBUG+x}  ]
then
  trap '' TSTP
fi

export LKL_HIJACK_SYSCTL="net.ipv4.tcp_rmem=524288 1048576 2097152;net.ipv4.tcp_wmem=262144 524288 1048576"
export LKL_HIJACK_SINGLE_CPU=2

# Tweaks...
iptables -A OUTPUT -p tcp --tcp-flags RST RST  -j DROP

LD_PRELOAD=${script_dir}/${LKL_HIJACK_LIBNAME}   \
 LKL_HIJACK_NET_IFTYPE0=raw LKL_HIJACK_NET_IFPARAMS0=wlan0 \
 LKL_HIJACK_NET_IP0=$WIFI_IP LKL_HIJACK_NET_NETMASK_LEN0=$WIFI_MASK LKL_HIJACK_NET_GATEWAY0=$WIFI_GATEWAY \
 LKL_HIJACK_NET_GATEWAY=$DEF_GATEWAY \
 LKL_HIJACK_NET_MAC0=$WIFI_MAC \
 LKL_HIJACK_NET_IFTYPE1=raw-ipenc LKL_HIJACK_NET_IFPARAMS1=rmnet0 \
 LKL_HIJACK_NET_IP1=$RMNET_IP LKL_HIJACK_NET_NETMASK_LEN1=$RMNET_MASK LKL_HIJACK_NET_GATEWAY1=$RMNET_GATEWAY \
 $*

iptables -D OUTPUT -p tcp --tcp-flags RST RST  -j DROP
