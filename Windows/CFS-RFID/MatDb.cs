
using System;
using System.Collections.Generic;
using System.Text;
using CFS_RFID.Properties;
using Newtonsoft.Json.Linq;

public class MatDB
{

    public static List<Filament> mdb;

    public static void LoadFilaments(string pType)
    {
        JObject materials;
        mdb = new List<Filament>();
        try
        {
            if (pType.ToLower() == "k2")
            {
                materials = JObject.Parse(Encoding.ASCII.GetString(Resources.k2));
            }
            else if (pType.ToLower() == "k1")
            {
                materials = JObject.Parse(Encoding.ASCII.GetString(Resources.k1));
            }
            else
            {
                materials = JObject.Parse(Encoding.ASCII.GetString(Resources.hi));
            }
            JObject result = (JObject)materials["result"];
            JArray list = (JArray)result["list"];
            int i = 0;
            foreach (JToken itemToken in list)
            {
                JObject item = (JObject)itemToken;
                JObject baseObject = (JObject)item["base"];
                Filament filament = new Filament();
                filament.FilamentId = baseObject["id"].Value<string>().Trim();
                filament.FilamentName = baseObject["name"].Value<string>().Trim();
                filament.FilamentVendor = baseObject["brand"].Value<string>().Trim();
                filament.FilamentParam = item.ToString(Newtonsoft.Json.Formatting.None);
                mdb.Add(filament);
                i++;
            }

        }
        catch (Exception)
        {

        }
    }

    public static Filament GetFilamentById(string filamentId)
    {
        try
        {
            foreach (Filament item in mdb)
            {
                if (item.FilamentId.Trim() == filamentId)
                {
                    return item;
                }
            }
            return null;
        }
        catch (Exception)
        {
            return null;
        }
    }

    public static Filament GetFilamentByName(string filamentName)
    {
        try
        {
            foreach (Filament item in mdb)
            {
                if (item.FilamentName.Trim() == filamentName)
                {
                    return item;
                }
            }
            return null;
        }
        catch (Exception)
        {
            return null;
        }
    }

    public static List<Filament> GetFilamentsByVendor(string filamentVendor)
    {
        List<Filament> filaments = new List<Filament>();
        try
        {
            foreach (Filament item in mdb)
            {
                if (item.FilamentVendor.Trim() == filamentVendor)
                {
                    filaments.Add(item);
                }
            }
            return filaments;
        }
        catch (Exception)
        {
            return filaments;
        }
    }

    public static List<Filament> GetAllFilaments()
    {
        return mdb;
    }
}






