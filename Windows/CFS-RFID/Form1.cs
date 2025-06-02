using System;
using System.Drawing;
using System.Linq;
using System.Reflection;
using System.Windows.Forms;
using PCSC;
using PCSC.Iso7816;
using PCSC.Monitoring;
using static CFS_RFID.Utils;


namespace CFS_RFID
{

    public partial class Form1 : Form
    {
        private ISCardContext context;
        private IsoReader isoReader;
        private Monitor monitor;
        private Reader reader;
        private string MaterialColor, PrinterType, MaterialName, MaterialID, MaterialWeight;


        private void CardInserted(CardStatusEventArgs args)
        {
            try
            {
                isoReader = new IsoReader(context: context, readerName: args.ReaderName, mode: SCardShareMode.Shared, protocol: SCardProtocol.Any, releaseContextOnDispose: false);
                reader = new Reader(isoReader);
                Invoke((MethodInvoker)delegate ()
                {
                    lblUid.Text = BitConverter.ToString(reader.GetData()).Replace("-", " ");
                    if (checkBox1.Checked)
                    {
                        ReadSpoolData();
                    }
                });
            }
            catch (Exception ex)
            {
                Crumpet(lblMsg, ex.Message, 2000);
            }
        }


        private void CardRemoved(CardStatusEventArgs args)
        {
            try
            {
                if (isoReader != null)
                {
                    isoReader.Disconnect(SCardReaderDisposition.Reset);
                }

                Invoke((MethodInvoker)delegate ()
                {
                    lblUid.Text = string.Empty;
                });
            }
            catch (Exception ex)
            {
                Crumpet(lblMsg, ex.Message, 2000);
            }
        }


        private void ConnectReader() {
            try
            {
                var readers = context.GetReaders();
                if (readers.Length > 0)
                {
                    if (monitor != null)
                    {
                        monitor.Dispose();
                    }
                    monitor = new Monitor(readers);
                    monitor.CardInserted += CardInserted;
                    monitor.CardRemoved += CardRemoved;
                    lblConnect.Visible = false;
                }
                else
                {
                    lblConnect.Visible = true;
                }
            }
            catch (Exception) {}
        }


        public Form1()
        {
            InitializeComponent();
        }


        private void Form1_Load(object sender, EventArgs e)
        {
            context = ContextFactory.Instance.Establish(SCardScope.System);

            btnDel.Visible = false;
            btnAdd.Visible = false;
            btnEdit.Visible = false;



            BackColor = ColorTranslator.FromHtml("#F4F4F4");
            lblConnect.BackColor = ColorTranslator.FromHtml("#F4F4F4");
            btnRead.BackColor = ColorTranslator.FromHtml("#1976D2");
            btnWrite.BackColor = ColorTranslator.FromHtml("#1976D2");

            materialType.Text = "PLA";
            materialWeight.Text = "1 KG";
            MaterialColor = "0000FF";
            btnColor.BackColor = ColorTranslator.FromHtml("#" + MaterialColor);

            printerModel.Items.AddRange(printerTypes);
            printerModel.SelectedIndex = Settings.GetSetting("printerType", 0);

            lblConnect.Location = new Point(0, 0);


            ConnectReader();



        }


        public void ReadSpoolData()
        {
            try
            {
                if (reader == null)
                {
                    Crumpet(lblMsg, "Error reading tag", 2000);
                }
                else
                {
                    reader.LoadKey(0, 0, new byte[] { 255, 255, 255, 255, 255, 255 });
                    var uid = reader.GetData();
                    lblUid.Text = BitConverter.ToString(uid).Replace("-", " ");
                    byte[] encKey = CreateKey(uid);
                    var loadKeySuccessful = reader.LoadKey(0, 1, encKey);
                    if (reader.Authenticate(0, 7, 96, 0))
                    {
                        Crumpet(lblMsg, "Empty tag", 2000);
                    }
                    else
                    {
                        string tagData = ReadTag(reader);
                        if (tagData != null && tagData.Length >= 40)
                        {
                            try
                            {
                                string materialId = tagData.Substring(12, 5);
                                string[] materialInfo = GetMaterialName(materialId);
                                if (materialInfo != null && materialInfo.Length >= 2)
                                {
                                    MaterialColor = tagData.Substring(18, 6);
                                    string length = tagData.Substring(24, 4);
                                    btnColor.BackColor = ColorTranslator.FromHtml("#" + MaterialColor);
                                    MaterialName = materialInfo[0];
                                    vendorName.SelectedIndex = vendorName.FindStringExact(materialInfo[1]);
                                    materialType.SelectedIndex = materialType.FindStringExact(MaterialName);
                                    materialWeight.SelectedIndex = materialWeight.FindStringExact(GetMaterialWeight(length));
                                    Crumpet(lblMsg, "Data read from tag", 2000);
                                }
                                else
                                {
                                    Crumpet(lblMsg, "Unknown or empty tag", 2000);
                                }
                            }
                            catch (Exception)
                            {
                                Crumpet(lblMsg, "Error reading tag", 2000);
                            }
                        }

                    }
                }
            }
            catch (Exception) {
                Crumpet(lblMsg, "Error with reader or no tag", 2000);
            }
        }


