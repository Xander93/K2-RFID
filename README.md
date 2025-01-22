# K2-RFID
K2/CFS RFID Programming.<br>

## Note i have been informed by Guilouz that in the upcoming beta firmware creality is blocking the use of your own RFID tags stating its a "security risk".<br> This can only mean that the "security" of their filament sales is at risk if you use your own tags on generic filament.<br><br>For now the highest version of firmware for this to work is V1.1.1.7


The tags required are <a href=https://en.wikipedia.org/wiki/MIFARE>MIFARE</a> Classic 1k tags.<br>


<br>
<a href=https://github.com/DnG-Crafts/K2-RFID/tree/main/Android/SpoolID>Android Code</a>
<br>
<br>

<a href=https://github.com/DnG-Crafts/K2-RFID/tree/main/Arduino/Spool_ID>Arduino Code</a>
<br>
<br>

[![https://www.youtube.com/watch?v=6EA4t7zgq90](https://img.youtube.com/vi/6EA4t7zgq90/0.jpg)](https://www.youtube.com/watch?v=6EA4t7zgq90)

https://www.youtube.com/watch?v=6EA4t7zgq90<br>


<br><br><br>
The app is available on google play<br>
<a href="https://play.google.com/store/apps/details?id=dngsoftware.spoolid&hl=en"><img src=https://github.com/DnG-Crafts/K2-RFID/blob/main/gp.webp width="20%" height="20%"></a>
<br>



## Tag Format
```
Creality RFID Tag Data

 AB1240276A21010010FFFFFF0165000001000000
 AB1240276A21010010C12E1F0165000001000000
 9A2240276A210100100000000165000001000000
 AB1240276A21010010C12E1F0165000001000000
 
    date             
|  M DD YY  | venderId | batch | filamentId |  color  | filamentLen | serialNum | reserve |
|           |          |       |            |         |             |           |         |
|  A B1 24  |   0276   |  A2   |   101001   | 0FFFFFF |    0165     |   000001  |  000000 |
|           |          |       |            |         |             |           |         |
|  A B1 24  |   0276   |  A2   |   101001   | 0C12E1F |    0165     |   000001  |  000000 |
|           |          |       |            |         |             |           |         |
|  9 A2 24  |   0276   |  A2   |   101001   | 0000000 |    0165     |   000001  |  000000 |
|           |          |       |            |         |             |           |         |
|  A B1 24  |   0276   |  A2   |   101001   | 0C12E1F |    0165     |   000001  |  000000 |
  

Extracted from cfs0_050_G32-cfs0_000_112.bin

[character]   month=%c       >  ?
[string]      day=%.*s       >  ?
[string]      year=%.*s      >  ?
[string]      supplier=%.*s  >  venderId
[string]      batch=%.*s     >  ?
[string]      mat_id=%.*s    >  filamentId
[string]      col=%.*s       >  color
[string]      len=%.*s       >  filamentLen
[string]      number=%.*s    >  serialNum
[string]      reserve=%.*s   >  reserve

```

## Files of interest
```
 /mnt/UDISK/creality/userdata/box/tn_data.json
 /mnt/UDISK/creality/userdata/box/material_box_info.json
 /mnt/UDISK/creality/userdata/box/material_database.json
 /mnt/UDISK/creality/userdata/box/material_modify_info.json
```


## Arduino

Default Access point information:<br>
```
SSID:    K2_RFID
PASS:    password
Web URL: http://10.1.0.1/
```
<br>
Hardware:<br>
<a href=https://en.wikipedia.org/wiki/ESP32>ESP32</a><br>
<a href=https://esphome.io/components/binary_sensor/rc522.html>RC522</a><br>
<br>
GPIO Connections:<br>
<img src=https://github.com/DnG-Crafts/K2-RFID/blob/main/pins.jpg>







