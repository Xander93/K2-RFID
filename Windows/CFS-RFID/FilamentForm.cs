using CFS_RFID.Properties;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Reflection;
using System.Windows.Forms;
using static CFS_RFID.Utils;
using static System.Windows.Forms.ListViewItem;

namespace CFS_RFID
{
    
    public partial class FilamentForm : Form
    {

        public string SelkectedFilament { get; set; }
        public bool IsEdit { get; set; }

        public FilamentForm()
        {
            InitializeComponent();
            txtId.ContextMenuStrip = new ContextMenuStrip();
            txtMaxTemp.ContextMenuStrip = new ContextMenuStrip();
            txtMinTemp.ContextMenuStrip = new ContextMenuStrip();
        }

        private void FilamentForm_Load(object sender, EventArgs e)
        {
            BackColor = ColorTranslator.FromHtml("#F4F4F4");
            tabControl1.BackColor = ColorTranslator.FromHtml("#F4F4F4");
           
            btnCancel.BackColor = ColorTranslator.FromHtml("#1976D2");
            btnSave.BackColor = ColorTranslator.FromHtml("#1976D2");
            lblFilament.ForeColor = ColorTranslator.FromHtml("#1976D2");
            btnSave.FlatAppearance.BorderSize = 0;
            btnCancel.FlatAppearance.BorderSize = 0;
    
            paramList.View = View.Details;
            paramList.GridLines = true;
            paramList.FullRowSelect = true;
            paramList.LabelEdit = true;
            paramList.HeaderStyle = ColumnHeaderStyle.None;
            paramList.ShowItemToolTips = true;
            paramList.Columns.Add("", 250, HorizontalAlignment.Left);
            paramList.Columns.Add("", 470, HorizontalAlignment.Left);

            cboBrand.Items.AddRange(filamentVendors);
            cboBrand.AutoCompleteMode = AutoCompleteMode.SuggestAppend;
            cboBrand.AutoCompleteSource = AutoCompleteSource.ListItems;
            cboBrand.DropDownStyle = ComboBoxStyle.DropDown;

            cboType.Items.AddRange(filamentTypes);
            cboType.AutoCompleteMode = AutoCompleteMode.SuggestAppend;
            cboType.AutoCompleteSource = AutoCompleteSource.ListItems;
            cboType.DropDownStyle = ComboBoxStyle.DropDown;


            if (!string.IsNullOrEmpty(SelkectedFilament))
            {
                LoadData();
            }
            else
            {
                this.DialogResult = DialogResult.Cancel;
                this.Close();
            }
        }

        void LoadData()
        {
            try
            {
                JObject js = JObject.Parse(GetMaterialByID(SelkectedFilament).FilamentParam);
                JObject kvparam = (JObject)js["kvParam"];
                JObject baseval = (JObject)js["base"];

                if (!IsEdit)
                {
                    Random random = new Random();
                    txtId.Text = string.Format("{0:D5}", random.Next(99999));
                    cboBrand.Text = string.Empty;// baseval["brand"].ToString();
                    txtName.Text = string.Empty;// baseval["name"].ToString();
                    cboType.Text = baseval["meterialType"].ToString();
                    txtMinTemp.Text = baseval["minTemp"].ToString();
                    txtMaxTemp.Text = baseval["maxTemp"].ToString();
                    chkSoluble.Checked = (bool)baseval["isSoluble"];
                    chkSupport.Checked = (bool)baseval["isSupport"];
                    Text = "Add Filament";
                    SetDescMessage(baseval["brand"].ToString(), baseval["name"].ToString());
                }
                else
                {
                    Text = "Edit Filament";
                    lblFilament.Text = baseval["brand"].ToString() + " - " + baseval["name"].ToString();
                    tabControl1.TabPages.Remove(basePage);
                }
        
                List<JsonItem> paramItems = new List<JsonItem>();
                foreach (JProperty property in kvparam.Properties())
                {
                    paramItems.Add(new JsonItem
                    {
                        Key = property.Name,
                        Value = property.Value.ToString()
                    });
                }
                foreach (var item in paramItems)
                {
                    ListViewItem lvi = new ListViewItem(item.Key);
                    lvi.SubItems.Add(item.Value.ToString());
                    lvi.Tag = item;
                    paramList.Items.Add(lvi);
                }

            }
            catch {}
        }

