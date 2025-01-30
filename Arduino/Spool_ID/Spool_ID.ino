#include <SPI.h>
#include <MFRC522.h>
#include <WiFi.h>
#include <ESPmDNS.h>
#include "mbedtls/aes.h"
#include "SPIFFS.h"
#include "html.h"

#define SS_PIN 5
#define RST_PIN 21
#define SPK_PIN 27

MFRC522 mfrc522(SS_PIN, RST_PIN);
MFRC522::MIFARE_Key key;
MFRC522::MIFARE_Key ekey;

WiFiServer webServer(80);
IPAddress Server_IP(10, 1, 0, 1);
IPAddress Subnet_Mask(255, 255, 255, 0);
String spoolData = "AB1240276A210100100000FF016500000100000000000000";
String AP_SSID = "K2_RFID";
String AP_PASS = "password";
String WIFI_SSID = "";
String WIFI_PASS = "";
String WIFI_HOSTNAME = "k2.local";
bool encrypted = false;

void setup()
{
    SPIFFS.begin(true);
    loadConfig();
    SPI.begin();
    mfrc522.PCD_Init();
    key = {255, 255, 255, 255, 255, 255};
    pinMode(SPK_PIN, OUTPUT);
    if (AP_SSID == "" || AP_PASS == "")
    {
        AP_SSID = "K2_RFID";
        AP_PASS = "password";
    }
    WiFi.softAPConfig(Server_IP, Server_IP, Subnet_Mask);
    WiFi.softAP(AP_SSID.c_str(), AP_PASS.c_str());
    WiFi.softAPConfig(Server_IP, Server_IP, Subnet_Mask);

    if (WIFI_SSID != "" && WIFI_PASS != "")
    {
        WiFi.setAutoConnect(true);
        WiFi.setAutoReconnect(true);
        WiFi.hostname(WIFI_HOSTNAME);
        WiFi.begin(WIFI_SSID.c_str(), WIFI_PASS.c_str());
        if (WiFi.waitForConnectResult() == WL_CONNECTED)
        {
            IPAddress LAN_IP = WiFi.localIP();
        }
    }
    if (WIFI_HOSTNAME != "")
    {
        String mdnsHost = WIFI_HOSTNAME;
        mdnsHost.replace(".local", "");
        MDNS.begin(mdnsHost.c_str());
    }
    webServer.setTimeout(100);
    webServer.begin();
}

void loop()
{
    handleWebServer();
    if (!mfrc522.PICC_IsNewCardPresent())
        return;

    if (!mfrc522.PICC_ReadCardSerial())
        return;

    encrypted = false;

    MFRC522::PICC_Type piccType = mfrc522.PICC_GetType(mfrc522.uid.sak);
    if (piccType != MFRC522::PICC_TYPE_MIFARE_MINI && piccType != MFRC522::PICC_TYPE_MIFARE_1K && piccType != MFRC522::PICC_TYPE_MIFARE_4K)
    {
        tone(SPK_PIN, 400, 400);
        delay(2000);
        return;
    }

    int x = 0;
    char tuid[16];
    for (int i = 0; i < 16; i++)
    {
        if (x >= 4)
            x = 0;
        tuid[i] = mfrc522.uid.uidByte[x];
        x++;
    }
    createKey(tuid);

    MFRC522::StatusCode status;
    status = (MFRC522::StatusCode)mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, 7, &key, &(mfrc522.uid));
    if (status != MFRC522::STATUS_OK)
    {
        if (!mfrc522.PICC_IsNewCardPresent())
            return;
        if (!mfrc522.PICC_ReadCardSerial())
            return;
        status = (MFRC522::StatusCode)mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, 7, &ekey, &(mfrc522.uid));
        if (status != MFRC522::STATUS_OK)
        {
            tone(SPK_PIN, 400, 150);
            delay(300);
            tone(SPK_PIN, 400, 150);
            delay(2000);
            return;
        }
        encrypted = true;
    }

    byte blockData[17];
    int blockID = 4;
    for (int i = 0; i < spoolData.length(); i += 16)
    {
        spoolData.substring(i, i + 16).getBytes(blockData, 17);
        if (blockID >= 4 && blockID < 7)
        {
            unsigned char bufOut[16];
            encrypt((char *)blockData, bufOut);
            mfrc522.MIFARE_Write(blockID, bufOut, 16);
        }
        blockID++;
    }

    if (!encrypted)
    {
        byte buffer[18];
        byte byteCount = sizeof(buffer);
        byte block = 7;
        status = mfrc522.MIFARE_Read(block, buffer, &byteCount);
        int y = 0;
        for (int i = 10; i < 16; i++)
        {
            buffer[i] = ekey.keyByte[y];
            y++;
        }
        for (int i = 0; i < 6; i++)
        {
            buffer[i] = ekey.keyByte[i];
        }
        mfrc522.MIFARE_Write(7, buffer, 16);
    }

    mfrc522.PICC_HaltA();
    mfrc522.PCD_StopCrypto1();
    tone(SPK_PIN, 1000, 200);
    delay(2000);
}

