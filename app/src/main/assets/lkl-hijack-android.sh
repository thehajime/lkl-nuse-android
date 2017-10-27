
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

DEF_GATEWAY=$WIFI_GATEWAY
if [ -z $WIFI_GATEWAY ] ; then
    DEF_GATEWAY=$RMNET_GATEWAY
fi

if [ -n ${LKL_HIJACK_DEBUG+x}  ]
then
  trap '' TSTP
fi


# Tweaks...
iptables -A OUTPUT -p tcp --tcp-flags RST RST  -j DROP


# configs
#export LKL_HIJACK_SYSCTL="net.ipv4.tcp_rmem=524288 1048576 2097152;net.ipv4.tcp_wmem=262144 524288 1048576"
#export LKL_HIJACK_SINGLE_CPU=2
export LKL_HIJACK_CONFIG_FILE="${script_dir}/lkl-hijack.json"

cat <<EOF > ${script_dir}/lkl-hijack.json
{
    "gateway": "$DEF_GATEWAY",
    "interfaces": [
        {
            "ifgateway": "$WIFI_GATEWAY",
            "ip": "$WIFI_IP",
            "mac": "$WIFI_MAC",
            "masklen": "$WIFI_MASK",
            "param": "wlan0",
            "type": "raw"
        },
        {
            "ifgateway": "$RMNET_GATEWAY",
            "ip": "$RMNET_IP",
            "masklen": "$RMNET_MASK",
            "param": "rmnet0",
            "type": "raw-ipenc"
        }
    ],
    "singlecpu": "1"
}
EOF

LD_PRELOAD=${script_dir}/${LKL_HIJACK_LIBNAME} $*

iptables -D OUTPUT -p tcp --tcp-flags RST RST  -j DROP
