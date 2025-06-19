namespace CFS_RFID
{
    partial class MainForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(MainForm));
            this.btnWrite = new System.Windows.Forms.Button();
            this.btnRead = new System.Windows.Forms.Button();
            this.lblMsg = new System.Windows.Forms.Label();
            this.btnColor = new System.Windows.Forms.Button();
            this.lblTagId = new System.Windows.Forms.Label();
            this.materialWeight = new System.Windows.Forms.ComboBox();
            this.materialName = new System.Windows.Forms.ComboBox();
            this.lblUid = new System.Windows.Forms.Label();
            this.printerModel = new System.Windows.Forms.ComboBox();
            this.vendorName = new System.Windows.Forms.ComboBox();
            this.lblConnect = new System.Windows.Forms.Label();
            this.btnUpload = new System.Windows.Forms.Button();
            this.btnDel = new System.Windows.Forms.Button();
            this.btnEdit = new System.Windows.Forms.Button();
            this.btnAdd = new System.Windows.Forms.Button();
            this.lblAutoRead = new System.Windows.Forms.Label();
            this.btnUpdate = new System.Windows.Forms.Button();
            this.btnFormat = new System.Windows.Forms.Button();
            this.lblAutoWrite = new System.Windows.Forms.Label();
            this.chkAutoWrite = new SwitchCheckBox();
            this.chkAutoRead = new SwitchCheckBox();
            this.SuspendLayout();
            // 
            // btnWrite
            // 
            this.btnWrite.BackColor = System.Drawing.Color.RoyalBlue;
            this.btnWrite.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnWrite.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnWrite.ForeColor = System.Drawing.Color.White;
            this.btnWrite.Location = new System.Drawing.Point(216, 616);
            this.btnWrite.Name = "btnWrite";
            this.btnWrite.Size = new System.Drawing.Size(140, 47);
            this.btnWrite.TabIndex = 23;
            this.btnWrite.Text = "Write Tag";
            this.btnWrite.UseVisualStyleBackColor = false;
            this.btnWrite.Click += new System.EventHandler(this.BtnWrite_Click);
            // 
            // btnRead
            // 
            this.btnRead.BackColor = System.Drawing.Color.RoyalBlue;
            this.btnRead.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnRead.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnRead.ForeColor = System.Drawing.Color.White;
            this.btnRead.Location = new System.Drawing.Point(33, 616);
            this.btnRead.Name = "btnRead";
            this.btnRead.Size = new System.Drawing.Size(140, 47);
            this.btnRead.TabIndex = 22;
            this.btnRead.Text = "Read Tag";
            this.btnRead.UseVisualStyleBackColor = false;
            this.btnRead.Click += new System.EventHandler(this.BtnRead_Click);
            // 
            // lblMsg
            // 
            this.lblMsg.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblMsg.Location = new System.Drawing.Point(36, 547);
            this.lblMsg.Name = "lblMsg";
            this.lblMsg.Size = new System.Drawing.Size(317, 48);
            this.lblMsg.TabIndex = 29;
            this.lblMsg.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            // 
            // btnColor
            // 
            this.btnColor.BackColor = System.Drawing.SystemColors.Control;
            this.btnColor.FlatAppearance.BorderSize = 0;
            this.btnColor.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnColor.Location = new System.Drawing.Point(69, 320);
            this.btnColor.Name = "btnColor";
            this.btnColor.Size = new System.Drawing.Size(259, 40);
            this.btnColor.TabIndex = 25;
            this.btnColor.UseVisualStyleBackColor = false;
            this.btnColor.Click += new System.EventHandler(this.BtnColor_Click);
            // 
            // lblTagId
            // 
            this.lblTagId.AutoSize = true;
            this.lblTagId.Font = new System.Drawing.Font("Microsoft Sans Serif", 8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblTagId.Location = new System.Drawing.Point(46, 510);
            this.lblTagId.Name = "lblTagId";
            this.lblTagId.Size = new System.Drawing.Size(68, 20);
            this.lblTagId.TabIndex = 28;
            this.lblTagId.Text = "Tag ID:";
            this.lblTagId.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            this.lblTagId.Visible = false;
            // 
            // materialWeight
            // 
            this.materialWeight.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.materialWeight.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.materialWeight.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.materialWeight.FormattingEnabled = true;
            this.materialWeight.Items.AddRange(new object[] {
            "1 KG",
            "750 G",
            "600 G",
            "500 G",
            "250 G"});
            this.materialWeight.Location = new System.Drawing.Point(69, 259);
            this.materialWeight.Name = "materialWeight";
            this.materialWeight.Size = new System.Drawing.Size(259, 37);
            this.materialWeight.TabIndex = 26;
            this.materialWeight.SelectedIndexChanged += new System.EventHandler(this.MaterialWeight_SelectedIndexChanged);
            // 
            // materialName
            // 
            this.materialName.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.materialName.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.materialName.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.materialName.FormattingEnabled = true;
            this.materialName.Location = new System.Drawing.Point(69, 200);
            this.materialName.Name = "materialName";
            this.materialName.Size = new System.Drawing.Size(259, 37);
            this.materialName.TabIndex = 24;
            this.materialName.SelectedIndexChanged += new System.EventHandler(this.MaterialName_SelectedIndexChanged);
            // 
            // lblUid
            // 
            this.lblUid.Font = new System.Drawing.Font("Microsoft Sans Serif", 8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblUid.Location = new System.Drawing.Point(120, 506);
            this.lblUid.Name = "lblUid";
            this.lblUid.Size = new System.Drawing.Size(154, 29);
            this.lblUid.TabIndex = 21;
            this.lblUid.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // printerModel
            // 
            this.printerModel.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.printerModel.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.printerModel.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.printerModel.FormattingEnabled = true;
            this.printerModel.Location = new System.Drawing.Point(69, 85);
            this.printerModel.Name = "printerModel";
            this.printerModel.Size = new System.Drawing.Size(259, 37);
            this.printerModel.TabIndex = 34;
            this.printerModel.SelectedIndexChanged += new System.EventHandler(this.PrinterModel_SelectedIndexChanged);
            this.printerModel.SelectionChangeCommitted += new System.EventHandler(this.PrinterModel_SelectionChangeCommitted);
            // 
            // vendorName
            // 
            this.vendorName.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.vendorName.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.vendorName.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.vendorName.FormattingEnabled = true;
            this.vendorName.Location = new System.Drawing.Point(69, 142);
            this.vendorName.Name = "vendorName";
            this.vendorName.Size = new System.Drawing.Size(259, 37);
            this.vendorName.TabIndex = 35;
            this.vendorName.SelectedIndexChanged += new System.EventHandler(this.VendorName_SelectedIndexChanged);
            // 
            // lblConnect
            // 
            this.lblConnect.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblConnect.ForeColor = System.Drawing.Color.IndianRed;
            this.lblConnect.Location = new System.Drawing.Point(36, 374);
            this.lblConnect.Name = "lblConnect";
            this.lblConnect.Size = new System.Drawing.Size(317, 65);
            this.lblConnect.TabIndex = 36;
            this.lblConnect.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            this.lblConnect.Click += new System.EventHandler(this.LblConnect_Click);
            // 
            // btnUpload
            // 
            this.btnUpload.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnUpload.FlatAppearance.BorderSize = 0;
            this.btnUpload.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnUpload.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnUpload.Image = global::CFS_RFID.Properties.Resources.upload;
            this.btnUpload.Location = new System.Drawing.Point(92, 17);
            this.btnUpload.Name = "btnUpload";
            this.btnUpload.Size = new System.Drawing.Size(48, 48);
            this.btnUpload.TabIndex = 37;
            this.btnUpload.UseVisualStyleBackColor = false;
            this.btnUpload.Click += new System.EventHandler(this.BtnUpload_Click);
            this.btnUpload.MouseLeave += new System.EventHandler(this.BtnUpload_MouseLeave);
            // 
            // btnDel
            // 
            this.btnDel.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnDel.FlatAppearance.BorderSize = 0;
            this.btnDel.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnDel.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnDel.Image = global::CFS_RFID.Properties.Resources.delete;
            this.btnDel.Location = new System.Drawing.Point(171, 17);
            this.btnDel.Name = "btnDel";
            this.btnDel.Size = new System.Drawing.Size(48, 48);
            this.btnDel.TabIndex = 33;
            this.btnDel.UseVisualStyleBackColor = false;
            this.btnDel.Click += new System.EventHandler(this.BtnDel_Click);
            this.btnDel.MouseLeave += new System.EventHandler(this.BtnDel_MouseLeave);
            // 
            // btnEdit
            // 
            this.btnEdit.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnEdit.FlatAppearance.BorderSize = 0;
            this.btnEdit.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnEdit.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnEdit.Image = global::CFS_RFID.Properties.Resources.edit;
            this.btnEdit.Location = new System.Drawing.Point(248, 17);
            this.btnEdit.Name = "btnEdit";
            this.btnEdit.Size = new System.Drawing.Size(48, 48);
            this.btnEdit.TabIndex = 32;
            this.btnEdit.UseVisualStyleBackColor = false;
            this.btnEdit.Click += new System.EventHandler(this.BtnEdit_Click);
            this.btnEdit.MouseLeave += new System.EventHandler(this.BtnEdit_MouseLeave);
            // 
            // btnAdd
            // 
            this.btnAdd.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnAdd.FlatAppearance.BorderSize = 0;
            this.btnAdd.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnAdd.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnAdd.Image = global::CFS_RFID.Properties.Resources.add;
            this.btnAdd.Location = new System.Drawing.Point(325, 17);
            this.btnAdd.Name = "btnAdd";
            this.btnAdd.Size = new System.Drawing.Size(48, 48);
            this.btnAdd.TabIndex = 31;
            this.btnAdd.UseVisualStyleBackColor = false;
            this.btnAdd.Click += new System.EventHandler(this.BtnAdd_Click);
            this.btnAdd.MouseLeave += new System.EventHandler(this.BtnAdd_MouseLeave);
            // 
            // lblAutoRead
            // 
            this.lblAutoRead.AutoSize = true;
            this.lblAutoRead.Font = new System.Drawing.Font("Microsoft Sans Serif", 8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblAutoRead.Location = new System.Drawing.Point(46, 391);
            this.lblAutoRead.Name = "lblAutoRead";
            this.lblAutoRead.Size = new System.Drawing.Size(197, 20);
            this.lblAutoRead.TabIndex = 40;
            this.lblAutoRead.Text = "Auto read tag on scan?";
            // 
            // btnUpdate
            // 
            this.btnUpdate.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnUpdate.FlatAppearance.BorderSize = 0;
            this.btnUpdate.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnUpdate.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnUpdate.Image = global::CFS_RFID.Properties.Resources.download;
            this.btnUpdate.Location = new System.Drawing.Point(15, 17);
            this.btnUpdate.Name = "btnUpdate";
            this.btnUpdate.Size = new System.Drawing.Size(48, 48);
            this.btnUpdate.TabIndex = 41;
            this.btnUpdate.UseVisualStyleBackColor = false;
            this.btnUpdate.Click += new System.EventHandler(this.BtnUpdate_Click);
            this.btnUpdate.MouseLeave += new System.EventHandler(this.BtnUpdate_MouseLeave);
            // 
            // btnFormat
            // 
            this.btnFormat.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnFormat.FlatAppearance.BorderSize = 0;
            this.btnFormat.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnFormat.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.btnFormat.Image = global::CFS_RFID.Properties.Resources.format;
            this.btnFormat.Location = new System.Drawing.Point(283, 495);
            this.btnFormat.Name = "btnFormat";
            this.btnFormat.Size = new System.Drawing.Size(48, 48);
            this.btnFormat.TabIndex = 42;
            this.btnFormat.UseVisualStyleBackColor = false;
            this.btnFormat.Click += new System.EventHandler(this.BtnFormat_Click);
            this.btnFormat.MouseLeave += new System.EventHandler(this.BtnFormat_MouseLeave);
            // 
            // lblAutoWrite
            // 
            this.lblAutoWrite.AutoSize = true;
            this.lblAutoWrite.Font = new System.Drawing.Font("Microsoft Sans Serif", 8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblAutoWrite.Location = new System.Drawing.Point(46, 442);
            this.lblAutoWrite.Name = "lblAutoWrite";
            this.lblAutoWrite.Size = new System.Drawing.Size(199, 20);
            this.lblAutoWrite.TabIndex = 44;
            this.lblAutoWrite.Text = "Auto write tag on scan?";
            // 
            // chkAutoWrite
            // 
            this.chkAutoWrite.BorderThickness = 1;
            this.chkAutoWrite.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.chkAutoWrite.Location = new System.Drawing.Point(278, 437);
            this.chkAutoWrite.MaximumSize = new System.Drawing.Size(100, 50);
            this.chkAutoWrite.MinimumSize = new System.Drawing.Size(30, 15);
            this.chkAutoWrite.Name = "chkAutoWrite";
            this.chkAutoWrite.Size = new System.Drawing.Size(60, 31);
            this.chkAutoWrite.SwitchOffColor = System.Drawing.Color.LightGray;
            this.chkAutoWrite.SwitchOnColor = System.Drawing.Color.FromArgb(((int)(((byte)(25)))), ((int)(((byte)(118)))), ((int)(((byte)(210)))));
            this.chkAutoWrite.TabIndex = 43;
            this.chkAutoWrite.Text = "Auto read on tag scan?";
            this.chkAutoWrite.ThumbColor = System.Drawing.Color.White;
            this.chkAutoWrite.UseVisualStyleBackColor = true;
            this.chkAutoWrite.CheckedChanged += new System.EventHandler(this.ChkAutoWrite_CheckedChanged);
            // 
            // chkAutoRead
            // 
            this.chkAutoRead.BorderThickness = 1;
            this.chkAutoRead.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.chkAutoRead.Location = new System.Drawing.Point(278, 386);
            this.chkAutoRead.MaximumSize = new System.Drawing.Size(100, 50);
            this.chkAutoRead.MinimumSize = new System.Drawing.Size(30, 15);
            this.chkAutoRead.Name = "chkAutoRead";
            this.chkAutoRead.Size = new System.Drawing.Size(60, 31);
            this.chkAutoRead.SwitchOffColor = System.Drawing.Color.LightGray;
            this.chkAutoRead.SwitchOnColor = System.Drawing.Color.FromArgb(((int)(((byte)(25)))), ((int)(((byte)(118)))), ((int)(((byte)(210)))));
            this.chkAutoRead.TabIndex = 39;
            this.chkAutoRead.Text = "Auto read on tag scan?";
            this.chkAutoRead.ThumbColor = System.Drawing.Color.White;
            this.chkAutoRead.UseVisualStyleBackColor = true;
            this.chkAutoRead.CheckedChanged += new System.EventHandler(this.ChkAutoRead_CheckedChanged);
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(9F, 20F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(244)))), ((int)(((byte)(244)))), ((int)(((byte)(244)))));
            this.ClientSize = new System.Drawing.Size(389, 693);
            this.Controls.Add(this.lblAutoWrite);
            this.Controls.Add(this.chkAutoWrite);
            this.Controls.Add(this.btnFormat);
            this.Controls.Add(this.btnUpdate);
            this.Controls.Add(this.lblAutoRead);
            this.Controls.Add(this.chkAutoRead);
            this.Controls.Add(this.btnUpload);
            this.Controls.Add(this.vendorName);
            this.Controls.Add(this.printerModel);
            this.Controls.Add(this.btnWrite);
            this.Controls.Add(this.btnRead);
            this.Controls.Add(this.btnDel);
            this.Controls.Add(this.btnEdit);
            this.Controls.Add(this.btnAdd);
            this.Controls.Add(this.lblMsg);
            this.Controls.Add(this.btnColor);
            this.Controls.Add(this.lblTagId);
            this.Controls.Add(this.materialWeight);
            this.Controls.Add(this.materialName);
            this.Controls.Add(this.lblUid);
            this.Controls.Add(this.lblConnect);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.Name = "MainForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "CFS RFID";
            this.FormClosed += new System.Windows.Forms.FormClosedEventHandler(this.MainForm_FormClosed);
            this.Load += new System.EventHandler(this.MainForm_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion
        private System.Windows.Forms.Button btnWrite;
        private System.Windows.Forms.Button btnRead;
        private System.Windows.Forms.Button btnDel;
        private System.Windows.Forms.Button btnEdit;
        private System.Windows.Forms.Button btnAdd;
        private System.Windows.Forms.Label lblMsg;
        private System.Windows.Forms.Button btnColor;
        private System.Windows.Forms.Label lblTagId;
        private System.Windows.Forms.ComboBox materialWeight;
        private System.Windows.Forms.ComboBox materialName;
        private System.Windows.Forms.Label lblUid;
        private System.Windows.Forms.ComboBox printerModel;
        private System.Windows.Forms.ComboBox vendorName;
        private System.Windows.Forms.Label lblConnect;
        private System.Windows.Forms.Button btnUpload;
        private SwitchCheckBox chkAutoRead;
        private System.Windows.Forms.Label lblAutoRead;
        private System.Windows.Forms.Button btnUpdate;
        private System.Windows.Forms.Button btnFormat;
        private System.Windows.Forms.Label lblAutoWrite;
        private SwitchCheckBox chkAutoWrite;
    }
}

