using CFS_RFID.Properties;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Renci.SshNet;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace CFS_RFID
{
    internal class Utils
    {

        public static void SaveMaterials(string pType, string version)
        {
            MatDB.SaveFilaments(pType.ToLower(), version);
        }

        public static void AddMaterial(Filament filament)
        {
            MatDB.AddFilament(filament);
        }

        public static void EditMaterial(Filament filament)
        {
            MatDB.EditFilament(filament);
        }

        public static void RemoveMaterial(Filament filament)
        {
            MatDB.RemoveFilament(filament);
        }

        public static void RemoveMaterial(string materialId)
        {
            MatDB.RemoveFilament(MatDB.GetFilamentById(materialId));
        }

        public static void LoadMaterials(string pType)
        {
            MatDB.LoadFilaments(pType);
        }

        public static string GetDatabaseVersion(string pType)
        {
            return MatDB.GetVersion(pType);
        }

        public static void SetDatabaseVersion(string pType, string version)
        {
            MatDB.SetVersion(pType, version);
        }

        public static string[] GetMaterials()
        {
            List<Filament> items = MatDB.GetAllFilaments();
            string[] materials = new string[items.Count];
            for (int i = 0; i < items.Count; i++)
            {
                materials[i] = items[i].FilamentName;
            }
            return materials;
        }

        public static string[] GetMaterialsByBrand(string materialBrand)
        {
            List<Filament> items = MatDB.GetFilamentsByVendor(materialBrand);
            string[] materials = new string[items.Count];
            for (int i = 0; i < items.Count; i++)
            {
                materials[i] = items[i].FilamentName;
            }
            return materials;
        }

        public static string[] GetMaterialName(string materialId)
        {
            Filament item = MatDB.GetFilamentById(materialId);
            if (item == null)
            {
                return null;
            }
            else
            {
                return new string[] { item.FilamentName, item.FilamentVendor };
            }
        }

        public static string GetMaterialParam(string materialId)
        {
            Filament item = MatDB.GetFilamentById(materialId);
            return item.FilamentParam;
        }

        public static string GetMaterialID(string materialName)
        {
            Filament item = MatDB.GetFilamentByName(materialName);
            return item.FilamentId;
        }

        public static string GetMaterialBrand(string materialId)
        {
            Filament item = MatDB.GetFilamentById(materialId);
            return item.FilamentVendor;
        }

        public static Filament GetMaterialByID(string materialId)
        {
            return MatDB.GetFilamentById(materialId);
        }

        public static string[] GetMaterialBrands()
        {
            List<Filament> items = MatDB.GetAllFilaments();
            HashSet<string> uniqueBrandsSet = new HashSet<string>();
            foreach (Filament item in items)
            {
                uniqueBrandsSet.Add(item.FilamentVendor);
            }
            return uniqueBrandsSet.ToArray();
        }

        public static string GetMaterialLength(string materialWeight)
        {
            switch (materialWeight)
            {
                case "1 KG":
                    return "0330";
                case "750 G":
                    return "0247";
                case "600 G":
                    return "0198";
                case "500 G":
                    return "0165";
                case "250 G":
                    return "0082";
            }
            return "0330";
        }

        public static string GetMaterialWeight(string materialLength)
        {
            switch (materialLength)
            {
                case "0330":
                    return "1 KG";
                case "0247":
                    return "750 G";
                case "0198":
                    return "600 G";
                case "0165":
                    return "500 G";
                case "0082":
                    return "250 G";
            }
            return "1 KG";
        }

        public static string[] printerTypes = {
            "K2",
            "K1",
            "HI"};

        public static byte[] CreateKey(byte[] tagId)
        {
            try
            {
                using (AesCryptoServiceProvider aesAlg = new AesCryptoServiceProvider())
                {
                    aesAlg.Mode = CipherMode.ECB;
                    aesAlg.Padding = PaddingMode.None;
                    aesAlg.Key = new byte[]
                    {113, 51, 98, 117, 94, 116, 49, 110, 113, 102, 90, 40, 112, 102, 36, 49};
                    ICryptoTransform encryptor = aesAlg.CreateEncryptor(aesAlg.Key, null);
                    int x = 0;
                    byte[] encB = new byte[16];
                    for (int i = 0; i < 16; i++)
                    {
                        if (x >= 4) x = 0;
                        encB[i] = tagId[x];
                        x++;
                    }
                    byte[] encryptedBytes = encryptor.TransformFinalBlock(encB, 0, encB.Length);

                    return encryptedBytes.Take(6).ToArray();
                }
            }
            catch (Exception)
            {
                return new byte[] { 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF };
            }
        }

        public static byte[] CipherData(int mode, byte[] tagData)
        {
            try
            {
                using (AesCryptoServiceProvider aesAlg = new AesCryptoServiceProvider())
                {
                    aesAlg.Mode = CipherMode.ECB;
                    aesAlg.Padding = PaddingMode.None;
                    aesAlg.Key = new byte[]
                    {72, 64, 67, 70, 107, 82, 110, 122, 64, 75, 65, 116, 66, 74, 112, 50};
                    ICryptoTransform cryptoTransform;
                    if (mode == 1)
                    {
                        cryptoTransform = aesAlg.CreateEncryptor(aesAlg.Key, null);
                    }
                    else
                    {
                        cryptoTransform = aesAlg.CreateDecryptor(aesAlg.Key, null);
                    }
                    return cryptoTransform.TransformFinalBlock(tagData, 0, tagData.Length);
                }
            }
            catch (Exception)
            { }
            return null;
        }

        public static string ReadTag(Reader reader)
        {
            reader.Authenticate(0, 4, 96, 1);
            MemoryStream buff = new MemoryStream(48);
            buff.Write(reader.ReadBinary(0, 4, 16), 0, 16);
            buff.Write(reader.ReadBinary(0, 5, 16), 0, 16);
            buff.Write(reader.ReadBinary(0, 6, 16), 0, 16);
            return Encoding.UTF8.GetString(Utils.CipherData(0, buff.ToArray()));
        }

        public static void WriteTag(Reader reader, String tagData)
        {
            if (!reader.Authenticate(0, 7, 96, 1))
            {
                reader.Authenticate(0, 4, 96, 0);
            }
            byte[] sectorData = Encoding.UTF8.GetBytes(tagData + "00000000");
            int blockIndex = 4;
            for (int i = 0; i < 48; i += 16)
            {
                reader.UpdateBinary(0, (byte)blockIndex, Utils.CipherData(1, sectorData.Skip(i).Take(16).ToArray()));
                blockIndex++;
            }
        }

        public static string RandomSerial()
        {
            using (RNGCryptoServiceProvider rng = new RNGCryptoServiceProvider())
            {
                byte[] randomNumber = new byte[4];
                rng.GetBytes(randomNumber);
                int randomInt = BitConverter.ToInt32(randomNumber, 0);
                int randomNumberInRange = Math.Abs(randomInt % 900000);
                return randomNumberInRange.ToString("D6");
            }
        }

        public static void Crumpet(Label label, string message, int duration)
        {
            Task task = Task.Run(() =>
            {
                label.Invoke((MethodInvoker)delegate ()
                {
                    label.Text = message;
                });
                Thread.Sleep(duration);
            }).ContinueWith(t =>
            {
                label.Invoke((MethodInvoker)delegate ()
                {
                    label.Text = string.Empty;
                });
            }, TaskScheduler.Default);
        }



        public static bool CheckDBfile(string pType)
        {
            string filePath = AppDomain.CurrentDomain.BaseDirectory + "\\material_database\\" + pType + ".json";
            if (File.Exists(filePath))
            {
                return true;
            }
            return false;
        }

        public static byte[] GetDBfile(string pType)
        {
            string filePath = AppDomain.CurrentDomain.BaseDirectory + "\\material_database\\" + pType + ".json";
            try
            {
                byte[] fileBytes = File.ReadAllBytes(filePath);
                return fileBytes;
            }
            catch (Exception)
            {
                return null;
            }
        }

        public static void SetDBfile(string fileName, byte[] resData)
        {
            string filePath = AppDomain.CurrentDomain.BaseDirectory + "\\material_database\\" + fileName;
            if (File.Exists(filePath))
            {
                return;
            }
            string directory = Path.GetDirectoryName(filePath);
            if (!string.IsNullOrEmpty(directory) && !Directory.Exists(directory))
            {
                try
                {
                    Directory.CreateDirectory(directory);
                }
                catch (Exception)
                {
                    return;
                }
            }
            if (resData == null || resData.Length == 0)
            {
                if (fileName.Equals("k2.json", StringComparison.OrdinalIgnoreCase))
                {
                    resData = Resources.k2;
                }
                else if (fileName.Equals("k1.json", StringComparison.OrdinalIgnoreCase))
                {
                    resData = Resources.k1;
                }
                else
                {
                    resData = Resources.hi;
                }
            }
            using (MemoryStream memoryStream = new MemoryStream(resData))
            {
                byte[] readBytes = new byte[memoryStream.Length];
                int bytesRead = memoryStream.Read(readBytes, 0, readBytes.Length);
                memoryStream.Position = 0;
                using (FileStream fileStream = File.Create(filePath))
                {
                    memoryStream.CopyTo(fileStream);
                }
            }

        }

        public static string SendSShCommand(string psw, string host, string command)
        {
            using (var client = new SshClient(host, 22, "root", psw))
            {
                try
                {
                    client.Connect();
                    using (var cmd = client.CreateCommand(command))
                    {
                        cmd.CommandTimeout = TimeSpan.FromSeconds(5);
                        string result = cmd.Execute();
                        return result;
                    }
                }
                catch (Exception e)
                {
                    throw new Exception(e.Message);
                }
                finally
                {
                    if (client.IsConnected)
                    {
                        client.Disconnect();
                    }
                }
            }
        }

        public static void ResetJsonDB(string psw, string host, string pType, bool resetApp)
        {
            using (var client = new ScpClient(host, 22, "root", psw))
            {
                try
                {
                    byte[] resData;
                    if (pType.Equals("k2", StringComparison.OrdinalIgnoreCase))
                    {
                        resData = Resources.k2;
                    }
                    else if (pType.Equals("k1", StringComparison.OrdinalIgnoreCase))
                    {
                        SetJsonDB(Resources.k1o, psw, host, pType, "material_option.json");
                        resData = Resources.k1;
                    }
                    else
                    {
                        resData = Resources.hi;
                    }
                    SetJsonDB(resData, psw, host, pType, "material_database.json");
                    if (resetApp)
                    {
                        string filePath = AppDomain.CurrentDomain.BaseDirectory + "\\material_database\\" + pType.ToLower() + ".json";
                        using (MemoryStream memoryStream = new MemoryStream(resData))
                        {
                            byte[] readBytes = new byte[memoryStream.Length];
                            int bytesRead = memoryStream.Read(readBytes, 0, readBytes.Length);
                            memoryStream.Position = 0;
                            using (FileStream fileStream = File.Create(filePath))
                            {
                                memoryStream.CopyTo(fileStream);
                            }
                        }
                    }
                    SendSShCommand(psw, host, "reboot");
                }
                catch (Exception e)
                {
                    throw new Exception(e.Message);
                }
            }
        }

        public static void SetJsonDB(string psw, string host, string pType)
        {
            using (var client = new ScpClient(host, 22, "root", psw))
            {
                try
                {
                    client.Connect();
                    string filepath = "/mnt/UDISK/creality/userdata/box/material_database.json";
                    if (pType.Equals("k1", StringComparison.OrdinalIgnoreCase))
                    {
                        filepath = "/usr/data/creality/userdata/box/material_database.json";
                    }
                    using (var stream = new MemoryStream(GetDBfile(pType.ToLower())))
                    {
                        client.Upload(stream, filepath);
                    }
                }
                catch (Exception e)
                {
                    throw new Exception(e.Message);
                }
                finally
                {
                    if (client.IsConnected)
                    {
                        client.Disconnect();
                    }
                }
            }
        }

        public static void SetJsonDB(string dbData, string psw, string host, string pType, string fileName)
        {
            using (var client = new ScpClient(host, 22, "root", psw))
            {
                try
                {
                    client.Connect();
                    string filepath = "/mnt/UDISK/creality/userdata/box/" + fileName;
                    if (pType.Equals("k1", StringComparison.OrdinalIgnoreCase))
                    {
                        filepath = "/usr/data/creality/userdata/box/" + fileName;
                    }
                    using (var stream = new MemoryStream(Encoding.ASCII.GetBytes(dbData)))
                    {
                        client.Upload(stream, filepath);
                    }
                }
                catch (Exception e)
                {
                    throw new Exception(e.Message);
                }
                finally
                {
                    if (client.IsConnected)
                    {
                        client.Disconnect();
                    }
                }
            }
        }

        public static void SetJsonDB(byte[] dbData, string psw, string host, string pType, string fileName)
        {
            using (var client = new ScpClient(host, 22, "root", psw))
            {
                try
                {
                    client.Connect();
                    string filepath = "/mnt/UDISK/creality/userdata/box/" + fileName;
                    if (pType.Equals("k1", StringComparison.OrdinalIgnoreCase))
                    {
                        filepath = "/usr/data/creality/userdata/box/" + fileName;
                    }
                    using (var stream = new MemoryStream(dbData))
                    {
                        client.Upload(stream, filepath);
                    }
                }
                catch (Exception e)
                {
                    throw new Exception(e.Message);
                }
                finally
                {
                    if (client.IsConnected)
                    {
                        client.Disconnect();
                    }
                }
            }
        }

        public static void GetJsonDB(string psw, string host, string pType, string fileName)
        {
            string localfilePath = AppDomain.CurrentDomain.BaseDirectory + "\\material_database\\" + pType.ToLower() + ".json";
            string directory = Path.GetDirectoryName(localfilePath);
            if (!string.IsNullOrEmpty(directory) && !Directory.Exists(directory))
            {
                try
                {
                    Directory.CreateDirectory(directory);
                }
                catch (Exception e)
                {
                    throw new Exception(e.Message);
                }
            }
            using (var client = new ScpClient(host, 22, "root", psw))
            {
                try
                {
                    client.Connect();
                    string filepath = "/mnt/UDISK/creality/userdata/box/" + fileName;
                    if (pType.Equals("k1", StringComparison.OrdinalIgnoreCase))
                    {
                        filepath = "/usr/data/creality/userdata/box/" + fileName;
                    }
                    using (var stream = new FileStream(localfilePath, FileMode.Create))
                    {
                        client.Download(filepath, stream);
                    }
                }
                catch (Exception e)
                {
                    throw new Exception(e.Message);
                }
                finally
                {
                    if (client.IsConnected)
                    {
                        client.Disconnect();
                    }
                }
            }
        }

        public static void SaveMatOption(string psw, string host, string pType, bool reboot)
        {
            JObject materials = JObject.Parse(Encoding.ASCII.GetString(GetDBfile(pType.ToLower())));
            JObject options = new JObject();
            JObject result = (JObject)materials["result"];
            JArray list = (JArray)result["list"];
            HashSet<string> uniqueBrandsSet = new HashSet<string>();
            foreach (JToken itemToken in list)
            {
                JObject items = (JObject)itemToken;
                JObject baseObject = (JObject)items["base"];
                uniqueBrandsSet.Add(baseObject.Value<string>("brand"));
            }
            foreach (string brand in uniqueBrandsSet)
            {
                options[brand] = new JObject();
                JObject vendor = (JObject)options[brand];
                foreach (JToken itemToken in list)
                {
                    JObject items = (JObject)itemToken;
                    JObject baseObject = (JObject)items["base"];
                    if (baseObject.Value<string>("brand").Equals(brand))
                    {
                        string tmpType = baseObject.Value<string>("meterialType");
                        string name = baseObject.Value<string>("name");
                        if (vendor.ContainsKey(tmpType))
                        {
                            vendor[tmpType] = vendor.Value<string>(tmpType) + "\n" + name;
                        }
                        else
                        {
                            vendor[tmpType] = name;
                        }
                    }
                }
            }
            SetJsonDB(options.ToString(Formatting.Indented), psw, host, pType, "material_option.json");
            if (reboot)
            {
                SendSShCommand(psw, host, "reboot");
            }
        }

        public static string[] filamentVendors = {
            "3Dgenius",
            "3DJake",
            "3DXTECH",
            "3D BEST-Q",
            "3D Hero",
            "3D-Fuel",
            "Aceaddity",
            "AddNorth",
            "Amazon Basics",
            "AMOLEN",
            "Ankermake",
            "Anycubic",
            "Atomic",
            "AzureFilm",
            "BASF",
            "Bblife",
            "BCN3D",
            "Beyond Plastic",
            "California Filament",
            "Capricorn",
            "CC3D",
            "Colour Dream",
            "colorFabb",
            "Comgrow",
            "Cookiecad",
            "Creality",
            "CERPRiSE",
            "Das Filament",
            "DO3D",
            "DOW",
            "DSM",
            "Duramic",
            "ELEGOO",
            "Eryone",
            "Essentium",
            "eSUN",
            "Extrudr",
            "Fiberforce",
            "Fiberlogy",
            "FilaCube",
            "Filamentive",
            "Fillamentum",
            "FLASHFORGE",
            "Formfutura",
            "Francofil",
            "FilamentOne",
            "Fil X",
            "GEEETECH",
            "Generic",
            "Giantarm",
            "Gizmo Dorks",
            "GreenGate3D",
            "HATCHBOX",
            "Hello3D",
            "IC3D",
            "IEMAI",
            "IIID Max",
            "INLAND",
            "iProspect",
            "iSANMATE",
            "Justmaker",
            "Keene Village Plastics",
            "Kexcelled",
            "LDO",
            "MakerBot",
            "MatterHackers",
            "MIKA3D",
            "NinjaTek",
            "Nobufil",
            "Novamaker",
            "OVERTURE",
            "OVVNYXE",
            "Polymaker",
            "Priline",
            "Printed Solid",
            "Protopasta",
            "Prusament",
            "Push Plastic",
            "R3D",
            "Re-pet3D",
            "Recreus",
            "Regen",
            "Sain SMART",
            "SliceWorx",
            "Snapmaker",
            "SnoLabs",
            "Spectrum",
            "SUNLU",
            "TTYT3D",
            "Tianse",
            "UltiMaker",
            "Valment",
            "Verbatim",
            "VO3D",
            "Voxelab",
            "VOXELPLA",
            "YOOPAI",
            "Yousu",
            "Ziro",
            "Zyltech"};

        public static string[] filamentTypes = {
            "ABS",
            "ASA",
            "HIPS",
            "PA",
            "PA-CF",
            "PC",
            "PLA",
            "PLA-CF",
            "PVA",
            "PP",
            "TPU",
            "PETG",
            "BVOH",
            "PET-CF",
            "PETG-CF",
            "PA6-CF",
            "PAHT-CF",
            "PPS",
            "PPS-CF",
            "PET",
            "ASA-CF",
            "PA-GF",
            "PETG-GF",
            "PP-CF",
            "PCTG"};

    }
}
