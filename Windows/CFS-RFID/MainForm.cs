using PCSC;
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
        private Monitor monitor;
        private ICardReader icReader;
        private Reader reader;
        byte[] encKey;
        private string DbVersion = "0", FwVersion;
        private string MaterialColor, PrinterType, MaterialName, MaterialID, MaterialWeight;
        private readonly System.Windows.Forms.ToolTip toolTip = new System.Windows.Forms.ToolTip(), balloonTip = new System.Windows.Forms.ToolTip();

        private void CardInserted(CardStatusEventArgs args)
        {
            try
            {
                icReader = context.ConnectReader(args.ReaderName, SCardShareMode.Shared, SCardProtocol.Any);
                reader = new Reader(icReader);
                byte[] uid = reader.GetData();
                if (uid == null) {
                    Invoke((MethodInvoker)delegate ()
                    {
                        Toast.Show(this, "Tag not compatible", Toast.LENGTH_LONG, true);
                       
                    });
                    icReader.Dispose();
                    reader = null;
                    return;
                }
                if (!reader.LoadAuthenticationKeys(0, 0, KEY_DEFAULT))
                {
                    reader.LoadAuthenticationKeys(32, 0, KEY_DEFAULT);
                }
                encKey = CreateKey(uid);
                if (!reader.LoadAuthenticationKeys(0, 1, encKey))
                {
                    if (!reader.LoadAuthenticationKeys(32, 1, encKey))
                    {
                        Invoke((MethodInvoker)delegate ()
                        {
                            Toast.Show(this, "Failed to load encKey", Toast.LENGTH_SHORT);
                        });
                    }
                }
                Invoke((MethodInvoker)delegate ()
                {
                    lblUid.Text = BitConverter.ToString(uid).Replace("-", " ");
                    lblTagId.Visible = true;
                    if (string.IsNullOrEmpty(FwVersion))
                    {
                        FwVersion = ReaderVersion(reader);
                        Text = string.IsNullOrEmpty(FwVersion) ? "CFS RFID" : FwVersion;
                    }
                    if ((reader.Authentication10byte(7, 96, 0) || reader.Authentication6byte(7, 96, 0)))
                    {
                        balloonTip.Hide(imgEnc);
                        imgEnc.Visible = false;
                        lblUid.Left = lblTagId.Right;
                    }
                    else {
                        imgEnc.Visible = true;
                        balloonTip.SetToolTip(imgEnc, "Key: " + BitConverter.ToString(encKey).Replace("-", ""));
                        lblUid.Left = imgEnc.Right;
                    }
                    if (chkAutoRead.Checked)
                    {
                        ReadSpoolData();
                    }
                    else if (chkAutoWrite.Checked)
                    {
                        WriteSpoolData(MaterialID, MaterialColor, GetMaterialLength(MaterialWeight));
                    }
                });
            }
            catch { }
        }

        private void CardRemoved(CardStatusEventArgs args)
        {
            try
            {
                if (icReader != null)
                {
                    icReader.Dispose();
                    reader = null;
                }
                Invoke((MethodInvoker)delegate ()
                {
                    imgEnc.Visible = false;
                    lblUid.Text = string.Empty;
                    lblTagId.Visible = false;
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
            btnFormat.Visible = false;
            chkAutoRead.Visible = false;
            chkAutoWrite.Visible = false;
            btnColor.Visible = false;
            materialWeight.Visible = false;
            lblUid.Visible = false;
            lblAutoRead.Visible = false;
            lblAutoWrite.Visible = false;
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
                    btnFormat.Visible = true;
                    chkAutoRead.Visible = true;
                    chkAutoWrite.Visible = true;
                    btnColor.Visible = true;
                    materialWeight.Visible = true;
                    lblUid.Visible = true;
                    lblAutoRead.Visible = true;
                    lblAutoWrite.Visible = true;

                }
                else
                {

                    Toast.Show(this, "Connect Failed", Toast.LENGTH_SHORT);
                    lblConnect.Visible = true;
                    lblConnect.ForeColor = Color.IndianRed;
                    lblConnect.Text = "No Reader Found\nClick here to connect";
                    btnRead.Visible = false;
                    btnWrite.Visible = false;
                    btnFormat.Visible = false;
                    chkAutoRead.Visible = false;
                    chkAutoWrite.Visible = false;
                    btnColor.Visible = false;
                    materialWeight.Visible = false;
                    lblUid.Visible = false;
                    ActiveControl = lblConnect;
                    lblAutoRead.Visible = false;
                    lblAutoWrite.Visible = false;

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
                ActiveControl = lblConnect;
                lblAutoRead.Visible = false;
                Toast.Show(this, e.Message, Toast.LENGTH_LONG, true);
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

            btnRead.FlatAppearance.BorderSize = 0;
            btnWrite.FlatAppearance.BorderSize = 0;
            btnColor.FlatAppearance.BorderSize = 0;
            btnDel.FlatAppearance.BorderSize = 0;
            btnEdit.FlatAppearance.BorderSize = 0;
            btnAdd.FlatAppearance.BorderSize = 0;
            btnFormat.FlatAppearance.BorderSize = 0;
            btnUpdate.FlatAppearance.BorderSize = 0;
            btnUpload.FlatAppearance.BorderSize = 0;

            toolTip.SetToolTip(btnUpload, "Upload database to printer");
            toolTip.SetToolTip(btnDel, "Delete selected filament");
            toolTip.SetToolTip(btnEdit, "Edit selected filament");
            toolTip.SetToolTip(btnAdd, "Add a new filament");
            toolTip.SetToolTip(btnUpdate, "Download database from printer");
            toolTip.SetToolTip(btnFormat, "Format tag");
            balloonTip.IsBalloon = true;

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
                    Toast.Show(this, "Error reading tag", Toast.LENGTH_SHORT);
                }
                else
                {
                    if ((reader.Authentication10byte(7, 96, 0) || reader.Authentication6byte(7, 96, 0)))
                    {
                        Toast.Show(this, "Empty tag", Toast.LENGTH_SHORT);
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
                                    Toast.Show(this, "Data read from tag", Toast.LENGTH_SHORT);
                                }
                                else
                                {
                                    Toast.Show(this, "Unknown or empty tag", Toast.LENGTH_SHORT);
                                }
                            }
                            catch
                            {
                                Toast.Show(this, "Error reading tag", Toast.LENGTH_SHORT);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Toast.Show(this, e.Message, Toast.LENGTH_LONG, true);
            }
        }

        void WriteSpoolData(string MaterialID, string Color, string Length)
        {
            try
            {
                if (reader == null)
                {
                    Toast.Show(this, "Error writing tag", Toast.LENGTH_SHORT);
                }
                else
                {
                    string filamentId = "1" + MaterialID;
                    string vendorId = "0276";
                    string color = "0" + Color;
                    string serialNum = "000001"; // RandomSerial();
                    string reserve = "000000";
                    string endblock = "00000000";
                    WriteTag(reader, "AB124" + vendorId + "A2" + filamentId + color + Length + serialNum + reserve + endblock);
                    if ((reader.Authentication10byte(7, 96, 0) || reader.Authentication6byte(7, 96, 0)))
                    {
                        byte[] data = reader.ReadBinaryBlocks(7, 16);
                        Array.Copy(encKey, 0, data, 0, encKey.Length);
                        Array.Copy(encKey, 0, data, 10, encKey.Length);
                        reader.UpdateBinaryBlocks(7, 16, data.Take(16).ToArray());
                        balloonTip.SetToolTip(imgEnc, "Key: " + BitConverter.ToString(encKey).Replace("-", ""));
                        imgEnc.Visible = true;
                        lblUid.Left = imgEnc.Right;
                    }
                    Toast.Show(this, "Data written to tag", Toast.LENGTH_SHORT);
                }
            }
            catch (Exception e)
            {
                Toast.Show(this, e.Message, Toast.LENGTH_LONG, true);
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
                DialogResult result = MessageBox.Show(this,
                    "Do you want to Delete?\n\n    "
                    + vendorName.Text + "\n    "
                    + materialName.Text,
                    "Delete Filament",
                    MessageBoxButtons.OKCancel,
                    MessageBoxIcon.Warning, MessageBoxDefaultButton.Button2);
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

        private void BtnFormat_Click(object sender, EventArgs e)
        {
            try
            {
                DialogResult result = MessageBox.Show(this,
                    "This will erase the tag and set the default MIFARE key",
                    "Format tag",
                    MessageBoxButtons.OKCancel,
                    MessageBoxIcon.Question, MessageBoxDefaultButton.Button2);
                if (result == DialogResult.OK)
                {
                    if (reader == null)
                    {
                        Toast.Show(this, "Error formatting tag", Toast.LENGTH_SHORT);
                    }
                    else
                    {
                        FormatTag(reader);
                        imgEnc.Visible = false;
                        lblUid.Left = lblTagId.Right;
                        Toast.Show(this, "Tag formatted", Toast.LENGTH_SHORT);
                    }
                }
            }
            catch (Exception ex)
            {
                Toast.Show(this, ex.Message, Toast.LENGTH_LONG, true);
            }
        }

        private void BtnUpload_MouseLeave(object sender, EventArgs e)
        {
            toolTip.Hide(btnUpload);
        }

        private void ChkAutoRead_CheckedChanged(object sender, EventArgs e)
        {
            if (chkAutoRead.Checked)
            {
                chkAutoWrite.Checked = false;
            }
        }

        private void ChkAutoWrite_CheckedChanged(object sender, EventArgs e)
        {
            if (chkAutoWrite.Checked)
            {
                chkAutoRead.Checked = false;
            }
        }

        private void BtnFormat_MouseLeave(object sender, EventArgs e)
        {
            toolTip.Hide(btnFormat);
        }

        private void BtnUpdate_MouseLeave(object sender, EventArgs e)
        {
            toolTip.Hide(btnUpdate);
        }

        private void BtnDel_MouseLeave(object sender, EventArgs e)
        {
            toolTip.Hide(btnDel);
        }

        private void BtnEdit_MouseLeave(object sender, EventArgs e)
        {
            toolTip.Hide(btnEdit);
        }

        private void ImgEnc_MouseLeave(object sender, EventArgs e)
        {
            balloonTip.Hide(imgEnc);
        }

        private void MainForm_LocationChanged(object sender, EventArgs e)
        {
            if (Toast.currentToastInstance != null && !Toast.currentToastInstance.IsDisposed)
            {
                Toast.currentToastInstance.UpdatePosition(this);
            }
        }

        private void ImgEnc_Click(object sender, EventArgs e)
        {
            try
            {
                Clipboard.Clear();
                Clipboard.SetText("UID: " + lblUid.Text + "\r\nKey: " + BitConverter.ToString(encKey).Replace("-", ""));
                Toast.Show(this, "Key copied to clipboard", Toast.LENGTH_SHORT);
            }
            catch { }
        }

        private void BtnAdd_MouseLeave(object sender, EventArgs e)
        {
            toolTip.Hide(btnAdd);
        }

        private void MainForm_FormClosed(object sender, FormClosedEventArgs e)
        {
            try { Environment.Exit(0); } catch { }
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
