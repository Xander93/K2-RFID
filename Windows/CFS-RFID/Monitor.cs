using PCSC.Monitoring;
using PCSC;

namespace CFS_RFID
{
    public class Monitor
    {
        private readonly ISCardMonitor monitor;
        public event CardInsertedHandler CardInserted;
        public delegate void CardInsertedHandler(CardStatusEventArgs args);
        public event CardRemovedHandler CardRemoved;
        public delegate void CardRemovedHandler(CardStatusEventArgs args);
        public event StatusChangedHandler StatusChanged;
        public delegate void StatusChangedHandler(StatusChangeEventArgs args);

        public Monitor(string readerName)
        {
            monitor = MonitorFactory.Instance.Create(SCardScope.System);
            monitor.CardInserted += Monitor_CardInserted;
            monitor.CardRemoved += Monitor_CardRemoved;
            monitor.StatusChanged += Monitor_StatusChanged;
            monitor.Start(readerName);
        }

        public Monitor(string[] readerNames)
        {
            monitor = MonitorFactory.Instance.Create(SCardScope.System);
            monitor.CardInserted += Monitor_CardInserted;
            monitor.CardRemoved += Monitor_CardRemoved;
            monitor.StatusChanged += Monitor_StatusChanged;
            monitor.Start(readerNames);
        }

        private void Monitor_CardInserted(object sender, CardStatusEventArgs e)
        {
            CardInserted?.Invoke(e);
        }

        private void Monitor_CardRemoved(object sender, CardStatusEventArgs e)
        {
            CardRemoved?.Invoke(e);
        }

        private void Monitor_StatusChanged(object sender, StatusChangeEventArgs e)
        {
            StatusChanged?.Invoke(e);
        }

        public void Dispose()
        {
            monitor?.Dispose();
        }
    }
}
