#!/bin/bash
trap_ctrlc()
{
    exit 0
}
trap trap_ctrlc SIGHUP SIGINT SIGTERM
if [ -n "$SUDO_USER" ];
then
	echo "Attempting fix for user $SUDO_USER"
	if [ -f /etc/arch-release ];
	then
		echo "Arch-based distro detected"
		usermod -a -G uucp $SUDO_USER
		if [ $? != 0 ];
		then
			echo "Failed, follow the manual instructions"
			exit 3
		else
			echo "Success"
			echo "Rebooting in 10 seconds. Press CTRL+C to abort"
			sleep 10
			reboot now
			exit 0
		fi
	fi
	if [ -f /etc/debian_version ];
	then
		echo "Debian-based distro detected"
		usermod -a -G dialout $SUDO_USER
		if [ $? != 0 ];
		then
			echo "Failed, follow the manual instructions"
			exit 3
		else
			echo "Success"
			echo "Rebooting in 10 seconds. Press CTRL+C to abort"
			sleep 10
			reboot now
			exit 0
		fi
	fi
	if [ -f /etc/SUSE-brand ];
	then
		echo "SUSE-based distro detected"
		usermod -a -G dialout,lock,tty $SUDO_USER
		if [ $? != 0 ];
		then
			echo "Failed, follow the manual instructions"
			exit 3
		else
			echo "Success"
			echo "Rebooting in 10 seconds. Press CTRL+C to abort"
			sleep 10
			reboot now
			exit 0
		fi
	fi
	if [ -f /etc/redhat-release ];
	then
		echo "Fedora/RedHat-based distro detected"
		usermod -a -G dialout,lock $SUDO_USER
		if [ $? != 0 ];
		then
			echo "Failed, follow the manual instructions"
			exit 3
		else
			echo "Success"
			echo "Rebooting in 10 seconds. Press CTRL+C to abort"
			sleep 10
			reboot now
			exit 0
		fi
	fi
	echo "Unknown distro, follow the manual instructions"
	exit 2
else
	sudo "$0" "$@"
	if [ $? != 0 ]; then
		sleep 5
	fi
	exit 0
fi
