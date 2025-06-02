namespace CFS_RFID
{
    partial class Form1
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form1));
            this.btnWrite = new System.Windows.Forms.Button();
            this.btnRead = new System.Windows.Forms.Button();
            this.btnDel = new System.Windows.Forms.Button();
            this.btnEdit = new System.Windows.Forms.Button();
            this.btnAdd = new System.Windows.Forms.Button();
            this.lblMsg = new System.Windows.Forms.Label();
            this.btnColor = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.checkBox1 = new System.Windows.Forms.CheckBox();
            this.materialWeight = new System.Windows.Forms.ComboBox();
            this.materialType = new System.Windows.Forms.ComboBox();
            this.lblUid = new System.Windows.Forms.Label();
            this.printerModel = new System.Windows.Forms.ComboBox();
            this.vendorName = new System.Windows.Forms.ComboBox();
            this.lblConnect = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // btnWrite
            // 
            this.btnWrite.BackColor = System.Drawing.Color.RoyalBlue;
            this.btnWrite.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnWrite.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnWrite.ForeColor = System.Drawing.Color.White;
            this.btnWrite.Location = new System.Drawing.Point(227, 552);
            this.btnWrite.Name = "btnWrite";
            this.btnWrite.Size = new System.Drawing.Size(126, 47);
            this.btnWrite.TabIndex = 23;
            this.btnWrite.Text = "Write Tag";
            this.btnWrite.UseVisualStyleBackColor = false;
            this.btnWrite.Click += new System.EventHandler(this.btnWrite_Click);
            // 
            // btnRead
            // 
            this.btnRead.BackColor = System.Drawing.Color.RoyalBlue;
            this.btnRead.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnRead.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnRead.ForeColor = System.Drawing.Color.White;
            this.btnRead.Location = new System.Drawing.Point(36, 552);
            this.btnRead.Name = "btnRead";
            this.btnRead.Size = new System.Drawing.Size(126, 47);
            this.btnRead.TabIndex = 22;
            this.btnRead.Text = "Read Tag";
            this.btnRead.UseVisualStyleBackColor = false;
            this.btnRead.Click += new System.EventHandler(this.btnRead_Click);
            // 
            // btnDel
            // 
            this.btnDel.FlatAppearance.BorderSize = 0;
            this.btnDel.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnDel.Image = global::CFS_RFID.Properties.Resources.delete;
            this.btnDel.Location = new System.Drawing.Point(144, 26);
            this.btnDel.Name = "btnDel";
            this.btnDel.Size = new System.Drawing.Size(48, 48);
            this.btnDel.TabIndex = 33;
            this.btnDel.UseVisualStyleBackColor = true;
            // 
            // btnEdit
            // 
            this.btnEdit.FlatAppearance.BorderSize = 0;
            this.btnEdit.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnEdit.Image = global::CFS_RFID.Properties.Resources.edit;
            this.btnEdit.Location = new System.Drawing.Point(215, 26);
            this.btnEdit.Name = "btnEdit";
            this.btnEdit.Size = new System.Drawing.Size(48, 48);
            this.btnEdit.TabIndex = 32;
            this.btnEdit.UseVisualStyleBackColor = true;
            // 
            // btnAdd
            // 
            this.btnAdd.FlatAppearance.BorderSize = 0;
            this.btnAdd.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.btnAdd.Image = global::CFS_RFID.Properties.Resources.add;
            this.btnAdd.Location = new System.Drawing.Point(280, 26);
            this.btnAdd.Name = "btnAdd";
            this.btnAdd.Size = new System.Drawing.Size(48, 48);
            this.btnAdd.TabIndex = 31;
            this.btnAdd.UseVisualStyleBackColor = true;
            // 
            // lblMsg
            // 
            this.lblMsg.Font = new System.Drawing.Font("Microsoft Sans Serif", 10F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblMsg.Location = new System.Drawing.Point(41, 503);
            this.lblMsg.Name = "lblMsg";
            this.lblMsg.Size = new System.Drawing.Size(312, 31);
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
            this.btnColor.Click += new System.EventHandler(this.btnColor_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Font = new System.Drawing.Font("Microsoft Sans Serif", 8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label1.Location = new System.Drawing.Point(46, 462);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(68, 20);
            this.label1.TabIndex = 28;
            this.label1.Text = "Tag ID:";
            // 
            // checkBox1
            // 
            this.checkBox1.Font = new System.Drawing.Font("Microsoft Sans Serif", 8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.checkBox1.Location = new System.Drawing.Point(69, 393);
            this.checkBox1.Name = "checkBox1";
            this.checkBox1.RightToLeft = System.Windows.Forms.RightToLeft.Yes;
            this.checkBox1.Size = new System.Drawing.Size(259, 33);
            this.checkBox1.TabIndex = 27;
            this.checkBox1.Text = "  ?Auto read on tag scan ";
            this.checkBox1.UseVisualStyleBackColor = true;
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
            this.materialWeight.SelectedIndexChanged += new System.EventHandler(this.materialWeight_SelectedIndexChanged);
            // 
            // materialType
            // 
            this.materialType.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.materialType.FlatStyle = System.Windows.Forms.FlatStyle.Flat;
            this.materialType.Font = new System.Drawing.Font("Microsoft Sans Serif", 12F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.materialType.FormattingEnabled = true;
            this.materialType.Location = new System.Drawing.Point(69, 200);
            this.materialType.Name = "materialType";
            this.materialType.Size = new System.Drawing.Size(259, 37);
            this.materialType.TabIndex = 24;
            this.materialType.SelectedIndexChanged += new System.EventHandler(this.materialType_SelectedIndexChanged);
            // 
            // lblUid
            // 
            this.lblUid.Font = new System.Drawing.Font("Microsoft Sans Serif", 8F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblUid.Location = new System.Drawing.Point(120, 462);
            this.lblUid.Name = "lblUid";
            this.lblUid.Size = new System.Drawing.Size(208, 25);
            this.lblUid.TabIndex = 21;
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
            this.printerModel.SelectedIndexChanged += new System.EventHandler(this.printerModel_SelectedIndexChanged);
            this.printerModel.SelectionChangeCommitted += new System.EventHandler(this.printerModel_SelectionChangeCommitted);
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
            this.vendorName.SelectedIndexChanged += new System.EventHandler(this.vendorName_SelectedIndexChanged);
            // 
            // lblConnect
            // 
            this.lblConnect.Cursor = System.Windows.Forms.Cursors.Hand;
            this.lblConnect.Font = new System.Drawing.Font("Microsoft Sans Serif", 11F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.lblConnect.ForeColor = System.Drawing.Color.IndianRed;
            this.lblConnect.Location = new System.Drawing.Point(518, 9);
            this.lblConnect.Name = "lblConnect";
            this.lblConnect.Size = new System.Drawing.Size(387, 605);
            this.lblConnect.TabIndex = 36;
            this.lblConnect.Text = "Failed to find a NFC reader\r\n\r\n\r\nClick to connect to reader";
            this.lblConnect.TextAlign = System.Drawing.ContentAlignment.MiddleCenter;
            this.lblConnect.Click += new System.EventHandler(this.lblConnect_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(9F, 20F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(385, 623);
            this.Controls.Add(this.lblConnect);
            this.Controls.Add(this.vendorName);
            this.Controls.Add(this.printerModel);
            this.Controls.Add(this.btnWrite);
            this.Controls.Add(this.btnRead);
            this.Controls.Add(this.btnDel);
            this.Controls.Add(this.btnEdit);
            this.Controls.Add(this.btnAdd);
            this.Controls.Add(this.lblMsg);
            this.Controls.Add(this.btnColor);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.checkBox1);
            this.Controls.Add(this.materialWeight);
            this.Controls.Add(this.materialType);
            this.Controls.Add(this.lblUid);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.Name = "Form1";
            this.Text = "CFS RFID";
            this.FormClosed += new System.Windows.Forms.FormClosedEventHandler(this.Form1_FormClosed);
            this.Load += new System.EventHandler(this.Form1_Load);
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
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.CheckBox checkBox1;
        private System.Windows.Forms.ComboBox materialWeight;
        private System.Windows.Forms.ComboBox materialType;
        private System.Windows.Forms.Label lblUid;
        private System.Windows.Forms.ComboBox printerModel;
        private System.Windows.Forms.ComboBox vendorName;
        private System.Windows.Forms.Label lblConnect;
    }
}

