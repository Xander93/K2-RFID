# K2-RFID
K2/CFS RFID Programming

<br>
<a href=https://github.com/DnG-Crafts/K2-RFID/tree/main/Android/SpoolID>Android Code</a>
<br>
<br>

<a href=https://github.com/DnG-Crafts/K2-RFID/tree/main/Arduino/Spool_ID>Arduino Code</a>
<br>



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
 
                      batch?
|  ?????? | venderId | ?? | filamentId |  color  | filamentLen | serialNum | reserve |
|         |          |    |            |         |             |           |         |
|  AB124  |   0276   | A2 |   101001   | 0FFFFFF |    0165     |   000001  |  000000 |
|         |          |    |            |         |             |           |         |
|  AB124  |   0276   | A2 |   101001   | 0C12E1F |    0165     |   000001  |  000000 |
|         |          |    |            |         |             |           |         |
|  9A224  |   0276   | A2 |   101001   | 0000000 |    0165     |   000001  |  000000 |
|         |          |    |            |         |             |           |         |
|  AB124  |   0276   | A2 |   101001   | 0C12E1F |    0165     |   000001  |  000000 |
  

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
```
