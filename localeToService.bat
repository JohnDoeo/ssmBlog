
::@echo off

::scp .\target\weblog-0.0.1-SNAPSHOT.jar root@39.105.154.131:/javaweb/project/weblog/weblog-1.0.jar

::    ..@Dhy..211315 

@Echo Off
Echo open 39.105.154.131 22>ftp.up
echo --1
Echo root>>ftp.up

echo --2

Echo ..@Dhy..211315>>ftp.up

echo --3

Echo Cd .\ >>ftp.up
Echo binary>>ftp.up
Echo put "D:\¹¤×÷\Ñ§Ï°\study\java\ssmBlog\target\test.txt">>ftp.up
Echo bye>>ftp.up
FTP -s:ftp.up
del ftp.up /q