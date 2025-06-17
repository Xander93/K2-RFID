using CFS_RFID.Properties;
using Newtonsoft.Json.Linq;
using System;
using System.Drawing;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using static CFS_RFID.Utils;

namespace CFS_RFID
{
    public partial class UpdateForm : Form
    {
        public string SelectedPrinter { get; set; }
        private string sshDefault;
        private string currentVersion;
        private string newVersion;


        public UpdateForm()
        {
            InitializeComponent();
        }

        private void UpdateForm_Load(object sender, EventArgs e)
        {

            btnCancel.BackColor = ColorTranslator.FromHtml("#1976D2");
            btnUpdate.BackColor = ColorTranslator.FromHtml("#1976D2");
            btnCheck.BackColor = ColorTranslator.FromHtml("#1976D2");
            BackColor = ColorTranslator.FromHtml("#F4F4F4");
            lblPrinter.ForeColor = ColorTranslator.FromHtml("#1976D2");

            if (SelectedPrinter.Equals("hi", StringComparison.OrdinalIgnoreCase))
            {
                sshDefault = Resources.hiPsw;
            }
            else if (SelectedPrinter.Equals("k1", StringComparison.OrdinalIgnoreCase))
            {
                sshDefault = Resources.k1Psw;
            }
            else
            {
                sshDefault = Resources.k2Psw;
            }

            txtIP.Text = Settings.GetSetting("host_" + SelectedPrinter, string.Empty);
            txtPass.Text = Settings.GetSetting("psw_" + SelectedPrinter, sshDefault);
            lblPrinter.Text = "Creality " + SelectedPrinter.ToUpper();
            SetDescMessage(string.Format(Resources.downloadDesc, SelectedPrinter.ToUpper()));
            currentVersion = GetDatabaseVersion(SelectedPrinter);
            lblCver.Text = currentVersion;
        }

        private void BtnCheck_Click(object sender, EventArgs e)
        {
            try
            {
                Settings.SaveSetting("host_" + SelectedPrinter, txtIP.Text);
                Settings.SaveSetting("psw_" + SelectedPrinter, txtPass.Text);
                newVersion = GetPrinterVersion(txtPass.Text, txtIP.Text, SelectedPrinter);
                lblNver.Visible = true; 
                lblAvl.Visible = true;
                lblNver.Text = newVersion;
                if (long.Parse(newVersion) > long.Parse(currentVersion))
                {
                    btnUpdate.Visible = true;
                }
                else
                {
                    lblMsg.Text = "No update available";
                }
            }
            catch
            {
                lblMsg.Text = "Error checking version";
            }
        }

        private void BtnUpdate_Click(object sender, EventArgs e)
        {
            try
            {
                string dbData = GetJsonDB(txtPass.Text, txtIP.Text, SelectedPrinter);
                if (dbData != null && !dbData.Equals(string.Empty))
                {
                    JObject materials = JObject.Parse(dbData);
                    JObject result = (JObject)materials["result"];
                    if (result != null)
                    {
                        JArray list = (JArray)result["list"];
                        foreach (JToken itemToken in list)
                        {
                            JObject item = (JObject)itemToken;
                            JObject baseObject = (JObject)item["base"];
                            Filament filament = GetMaterialByID(baseObject["id"].Value<string>().Trim());
                            if (filament != null)
                            {
                                filament.FilamentParam = item.ToString();
                                EditMaterial(filament);
                            }
                            else
                            {
                                filament = new Filament
                                {
                                    FilamentVendor = baseObject.GetValue("brand").ToString(),
                                    FilamentName = baseObject.GetValue("name").ToString(),
                                    FilamentId = baseObject.GetValue("id").ToString(),
                                    FilamentType = baseObject.GetValue("meterialType").ToString(),
                                    FilamentParam = item.ToString()
                                };
                                AddMaterial(filament);
                            }
                        }
                    }
                    Task task = Task.Run(() =>
                    {
                        lblMsg.Invoke((MethodInvoker)delegate ()
                        {
                            lblMsg.Text = "Database Updated";
                        });
                        SaveMaterials(SelectedPrinter, newVersion);
                        Thread.Sleep(1000);
                    }).ContinueWith(t =>
                    {
                        this.DialogResult = DialogResult.OK;
                        this.Close();
                    }, TaskScheduler.Default);
                }
                else
                {
                    lblMsg.Text = "Database not found";
                }
            }
            catch
            {
                lblMsg.Text = "Error updating database";
            }
        }

        private void BtnCancel_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Cancel;
            this.Close();
        }

        private void SetDescMessage(string msg)
        {
            rtbDesc.Text = msg;
            rtbDesc.BackColor = BackColor;
            if (rtbDesc.Find(" " + SelectedPrinter.ToUpper() + " ") != -1)
            {
                rtbDesc.Select(rtbDesc.Find(" " + SelectedPrinter.ToUpper() + " "), 4);
                rtbDesc.SelectionColor = ColorTranslator.FromHtml("#1976D2");
            }
            rtbDesc.DeselectAll();
            rtbDesc.SelectionChanged += (sender, e) => {
                lblPrinter.Focus();
            };
            rtbDesc.MouseDown += (sender, e) => {
                if (e.Button == MouseButtons.Right)
                {
                    rtbDesc.ContextMenuStrip = null;
                }
                else
                {
                    lblPrinter.Focus();
                }
            };
        }

    }
}