        private void ParamList_AfterLabelEdit(object sender, LabelEditEventArgs e)
        {
            if (e.Label != null && e.Item >= 0)
            {
                ListViewItem editedItem = paramList.Items[e.Item];
                if (editedItem.Tag is JsonItem item)
                {
                    item.Value = e.Label;
                    editedItem.SubItems[1].Text = e.Label;
                }
            }
        }

        private void ParamList_MouseClick(object sender, MouseEventArgs e)
        {
            ListViewHitTestInfo hitTest = paramList.HitTest(e.Location);
            if (hitTest.Item != null && hitTest.SubItem != null)
            {
                ListViewItem clickedItem = hitTest.Item;
                ListViewSubItem clickedSubItem = hitTest.SubItem;
                int columnIndex = clickedItem.SubItems.IndexOf(clickedSubItem);
                if (columnIndex == 1)
                {
                    System.Windows.Forms.TextBox textbox = new System.Windows.Forms.TextBox
                    {
                        Text = clickedSubItem.Text,
                        BorderStyle = BorderStyle.None,
                        Bounds = clickedSubItem.Bounds,
                        Tag = new Tuple<ListViewItem, int>(clickedItem, columnIndex)
                    };
                    textbox.LostFocus += ParamListEdit_LostFocus;
                    textbox.KeyPress += ParamListEdit_KeyPress;
                    textbox.MouseWheel += ParamListEdit_MouseWheel;
                    paramList.Controls.Add(textbox);
                    textbox.Focus();
                }
            }
        }

        private void ParamListEdit_LostFocus(object sender, EventArgs e)
        {
            if (sender is System.Windows.Forms.TextBox textbox)
            {
                ApplySubItemEdit(textbox);
                paramList.Controls.Remove(textbox);
            }
        }

        private void ParamListEdit_KeyPress(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar == (char)Keys.Enter)
            {
                if (sender is System.Windows.Forms.TextBox textbox)
                {
                    ApplySubItemEdit(textbox);
                    paramList.Controls.Remove(textbox);
                }
                e.Handled = true; 
            }
        }

        private void ParamListEdit_MouseWheel(object sender, MouseEventArgs e)
        {
            if (e.Button ==  MouseButtons.None)
            {
                if (sender is System.Windows.Forms.TextBox textbox)
                {
                    ApplySubItemEdit(textbox);
                     paramList.Controls.Remove(textbox);
                  }
            }
        }

        private void ApplySubItemEdit(System.Windows.Forms.TextBox textbox)
        {
            if (textbox.Tag is Tuple<ListViewItem, int> tagInfo)
            {
                ListViewItem item = tagInfo.Item1;
                int columnIndex = tagInfo.Item2;
                if (item.Tag is JsonItem product)
                {
                    string newText = textbox.Text;
                    try
                    {
                        if (columnIndex == 1)
                        {
                            product.Value = newText;
                            item.SubItems[columnIndex].Text = newText;
                        }
                    }
                    catch{}
                }
            }
        }

        void SaveJsonEdit()
        {
            if (paramList.Items.Count > 0)
            {
                JObject jsonParam = new JObject();
                foreach (ListViewItem item in paramList.Items)
                {
                    jsonParam.Add(item.SubItems[0].Text, item.SubItems[1].Text.Trim());
                }
                JObject jsonitem = JObject.Parse(GetMaterialByID(SelkectedFilament).FilamentParam);
                jsonitem["kvParam"] = jsonParam;
                Filament filament = GetMaterialByID(SelkectedFilament);
                filament.FilamentParam = jsonitem.ToString();
                EditMaterial(filament);
                this.DialogResult = DialogResult.OK;
                this.Close();
            }
            else
            {
                Toast.Show(this, "Invalid paramList", Toast.LENGTH_LONG, true);
            }
        }

