using PCSC;
using PCSC.Iso7816;
using PCSC.Monitoring;
using System;
using System.Drawing;
using System.Linq;
using System.Windows.Forms;
using static CFS_RFID.Utils;

namespace CFS_RFID
{

    public partial class MainForm : Form
    {
        private ISCardContext context = null;
        private IsoReader isoReader;
        private Monitor monitor;
        private Reader reader;
        private string DbVersion = "0";
        private string MaterialColor, PrinterType, MaterialName, MaterialID, MaterialWeight;
        System.Windows.Forms.ToolTip upTip, delTip, edtTip, addTip, updTip;

        private void CardInserted(CardStatusEventArgs args)
        {
            try
            {
                isoReader = new IsoReader(context: context, readerName: args.ReaderName, 
                    mode: SCardShareMode.Shared, protocol: SCardProtocol.Any, releaseContextOnDispose: false);
                reader = new Reader(isoReader);
                Invoke((MethodInvoker)delegate ()
                {
                    lblUid.Text = BitConverter.ToString(reader.GetData()).Replace("-", " ");
                    if (chkAutoRead.Checked)
                    {
                        ReadSpoolData();
                    }
                });
            }
            catch { }
        }


        private void CardRemoved(CardStatusEventArgs args)
        {
            try
            {
                if (isoReader != null)
                {
                    isoReader.Disconnect(SCardReaderDisposition.Leave);
                    isoReader = null;
                    reader = null;
                }

                Invoke((MethodInvoker)delegate ()
                {
                    lblUid.Text = string.Empty;
                });
            }
            catch { }
        }


