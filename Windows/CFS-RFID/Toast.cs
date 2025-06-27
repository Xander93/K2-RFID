using System;
using System.Drawing;
using System.Runtime.InteropServices;
using System.Windows.Forms;

public partial class Toast : Form
{

    public enum DWMWINDOWATTRIBUTE
    {
        DWMWA_WINDOW_CORNER_PREFERENCE = 33
    }

    public enum DWM_WINDOW_CORNER_PREFERENCE
    {
        DWMWCP_DEFAULT = 0,
        DWMWCP_DONOTROUND = 1,
        DWMWCP_ROUND = 2,
        DWMWCP_ROUNDSMALL = 3
    }

    [DllImport("dwmapi.dll", CharSet = CharSet.Unicode, PreserveSig = false)]
    internal static extern void DwmSetWindowAttribute(IntPtr hwnd, DWMWINDOWATTRIBUTE attribute, ref DWM_WINDOW_CORNER_PREFERENCE pvAttribute, uint cbAttribute);

    private Timer fadeInTimer;
    private Timer displayTimer;
    private Timer fadeOutTimer;
    private const int FadeInDuration = 200;
    private const int FadeOutDuration = 300;
    private const int StepInterval = 10;
    private readonly int totalDisplayDuration;
    private const int OffsetFromParentBottom = 30;
    public static Toast currentToastInstance;
    private readonly string messageText;
    public const int LENGTH_LONG = 3500;
    public const int LENGTH_SHORT = 2000;
    private static Color toastColor = ColorTranslator.FromHtml("#333333");

    public Toast(string message, int durationMs)
    {
        try
        {
            SuspendLayout();
            ClientSize = new System.Drawing.Size(284, 61);
            Name = "Toast";
            Text = string.Empty;
            ResumeLayout(false);

            totalDisplayDuration = durationMs;
            FormBorderStyle = FormBorderStyle.None;
            StartPosition = FormStartPosition.Manual;
            ShowInTaskbar = false;
            TopMost = true;
            Opacity = 0;
            BackColor = toastColor;
            Padding = new Padding(10, 10, 10, 10);
            DoubleBuffered = true;
            messageText = message;

            using (Graphics g = this.CreateGraphics())
            {
                SizeF textSize = g.MeasureString(messageText, new Font("Arial", 11, FontStyle.Bold));
                Width = (int)Math.Ceiling(textSize.Width) + (Padding.Left + Padding.Right);
                Height = (int)Math.Ceiling(textSize.Height) + (Padding.Top + Padding.Bottom);
            }

            var attribute = DWMWINDOWATTRIBUTE.DWMWA_WINDOW_CORNER_PREFERENCE;
            var preference = DWM_WINDOW_CORNER_PREFERENCE.DWMWCP_ROUND;
            DwmSetWindowAttribute(Handle, attribute, ref preference, sizeof(uint));

            fadeInTimer = new Timer
            {
                Interval = StepInterval
            };

            fadeInTimer.Tick += (sender, e) =>
            {
                Opacity += (double)StepInterval / FadeInDuration;
                if (Opacity >= 1.0)
                {
                    Opacity = 1.0;
                    fadeInTimer.Stop();
                    displayTimer.Start();
                }
            };

            displayTimer = new Timer();
            int displayDuration = Math.Max(0, totalDisplayDuration - FadeInDuration - FadeOutDuration);
            displayTimer.Interval = displayDuration;

            displayTimer.Tick += (sender, e) =>
            {
                displayTimer.Stop();
                fadeOutTimer.Start();
            };

            fadeOutTimer = new Timer
            {
                Interval = StepInterval
            };

            fadeOutTimer.Tick += (sender, e) =>
            {
                Opacity -= (double)StepInterval / FadeOutDuration;
                if (Opacity <= 0.0)
                {
                    Close();
                }
            };
        }
        catch { }
    }

    protected override void OnPaint(PaintEventArgs e)
    {
        base.OnPaint(e);
        using (SolidBrush textBrush = new SolidBrush(Color.White))
        using (Font textFont = new Font("Arial", 11, FontStyle.Bold))
        {
            StringFormat stringFormat = new StringFormat
            {
                Alignment = StringAlignment.Center,
                LineAlignment = StringAlignment.Center
            };
            e.Graphics.DrawString(messageText, textFont, textBrush, this.ClientRectangle, stringFormat);
        }
    }

    protected override bool ShowWithoutActivation
    {
        get { return true; }
    }

    public void UpdatePosition(Form parentForm)
    {
        if (parentForm == null || parentForm.IsDisposed) return;
        int x = parentForm.Location.X + (parentForm.ClientRectangle.Width - ClientRectangle.Width) / 2 + 8;
        int y = parentForm.Location.Y + parentForm.ClientRectangle.Height - ClientRectangle.Height - OffsetFromParentBottom;
        Location = new Point(x, y);
    }

    public static void Show(Form parentForm, string message, int durationMs = LENGTH_SHORT, bool isError = false)
    {
        try
        {
            if (currentToastInstance != null && !currentToastInstance.IsDisposed)
            {
                currentToastInstance.Close();
            }
            if (isError)
            {
                toastColor = ColorTranslator.FromHtml("#990000");
            }
            else
            {
                toastColor = ColorTranslator.FromHtml("#333333");
            }
            Toast toast = new Toast(message, durationMs);
            currentToastInstance = toast;
            parentForm.AddOwnedForm(toast);
            toast.UpdatePosition(parentForm);
            toast.Show();
            toast.fadeInTimer?.Start();
        }
        catch { }
    }

    protected override void OnFormClosed(FormClosedEventArgs e)
    {
        base.OnFormClosed(e);
        if (fadeInTimer != null)
        {
            fadeInTimer.Stop();
            fadeInTimer.Dispose();
            fadeInTimer = null;
        }
        if (displayTimer != null)
        {
            displayTimer.Stop();
            displayTimer.Dispose();
            displayTimer = null;
        }
        if (fadeOutTimer != null)
        {
            fadeOutTimer.Stop();
            fadeOutTimer.Dispose();
            fadeOutTimer = null;
        }
        if (currentToastInstance == this)
        {
            currentToastInstance = null;
        }
    }
}