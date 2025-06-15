
using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

public class SwitchCheckBox : CheckBox
{
    public Color SwitchOnColor { get; set; } = ColorTranslator.FromHtml("#1976D2");
    public Color SwitchOffColor { get; set; } = Color.LightGray;
    public Color ThumbColor { get; set; } = Color.White;
    public int BorderThickness { get; set; } = 1;

    public SwitchCheckBox()
    {
        SetStyle(ControlStyles.UserPaint |
                 ControlStyles.AllPaintingInWmPaint |
                 ControlStyles.OptimizedDoubleBuffer |
                 ControlStyles.SupportsTransparentBackColor, true);

        this.Size = new Size(50, 25);
        this.MinimumSize = new Size(40, 20);
      //  this.MaximumSize = new Size(100, 50);
        this.AutoSize = false;
        this.Text = "";
    }

    protected override void OnPaint(PaintEventArgs e)
    {
        e.Graphics.SmoothingMode = SmoothingMode.AntiAlias;
        Color backgroundColor = Parent?.BackColor ?? SystemColors.Control;
        e.Graphics.Clear(backgroundColor); 

        int h = this.Height - (BorderThickness * 2); 
        int w = this.Width - (BorderThickness * 2); 
        int thumbSize = h - 4;
        int arcRadius = h / 2;
        Rectangle trackRect = new Rectangle(BorderThickness, BorderThickness, w, h);
        GraphicsPath trackPath = new GraphicsPath();
        trackPath.AddArc(trackRect.X, trackRect.Y, arcRadius * 2, h, 180, 90);
        trackPath.AddArc(trackRect.X + w - arcRadius * 2, trackRect.Y, arcRadius * 2, h, 270, 90);
        trackPath.AddArc(trackRect.X + w - arcRadius * 2, trackRect.Y + h - arcRadius * 2, arcRadius * 2, h, 0, 90);
        trackPath.AddArc(trackRect.X, trackRect.Y + h - arcRadius * 2, arcRadius * 2, h, 90, 90);
        trackPath.CloseFigure();
        using (SolidBrush trackBrush = new SolidBrush(this.Checked ? SwitchOnColor : SwitchOffColor))
        {
            e.Graphics.FillPath(trackBrush, trackPath);
        }
        using (Pen borderPen = new Pen(this.Checked ? ControlPaint.Dark(SwitchOnColor, 0.1f) : ControlPaint.Dark(SwitchOffColor, 0.1f), BorderThickness))
        {
            e.Graphics.DrawPath(borderPen, trackPath);
        }
        int maxThumbX = w - thumbSize - 2;
        int minThumbX = 2; 
        int thumbX = this.Checked ? maxThumbX : minThumbX;
        Rectangle thumbRect = new Rectangle(thumbX + BorderThickness, 2 + BorderThickness, thumbSize, thumbSize);
        using (SolidBrush thumbBrush = new SolidBrush(ThumbColor))
        {
            e.Graphics.FillEllipse(thumbBrush, thumbRect);
        }
        using (Pen thumbBorderPen = new Pen(Color.Gray, 1))
        {
            e.Graphics.DrawEllipse(thumbBorderPen, thumbRect);
        }
    }

    protected override void OnClick(EventArgs e)
    {
        base.OnClick(e);
        this.Invalidate();
    }

    protected override void OnResize(EventArgs e)
    {
        base.OnResize(e);
        this.Invalidate();
    }
}