        private void ConnectReader()
        {
            lblConnect.Visible = true;
            lblConnect.ForeColor = Color.MediumSeaGreen;
            lblConnect.Text = "Connecting...";
            btnRead.Visible = false;
            btnWrite.Visible = false;
            chkAutoRead.Visible = false;
            btnColor.Visible = false;
            materialWeight.Visible = false;
            lblUid.Visible = false;
            lblTagId.Visible = false;
            lblAutoRead.Visible = false;
            ActiveControl = lblConnect;

            try
            {
                if (context == null)
                {
                    context = ContextFactory.Instance.Establish(SCardScope.System);
                }
                var readers = context.GetReaders();


                if (readers.Length > 0)
                {
                    monitor?.Dispose();
                    monitor = new Monitor(readers);
                    monitor.CardInserted += CardInserted;
                    monitor.CardRemoved += CardRemoved;

                    lblConnect.Visible = false;
                    lblConnect.Text = string.Empty;
                    btnRead.Visible = true;
                    btnWrite.Visible = true;
                    chkAutoRead.Visible = true;
                    btnColor.Visible = true;
                    materialWeight.Visible = true;
                    lblUid.Visible = true;
                    lblTagId.Visible = true;
                    ActiveControl = lblMsg;
                    lblAutoRead.Visible = true;

                }
                else
                {

                    Crumpet(lblMsg, "Connect Failed", 2000);
                    lblConnect.Visible = true;
                    lblConnect.ForeColor = Color.IndianRed;
                    lblConnect.Text = "No Reader Found\nClick here to connect";
                    btnRead.Visible = false;
                    btnWrite.Visible = false;
                    chkAutoRead.Visible = false;
                    btnColor.Visible = false;
                    materialWeight.Visible = false;
                    lblUid.Visible = false;
                    lblTagId.Visible = false;
                    ActiveControl = lblConnect;
                    lblAutoRead.Visible = false;

                }
            }
            catch (Exception e)
            {
                lblConnect.Visible = true;
                lblConnect.ForeColor = Color.IndianRed;
                lblConnect.Text = "NFC reader failed";
                btnRead.Visible = false;
                btnWrite.Visible = false;
                chkAutoRead.Visible = false;
                btnColor.Visible = false;
                materialWeight.Visible = false;
                lblUid.Visible = false;
                lblTagId.Visible = false;
                ActiveControl = lblConnect;
                lblAutoRead.Visible = false;
                MessageBox.Show(this, e.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }


        public MainForm()
        {
            InitializeComponent();
        }

        private void MainForm_Load(object sender, EventArgs e)
        {

            BackColor = ColorTranslator.FromHtml("#F4F4F4");

            btnRead.BackColor = ColorTranslator.FromHtml("#1976D2");
            btnWrite.BackColor = ColorTranslator.FromHtml("#1976D2");

            SetDBfile("k2.json", null);
            SetDBfile("k1.json", null);
            SetDBfile("hi.json", null);

            materialName.Text = "PLA";
            materialWeight.Text = "1 KG";
            MaterialColor = "0000FF";
            btnColor.BackColor = ColorTranslator.FromHtml("#" + MaterialColor);
            btnUpload.ForeColor = BackColor;
            btnEdit.ForeColor = BackColor;
            btnDel.ForeColor = BackColor;
            btnAdd.ForeColor = BackColor;

            upTip = new System.Windows.Forms.ToolTip();
            upTip.SetToolTip(btnUpload, "Upload database to printer");
            delTip = new System.Windows.Forms.ToolTip();
            delTip.SetToolTip(btnDel, "Delete selected filament");
            edtTip = new System.Windows.Forms.ToolTip();
            edtTip.SetToolTip(btnEdit, "Edit selected filament");
            addTip = new System.Windows.Forms.ToolTip();
            addTip.SetToolTip(btnAdd, "Add a new filament");
            updTip = new System.Windows.Forms.ToolTip();
            updTip.SetToolTip(btnUpdate, "Download database from printer");

            printerModel.Items.AddRange(printerTypes);
            printerModel.SelectedIndex = Settings.GetSetting("printerType", 0);

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
                                    materialName.SelectedIndex = materialName.FindStringExact(MaterialName);
                                    materialWeight.SelectedIndex = materialWeight.FindStringExact(GetMaterialWeight(length));
                                    Crumpet(lblMsg, "Data read from tag", 2000);
                                }
                                else
                                {
                                    Crumpet(lblMsg, "Unknown or empty tag", 2000);
                                }
                            }
                            catch
                            {
                                Crumpet(lblMsg, "Error reading tag", 2000);
                            }
                        }
                    }
                }
            }
            catch(Exception e)
            {
                Crumpet(lblMsg, "Error with reader or no tag", 2000);
                MessageBox.Show(this, e.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
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
            catch(Exception e)
            {
                Crumpet(lblMsg, "Error with reader or no tag", 2000);
                MessageBox.Show(this, e.Message, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }


        private void BtnRead_Click(object sender, EventArgs e)
        {
            ReadSpoolData();
        }


        private void BtnWrite_Click(object sender, EventArgs e)
        {
            WriteSpoolData(MaterialID, MaterialColor, GetMaterialLength(MaterialWeight));
        }


        private void BtnColor_Click(object sender, EventArgs e)
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


        private void MaterialWeight_SelectedIndexChanged(object sender, EventArgs e)
        {
            try
            {
                MaterialWeight = materialWeight.Items[materialWeight.SelectedIndex].ToString();
            }
            catch { }
        }


        private void PrinterModel_SelectedIndexChanged(object sender, EventArgs e)
        {
            try
            {
                PrinterType = printerModel.Items[printerModel.SelectedIndex].ToString();
                LoadMaterials(PrinterType);
                DbVersion = GetDatabaseVersion(PrinterType);
                vendorName.Items.Clear();
                materialName.Items.Clear();
                vendorName.Items.AddRange(GetMaterialBrands());
                vendorName.SelectedIndex = 0;
            }
            catch { }
        }


        void AddFilament()
        {
            try
            {
                FilamentForm filamentForm = new FilamentForm
                {
                    SelkectedFilament = MaterialID,
                    IsEdit = false,
                    StartPosition = FormStartPosition.CenterParent
                };
                DialogResult result = filamentForm.ShowDialog();
                if (result == DialogResult.OK)
                {
                    SaveMaterials(PrinterType, DbVersion);
                    vendorName.Items.Clear();
                    materialName.Items.Clear();
                    LoadMaterials(PrinterType);
                    DbVersion = GetDatabaseVersion(PrinterType);
                    vendorName.Items.AddRange(GetMaterialBrands());
                    vendorName.SelectedIndex = 0;

                }
                filamentForm.Dispose();
            }
            catch { }
        }


        void EditFilament()
        {
            try
            {
                FilamentForm filamentForm = new FilamentForm
                {
                    SelkectedFilament = MaterialID,
                    IsEdit = true,
                    StartPosition = FormStartPosition.CenterParent
                };
                DialogResult result = filamentForm.ShowDialog();
                if (result == DialogResult.OK)
                {
                    SaveMaterials(PrinterType, DbVersion);
                    vendorName.Items.Clear();
                    materialName.Items.Clear();
                    LoadMaterials(PrinterType);
                    DbVersion = GetDatabaseVersion(PrinterType);
                    vendorName.Items.AddRange(GetMaterialBrands());
                    vendorName.SelectedIndex = 0;

                }
                filamentForm.Dispose();
            }
            catch { }
        }


        void DeleteFilament()
        {
            try
            {
                DialogResult result = MessageBox.Show(
                    "Do you want to Delete?\n\n    "
                    + vendorName.Text + "\n    "
                    + materialName.Text,
                    "Delete Filament",
                    MessageBoxButtons.OKCancel,
                    MessageBoxIcon.Question);
                if (result == DialogResult.OK)
                {
                    RemoveMaterial(MaterialID);
                    SaveMaterials(PrinterType, DbVersion);
                    vendorName.Items.Clear();
                    materialName.Items.Clear();
                    LoadMaterials(PrinterType);
                    DbVersion = GetDatabaseVersion(PrinterType);
                    vendorName.Items.AddRange(GetMaterialBrands());
                    vendorName.SelectedIndex = 0;
                }
            }
            catch { }
        }


        private void BtnDel_Click(object sender, EventArgs e)
        {
            DeleteFilament();
        }


        private void BtnAdd_Click(object sender, EventArgs e)
        {
            AddFilament();
        }


        private void BtnEdit_Click(object sender, EventArgs e)
        {
            EditFilament();
        }


        private void LblConnect_Click(object sender, EventArgs e)
        {
            ConnectReader();
        }


        private void BtnUpload_Click(object sender, EventArgs e)
        {
            try
            {
               UploadForm uploadForm = new UploadForm
               {
                    SelectedPrinter = PrinterType,
                    StartPosition = FormStartPosition.CenterParent
                };
                DialogResult result = uploadForm.ShowDialog();
                if (result == DialogResult.OK)
                {
                    vendorName.Items.Clear();
                    materialName.Items.Clear();
                    LoadMaterials(PrinterType);
                    DbVersion = GetDatabaseVersion(PrinterType);
                    vendorName.Items.AddRange(GetMaterialBrands());
                    vendorName.SelectedIndex = 0;
                }
                uploadForm.Dispose();
            }
            catch { }
        }

        private void BtnUpdate_Click(object sender, EventArgs e)
        {
            try
            {
                UpdateForm updateForm = new UpdateForm
                {
                    SelectedPrinter = PrinterType,
                    StartPosition = FormStartPosition.CenterParent
                };
                DialogResult result = updateForm.ShowDialog();
                if (result == DialogResult.OK)
                {
                    vendorName.Items.Clear();
                    materialName.Items.Clear();
                    LoadMaterials(PrinterType);
                    DbVersion = GetDatabaseVersion(PrinterType);
                    vendorName.Items.AddRange(GetMaterialBrands());
                    vendorName.SelectedIndex = 0;
                }
                updateForm.Dispose();
            }
            catch { }
        }

        private void BtnUpload_MouseLeave(object sender, EventArgs e)
        {
            upTip.Hide(btnUpload);
        }

        private void BtnUpdate_MouseLeave(object sender, EventArgs e)
        {
            updTip.Hide(btnUpdate);
        }

        private void BtnDel_MouseLeave(object sender, EventArgs e)
        {
            delTip.Hide(btnDel);
        }

        private void BtnEdit_MouseLeave(object sender, EventArgs e)
        {
            edtTip.Hide(btnEdit);
        }

        private void BtnAdd_MouseLeave(object sender, EventArgs e)
        {
            addTip.Hide(btnAdd);
        }

        private void MainForm_FormClosed(object sender, FormClosedEventArgs e)
        {
            try{Environment.Exit(0);}catch{}
        }

        private void PrinterModel_SelectionChangeCommitted(object sender, EventArgs e)
        {
            Settings.SaveSetting("printerType", printerModel.SelectedIndex);
        }

        private void VendorName_SelectedIndexChanged(object sender, EventArgs e)
        {
            try
            {
                materialName.Items.Clear();
                materialName.Items.AddRange(GetMaterialsByBrand(vendorName.Text));
                materialName.SelectedIndex = 0;
            }
            catch { }
        }

        private void MaterialName_SelectedIndexChanged(object sender, EventArgs e)
        {
            try
            {
                MaterialID = GetMaterialID(materialName.Items[materialName.SelectedIndex].ToString());
            }
            catch { }
        }


    }
}