        void SaveJsonAdd()
        {
            if (txtId.Text.Equals(string.Empty) ||
                 cboBrand.Text.Equals(string.Empty) ||
                 txtName.Text.Equals(string.Empty) ||
                  cboType.Text.Equals(string.Empty) ||
                   txtMinTemp.Text.Equals(string.Empty) ||
                     txtMaxTemp.Text.Equals(string.Empty)){
                Toast.Show(this, "You must fill all fields", Toast.LENGTH_LONG, true);
                return;
            }
            else
            {
                if (txtId.Text.Length != 5)
                {
                    Toast.Show(this, "ID must be 5 digits long", Toast.LENGTH_LONG, true);
                    return;
                }
                if (!int.TryParse(txtId.Text, out _))
                {
                    Toast.Show(this, "ID must be a number", Toast.LENGTH_LONG, true);
                    return;
                }
                if (!int.TryParse(txtMinTemp.Text, out _))
                {
                    Toast.Show(this, "Min Temp must be a number", Toast.LENGTH_LONG, true);
                    return;
                }
                if (!int.TryParse(txtMaxTemp.Text, out _))
                {
                    Toast.Show(this, "Max Temp must be a number", Toast.LENGTH_LONG, true);
                    return;
                }
                if (GetMaterialByID(txtId.Text.Trim()) == null)
                {
                    if (paramList.Items.Count > 0)
                    {

                        JObject jsonParam = new JObject();
                        foreach (ListViewItem item in paramList.Items)
                        {
                            if (item.SubItems[0].Text.Equals("filament_vendor", StringComparison.OrdinalIgnoreCase))
                            {
                                jsonParam.Add(item.SubItems[0].Text, cboBrand.Text.Trim());
                            }
                            else if(item.SubItems[0].Text.Equals("filament_type", StringComparison.OrdinalIgnoreCase))
                            {
                                jsonParam.Add(item.SubItems[0].Text, cboType.Text.Trim());
                            }
                            else
                            {
                                jsonParam.Add(item.SubItems[0].Text, item.SubItems[1].Text.Trim());
                            }
                        }

                        JObject jsonitem = JObject.Parse(GetMaterialByID(SelkectedFilament).FilamentParam);
                        JObject baseval = (JObject)jsonitem["base"];
                        jsonitem["kvParam"] = jsonParam;

                        baseval["id"] = txtId.Text.Trim();
                        baseval["brand"] = cboBrand.Text.Trim();
                        baseval["name"] = txtName.Text.Trim();
                        baseval["meterialType"] = cboType.Text.Trim();

                        //   JArray colors = (JArray)baseval["colors"];
                        //   colors[0] = "#ffffff";

                        //   baseval["density"] = 1.24;
                        //   baseval["diameter"] = "1.75";
                        //   baseval["costPerMeter"] = 0;
                        //   baseval["weightPerMeter"] = 0;
                        //   baseval["rank"] = 10000;

                        try
                        {
                            baseval["minTemp"] = int.Parse(txtMinTemp.Text.Trim());
                        }
                        catch 
                        {
                            baseval["minTemp"] = txtMinTemp.Text.Trim();
                        }

                        try
                        {
                            baseval["maxTemp"] = int.Parse(txtMaxTemp.Text.Trim());
                        }
                        catch 
                        {
                            baseval["maxTemp"] = txtMaxTemp.Text.Trim();
                        }

                        baseval["isSoluble"] = chkSoluble.Checked;
                        baseval["isSupport"] = chkSupport.Checked;

                        //   baseval["shrinkageRate"] = 0;
                        //   baseval["softeningTemp"] = 0;
                        //   baseval["dryingTemp"] = 0;
                        //   baseval["dryingTime"] = 0;

                        Filament filament = new Filament
                        {
                            FilamentVendor = baseval.GetValue("brand").ToString().Trim(),
                            FilamentName = baseval.GetValue("name").ToString().Trim(),
                            FilamentId = baseval.GetValue("id").ToString().Trim(),
                            FilamentType = baseval.GetValue("meterialType").ToString().Trim(),
                            FilamentParam = jsonitem.ToString().Trim()
                        };

                        AddMaterial(filament);

                        this.DialogResult = DialogResult.OK;
                        this.Close();
                    }
                    else
                    {
                        Toast.Show(this, "Invalid paramList", Toast.LENGTH_LONG, true);
                    }
                }
                else
                {
                    Toast.Show(this, "Filament ID Exists\nDuplicate IDs are not allowed", Toast.LENGTH_LONG, true);
                }
            }
        }