void encrypt(char *dataChr, unsigned char *outDat)
{
    char seed[16] = {72, 64, 67, 70, 107, 82, 110, 122, 64, 75, 65, 116, 66, 74, 112, 50};
    mbedtls_aes_context aes;
    mbedtls_aes_init(&aes);
    mbedtls_aes_setkey_enc(&aes, (const unsigned char *)seed, strlen(seed) * 8);
    mbedtls_aes_crypt_ecb(&aes, MBEDTLS_AES_ENCRYPT, (const unsigned char *)dataChr, outDat);
    mbedtls_aes_free(&aes);
}

void createKey(char *uid)
{
    char seed[16] = {113, 51, 98, 117, 94, 116, 49, 110, 113, 102, 90, 40, 112, 102, 36, 49};
    unsigned char bufOut[6];
    mbedtls_aes_context aes;
    mbedtls_aes_init(&aes);
    mbedtls_aes_setkey_enc(&aes, (const unsigned char *)seed, strlen(seed) * 8);
    mbedtls_aes_crypt_ecb(&aes, MBEDTLS_AES_ENCRYPT, (const unsigned char *)uid, bufOut);
    mbedtls_aes_free(&aes);
    for (int i = 0; i < 6; i++)
    {
        ekey.keyByte[i] = bufOut[i];
    }
}

