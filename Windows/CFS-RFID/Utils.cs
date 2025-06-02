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

        public static void LoadMaterials(string pType)
        {
            MatDB.LoadFilaments(pType); 
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

        public static string GetMaterialInfo(string materialId)
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

        public static String GetMaterialLength(String materialWeight)
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

        public static String GetMaterialWeight(String materialLength)
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

        public static String[] printerTypes = {
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
    }
}
