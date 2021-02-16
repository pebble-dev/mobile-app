#!/usr/bin/env bash

if [[ "$PEBBLE_SDK" == "" ]]; then
  if [[ "$OSTYPE" == "darwin"* ]]; then
    PEBBLE_SDK="$HOME/Library/Application Support/Pebble SDK"
  else
    PEBBLE_SDK="$HOME/.pebble-sdk"
  fi
fi

if [[ ! -d $PEBBLE_SDK ]]; then
  echo "Pebble SDK directory not found, install the pebble SDK or manually define its location with the env varibalbe PEBBLE_SDK"
  exit 1
fi

if [[ "$1" == "" ]]; then
  echo "Syntax: $0 <aplite|basalt|chalk|diorite|emery>"
  exit 1
fi

platform_micro="${PEBBLE_SDK}/SDKs/current/sdk-core/pebble/$1/qemu/qemu_micro_flash.bin"
platform_spi="${PEBBLE_SDK}/SDKs/current/sdk-core/pebble/$1/qemu/qemu_spi_flash.bin"
declare -A platform_machine=( [aplite]=pebble-bb2 [basalt]=pebble-snowy-bb [chalk]=pebble-s4-bb [diorite]=pebble-silk-bb [emery]=pebble-robert-bb )
qemu_port=8080

if [[ ! -f "$platform_spi" ]]; then
  echo "SPI flash file doesn't exist, trying to decompress it"
  if ! bzip2 -dc "${platform_spi}.bz2" > "$platform_spi"; then
    echo "Couldn't decompress either, giving up."
    exit 2
  fi
fi
platform_args="-pflash ${platform_micro} -machine ${platform_machine[$1]}"
if [[ "$1" == "aplite" ]]; then
  platform_args="${platform_args} -cpu cortex-m3 -mtdblock ${platform_spi}"
else
  platform_args="${platform_args} -cpu cortex-m4 -pflash ${platform_spi}"
fi

# shellcheck disable=SC2086
qemu-pebble -rtc base=localtime -serial null -serial tcp::${qemu_port},server,nowait -serial null $platform_args