void handleWebServer()
{
    WiFiClient client = webServer.available();
    if (client)
    {
        while (client.connected())
        {
            if (client.available() > 0)
            {
                String path;
                String cData = client.readStringUntil('\r');
                if (instr(cData, "GET "))
                {
                    path = split(cData, "GET ", " HTTP");
                    if (instr(path, "config"))
                    {
                        client.flush();
                        String htmStr = AP_SSID + "|-|" + WIFI_SSID + "|-|" + WIFI_HOSTNAME;
                        client.print("HTTP/1.1 200 OK\r\n");
                        client.print("Content-type: text/plain\r\n");
                        client.print("Content-Length: " + String(htmStr.length()) + "\r\n");
                        client.print("Connection: close\r\n\r\n");
                        client.print(htmStr);
                    }
                    else if (instr(path, "index.html") || path == "/")
                    {
                        client.flush();
                        client.print("HTTP/1.1 200 OK\r\n");
                        client.print("Content-type: text/html\r\n");
                        client.print("Content-Length: " + String(sizeof(indexData) - 1) + "\r\n");
                        client.print("Connection: close\r\n\r\n");
                        client.write(indexData);
                    }
                    else
                    {
                        client.flush();
                        client.print("HTTP/1.1 404 Not Found\r\n");
                        client.print("Connection: close\r\n");
                    }
                }
                else if (instr(cData, "POST "))
                {
                    path = split(cData, "POST ", " HTTP");
                    if (instr(path, "config"))
                    {
                        String pdata = client.readString();
                        client.flush();
                        if (instr(pdata, "ap_ssid") && instr(pdata, "ap_pass") && instr(pdata, "wifi_ssid") && instr(pdata, "wifi_pass") && instr(pdata, "wifi_host"))
                        {
                            AP_SSID = split(pdata, "ap_ssid=", "&ap_pass");
                            if (!split(pdata, "ap_pass=", "&wifi_ssid").equals("********"))
                            {
                                AP_PASS = split(pdata, "ap_pass=", "&wifi_ssid");
                            }
                            WIFI_SSID = split(pdata, "wifi_ssid=", "&wifi_pass");
                            if (!split(pdata, "wifi_pass=", "&wifi_host").equals("********"))
                            {
                                WIFI_PASS = split(pdata, "wifi_pass=", "&wifi_host");
                            }
                            WIFI_HOSTNAME = split(pdata, "wifi_host=", "&submit");
                            File file = SPIFFS.open("/config.ini", "w");
                            if (file)
                            {
                                file.print("\r\nAP_SSID=" + AP_SSID + "\r\nAP_PASS=" + AP_PASS + "\r\nWIFI_SSID=" + WIFI_SSID + "\r\nWIFI_PASS=" + WIFI_PASS + "\r\nWIFI_HOST=" + WIFI_HOSTNAME + "\r\n");
                                file.close();
                            }
                            String htmStr = "OK";
                            client.print("HTTP/1.1 200 OK\r\n");
                            client.print("Content-type: text/html\r\n");
                            client.print("Content-Length: " + String(htmStr.length()) + "\r\n");
                            client.print("Connection: close\r\n\r\n");
                            client.print(htmStr);
                            delay(1000);
                            ESP.restart();
                        }
                        else
                        {
                            client.print("HTTP/1.1 417 Expectation Failed\r\n");
                            client.print("Connection: close\r\n");
                        }
                    }
                    else if (instr(path, "spooldata"))
                    {
                        String pdata = client.readString();
                        client.flush();
                        String materialColor = split(pdata, "materialColor[", "]");
                        materialColor.replace("#", "");
                        String filamentId = "1" + GetMaterialID(split(pdata, "materialType[", "]")); // material_database.json
                        String vendorId = "0276";                                                    // 0276 creality
                        String color = "0" + materialColor;
                        String filamentLen = GetMaterialLength(split(pdata, "materialWeight[", "]"));
                        String serialNum = String(random(100000, 999999)); // 000001
                        String reserve = "000000";
                        spoolData = "AB124" + vendorId + "A2" + filamentId + color + filamentLen + serialNum + reserve + "00000000";
                        File file = SPIFFS.open("/spool.ini", "w");
                        if (file)
                        {
                            file.print(spoolData);
                            file.close();
                        }
                        client.print("HTTP/1.1 200 OK\r\n");
                        client.print("Content-type: text/plain\r\n");
                        client.print("Content-Length: 2\r\n");
                        client.print("Connection: close\r\n\r\nOK");
                    }
                    else
                    {
                        client.flush();
                        client.print("HTTP/1.1 404 Not Found\r\n");
                        client.print("Connection: close\r\n");
                    }
                }
                client.stop();
            }
        }
    }
}

String GetMaterialLength(String materialWeight)
{
    if (materialWeight == "1 KG")
    {
        return "0330";
    }
    else if (materialWeight == "750 G")
    {
        return "0247";
    }
    else if (materialWeight == "600 G")
    {
        return "0198";
    }
    else if (materialWeight == "500 G")
    {
        return "0165";
    }
    else if (materialWeight == "250 G")
    {
        return "0082";
    }
    return "0330";
}