        void WriteSpoolData(string MaterialID, string Color, string Length)
        {
            try
            {
                if (reader == null)
                {
                    Crumpet(lblMsg, "Error writing tag", 2000);
                }
                else
                {
                    reader.LoadKey(0, 0, new byte[] { 255, 255, 255, 255, 255, 255 });
                    var uid = reader.GetData();
                    lblUid.Text = BitConverter.ToString(uid).Replace("-", " ");
                    byte[] encKey = CreateKey(uid);
                    var loadKeySuccessful = reader.LoadKey(0, 1, encKey);
                    string filamentId = "1" + MaterialID;
                    string vendorId = "0276";
                    string color = "0" + Color;
                    string serialNum = "000001"; // RandomSerial();
                    string reserve = "000000";
                    WriteTag(reader, "AB124" + vendorId + "A2" + filamentId + color + Length + serialNum + reserve);
                    if (reader.Authenticate(0, 7, 96, 0))
                    {
                        byte[] data = reader.ReadBinary(0, 7, 16);
                        Array.Copy(encKey, 0, data, 0, encKey.Length);
                        Array.Copy(encKey, 0, data, 10, encKey.Length);
                        reader.UpdateBinary(0, 7, data.Take(16).ToArray());
                    }
                    Crumpet(lblMsg, "Data written to tag", 2000);
                }
            }
            catch (Exception)
            {
                Crumpet(lblMsg, "Error with reader or no tag", 2000);  
            }
        }


        private void btnRead_Click(object sender, EventArgs e)
        {
            ReadSpoolData();
        }


        private void btnWrite_Click(object sender, EventArgs e)
        {
            WriteSpoolData(MaterialID, MaterialColor, GetMaterialLength(MaterialWeight));
        }


        private void btnColor_Click(object sender, EventArgs e)
        {
            ColorDialog dlg = new ColorDialog
            {
                AllowFullOpen = true,
                FullOpen = true,
                AnyColor = true,
                Color = ColorTranslator.FromHtml("#" + MaterialColor)
            };
            if (dlg.ShowDialog() == DialogResult.OK)
            {
                btnColor.BackColor = dlg.Color;
                MaterialColor = (dlg.Color.ToArgb() & 0x00FFFFFF).ToString("X6");
            }
        }


        private void materialWeight_SelectedIndexChanged(object sender, EventArgs e)
        {
            MaterialWeight = materialWeight.Items[materialWeight.SelectedIndex].ToString();
        }

        private void printerModel_SelectedIndexChanged(object sender, EventArgs e)
        {
            PrinterType = printerModel.Items[printerModel.SelectedIndex].ToString();
            LoadMaterials(PrinterType);
            vendorName.Items.Clear();
            vendorName.Items.AddRange(GetMaterialBrands());
            vendorName.SelectedIndex = 0;
        }

        private void lblConnect_Click(object sender, EventArgs e)
        {
            ConnectReader();
        }

        private void Form1_FormClosed(object sender, FormClosedEventArgs e)
        {
            Environment.Exit(0);
        }


        private void printerModel_SelectionChangeCommitted(object sender, EventArgs e)
        {
            Settings.SaveSetting("printerType", printerModel.SelectedIndex);
        }


        private void vendorName_SelectedIndexChanged(object sender, EventArgs e)
        {
            materialType.Items.Clear();
            materialType.Items.AddRange(GetMaterialsByBrand(vendorName.Text));
            materialType.SelectedIndex = 0;
        }


        private void materialType_SelectedIndexChanged(object sender, EventArgs e)
        {
            MaterialID = GetMaterialID(materialType.Items[materialType.SelectedIndex].ToString());
        }








    }
}
