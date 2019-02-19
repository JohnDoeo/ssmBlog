#! /bin/bash
echo "----------------开始更新weblog项目-----------------------"
echo "-------------------开始替换jar包-------------------------"
cp ./weblog-1.0.jar /var/lib/docker/overlay2/2ec8b12eb12a9e1a0dbea0a7a42f1a55aa253f659c337c97c69b92ec2147c2e9/merged/weblog.jar
cp ./weblog-1.0.jar /var/lib/docker/overlay2/92af3b8e2f484e061d03e5e441321506e9ab65b5a46cccf4df32fb5b794e4f31/diff/weblog.jar
cp ./weblog-1.0.jar /var/lib/docker/overlay2/acd8ae5ed9a7dd830deb6fa1a7e22162b9fe440f0e322117032625c050d1201c/diff/weblog.jar
echo "-------------------jar包替换完成-------------------------"
echo "-------------------停止weblog容器------------------------"
docker stop 76033d7348f2 
echo "-----------------重新启动weblog容器----------------------"
docker start 76033d7348f2 
echo "-----------------weblog项目更新完成----------------------"