        private void SetDescMessage(string brand, string name)
        {
            rtbDesc.Text = string.Format(Resources.addDesc, brand, name);
            rtbDesc.BackColor = tabControl1.BackColor;
            if (rtbDesc.Find(brand) != -1)
            {
                rtbDesc.Select(rtbDesc.Find(brand), brand.Length);
                rtbDesc.SelectionColor = ColorTranslator.FromHtml("#1976D2");
                rtbDesc.SelectionFont = new System.Drawing.Font(rtbDesc.SelectionFont.FontFamily, 10f, FontStyle.Bold);
            }
            if (rtbDesc.Find(name) != -1)
            {
                rtbDesc.Select(rtbDesc.Find(name), name.Length);
                rtbDesc.SelectionColor = ColorTranslator.FromHtml("#1976D2");
                rtbDesc.SelectionFont = new System.Drawing.Font(rtbDesc.SelectionFont.FontFamily, 10f, FontStyle.Bold);

            }
            rtbDesc.DeselectAll();
            rtbDesc.SelectionChanged += (sender, e) => {
                lblID.Focus();
            };
            rtbDesc.MouseDown += (sender, e) => {
                if (e.Button == MouseButtons.Right)
                {
                    rtbDesc.ContextMenuStrip = null;
                }
                else
                {
                    lblID.Focus();
                }
            };
        }

        private void BtnCancel_Click(object sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Cancel;
            this.Close();
        }

        private void BtnSave_Click(object sender, EventArgs e)
        {
            if (IsEdit)
            {
                SaveJsonEdit();
            }
            else
            {
                SaveJsonAdd();
            }
        }

        private void TxtId_KeyPress(object sender, KeyPressEventArgs e)
        {
            if (char.IsDigit(e.KeyChar))
            {
                e.Handled = false;
            }
            else if (char.IsControl(e.KeyChar))
            {
                e.Handled = false;
            }
            else
            {
                e.Handled = true;
            }
        }

        private void TxtId_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.Control && e.KeyCode == Keys.V)
            {
                e.SuppressKeyPress = true;
                e.Handled = true;
            }
            if (e.Shift && e.KeyCode == Keys.Insert)
            {
                e.SuppressKeyPress = true;
                e.Handled = true;
            }
        }

        private void TxtMinTemp_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.Control && e.KeyCode == Keys.V)
            {
                e.SuppressKeyPress = true;
                e.Handled = true;
            }
            if (e.Shift && e.KeyCode == Keys.Insert)
            {
                e.SuppressKeyPress = true;
                e.Handled = true;
            }
        }

        private void TxtMinTemp_KeyPress(object sender, KeyPressEventArgs e)
        {
            if (char.IsDigit(e.KeyChar))
            {
                e.Handled = false;
            }
            else if (char.IsControl(e.KeyChar))
            {
                e.Handled = false;
            }
            else
            {
                e.Handled = true;
            }
        }

        private void TxtMaxTemp_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.Control && e.KeyCode == Keys.V)
            {
                e.SuppressKeyPress = true;
                e.Handled = true;
            }
            if (e.Shift && e.KeyCode == Keys.Insert)
            {
                e.SuppressKeyPress = true;
                e.Handled = true;
            }
        }

        private void TxtMaxTemp_KeyPress(object sender, KeyPressEventArgs e)
        {
            if (char.IsDigit(e.KeyChar))
            {
                e.Handled = false;
            }
            else if (char.IsControl(e.KeyChar))
            {
                e.Handled = false;
            }
            else
            {
                e.Handled = true;
            }
        }
    }
}