String GetMaterialID(String materialName)
{
    if (materialName == "Hyper PLA")
    {
        return "01001";
    }
    else if (materialName == "Hyper PLA-CF")
    {
        return "02001";
    }
    else if (materialName == "Hyper PETG")
    {
        return "06002";
    }
    else if (materialName == "Hyper ABS")
    {
        return "03001";
    }
    else if (materialName == "CR-PLA")
    {
        return "04001";
    }
    else if (materialName == "CR-Silk")
    {
        return "05001";
    }
    else if (materialName == "CR-PETG")
    {
        return "06001";
    }
    else if (materialName == "CR-ABS")
    {
        return "07001";
    }
    else if (materialName == "Ender-PLA")
    {
        return "08001";
    }
    else if (materialName == "EN-PLA+")
    {
        return "09001";
    }
    else if (materialName == "HP-TPU")
    {
        return "10001";
    }
    else if (materialName == "CR-Nylon")
    {
        return "11001";
    }
    else if (materialName == "CR-PLA Carbon")
    {
        return "13001";
    }
    else if (materialName == "CR-PLA Matte")
    {
        return "14001";
    }
    else if (materialName == "CR-PLA Fluo")
    {
        return "15001";
    }
    else if (materialName == "CR-TPU")
    {
        return "16001";
    }
    else if (materialName == "CR-Wood")
    {
        return "17001";
    }
    else if (materialName == "HP Ultra PLA")
    {
        return "18001";
    }
    else if (materialName == "HP-ASA")
    {
        return "19001";
    }
    else if (materialName == "Generic PLA")
    {
        return "00001";
    }
    else if (materialName == "Generic PLA-Silk")
    {
        return "00002";
    }
    else if (materialName == "Generic PETG")
    {
        return "00003";
    }
    else if (materialName == "Generic ABS")
    {
        return "00004";
    }
    else if (materialName == "Generic TPU")
    {
        return "00005";
    }
    else if (materialName == "Generic PLA-CF")
    {
        return "00006";
    }
    else if (materialName == "Generic ASA")
    {
        return "00007";
    }
    else if (materialName == "Generic PA")
    {
        return "00008";
    }
    else if (materialName == "Generic PA-CF")
    {
        return "00009";
    }
    else if (materialName == "Generic BVOH")
    {
        return "00010";
    }
    else if (materialName == "Generic PVA")
    {
        return "00011";
    }
    else if (materialName == "Generic HIPS")
    {
        return "00012";
    }
    else if (materialName == "Generic PET-CF")
    {
        return "00013";
    }
    else if (materialName == "Generic PETG-CF")
    {
        return "00014";
    }
    else if (materialName == "Generic PA6-CF")
    {
        return "00015";
    }
    else if (materialName == "Generic PAHT-CF")
    {
        return "00016";
    }
    else if (materialName == "Generic PPS")
    {
        return "00017";
    }
    else if (materialName == "Generic PPS-CF")
    {
        return "00018";
    }
    else if (materialName == "Generic PP")
    {
        return "00019";
    }
    else if (materialName == "Generic PET")
    {
        return "00020";
    }
    else if (materialName == "Generic PC")
    {
        return "00021";
    }
    return "00001";
}

void loadConfig()
{
    if (SPIFFS.exists("/config.ini"))
    {
        File file = SPIFFS.open("/config.ini", "r");
        if (file)
        {
            String iniData;
            while (file.available())
            {
                char chnk = file.read();
                iniData += chnk;
            }
            file.close();
            if (instr(iniData, "AP_SSID="))
            {
                AP_SSID = split(iniData, "AP_SSID=", "\r\n");
                AP_SSID.trim();
            }

            if (instr(iniData, "AP_PASS="))
            {
                AP_PASS = split(iniData, "AP_PASS=", "\r\n");
                AP_PASS.trim();
            }

            if (instr(iniData, "WIFI_SSID="))
            {
                WIFI_SSID = split(iniData, "WIFI_SSID=", "\r\n");
                WIFI_SSID.trim();
            }

            if (instr(iniData, "WIFI_PASS="))
            {
                WIFI_PASS = split(iniData, "WIFI_PASS=", "\r\n");
                WIFI_PASS.trim();
            }

            if (instr(iniData, "WIFI_HOST="))
            {
                WIFI_HOSTNAME = split(iniData, "WIFI_HOST=", "\r\n");
                WIFI_HOSTNAME.trim();
            }
        }
    }
    else
    {
        File file = SPIFFS.open("/config.ini", "w");
        if (file)
        {
            file.print("\r\nAP_SSID=" + AP_SSID + "\r\nAP_PASS=" + AP_PASS + "\r\nWIFI_SSID=" + WIFI_SSID + "\r\nWIFI_PASS=" + WIFI_PASS + "\r\nWIFI_HOST=" + WIFI_HOSTNAME + "\r\n");
            file.close();
        }
    }

    if (SPIFFS.exists("/spool.ini"))
    {
        File file = SPIFFS.open("/spool.ini", "r");
        if (file)
        {
            String iniData;
            while (file.available())
            {
                char chnk = file.read();
                iniData += chnk;
            }
            file.close();
            spoolData = iniData;
        }
    }
    else
    {
        File file = SPIFFS.open("/spool.ini", "w");
        if (file)
        {
            file.print(spoolData);
            file.close();
        }
    }
}

String split(String str, String from, String to)
{
    String tmpstr = str;
    tmpstr.toLowerCase();
    from.toLowerCase();
    to.toLowerCase();
    int pos1 = tmpstr.indexOf(from);
    int pos2 = tmpstr.indexOf(to, pos1 + from.length());
    String retval = str.substring(pos1 + from.length(), pos2);
    return retval;
}

bool instr(String str, String search)
{
    int result = str.indexOf(search);
    if (result == -1)
    {
        return false;
    }
    return true;
}
