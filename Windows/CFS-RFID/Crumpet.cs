using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

public static class Crumpet
{
    private static CancellationTokenSource cancellationTokenSource;

    public static void Show(Label label, string message, int duration)
    {
        CancelCurrentCrumpet();
        cancellationTokenSource = new CancellationTokenSource();
        CancellationToken cancellationToken = cancellationTokenSource.Token;
        Task.Run(() =>
        {
            try
            {
                if (!cancellationToken.IsCancellationRequested)
                {
                    label.Invoke((MethodInvoker)delegate ()
                    {
                        label.Text = string.Empty;
                    });
                }
                label.Invoke((MethodInvoker)delegate ()
                {
                    label.Text = message;
                });
                Task.Delay(duration, cancellationToken).Wait(cancellationToken);
            }
            catch { }
            finally
            {
                label.Invoke((MethodInvoker)delegate ()
                {
                    label.Text = string.Empty;
                });
            }
        }, cancellationToken)
        .ContinueWith(t => { }, TaskScheduler.FromCurrentSynchronizationContext());
    }

    public static void CancelCurrentCrumpet()
    {
        if (cancellationTokenSource != null && !cancellationTokenSource.IsCancellationRequested)
        {
            cancellationTokenSource.Cancel();
            cancellationTokenSource.Dispose();
            cancellationTokenSource = null;
        }
    }
}